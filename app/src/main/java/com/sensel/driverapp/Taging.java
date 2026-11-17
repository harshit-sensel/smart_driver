package com.sensel.driverapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.ksoap2.serialization.PropertyInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by MS on 18-Apr-17.
 */

public class Taging extends AppCompatActivity {
    public String tagInOrOut = null;
    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 101;
    private static final String TAG = "Taging";
    private Camera camera = null;
    private String dir_path=null;
    String photos=null;
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
     String latlng="0,0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taging);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        Button taginButton = (Button) this.findViewById(R.id.tagin);
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        latlng = gpsTracker.getLocation();
        taginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tagInOrOut="TagIn";
                if(isDeviceSupportCamera())
                {
                    if (ContextCompat.checkSelfPermission(Taging.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Taging.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST);
                    }
                    else if (ContextCompat.checkSelfPermission(Taging.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(Taging.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        }
                    else {
                        takeSnapShots();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Device will not support camera",Toast.LENGTH_LONG).show();
                }
            }
        });
        Button tagoutButton = (Button) this.findViewById(R.id.tagout);
        tagoutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tagInOrOut="TagOut";
                if(isDeviceSupportCamera())
                {
                    if (ContextCompat.checkSelfPermission(Taging.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Taging.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST);
                    }
                    else if (ContextCompat.checkSelfPermission(Taging.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Taging.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                    else {
                        takeSnapShots();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Device will not support camera",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void takeSnapShots()
    {
        // here below "this" is activity context.
        SurfaceTexture surfaceTexture = new SurfaceTexture(0);

       // SurfaceView surface = new SurfaceView(this);
        if(camera!=null) {
            camera.release();
            camera=null;
        }
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (Exception e) {
           Toast.makeText(getApplicationContext(),"No front camera",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try {
            camera.setPreviewTexture(surfaceTexture);
           // camera.setPreviewDisplay(surface.getHolder());
            camera.startPreview();
            camera.takePicture(null,null,jpegCallback);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            String s=e.toString();
            e.printStackTrace();
        }

    }


    /** picture call back */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File file = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Config.IMAGE_DIRECTORY_NAME);
            // File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
            if (!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream outStream = null;
            try {
                dir_path = file.getAbsolutePath() + "/"+tagInOrOut +"_" + System.currentTimeMillis() + ".jpg";
                outStream = new FileOutputStream(dir_path);
                outStream.write(data);
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally
            {
                camera.stopPreview();
                camera.release();
               // camera = null;
                if(isNetworkAvailable())
                    uploadfile();
                else
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Taging.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Taging.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                    else {
                        takeSnapShots();
                    }

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
            case MY_PERMISSIONS_REQUEST_STORAGE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeSnapShots();
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
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void uploadfile()
    {
        if(isNetworkAvailable()) {
            final ProgressDialog progressDialog = new ProgressDialog(Taging.this);
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
                                if(dir_path!=null)
                                {
                                    String imageName="Taging_"+System.currentTimeMillis() + ".jpg";
                                    photos=imageName;
                                    RequestQueue mVolleyRequestQueue= Volley.newRequestQueue(getApplicationContext());
                                    String res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                            "UploadFile", dir_path,imageName, Splash.SESSIONID);
                                    try {
                                        if(!res.contains("success")) {
                                            res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                                    "UploadFile", dir_path,imageName, Splash.SESSIONID);
                                        }
                                    }catch (Exception e)
                                    {

                                    }
                                }
                                String result=InsertTaging();
                                if(result.contains("Exception"))
                                {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                                    new ContextThemeWrapper(Taging.this, android.R.style.Theme_Holo_Light_Dialog));
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
                                                    new ContextThemeWrapper(Taging.this, android.R.style.Theme_Holo_Light_Dialog));
                                            alertDialogBuilder.setIcon(R.drawable.success);
                                            alertDialogBuilder.setTitle("Success ");
                                            alertDialogBuilder.setMessage(tagInOrOut+" done successfully")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
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
        else
            Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
    }

    public String InsertTaging() {
        String device_id1="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            device_id1 = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id1 = android_id;
            }
        }
        final String device_id = device_id1;

        String SOAP_ACTION = "http://tempuri.org/TagingReport";
        String OPERATION_NAME = "TagingReport";

        PropertyInfo[] propertyInfos=new PropertyInfo[4];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        propertyInfos[0]=pi;

        pi = new PropertyInfo();
        pi.setName("latlng");
        pi.setValue(latlng);
        pi.setType(String.class);
        propertyInfos[1]=pi;

        pi = new PropertyInfo();
        pi.setName("tagInOrOut");
        pi.setValue(tagInOrOut);
        pi.setType(String.class);
        propertyInfos[2]=pi;

        pi = new PropertyInfo();
        pi.setName("Photopath");
        pi.setValue(photos);
        pi.setType(String.class);
        propertyInfos[3]=pi;

        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,propertyInfos,SOAP_ACTION);
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
        photos=dir_path.substring(dir_path.lastIndexOf("/")+1);
        try {
            out = new FileOutputStream(dir_path);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return dir_path;

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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
