package com.sensel.driverapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sensel.driverapp.adapter.ZerofatilityAdaptor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import androidx.core.app.ActivityCompat;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Daily_Checklist1 extends AppCompatActivity {
    public static String NAMESPACE = "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/";
    private static boolean executed = false;
    String[] Rules;
    private LinearLayout checklistContainer;
    private String[] images;
    private String[] comments;

    private ImageView popupImageView;
    private int selectedImageIndex = -1;
    private int globalItemCounter = 0; // used for unique indexing
    boolean[] mandatory;
    //String URL = "http://demo.fleetsmart.in/SenselRestService.svc/rest/v3/GetChecklistByLanguage";
    String URL = "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/GetChecklistByLanguage";
    //String URL=Config.DOMAINURL1+"/SenselRestService.svc/rest/v3/GetChecklistByLanguage";
    public String result;
    public Context context;
    public static String imei;
    private ImageView currentImageView;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 102;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 103;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    public static boolean checkList_flag=false;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_checklist1);
        checklistContainer = findViewById(R.id.checklist_container);
        fetchChecklistFromApi();
    }

    private void fetchChecklistFromApi() {
        imei = getIMEI();
        if (isNetworkAvailable()) {
            final ProgressDialog progressDialog = new ProgressDialog(Daily_Checklist1.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading, please wait...");
            progressDialog.show();

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            // If your API requires POST with params, use this JSON object, else pass `null`
            JSONObject obj = new JSONObject(); // Add any POST parameters if required
            try {
                //obj.put("imei",imei.toString() );
                obj.put("imei", imei.toString());
            } catch (Exception e) {
            }
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.POST,  // Change to GET if your API is GET-based
                    URL,
                    obj,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.e("ChecklistResponse", response.toString());

                            if (response == null || response.length() == 0) {
                                Toast.makeText(Daily_Checklist1.this,
                                        "FAILED TO GET CHECKLIST. ERROR CODE Z001", Toast.LENGTH_LONG).show();
                                finish();
                                return;
                            }

                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject section = response.getJSONObject(i);
                                    addSection(section);  // Reuse your dynamic layout method
                                }
                                // ✅ Add submit button AFTER all sections are added
                                addSubmitButton();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            progressDialog.dismiss();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("ChecklistError", error.toString());
                            Toast.makeText(Daily_Checklist1.this,
                                    "Something went wrong or network issue", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    }
            );

            requestQueue.add(jsonArrayRequest);
        } else {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    new ContextThemeWrapper(Daily_Checklist1.this, android.R.style.Theme_Holo_Light_Dialog));
            alertDialogBuilder.setIcon(R.drawable.error);
            alertDialogBuilder.setTitle("No Network Connection");
            alertDialogBuilder.setMessage("Check your internet connection")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        dialog.cancel();
                        finish();
                        System.exit(0);
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }


    private void addSection(JSONObject section) throws JSONException {
        View header = LayoutInflater.from(this).inflate(R.layout.item_section_header, checklistContainer, false);
        ((TextView) header.findViewById(R.id.section_code)).setText(section.getString("code"));
        ((TextView) header.findViewById(R.id.section_title)).setText(section.getString("title"));
        int iconRes = getResources().getIdentifier(section.getString("icon"), "drawable", getPackageName());
        ((ImageView) header.findViewById(R.id.section_icon)).setImageResource(iconRes);
        checklistContainer.addView(header);

        JSONArray items = section.getJSONArray("items");

        // Only initialize once, if needed
        if (images == null) {
            int totalItems = 0;
            // Calculate total size across all sections
            for (int i = 0; i < items.length(); i++) totalItems++;
            images = new String[1000];    // Optional: make it dynamic
            comments = new String[1000];
        }

        for (int j = 0; j < items.length(); j++) {
            JSONObject item = items.getJSONObject(j);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_checklist_row, checklistContainer, false);

            TextView idView = itemView.findViewById(R.id.item_id);
            TextView descView = itemView.findViewById(R.id.item_desc);
            ImageView imgView = itemView.findViewById(R.id.item_image);
            RadioGroup radioGroup = itemView.findViewById(R.id.radioType);
            RadioButton radioYes = itemView.findViewById(R.id.yes);
            RadioButton radioNo = itemView.findViewById(R.id.no);

            idView.setText(item.getString("id"));
            descView.setText(item.getString("description"));

            // Set unique index tag
            imgView.setTag(R.id.index_tag, globalItemCounter);
            imgView.setTag(R.id.description_tag, item.getString("description"));

            // Disabled by default until "No" is selected
            imgView.setEnabled(false);
            imgView.setAlpha(0.4f);

            // RadioGroup coloring and tag logic
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.no) {
                    itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light red
                    imgView.setEnabled(true); // ✅ Enable upload
                    imgView.setAlpha(1.0f);   // Fully visible

                } else if (checkedId == R.id.yes) {
                    itemView.setBackgroundColor(Color.parseColor("#a8eade")); // Green
                    imgView.setEnabled(false); // ✅ Disable upload
                    imgView.setAlpha(0.4f);    // Make it look faded
                }
            });


            checklistContainer.addView(itemView);
            globalItemCounter++;  // Increment global counter for each item
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getIMEI() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            try {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String device_id = tm.getDeviceId();
                return device_id;
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Give READ_PHONE_STATE permission", Toast.LENGTH_LONG).show();
                return null;
            }
        } else {
            String android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String device_id = "";
            if (android_id != null) {
                device_id = android_id;
            }
            return device_id;
        }
    }

    private void addSubmitButton() {
        Button submitButton = new Button(this);
        submitButton.setText("Submit Checklist");
        submitButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        submitButton.setTextColor(Color.WHITE);
        submitButton.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(32, 48, 32, 64);
        submitButton.setLayoutParams(params);

        submitButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                final ProgressDialog progressDialog = new ProgressDialog(Daily_Checklist1.this);
                progressDialog.setMessage("Loading please wait ...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                new Thread(() -> {
                    try {
                        String IMEI = getIMEI();
                        String methodStr = "GetWorkStartTime, IMEI";
                        //String res1 = getService(methodStr, IMEI); // Or your fixed value for testing
                        String res1 = getService(methodStr, IMEI); // Or your fixed value for testing

                        if (res1.contains("200")) {
                            // ✅ Step 1: Validate checklist + photo requirement
                            runOnUiThread(() -> {
                                if (validateChecklist()) {
                                    // ✅ Step 2: Upload photos & submit data
                                    uploadImagesAndSubmitChecklist(progressDialog);
                                } else {
                                    progressDialog.dismiss(); // Stop spinner on validation failure
                                }
                            });
                        } else {
                            // ❌ Work not started
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                new AlertDialog.Builder(Daily_Checklist1.this)
                                        .setIcon(R.drawable.error)
                                        .setTitle("Warning")
                                        .setMessage("Please start the work time to proceed...")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", (dialog, id) -> dialog.dismiss())
                                        .show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Internet not connected or poor connection", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                new AlertDialog.Builder(Daily_Checklist1.this)
                        .setIcon(R.drawable.error)
                        .setTitle("No Internet")
                        .setMessage("Please check your internet connection and try again.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        });

        checklistContainer.addView(submitButton); // <-- Add it after all sections
    }

    private boolean validateChecklist() {
        boolean allGood = true;
        int itemIndex = 0;

        for (int i = 0; i < checklistContainer.getChildCount(); i++) {
            View child = checklistContainer.getChildAt(i);
            RadioGroup rg = child.findViewById(R.id.radioType);
            TextView itemIdView = child.findViewById(R.id.item_id);

            if (rg == null || itemIdView == null) continue;

            String itemCode = itemIdView.getText().toString();
            int selected = rg.getCheckedRadioButtonId();

            // ✅ Step 1: Check if not selected
            if (selected == -1) {
                child.setBackgroundColor(Color.parseColor("#FFE0B2")); // light orange
                Toast.makeText(this, "Please select Yes or No for item: " + itemCode, Toast.LENGTH_SHORT).show();
                allGood = false;
                break;
            }

            // ✅ Step 2: If "No" selected, image is mandatory
            if (selected == R.id.no) {
                if (images == null || images[itemIndex] == null || images[itemIndex].isEmpty()) {
                    child.setBackgroundColor(Color.parseColor("#FFE0B2"));
                    Toast.makeText(this, "Please upload photo for item: " + itemCode, Toast.LENGTH_SHORT).show();
                    allGood = false;
                    break;
                }
            } else {
                // Reset background for valid entries
                child.setBackgroundColor(Color.parseColor("#a8eade"));
            }

            itemIndex++;
        }

        return allGood;
    }
    private void uploadImagesAndSubmitChecklist(ProgressDialog progressDialog) {
        Map<Integer, String> uploadedImageNames = new ConcurrentHashMap<>();
        List<UploadTask> tasks = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(4); // run 4 uploads in parallel

        for (int i = 0; i < images.length; i++) {
            if (images[i] != null && !images[i].isEmpty()) {
                UploadTask task = new UploadTask(this, i, images[i], uploadedImageNames);
                tasks.add(task);
                executor.execute(task::doInBackground);
            }
        }

        executor.shutdown();

        new Thread(() -> {
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);

                if (uploadedImageNames.size() == tasks.size()) {
                    runOnUiThread(() -> submitChecklistWithUploadedImages(uploadedImageNames, progressDialog));
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "One or more images failed to upload.", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void submitChecklistWithUploadedImages(Map<Integer, String> uploadedImageNames, ProgressDialog progressDialog) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("IMEI", getIMEI());

            // Replace with actual vehicle ID retrieval method
            //String vehicleId = getVehicleId();  // Make sure this returns non-empty value
            //requestJson.put("VehicleId", "KA01 AL 3143");

            JSONArray checklistArray = new JSONArray();
            int itemIndex = 0;

            for (int i = 0; i < checklistContainer.getChildCount(); i++) {
                View child = checklistContainer.getChildAt(i);
                RadioGroup rg = child.findViewById(R.id.radioType);
                TextView itemIdView = child.findViewById(R.id.item_id);

                if (rg == null || itemIdView == null) continue;

                String checklistItemId = itemIdView.getText().toString();
                String itemValue = (rg.getCheckedRadioButtonId() == R.id.yes) ? "Yes" : "No";
                String comment = (comments != null && comments[itemIndex] != null) ? comments[itemIndex] : "";
                String imageName = uploadedImageNames.containsKey(itemIndex) ? uploadedImageNames.get(itemIndex) : "";

                JSONObject checklistObj = new JSONObject();
                checklistObj.put("ChecklistItemId", checklistItemId);
                checklistObj.put("ItemValue", itemValue);
                checklistObj.put("Comment", comment);
                checklistObj.put("ImageName", imageName);

                checklistArray.put(checklistObj);
                itemIndex++;
            }

            requestJson.put("Checklists", checklistArray);

            // ✅ Log final payload for debugging
            Log.d("ChecklistPayload", requestJson.toString());

            //String url = "http://demo.fleetsmart.in/SenselRestService.svc/rest/v3/InsertDailyChecklistdata";
            String url = "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/InsertDailyChecklistdata";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        progressDialog.dismiss();
                        Log.d("ChecklistSubmit", "Server Response: " + response);

                        if (response.contains("200")) {
                            String currentDateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(new Date());

                            String message = "Checklist submitted successfully\nTime: " + currentDateTime;

                            showDialog("Success", message, R.drawable.success, true);
                        } else {
                            showDialog("Error", "Checklist submission failed.\nResponse: " + response, R.drawable.error);
                        }
                    },
                    error -> {
                        progressDialog.dismiss();

                        String errorMsg = "Checklist submission failed.";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                Log.e("VolleyServerError", "Error response: " + responseBody);
                                errorMsg = "Server Error: " + responseBody;
                            } catch (UnsupportedEncodingException e) {
                                Log.e("VolleyError", "Encoding issue", e);
                            }
                        } else {
                            Log.e("VolleyError", "No server response");
                        }

                        showDialog("Error", errorMsg, R.drawable.error);
                    }) {

                @Override
                public byte[] getBody() {
                    return requestJson.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            /*// ✅ Add custom retry policy here
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    20000, // 20 seconds timeout
                    2,     // 2 retries
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));*/
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000, // 10 sec initial
                    3,     // 3 retries
                    2.0f   // exponential backoff
            ));
            Volley.newRequestQueue(this).add(stringRequest);

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e("ChecklistException", "Error preparing request JSON", e);
            Toast.makeText(this, "Error preparing checklist data", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog(String title, String message, int icon) {
        showDialog(title, message, icon, false);
    }

    private void showDialog(String title, String message, int icon, boolean shouldFinish) {
        new AlertDialog.Builder(Daily_Checklist1.this)
                .setIcon(icon)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    dialog.dismiss();
                    if (shouldFinish) finish();
                })
                .show();
    }

    public String getService(String methodName, String... var) {

        StringBuffer res = new StringBuffer();
        String[] methodParam = methodName.split(",");
        if (methodParam.length != var.length + 1)
            return "";

        StringBuffer methodUrl = new StringBuffer(NAMESPACE + methodParam[0].trim());
        for (int i = 1; i < methodParam.length; i++) {
            if (i == 1) {
                methodUrl.append("?");
            }
            if (var[i - 1] == null || var[i - 1].equals("")) continue;
            methodUrl.append(methodParam[i].trim());
            methodUrl.append("=");
            var[i - 1] = var[i - 1].trim();
            var[i - 1] = var[i - 1].replace(" ", "%20");
            methodUrl.append(var[i - 1].trim());
            if (i != methodParam.length - 1) {
                methodUrl.append("&");
            }
        }
        try {

            java.net.URL url = new URL(methodUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                res.append(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        executed = true;
        return res.toString();
    }
    public void onButtonShowPopupWindowClick(final View view) {
        if (!view.isEnabled()) {
            return;
        }
        try {
            selectedImageIndex = (int) view.getTag(R.id.index_tag);
            Log.d("Popup", "Clicked on item index: " + selectedImageIndex);

            View mainLayout = findViewById(android.R.id.content);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.cmnt_with_photo, null);
            TextView header = popupView.findViewById(R.id.header);

            // Retrieve description from the clicked view's tag
            String headingText = (String) view.getTag(R.id.description_tag);
            if (headingText != null) {
                header.setText(headingText);
            }
            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    true);

            popupImageView = popupView.findViewById(R.id.image_camera);
            EditText editText = popupView.findViewById(R.id.edit_text);

            // Restore image if already present
            if (images[selectedImageIndex] != null) {
                File file = new File(images[selectedImageIndex]);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(images[selectedImageIndex]);
                    popupImageView.setImageBitmap(bitmap);
                    popupImageView.setTag(images[selectedImageIndex]);
                }
            }

            // Restore comment if available
            if (comments[selectedImageIndex] != null) {
                editText.setText(comments[selectedImageIndex]);
            }

            popupImageView.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(Daily_Checklist1.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else
                    dispatchTakePictureIntent();

            });
            Button cancel = popupView.findViewById(R.id.cancel);
            cancel.setOnClickListener(v1 -> popupWindow.dismiss());

            Button done = popupView.findViewById(R.id.done);
            done.setOnClickListener(v2 -> {
                if (popupImageView.getTag() != null) {
                    images[selectedImageIndex] = popupImageView.getTag().toString();
                }
                comments[selectedImageIndex] = editText.getText().toString();
                popupWindow.dismiss();
            });

            popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ChecklistPopup", "Error showing popup: " + e.getMessage());
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //  String imageFileName = "JPEG_" + timeStamp + "_";
        String imageFileName =  timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("StorageDirectory","StorageDirectory"+storageDir);
        storageDir.mkdirs(); // make sure you call mkdirs() and not mkdir()
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg" ,/* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    public void onClick(View v) {
        if(isDeviceSupportCamera()) {
            if (Build.VERSION.SDK_INT <= 32) {
                if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(Daily_Checklist1.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Daily_Checklist1.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                } else if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Daily_Checklist1.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                } else {
                    dispatchTakePictureIntent();
                }
            }
            else{
                if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(Daily_Checklist1.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    dispatchTakePictureIntent();
                }

            }
        }
        else
            Toast.makeText(getApplicationContext(), "This device not supporting camera", Toast.LENGTH_SHORT).show();

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null && popupImageView != null) {
                popupImageView.setImageBitmap(bitmap);
                popupImageView.setTag(currentPhotoPath);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "User cancelled image capture", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Daily_Checklist1.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_STORAGE);
                    } else {
                        dispatchTakePictureIntent();
                    }
                } else {

                    final AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("CAMERA permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Daily_Checklist1.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Daily_Checklist1.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    } else
                        dispatchTakePictureIntent();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                    final AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("Location permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    // Utility to compress image
    public static File compressImage(Context context, String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(filePath, options);
        File compressedFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(compressedFile)) {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out); // compress to 70%
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedFile;
    }
    class UploadTask extends AsyncTask<Void, Void, Boolean> {
        int index;
        String path;
        String imageName;
        Context context;
        Map<Integer, String> resultMap;

        UploadTask(Context context, int index, String path, Map<Integer, String> resultMap) {
            this.index = index;
            this.path = path;
            this.context = context;
            this.imageName = "Checklist_" + System.currentTimeMillis() + "_" + index + ".jpg";
            this.resultMap = resultMap;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File compressed = compressImage(context, path);
                int retries = 0;
                boolean success = false;

                while (retries < 3 && !success) {
                    String result = WebAccessor.getNewInstance().uploadImageService(
                            Volley.newRequestQueue(context),
                            "UploadFile",
                            compressed.getAbsolutePath(),
                            imageName,
                            Splash.SESSIONID
                    );
                    if (result != null && result.contains("success")) {
                        resultMap.put(index, imageName);
                        return true;
                    } else {
                        retries++;
                        Thread.sleep((long) Math.pow(2, retries) * 1000); // exponential backoff
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}


