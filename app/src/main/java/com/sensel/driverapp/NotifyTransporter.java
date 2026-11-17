package com.sensel.driverapp;

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
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.ksoap2.serialization.PropertyInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by User on 28-12-2016.
 */

public class NotifyTransporter extends AppCompatActivity {
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    Context cont;
    // Camera activity request codesMY_PERMISSIONS_REQUEST
    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 104;
    private static int RESULT_LOAD_IMAGE = 1;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    ImageView imageView;
    static String imagecapturepath = "";
    String image;
    String photo=null;
    private Uri fileUri; // file url to store image/video
    EditText subject;
    EditText message;
    String photos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        cont = this.getApplicationContext();
        subject=(EditText) findViewById(R.id.subject);
        message=(EditText) findViewById(R.id.message);
        imageView=(ImageView) findViewById(R.id.img1);
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
                if(subject.getText().length()<1)
                {
                    Toast.makeText(getApplicationContext(),"Enter subject",Toast.LENGTH_LONG).show();
                }
                else if(message.getText().length()<1)
                {
                    Toast.makeText(getApplicationContext(),"Enter message",Toast.LENGTH_LONG).show();
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

    public void onClick(View v) {
        // capture picture
        PopupMenu popup = new PopupMenu(NotifyTransporter.this, v);
        imageView=(ImageView) findViewById(v.getId());

        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle().equals("Camera"))
                {
                    if (Build.VERSION.SDK_INT <= 32) {
                        if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(NotifyTransporter.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        } else if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(NotifyTransporter.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        } else if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(NotifyTransporter.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        } else {
                            captureImage();
                        }
                    }
                    else{
                        if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(NotifyTransporter.this,
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
                        if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(NotifyTransporter.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        } else if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(NotifyTransporter.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_STORAGE);
                        } else if (ContextCompat.checkSelfPermission(NotifyTransporter.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(NotifyTransporter.this,
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
            String path = compressImage(picturePath);
            image = path;
            imageView.setTag(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; //1/2 of original size
            imageView.setImageBitmap(BitmapFactory.decodeFile(path, options));
        }

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                /*String path = compressImage(fileUri.getPath());
                imageView.setTag(path);
                image = path;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; //1/2 of original size
                imageView.setImageBitmap(BitmapFactory.decodeFile(path, options));
                // successfully captured the image
                // launching upload activity
                //   launchUploadActivity(true);*/
                try {
                    File imgFileExternalStorage = new File(imagecapturepath);
                    if (!imgFileExternalStorage.exists()) {
                        return;
                    }
                    String fileName = imgFileExternalStorage.getName();
                    String fileName1=getFilename();
                    String finalFilePath = imgFileExternalStorage.getPath();

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
                    image = fileName1;

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

        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                // launching upload activity
                // launchUploadActivity(false);

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
        String uriSting = (file.getAbsolutePath() + "/message_" + System.currentTimeMillis() + ".jpg");
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
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;

    }

    public void uploadfiles()
    {

        String device_id1="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            device_id1 = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(cont.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id1 = android_id;
            }
        }
        final String device_id = device_id1;
        if(isNetworkAvailable()) {
            final ProgressDialog progressDialog = new ProgressDialog(NotifyTransporter.this);
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
                                if(image!=null) {
                                    String imageName="NotifyTransporter_"+System.currentTimeMillis() + ".jpg";
                                    photos=imageName;
                                    RequestQueue mVolleyRequestQueue= Volley.newRequestQueue(getApplicationContext());
                                    String res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                            "UploadFile", image,imageName, Splash.SESSIONID);
                                    try {
                                        if (!res.contains("success")) {
                                            res = WebAccessor.getNewInstance().uploadImageService(mVolleyRequestQueue,
                                                    "UploadFile", image,imageName, Splash.SESSIONID);
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                                String result=InsertNotification(device_id);
                                if(result.contains("Exception"))
                                {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NotifyTransporter.this);
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
                                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NotifyTransporter.this);
                                            alertDialogBuilder.setIcon(R.drawable.success);
                                            alertDialogBuilder.setTitle("Success ");
                                            alertDialogBuilder.setMessage("Reported successfully")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                  subject.setText("");
                                                                  message.setText("");
                                                                  imageView.setImageResource(R.drawable.file_add);
                                                                    finish();
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
                                Toast.makeText(getApplicationContext(),"Something went wrong, try again",Toast.LENGTH_SHORT).show();
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

    public String InsertNotification(String device_id) {

        String SOAP_ACTION = "http://tempuri.org/addTransporterNotifications";
        String OPERATION_NAME = "addTransporterNotifications";
        PropertyInfo[] piarray=new PropertyInfo[2];

        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;

        String html="";
        if(image!=null) {
             //photos = image.substring(image.lastIndexOf("/") + 1);//format html to display image
             html = "<br/><img height=\"200\" width=\"210\" src=\""+Config.DOMAINURL1+"/Uploads/" + photos + "\"/>";
        }
        pi = new PropertyInfo();
        pi.setName("message");
        pi.setValue("<b>("+subject.getText().toString()+")</b> <br/>"+ message.getText().toString()+html);
        pi.setType(String.class);
        piarray[1]=pi;

        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }
}
