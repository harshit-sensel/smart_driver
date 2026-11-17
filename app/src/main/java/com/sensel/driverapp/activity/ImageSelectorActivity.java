package com.sensel.driverapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.sensel.driverapp.R;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.response.ImageDetails;
import com.sensel.driverapp.utility.PermissionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.sensel.driverapp.utility.ApplicationConstants.CAPTURE_IMAGES_REQUEST;
import static com.sensel.driverapp.utility.ApplicationConstants.IMAGE_LIST;
import static com.sensel.driverapp.utility.ApplicationConstants.PARENT_POSITION;
import static com.sensel.driverapp.utility.ApplicationConstants.PERMISSIONS_REQUEST_CAMERA;
import static com.sensel.driverapp.utility.ApplicationConstants.POSITION;
import static com.sensel.driverapp.utility.ApplicationConstants.REMARK;
import static com.sensel.driverapp.utility.ApplicationConstants.RULE_ID;


public class ImageSelectorActivity extends AppCompatActivity {

    private static final String TAG = ImageSelectorActivity.class.getName();

    private String[] permissions = new String[]{LOCATION_SERVICE, ACCESS_FINE_LOCATION, LOCATION_SERVICE, ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, WRITE_EXTERNAL_STORAGE
            , READ_EXTERNAL_STORAGE};

    @BindView(R.id.linearLayoutContainer)
    LinearLayout linearLayoutContainer;

    @BindView(R.id.imageViewSelectImage)
    ImageView imageViewSelectImage;

    @BindView(R.id.buttonSubmit)
    Button buttonSubmit;

    @BindView(R.id.buttonCancel)
    Button buttonCancel;

    @BindView(R.id.editTextRemark)
    EditText editTextRemark;

    int parentPosition, position;
    private String ruleId, remark;
    private String sessionID;
    private ArrayList<ImageDetails> imageDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        SharedPreferencesUtility sharedPreferencesUtility = new SharedPreferencesUtility(this);
        sessionID = sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID);

        parentPosition = getIntent().getIntExtra(PARENT_POSITION, 0);
        position = getIntent().getIntExtra(POSITION, 0);
        ruleId = getIntent().getStringExtra(RULE_ID);
        remark = getIntent().getStringExtra(REMARK);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().hasExtra("APP_NAME")) {
            String appName = getIntent().getStringExtra("APP_NAME");
            Objects.requireNonNull(getSupportActionBar()).setTitle(appName);
        }

        if (getIntent().hasExtra(IMAGE_LIST))
            imageDetailsList = (ArrayList<ImageDetails>) getIntent().getSerializableExtra(IMAGE_LIST);

        if (remark != null && !remark.isEmpty()) {
            editTextRemark.setText(remark);
        }

        if (imageDetailsList == null)
            imageDetailsList = new ArrayList<>(5);

        for (ImageDetails imageDetails : imageDetailsList) {
            addImageViewToContainer(imageDetails);
        }

        imageViewSelectImage.setOnClickListener(view -> takeImage());

        buttonSubmit.setOnClickListener(view -> returnResult());

        buttonCancel.setOnClickListener(view -> finish());
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void takeImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.checkPermissions(this, permissions);
            PermissionUtils.requestPermissions(this, 1, permissions);
        } else {
            cameraIntent();
        }
    }

    private void cameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile;
            try {
                pictureFile = getPictureFile();
            } catch (Exception ex) {
                Toast.makeText(this, "Photo file can't be created, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAPTURE_IMAGES_REQUEST);
            }
        }
    }

    private String pictureFilePath;

    private File getPictureFile() {
        Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String pictureFile = "vetting_" + sdf.format(currentTime) + "_" + ruleId;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = new File(storageDir + "/" + pictureFile + ".jpg");
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == CAPTURE_IMAGES_REQUEST) {
                    onCaptureImageResult(data);
                } else if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
                    cameraIntent();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: ", e);
        }
    }

    private void onCaptureImageResult(Intent data) {
        try {
            File imgFileExternalStorage = new File(pictureFilePath);
            if (!imgFileExternalStorage.exists()) {
                return;
            }
            String fileName = imgFileExternalStorage.getName();
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

            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setId(ruleId);
            imageDetails.setFinalFile(finalFilePath);
            imageDetails.setSessionid(sessionID);
            imageDetails.setFileName(fileName);

            imageDetailsList.add(imageDetails);

            //display image in the list
            addImageViewToContainer(imageDetails);

        } catch (Exception e) {
            Log.d("error massage", e.getMessage());
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void addImageViewToContainer(ImageDetails imageDetails) {
        View imageItem = getLayoutInflater().inflate(R.layout.item_image, linearLayoutContainer, false);
        ImageView imageView = imageItem.findViewById(R.id.imageView);
        if (!imageDetails.getFinalFile().isEmpty()) {
            Glide.with(this).load(imageDetails.getFinalFile()).into(imageView);
            imageView.setFocusable(true);
        }

        ImageView imageViewRemove = imageItem.findViewById(R.id.imageViewRemove);
        imageViewRemove.setOnClickListener(view -> {
            linearLayoutContainer.removeView(imageItem);
            imageDetailsList.remove(imageDetails);
            removeFileFromStorage(imageDetails);
        });
        linearLayoutContainer.addView(imageItem);
        //linearLayoutContainer.addView(imageItem, 0);
    }

    private void removeFileFromStorage(ImageDetails imageDetails) {
        try {
            if (imageDetails.getFinalFile() != null) {
                File uploadFile = new File(imageDetails.getFinalFile());
                if (uploadFile.exists()) {
                    uploadFile.delete();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(IMAGE_LIST, imageDetailsList);
        intent.putExtra(PARENT_POSITION, parentPosition);
        intent.putExtra(POSITION, position);
        intent.putExtra(RULE_ID, ruleId);
        intent.putExtra(REMARK, editTextRemark.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnResult();
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

}
