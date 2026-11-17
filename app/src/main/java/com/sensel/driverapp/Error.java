package com.sensel.driverapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;

/**
 * Created by User on 31-12-2016.
 */

public class Error extends AppCompatActivity {
 public static  String error_text="Something went wrong";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }



        if(BuildConfig.FLAVOR.contains("adani")){

            AlertDialog.Builder builder1 = new AlertDialog.Builder(Error.this);
            builder1.setTitle("Un authorised user");
            builder1.setMessage("Your IMEI "+Gridmenu.device_id+" not registered \nPlease contact admin");
            builder1.setIcon(R.drawable.ic_round_warning);

            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finish();
                        }
                    });

            builder1.show();


        }
        else {
            TextView textView=(TextView) findViewById(R.id.text);
            textView.setText(error_text);
        }


        //Get IMEI
    }
}
