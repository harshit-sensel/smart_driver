package com.sensel.driverapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by User on 09-12-2016.
 */

public class Daily_Checklist extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 104;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    private Uri fileUri;
    ImageView imageView;
    static int photocount = 0;
    String photos = null;
    static String imagecapturepath = "";
    String[] images = {"img1a", "img1b", "img1c", "img1d", "img1e", "img1f", "img2f", "img3f", "img1g", "img1h", "img1i", "img2i", "img3i", "img1j"};
    String[] checklists = {"chk1a", "chk1b", "chk1c", "chk1d", "chk1e", "chk1f", "chk2f", "chk3f", "chk1g", "chk1h", "chk1i", "chk2i", "chk3i", "chk1j"};
    CheckBox chk1a,chk1b,chk1c,chk1d,chk1e,chk1f,chk2f,chk3f,chk1g,chk1h,chk1i,chk2i,chk3i,chk1j;
    private static boolean executed=false;
    public static  String NAMESPACE = "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/";
    //public static  String NAMESPACE = "http://54.164.125.228:8080/SenselRestService.svc/rest/";

    //private String url = "http://54.164.125.228:8080/SenselRestService.svc/rest/GetWorkStartTime";

    public static String workstarttime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Daily_Checklist.this);
        alertDialogBuilder.setTitle("Confirmation ");
        alertDialogBuilder.setMessage("Work Time Started???")
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();

                    }   

                })
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_checklist);

        ActionBar actionBar = getSupportActionBar();
        chk1a=findViewById(R.id.chk1a);
        chk1b=findViewById(R.id.chk1b);
        chk1c=findViewById(R.id.chk1c);
        chk1d=findViewById(R.id.chk1d);
        chk1e=findViewById(R.id.chk1e);
        chk1f=findViewById(R.id.chk1f);
        chk2f=findViewById(R.id.chk2f);
        chk3f=findViewById(R.id.chk3f);
        chk1g=findViewById(R.id.chk1g);
        chk1h=findViewById(R.id.chk1h);
        chk1i=findViewById(R.id.chk1i);
        chk2i=findViewById(R.id.chk2i);
        chk3i=findViewById(R.id.chk3i);
        chk1j=findViewById(R.id.chk1j);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
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
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Daily_Checklist.this);
                    alertDialogBuilder.setTitle("Checklist ");
                    alertDialogBuilder.setMessage(R.string.alert_continue)
                            .setCancelable(false)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Submit",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            uploadfiles();
                                        }
                                    });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                    break;
                //}

        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        checkworktimestartedornot();
        // capture picture
        if (isDeviceSupportCamera()) {
            if (Build.VERSION.SDK_INT <= 32) {
                if (ContextCompat.checkSelfPermission(Daily_Checklist.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(Daily_Checklist.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else if (ContextCompat.checkSelfPermission(Daily_Checklist.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Daily_Checklist.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                } else if (ContextCompat.checkSelfPermission(Daily_Checklist.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Daily_Checklist.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                } else {
                    imageView = (ImageView) findViewById(v.getId());
                    captureImage();
                }
            }
            else{
                if (ContextCompat.checkSelfPermission(Daily_Checklist.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(Daily_Checklist.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    imageView = (ImageView) findViewById(v.getId());
                    captureImage();
                }

            }
        } else {
            Toast.makeText(getApplicationContext(), "Device will not support camera", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    final AlertDialog ad = new AlertDialog.Builder(this).create();
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
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
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
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME_CHECKLIST);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(DeviationReport.class.getSimpleName(), "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME_CHECKLIST + " directory");
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
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; //1/2 of original size
                imageView.setImageBitmap(BitmapFactory.decodeFile(fileUri.getPath(), options));
                String path = compressImage(fileUri.getPath());
                imageView.setTag(path);
                File file = new File(fileUri.getPath());
                if (file.exists())
                    file.delete();
                photocount++;*/
                try {
                    File imgFileExternalStorage = new File(imagecapturepath);
                    if (!imgFileExternalStorage.exists()) {
                        return;
                    }
                    String fileName1 = getFilename();
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
                    String path =fileName1;
                    imageView.setTag(path);
                    photocount++;

                } catch (Exception e) {
                    Log.d("error massage", e.getMessage());
                    Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
                }

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

        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public void uploadfiles() {

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

        if(!isAllChecked()){
            Toast.makeText(getApplicationContext(),"Please check all the checklist items..",Toast.LENGTH_LONG).show();
            return;

        }
        if (isNetworkAvailable()) {
            final ProgressDialog progressDialog = new ProgressDialog(Daily_Checklist.this);
            progressDialog.setMessage("Loading please wait ...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Thread splashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        while (waited < 300) {
                            sleep(10);
                            waited += 100;
                            if (waited == 100) {
                                getImages();
                                    String IMEI=getIMEI();
                                    String res1="200";
                                    //String res = InsertDeviation(device_id);
                                    String methodStr = "GetWorkStartTime, IMEI";
                                    res1=getService(methodStr, IMEI);
                                if (res1.contains("200")) {
                                    String res = InsertDeviation(device_id);
                                    if (res.contains("Exception")) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Daily_Checklist.this);
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
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Daily_Checklist.this);
                                                alertDialogBuilder.setIcon(R.drawable.success);
                                                alertDialogBuilder.setTitle("Success ");
                                                alertDialogBuilder.setMessage("Reported successfully")
                                                        .setCancelable(false)
                                                        .setPositiveButton("Ok",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                        finish();
                                                                    }
                                                                });
                                                AlertDialog alert = alertDialogBuilder.create();
                                                alert.show();
                                            }
                                        });
                                    }
                                }
                                else{runOnUiThread(new Runnable() {
                                    public void run() {
                                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Daily_Checklist.this);
                                        alertDialogBuilder.setIcon(R.drawable.error);
                                        alertDialogBuilder.setTitle("Wanring ");
                                        alertDialogBuilder.setMessage("Please start the work time to proceed...")
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
                                });}
                            }
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Internet not connected or poor connection", Toast.LENGTH_SHORT).show();
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
        String filePath = imageUri;
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
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
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
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
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

        try {
            out = new FileOutputStream(filename);
//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);

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
                Config.IMAGE_DIRECTORY_NAME_CHECKLIST);
        // File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/Checklist_" + System.currentTimeMillis() + ".jpg");
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
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;

    }

    public String getImages() {
        String package_name = getPackageName();
        for (int i = 0; i < images.length; i++) {

            int resId = getResources().getIdentifier(images[i], "id", package_name);
            imageView = (ImageView) findViewById(resId);
            if (imageView.getTag() != null) {
                String path = String.valueOf(imageView.getTag());
                String imageName = "DailyChecklist_" + System.currentTimeMillis() + ".jpg";
                if (photos == null)
                    photos = imageName;
                else
                    photos = photos + ":" + imageName;
                if (isNetworkAvailable()) {
                    RequestQueue mVolleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
                    String res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                            "UploadFile", path, imageName, Splash.SESSIONID);
                    try {
                        if (!res.contains("success")) {
                            res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                    "UploadFile", path, imageName, Splash.SESSIONID);
                        }
                    } catch (Exception e) {
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Daily_Checklist.this);
                            alertDialogBuilder.setIcon(R.drawable.error);
                            alertDialogBuilder.setTitle("Error ");
                            alertDialogBuilder.setMessage("Internet disconnected, try later")
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
            } else {
                if (photos == null)
                    photos = " :";
                else
                    photos = photos + ":";
            }

        }
        return null;
    }

    public String InsertDeviation(String device_id) {
        String SOAP_ACTION = "http://tempuri.org/InsertChecklistNew";
        String OPERATION_NAME = "InsertChecklistNew";
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        PropertyInfo[] piarray = new PropertyInfo[16];
        String package_name = getPackageName();
        CheckBox checkBox;

        int resId = getResources().getIdentifier(checklists[0], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);


        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0] = pi;

        pi = new PropertyInfo();
        pi.setName("EngineOil");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[1] = pi;

        resId = getResources().getIdentifier(checklists[1], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("CoolantLevel");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[2] = pi;

        resId = getResources().getIdentifier(checklists[2], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("WaterSeperator");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[3] = pi;

        resId = getResources().getIdentifier(checklists[3], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("EDCWarningLight");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[4] = pi;

        resId = getResources().getIdentifier(checklists[4], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("VaccumIndicator");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[5] = pi;

        resId = getResources().getIdentifier(checklists[5], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("TyrePressure");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[6] = pi;

        resId = getResources().getIdentifier(checklists[6], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("WheelNuts");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[7] = pi;

        resId = getResources().getIdentifier(checklists[7], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("TyreWear");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[8] = pi;

        resId = getResources().getIdentifier(checklists[8], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("Gauges");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[9] = pi;

        resId = getResources().getIdentifier(checklists[9], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("Wiper");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[10] = pi;

        resId = getResources().getIdentifier(checklists[10], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("CabinLights");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[11] = pi;

        resId = getResources().getIdentifier(checklists[11], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("WaterLevel");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[12] = pi;

        resId = getResources().getIdentifier(checklists[12], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("Battery");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[13] = pi;

        resId = getResources().getIdentifier(checklists[13], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("GPS");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[14] = pi;

        /*resId = getResources().getIdentifier(checklists[14], "id", package_name);
        checkBox = (CheckBox) findViewById(resId);
        pi = new PropertyInfo();
        pi.setName("WorkStart");
        pi.setValue(checkBox.isChecked());
        pi.setType(Boolean.class);
        piarray[15] = pi;*/

        pi = new PropertyInfo();
        pi.setName("photos");
        pi.setValue(photos);
        pi.setType(String.class);
        piarray[15] = pi;

        WebserviceCall webserviceCall = new WebserviceCall();
        return webserviceCall.callWebservice(Config.DOMAINURL1, Config.DOMAINURL2, OPERATION_NAME, piarray, SOAP_ACTION);
    }

    private boolean isAllChecked(){
       if(chk1a.isChecked()&&chk1b.isChecked()&&chk1c.isChecked()&&chk1d.isChecked()&&chk1e.isChecked()&&chk1f.isChecked()&&chk2f.isChecked()&&chk3f.isChecked()&&chk1g.isChecked()&&chk1h.isChecked()&&chk1i.isChecked()&&chk2i.isChecked()&&chk3i.isChecked()&&chk1j.isChecked()){
           return true;
       }
       return false;

    }

    public String getService(String methodName, String ... var) {

        StringBuffer res = new StringBuffer();
        String[] methodParam = methodName.split(",");
        if(methodParam.length != var.length + 1)
            return "";

        StringBuffer methodUrl = new StringBuffer(NAMESPACE + methodParam[0].trim());
        for (int i = 1; i < methodParam.length; i++)
        {
            if(i == 1)
            {
                methodUrl.append("?");
            }
            if(var[i -1] == null || var[i - 1].equals("")) continue;
            methodUrl.append(methodParam[i].trim());
            methodUrl.append("=");
            var[i -1] = var[i -1].trim();
            var[i -1] = var[i -1].replace(" ", "%20");
            methodUrl.append(var[i -1].trim());
            if(i != methodParam.length-1)
            {
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

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        executed=true;
        return res.toString();
    }
    /******* For getting IMEI number of device *******/
    public String getIMEI()
    {
        String device_id="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id = android_id;
            }
        }
        return device_id;
    }

    public void checkworktimestartedornot() {
        final ProgressDialog progressDialog = new ProgressDialog(Daily_Checklist.this);
        progressDialog.setMessage("Loading please wait ...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if(!progressDialog.isShowing())
            progressDialog.show();
        if (isNetworkAvailable()) {

            Thread splashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        while (waited < 300) {
                            sleep(10);
                            waited += 100;
                            if (waited == 100) {
                                String IMEI=getIMEI();
                                String res="200";
                                String methodStr = "GetWorkStartTime, IMEI";
                                res = getService(methodStr, IMEI);
                                Daily_Checklist.workstarttime = res;
                                    if (!res.contains("200")) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                                        new ContextThemeWrapper(Daily_Checklist.this, android.R.style.Theme_Holo_Light_Dialog));
                                                alertDialogBuilder.setIcon(R.drawable.error);
                                                alertDialogBuilder.setTitle("Warning ");
                                                alertDialogBuilder.setMessage("Please start the work time to proceed...")
                                                        .setCancelable(false)
                                                        .setPositiveButton("Ok",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.dismiss();
                                                                        return;
                                                                    }
                                                                });
                                                AlertDialog alert = alertDialogBuilder.create();
                                                alert.show();
                                                progressDialog.dismiss();
                                                return;
                                            }
                                        });
                                        return;
                                    }
                            }
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Internet not connected or poor connection", Toast.LENGTH_SHORT).show();
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
}




