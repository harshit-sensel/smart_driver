package com.sensel.driverapp.adapter;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sensel.driverapp.JavaScriptInterface;
import com.sensel.driverapp.ParkingSpotSelect;
import com.sensel.driverapp.R;
import com.sensel.driverapp.Splash;
import com.sensel.driverapp.objectclass.ParkingSpots;
import com.sensel.driverapp.objectclass.SelectedParingSpotObject;

import java.util.ArrayList;

public class ParkingSpotAdaptor extends RecyclerView.Adapter<ParkingSpotAdaptor.ViewHolder> {

    public static ParkingSpots selectedParking;
    ArrayList<ParkingSpots> parkingSpotsArrayList;
    public static String LatLan = "00.0000,00.0000#|#place##||##";


    public ParkingSpotAdaptor(ArrayList<ParkingSpots> parkingSpots) {
        this.parkingSpotsArrayList = parkingSpots;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_parkingspot, parent, false);
        ViewHolder evh = new ViewHolder(view);

        return evh;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_parkingName.setText(parkingSpotsArrayList.get(position).getSpotName());

    }


    @Override
    public int getItemCount() {
        return parkingSpotsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button add, viewmap;
        TextView txt_parkingName;
        BottomSheetDialog bottomSheetDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewmap = itemView.findViewById(R.id.viewmap);
            add = itemView.findViewById(R.id.add);
            txt_parkingName = itemView.findViewById(R.id.parkingname);

            viewmap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLan = parkingSpotsArrayList.get(getAdapterPosition()).getLatlan()+"##"+parkingSpotsArrayList.get(getAdapterPosition()).getSpotName();
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
                            LatLan="";

                                LatLan = LatLan+ parkingSpotsArrayList.get(getAdapterPosition()).getLatlan()+ "##"+parkingSpotsArrayList.get(getAdapterPosition()).getSpotName();
                                Log.e("lat",LatLan);
                                webView.loadUrl("javascript:singleParkSpotView('" + LatLan + "')");
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
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLan = parkingSpotsArrayList.get(getAdapterPosition()).getLatlan()+"##"+parkingSpotsArrayList.get(getAdapterPosition()).getSpotName();
                    selectedParking=parkingSpotsArrayList.get(getAdapterPosition());
                    ParkingSpotSelect parkingSpotSelect = new ParkingSpotSelect();
                    parkingSpotSelect.showConfirmDialog(parkingSpotsArrayList.get(getAdapterPosition()).getSpotName(),LatLan);
                }
            });


        }


    }
}
