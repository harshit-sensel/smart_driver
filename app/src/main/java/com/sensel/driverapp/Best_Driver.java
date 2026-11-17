package com.sensel.driverapp;

/**
 * Created by User on 20-10-2016.
 */
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class Best_Driver extends Fragment {
    TextView driver_name;
    TextView points;
    TextView transporter;
    public static String result;

    public Best_Driver() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.best_driver, container, false);

        try {
            driver_name = (TextView) rootView.findViewById(R.id.drivername);
            points = (TextView) rootView.findViewById(R.id.points);
            transporter = (TextView) rootView.findViewById(R.id.transporter);
            JSONObject data =new JSONObject(result);
            driver_name.setText(data.getString("name"));
            points.setText("This month Points : "+data.getString("point"));
            transporter.setText(data.getString("t_name"));
          //  names[j]=data.getString("name");
          }
        catch (JSONException e) {
            String error=e.toString();
        }


        return rootView;//inflater.inflate(R.layout.fragment_one, container, false);
    }

}