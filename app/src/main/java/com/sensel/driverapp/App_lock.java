package com.sensel.driverapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by User on 27-10-2016.
 */
public class App_lock extends Activity {
   public static boolean isopened=false;
    public static String applockPassword="Sensel12345";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_lock);

        Button ok_button=(Button) findViewById(R.id.submit);
        final EditText password=(EditText) findViewById(R.id.text);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().equals(applockPassword)) {
                    App_lock.this.moveTaskToBack(true);
                    SaveMyAppsService.app_allow = true;
                    password.setText("");
                    isopened=true;
                    if(android.os.Build.VERSION.SDK_INT >= 21)
                    {
                        finishAndRemoveTask();
                    }
                    else
                    {
                        finish();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed()
    {

    }

   @Override
    public void onDestroy()
    {
        //stopService(new Intent(this, SaveMyAppsService.class));
        super.onDestroy();
       /*  if(android.os.Build.VERSION.SDK_INT >= 21)
        {
            finishAndRemoveTask();
        }
        else
        {
            finish();
        }
       Intent i = new Intent(getApplicationContext(), App_lock.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);*/

    }



}
