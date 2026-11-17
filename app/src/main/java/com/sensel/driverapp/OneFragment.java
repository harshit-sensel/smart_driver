package com.sensel.driverapp;

/**
 * Created by User on 17-10-2016.
 */
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class OneFragment extends Fragment{
    TextView  driver_name;
    TextView  last_earned;
    TextView  this_month;
    TextView  this_year;
    TextView  negative_points;
    TextView  last_encashed;
    public static String[] points;
    static  boolean executed=false;

    public OneFragment() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
           points=Gridmenu.points;
           driver_name=(TextView) rootView.findViewById(R.id.drivername);
           last_earned=(TextView) rootView.findViewById(R.id.last_earned);
           this_month=(TextView) rootView.findViewById(R.id.this_month);
           this_year=(TextView) rootView.findViewById(R.id.this_year);
           negative_points=(TextView) rootView.findViewById(R.id.negative_points);
           last_encashed=(TextView) rootView.findViewById(R.id.last_encashed);
       TextView last_earned_date=(TextView) rootView.findViewById(R.id.last_earned_date);
        TextView vehicle=(TextView) rootView.findViewById(R.id.vehicle);
        TextView lbl_month=(TextView) rootView.findViewById(R.id.lblmonth);
        TextView lbl_year=(TextView) rootView.findViewById(R.id.lblyear);

        Calendar cal= Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());
        lbl_month.setText(month_name);
        int year = cal.get(Calendar.YEAR);
        lbl_year.setText("Year : "+String.valueOf(year));

        driver_name.setText(points[0]);
        last_earned.setText(points[3]);
        this_month.setText(points[1]);
        this_year.setText(points[2]);
        negative_points.setText(points[4]);
        last_encashed.setText(points[5]);
        last_earned_date.setText(points[6]);
        if(points.length==8)
        vehicle.setText(points[7]);
        else
         vehicle.setText("No vehicle");
        return rootView;//inflater.inflate(R.layout.fragment_one, container, false);
    }


    public  void getdata()
    {
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < 400) {
                        sleep(100);
                        waited += 100;
                        if (waited == 100) {
                            WebserviceCall webserviceCall=new WebserviceCall();
                            String driver_incentives = webserviceCall.GetDriver_Incentives(getIMEI());
                            OneFragment.points=driver_incentives.split(",");
                            waited=600;
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {

                            /*Toast.makeText(getContext(),"OOPS Unable to contact server",Toast.LENGTH_SHORT).show();*/
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                } finally {
                   executed=true;
                }
            }

        };
        splashThread.start();
    }

    /******* For getting IMEI number of device *******/
    public String getIMEI()
    {
        String device_id="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id = android_id;
            }
        }
        return device_id;
    }
}
