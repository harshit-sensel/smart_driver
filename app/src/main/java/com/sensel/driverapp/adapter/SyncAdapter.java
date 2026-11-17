package com.sensel.driverapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensel.driverapp.R;
import com.sensel.driverapp.db.model.VehicleVetting;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sensel.driverapp.utility.ApplicationConstants.CONST_CONFIG_REGULAR;

public class SyncAdapter extends RecyclerView.Adapter<SyncAdapter.ViewHolder> {

    private Context context;
    private List<VehicleVetting> items;
    private LayoutInflater layoutInflater;

    public SyncAdapter(Context context, List<VehicleVetting> items) {
        this.context = context;
        this.items = items;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewHolder viewHolder;
        View view = layoutInflater.inflate(R.layout.item_sync, viewGroup, false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VehicleVetting vehicleVetting = items.get(position);
        holder.textViewApp.setText("App : " + vehicleVetting.appName);
        if (vehicleVetting.appName.equals(CONST_CONFIG_REGULAR)) {
            holder.textViewLocation.setText("Location : " + vehicleVetting.staticDetails.getLocation());
            holder.textViewVehicleId.setVisibility(View.VISIBLE);
            holder.textViewVehicleType.setVisibility(View.VISIBLE);
            holder.textViewVehicleId.setText("Vehicle Id : " + vehicleVetting.staticDetails.getVehicleId());
            holder.textViewVehicleType.setText("Vehicle Type : " + vehicleVetting.staticDetails.getVehicleType());
            holder.textViewVettingDate.setText("Vetting Date : " + vehicleVetting.staticDetails.getVettingDate());
        } else {
            holder.textViewLocation.setText("Location : " + vehicleVetting.staticDetails.getLatitude() + " " + vehicleVetting.staticDetails.getLongitude());
            holder.textViewVehicleId.setVisibility(View.GONE);
            holder.textViewVehicleType.setVisibility(View.GONE);
            holder.textViewVettingDate.setText("Date : " + vehicleVetting.staticDetails.getDateTime());
        }
        holder.textViewFiles.setText(vehicleVetting.imageList != null ? " Files - " + vehicleVetting.imageList.size() : " Files - 0");
        holder.textViewSyncStatus.setText((vehicleVetting.isUploaded != null && vehicleVetting.isUploaded) ? "Synced" : "Not Synced");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewVehicleId)
        TextView textViewVehicleId;

        @BindView(R.id.textViewApp)
        TextView textViewApp;

        @BindView(R.id.textViewVehicleType)
        TextView textViewVehicleType;

        @BindView(R.id.textViewLocation)
        TextView textViewLocation;

        @BindView(R.id.textViewVettingDate)
        TextView textViewVettingDate;

        @BindView(R.id.textViewSyncStatus)
        TextView textViewSyncStatus;
        @BindView(R.id.textViewFiles)
        TextView textViewFiles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
