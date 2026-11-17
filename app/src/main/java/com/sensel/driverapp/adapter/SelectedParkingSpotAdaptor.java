package com.sensel.driverapp.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sensel.driverapp.JavaScriptInterface;
import com.sensel.driverapp.JourneyPlanner;
import com.sensel.driverapp.ParkingSpotSelect;
import com.sensel.driverapp.R;
import com.sensel.driverapp.Splash;
import com.sensel.driverapp.objectclass.SelectedParingSpotObject;

import java.util.ArrayList;


public class SelectedParkingSpotAdaptor extends RecyclerView.Adapter<SelectedParkingSpotAdaptor.ViewHolder> {
    public ArrayList<SelectedParingSpotObject> selectedParingSpotObjects;

    String ParkingPosition="00.00,00.00#|#name##|##";
    public SelectedParkingSpotAdaptor(ArrayList<SelectedParingSpotObject> selectedParingSpotObjects) {
        this.selectedParingSpotObjects = selectedParingSpotObjects;

    }

    @NonNull
    @Override
    public SelectedParkingSpotAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_selected_parking_spot, parent, false);
        SelectedParkingSpotAdaptor.ViewHolder evh = new SelectedParkingSpotAdaptor.ViewHolder(view);

        return evh;
    }


    @Override
    public void onBindViewHolder(@NonNull SelectedParkingSpotAdaptor.ViewHolder holder, int position) {
        holder.txt_spotname.setText(selectedParingSpotObjects.get(position).getParkingName());

        String eta = ParkingSpotSelect.sdf.format(selectedParingSpotObjects.get(position).getEstimatedArrival());
        holder.txt_eta.setText(eta);


        holder.txt_ata.setText(selectedParingSpotObjects.get(position).getActualArrival());
        holder.txt_resttime.setText(String.valueOf(selectedParingSpotObjects.get(position).getRestTime()));

        if(selectedParingSpotObjects.get(position).getIsCompleted().contains("Yes")){
            holder.delete.setVisibility(View.GONE);
        }

        if(JourneyPlanner.PLAN_STATUS.equals("Approved")){
            holder.delete.setVisibility(View.GONE);
        }


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ParkingSpotSelect.activity);


                builder1.setTitle("Delete ?");
                builder1.setMessage("You need to reselect the parking after this");
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int length = ParkingSpotSelect.spotObjectArrayList.size() - 1;

                                for (int i = length; i >= position; i--) {
                                    ParkingSpotSelect.spotObjectArrayList.remove(i);
                                    notifyDataSetChanged();
                                    dialog.cancel();
                                }
                            }
                        });
                builder1.setNegativeButton("close",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();


            }
        });

    }


    @Override
    public int getItemCount() {
        return selectedParingSpotObjects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button delete, viewmap;
        ImageView img_parkicon;
        TextView txt_spotname, txt_eta, txt_ata, txt_delay, txt_resttime;
        BottomSheetDialog bottomSheetDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewmap = itemView.findViewById(R.id.viewmap);
            delete = itemView.findViewById(R.id.delete);
            txt_spotname = itemView.findViewById(R.id.parkingname);
            txt_ata = itemView.findViewById(R.id.ataTime);
            txt_eta = itemView.findViewById(R.id.etaTime);
            txt_resttime = itemView.findViewById(R.id.resttime);
            img_parkicon = itemView.findViewById(R.id.parkingicon);


            viewmap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog = new BottomSheetDialog(v.getContext());
                    bottomSheetDialog.setContentView(R.layout.bottomsheet_mapview);
                    WebView webView = bottomSheetDialog.findViewById(R.id.mapwebview);
                    Button closebutton = bottomSheetDialog.findViewById(R.id.close);
                    TextView parkingspot = bottomSheetDialog.findViewById(R.id.parkingname);

                    bottomSheetDialog.setCancelable(false);


                    webView.setWebViewClient(
                            new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    view.loadUrl(url);
                                    return true;
                                }
                            });
                    webView.setWebChromeClient(new WebChromeClient() {
                        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                            callback.invoke(origin, true, false);
                        }
                    });
                    webView.setWebViewClient(new WebViewClient() {

                        public void onPageFinished(WebView view, String url) {
                            ParkingPosition=selectedParingSpotObjects.get(getAdapterPosition()).getLatLan()+"##"+selectedParingSpotObjects.get(getAdapterPosition()).getParkingName();


                                webView.loadUrl("javascript:singleParkSpotView('" + ParkingPosition + "')");
                                Log.e("called", "called");


                        }
                    });

                    WebSettings settings = webView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setAllowFileAccess(true);
                    //settings.setAppCacheEnabled(true);
                    settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

                    JavaScriptInterface jsInterface = new JavaScriptInterface(ParkingSpotSelect.activity, webView, v.getContext());
                    webView.addJavascriptInterface(jsInterface, "JSInterface");

                    webView.loadUrl("file:///android_asset/map.html");
                    closebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.show();

                }
            });
//            delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    selectedParingSpotObjects.subList(getAdapterPosition(), spotObjectArrayList.size()-1).clear();
//                    spotObjectArrayList.subList(getAdapterPosition(),spotObjectArrayList.size()-1).clear();
//                    Log.e(String.valueOf(spotObjectArrayList.size()-1),String.valueOf(getAdapterPosition()));
//                    notifyDataSetChanged();
//
//                }
//            });


        }


        public void showDialog(String Error, String description, int position, int length) {


            AlertDialog.Builder builder1 = new AlertDialog.Builder(ParkingSpotSelect.activity);


            builder1.setTitle(Error);
            builder1.setMessage(description);
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                        }
                    });
            builder1.setNegativeButton("close",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();

        }


    }
}
