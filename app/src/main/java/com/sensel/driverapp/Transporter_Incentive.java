package com.sensel.driverapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 18-10-2016.
 */
public class Transporter_Incentive extends Fragment {
    CustomListViewAdapter adapter;
    public static ListView listView;
    List<RowItem> rowItems;
    TextView textView;
    public static String[] ranks;
    public static String[] names;
    public static String[] points;
    public static String result;
    public Transporter_Incentive() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.transporter_incentive, container, false);
        textView = (TextView) rootView.findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        if(result!=null) {
            rowItems = new ArrayList<RowItem>();
            createarray();
            for (int i = 0; i < names.length; i++) {
                RowItem item = new RowItem(R.drawable.ic_no_user, ranks[i], names[i], points[i]);
                rowItems.add(item);
            }
            listView = (ListView) rootView.findViewById(R.id.list);
            adapter = new CustomListViewAdapter(getContext(),
                    R.layout.transporter_incentive_list, rowItems);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        else
        {
            textView.setVisibility(View.VISIBLE);
        }
        return rootView;//inflater.inflate(R.layout.fragment_one, container, false);
    }

    public  void createarray()
    {
        try {
            final JSONArray jArr = new JSONArray(result);
            names=new String[jArr.length()];
            points=new String[jArr.length()];
            ranks=new String[jArr.length()];
            for (int j = 0; j < jArr.length(); j++) {
                try {
                    JSONObject data = jArr.getJSONObject(j);
                    names[j]=data.getString("name");
                    points[j]=data.getString("point");
                    ranks[j]=String.valueOf(j+1);

                } catch (JSONException js) {

                }
            }

        } catch (JSONException e) {
        }
    }

}
