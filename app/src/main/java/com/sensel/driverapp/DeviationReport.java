package com.sensel.driverapp;

/**
 * Created by User on 29-04-2016.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Mallikarjun on 21-01-2016.
 */

public class DeviationReport extends AppCompatActivity {

    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    Context cont;
    public static String drawing_mode=null;
    public static String latlng=null;
    public static String place=null;
    int count=0;
    // Camera activity request codes
    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 104;
    private static int RESULT_LOAD_IMAGE = 1;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    ImageView imageView;
    String[] images=new String[3];
    static  int image_index=0;
    String photos=null;
    static String imagecapturepath = "";
    private Uri fileUri; // file url to store image/video
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviation_report);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        cont=this.getApplicationContext();

        ArrayAdapter<String> adapter;

        final Spinner spinner = (Spinner) findViewById(R.id.category);
        ArrayList categories = new ArrayList();
        categories.add("Select");
        categories.add("Bad Road");
        categories.add("Road blocked");
        categories.add("Accident zone");
        categories.add("Parking");
        categories.add("Other");

        // Creating adapter for spinner
        final ArrayAdapter dataAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        final ProgressDialog progressDialog = new ProgressDialog(DeviationReport.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Please wait....");
        progressDialog.show();

        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        myWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                progressDialog.dismiss();
            }
        });

        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        //settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        JavaScriptInterface jsInterface = new JavaScriptInterface(this,myWebView,getApplicationContext());
        myWebView.addJavascriptInterface(jsInterface, "JSInterface");
        myWebView.loadUrl("file:///android_asset/DeviationReport.html");

        final EditText placename = (EditText) findViewById(R.id.place);
        if (place != null) {
            placename.setText(place);
        }

        // Fast-tap fallback: if place is still coordinates, resolve street address in background
        if (latlng != null && latlng.contains(",")) {
            final String currentCoords = latlng;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Geocoder.isPresent()) {
                            String[] parts = currentCoords.split(",");
                            double lat = Double.parseDouble(parts[0].trim());
                            double lng = Double.parseDouble(parts[1].trim());

                            Geocoder geocoder = new Geocoder(DeviationReport.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address addr = addresses.get(0);
                                final String resolvedAddress = addr.getAddressLine(0);
                                if (resolvedAddress != null && currentCoords.equals(latlng)) {
                                    place = resolvedAddress;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (placename != null) {
                                                String currentText = placename.getText().toString().trim();
                                                if (currentText.isEmpty() || currentText.equals(currentCoords)) {
                                                    placename.setText(resolvedAddress);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DeviationReport", "Native geocoding fallback error: " + e.getMessage());
                    }
                }
            }).start();
        }

        myWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        /**
         * Capture image button click event
         */
        //popup menu








// Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                 finish();
                 break;
            case R.id.menu_add:
                EditText placename=(EditText) findViewById(R.id.place);
                Spinner spinner=(Spinner) findViewById(R.id.category);
                if(placename.getText().length()<1)
                {
                    Toast.makeText(getApplicationContext(),"Enter place",Toast.LENGTH_LONG).show();
                }
               else if (spinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(getApplicationContext(), "Please select place type ", Toast.LENGTH_SHORT).show();

                }
                 else if(drawing_mode==null || latlng==null){
                    Toast.makeText(getApplicationContext(),"Choose place in map",Toast.LENGTH_LONG).show();
                }
                else {
                    if(isNetworkAvailable())
                    uploadfiles();
                    else
                        Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_LONG).show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public String InsertDeviation(String device_id) {
        String soapaddress = "https://fleetsmart3.ui.sensel.in/SenselWebService.asmx";
        String SOAP_ACTION = "http://tempuri.org/DeviatioReport";
        String OPERATION_NAME = "DeviatioReport";
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);

        String[] geocode=DeviationReport.latlng.split(",");
        EditText placename=(EditText) findViewById(R.id.place);
        EditText comments=(EditText) findViewById(R.id.comment);
        Spinner spinner=(Spinner) findViewById(R.id.category);

        //PropertyInfo[] propertyInfos=new PropertyInfo[6];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        //propertyInfos[0]=pi;
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("place");
        pi.setValue(placename.getText().toString());
        pi.setType(String.class);
        //propertyInfos[1]=pi;
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("placetype");
        if(comments.getText().toString().length()>1)
        pi.setValue(spinner.getSelectedItem().toString()+"#"+comments.getText().toString());
        else
            pi.setValue(spinner.getSelectedItem().toString());
        pi.setType(String.class);
        //propertyInfos[2]=pi;
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("minlat");
        pi.setValue(geocode[0]);
        pi.setType(String.class);
        //propertyInfos[3]=pi;
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("minlng");
        pi.setValue(geocode[1]);
        pi.setType(String.class);
        //propertyInfos[4]=pi;
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("photo");
        pi.setValue(photos);
        pi.setType(String.class);
        //propertyInfos[5]=pi;
        request.addProperty(pi);
    //    Log.e("parameters",device_id+" | "+placename.getText().toString()+" | "+spinner.getSelectedItem().toString()+"#"+comments.getText().toString()+" | "+spinner.getSelectedItem().toString()+" | "+geocode[0]+" | "+geocode[1]+" | "+photos);



        /*WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,propertyInfos,SOAP_ACTION);*/
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(soapaddress);
        Object response = null;
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            response = envelope.getResponse();
        } catch (Exception exception) {
            response = exception.toString();
        }
        return response.toString();
    }

    public void onClick(View v) {
        // capture picture
        PopupMenu popup = new PopupMenu(DeviationReport.this, v);
        imageView=(ImageView) findViewById(v.getId());
        if(R.id.img1==v.getId())
        {
            image_index=0;
        }
        else if(R.id.img2==v.getId())
        {
            image_index=1;
        }
        else
        {
            image_index=2;
        }
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle().equals("Camera"))
                {
                    if (Build.VERSION.SDK_INT <= 32) {
                        if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        } else if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        } else if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        } else {
                            captureImage();
                        }
                    }
                    else{
                        if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        } else {
                            captureImage();
                        }

                    }
                }
                else if(item.getTitle().equals("Gallary"))
                {
                    if (Build.VERSION.SDK_INT <= 32) {
                        if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        } else if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        } else if (ContextCompat.checkSelfPermission(DeviationReport.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(DeviationReport.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        }
                        else {
                            Intent i = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            startActivityForResult(i, RESULT_LOAD_IMAGE);
                        }
                    }
                    else {
                        Intent i = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                    }
                }
                return true;
            }
        });
        popup.show();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        /*Log.e("camera ","captureImage() called");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);



        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
        }
        //fileUri = FileProvider.getUriForFile(this, "com.total.getit.provider", getOutputMediaFile(MEDIA_TYPE_IMAGE));
        //fileUri = FileProvider.getUriForFile(this, "com.total.driverapp.provider", photoFile);
        fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // Grant temporary permission to access the file URI
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName =  timeStamp;
        File storageDir =getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = new File(RULE.ruleImageDirectory);
        Log.d("StorageDirectory","StorageDirectory"+storageDir);
        storageDir.mkdirs(); // make sure you call mkdirs() and not mkdir()
        /*File image = File.createTempFile(
                FileName[0],  *//* prefix *//*
                ".PNG" ,*//* suffix *//*
                storageDir      *//* directory *//*
        );*/
        File image = new File(storageDir, imageFileName + ".jpg");
        imagecapturepath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            images[image_index] = picturePath;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; //1/2 of original size
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath, options));
            ImageView next;
            if (image_index == 0)
                next = (ImageView) findViewById(R.id.img2);
            else
                next = (ImageView) findViewById(R.id.img3);
            next.setVisibility(View.VISIBLE);
        }

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // successfully captured the image
                // launching upload activity
                launchUploadActivity(true);


            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                // launching upload activity
                launchUploadActivity(false);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                    final AlertDialog ad = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog)).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("STORAGE permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    final AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("Camera permission is mandatory to run app.");
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

    private void launchUploadActivity(boolean isImage){
        /*images[image_index]=fileUri.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize =2; //1/2 of original size
        imageView.setImageBitmap(BitmapFactory.decodeFile(fileUri.getPath(),options));
        ImageView next;
        if(image_index==0)
            next=(ImageView) findViewById(R.id.img2);
        else
            next=(ImageView) findViewById(R.id.img3);
        next.setVisibility(View.VISIBLE);
      //  startActivity(i);*/
        try {
            File imgFileExternalStorage = new File(imagecapturepath);
            if (!imgFileExternalStorage.exists()) {
                return;
            }
            String fileName = imgFileExternalStorage.getName();
            String fileName1 = getFilename();
            String finalFilePath = imgFileExternalStorage.getPath();
            images[image_index] = imgFileExternalStorage.getPath();
            //compress the captured image
            Uri takenPhotoUri = Uri.fromFile(imgFileExternalStorage);
            // by this point we have the camera photo on disk
            Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            rawTakenImage.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
            FileOutputStream fos = new FileOutputStream(imgFileExternalStorage);
            fos.write(bytes.toByteArray());
            fos.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(takenPhotoUri.getPath()));
            imageView.setTag(fileName1);
            ImageView next;
            if (image_index == 0)
                next = (ImageView) findViewById(R.id.img2);
            else
                next = (ImageView) findViewById(R.id.img3);
            next.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.d("error massage", e.getMessage());
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(DeviationReport.class.getSimpleName(),"Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void uploadfiles()
    {
        String device_id1="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            device_id1 = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id1 = android_id;
            }
        }
        final String device_id = device_id1;
        if(isNetworkAvailable()) {
            final ProgressDialog progressDialog = new ProgressDialog(DeviationReport.this);
            progressDialog.setMessage("Loading please wait ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            Thread splashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        while (waited < 1000) {
                            sleep(100);
                            waited += 100;
                            if (waited == 100) {
                                for(int i=0;i<images.length;i++)
                                {
                                    if(images[i]!=null)
                                    {
                                        String imageName="DeviationReport_"+System.currentTimeMillis() + ".jpg";
                                        if(photos==null || photos==""){
                                            photos=imageName;
                                        }
                                        else{
                                            photos=photos+","+imageName;
                                        }

                                        RequestQueue mVolleyRequestQueue= Volley.newRequestQueue(getApplicationContext());

                                        String res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                                "UploadFile", images[i],imageName, Splash.SESSIONID);
                                       Log.e("volley", res);
                                        try {
                                            if(!res.contains("success")) {
                                                res= res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                                        "UploadFile", images[i],imageName, Splash.SESSIONID);
                                                Log.e("try volley", res);
                                            }
                                        }catch (Exception e)
                                        {

                                        }
                                    }
                                }
                              String result=InsertDeviation(device_id);
                                Log.e("RESULT",result);
                                if(result.contains("Exception"))
                                {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                                    new ContextThemeWrapper(DeviationReport.this, android.R.style.Theme_Holo_Light_Dialog));
                                            alertDialogBuilder.setIcon(R.drawable.error);
                                            alertDialogBuilder.setTitle("Error ");
                                            alertDialogBuilder.setMessage("Server not responding, try later")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                }
                                                            });
                                            AlertDialog alert = alertDialogBuilder.create();
                                            alert.show();
                                        }
                                    });
                                }
                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                                    new ContextThemeWrapper(DeviationReport.this, android.R.style.Theme_Holo_Light_Dialog));
                                            alertDialogBuilder.setIcon(R.drawable.success);
                                            alertDialogBuilder.setTitle("Success ");
                                            alertDialogBuilder.setMessage("Reported successfully")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                    drawing_mode=null;
                                                                    latlng=null;
                                                                    place=null;
                                                                    finish();
                                                                   /* Intent i = new  Intent(getApplicationContext(), MainActivity.class);
                                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    getApplicationContext().startActivity(i);*/
                                                                }
                                                            });
                                            AlertDialog alert = alertDialogBuilder.create();
                                            alert.show();
                                        }
                                    });
                                }


                            }
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Internet not connected or poor connection",Toast.LENGTH_SHORT).show();
                            }
                        });
                    } finally {
                        progressDialog.dismiss();
                    }
                }

            };

            splashThread.start();
        }
    }

    public String compressImage(String imageUri) {

       // String filePath = getRealPathFromURI(imageUri);
        String filePath=imageUri;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        if(photos==null)
            photos=filename.substring(filename.lastIndexOf("/")+1);
        else
          photos=photos+","+filename.substring(filename.lastIndexOf("/")+1);
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file =getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        /*File file = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);
       // File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;*/
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + imagecapturepath.substring(imagecapturepath.lastIndexOf("/") + 1));
        return uriSting;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}