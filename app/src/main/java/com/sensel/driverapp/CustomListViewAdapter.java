package com.sensel.driverapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

    Context context;

    public CustomListViewAdapter(Context context, int resourceId,
                                 List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {

        //ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
        TextView txtaddress;
        ImageView award;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.transporter_incentive_list, null);
            holder = new ViewHolder();
            holder.txtaddress = (TextView) convertView.findViewById(R.id.desc);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
           // holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.txtDesc=(TextView) convertView.findViewById(R.id.status);
            holder.award=(ImageView) convertView.findViewById(R.id.award);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
            String status = rowItem.getDesc();
            String rank = rowItem.getTitle();
            if (rank.equals("1")) {
                holder.award.setImageResource(R.drawable.firstrank);
            } else if (rank.equals("2")) {
                holder.award.setImageResource(R.drawable.secondrank);
            } else if (rank.equals("3")) {
                holder.award.setImageResource(R.drawable.thirdrank);
            }
            else {
                holder.award.setImageResource(R.drawable.no_award);
            }


       /*  drawable = TextDrawable.builder()
                .buildRound("KA",Color.parseColor("#36A33D"));*/
            ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
           // holder.imageView = imageView;
            holder.txtTitle.setText(rank);
            holder.txtDesc.setText(status);
            holder.txtaddress.setText(rowItem.getAddress());
            // holder.imageView.setImageResource(rowItem.getImageId());
       /* if (position % 2 == 1) {
            convertView.setBackgroundColor(Color.parseColor("#EAFAF1"));
        }*/
        }
        return convertView;
    }
    @Override
    public int getViewTypeCount() {

        return 1;
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }
}