package com.sensel.driverapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensel.driverapp.R;
import com.sensel.driverapp.response.notification.Notification;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {


    private Context context;
    private List<Notification> items;
    private LayoutInflater layoutInflater;


    public NotificationAdapter(Context context, List<Notification> items) {
        this.context = context;
        this.items = items;
        layoutInflater = LayoutInflater.from(context);


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ViewHolder viewHolder;
        View view = layoutInflater.inflate(R.layout.notification_item, viewGroup, false);

        viewHolder = new ViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification notification = items.get(position);

        holder.tvNotificationMsg.setText(notification.getInfo());
        holder.tvNotificationTitle.setText(notification.getSubject());
        holder.tvNotificationDate.setText(notification.getDatetime());


    }


    @Override
    public int getItemCount() {
        return items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvNotificationTitle)
        TextView tvNotificationTitle;
        @BindView(R.id.tvNotificationMsg)
        TextView tvNotificationMsg;
        @BindView(R.id.tvNotificationDate)
        TextView tvNotificationDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


//            rbLike.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    makeCallToLikeAPI(getAdapterPosition());
//
//
//                }
//            });


        }

    }


}
