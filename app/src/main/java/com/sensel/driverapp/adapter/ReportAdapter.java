package com.sensel.driverapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensel.driverapp.R;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.response.report.ChecklistValue;
import com.sensel.driverapp.response.report.ReportResponse;
import com.sensel.driverapp.response.report.VettingReport;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReportAdapter extends RecyclerView.Adapter {

    private Context context;
    //private List<VettingReport> items;
    private List<Object> items;
    private LayoutInflater layoutInflater;
    private List<ChecklistValue> item;
    private CheckListReportAdapter checkListReportAdapter;
    private SharedPreferencesUtility sharedPreferencesUtility;
    private Gson gson = new Gson();
    private LayoutInflater inflater;

    public ReportAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
        layoutInflater = LayoutInflater.from(context);
        sharedPreferencesUtility = new SharedPreferencesUtility(context);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = layoutInflater.inflate(R.layout.report_item, viewGroup, false);
                return new VettingViewHolder(view);
            case 2:
                view = layoutInflater.inflate(R.layout.report_item_config, viewGroup, false);
                return new ConfigReportViewHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Object data = items.get(position);
        if (data instanceof VettingReport) {
            VettingViewHolder holder = (VettingViewHolder) viewHolder;
            holder.rvCheckList.setVisibility(View.VISIBLE);
            holder.titleVehicleId.setText("Vehicle ID :");
            holder.titleVehicleType.setText("Vehicle Type :");
            holder.titleDateTime.setText("Date and Time :");

            VettingReport vettingReport = (VettingReport) data;
            holder.tvVehicleid.setText(vettingReport.getVehicleId());
            holder.tvVehicleType.setText(vettingReport.getVehicleType());
            holder.tvDateTime.setText(vettingReport.getDateTime());
            holder.tvHeading.setText("Average Score  : " + vettingReport.getAvgScore() + "");
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            holder.rvCheckList.setLayoutManager(linearLayoutManager);
            item = vettingReport.getChecklistValues();
            checkListReportAdapter = new CheckListReportAdapter(context, item);
            holder.rvCheckList.setAdapter(checkListReportAdapter);
            if (vettingReport.getFilePath().equals("")) {
                holder.tvPdfLink.setVisibility(View.GONE);
            } else {
                holder.tvPdfLink.setVisibility(View.VISIBLE);
            }

            holder.tvPdfLink.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL) + "/Uploads/" + vettingReport.getFilePath()));
                context.startActivity(browserIntent);
            });

        } else if (data instanceof ReportResponse) {
            ConfigReportViewHolder holder = (ConfigReportViewHolder) viewHolder;
            ReportResponse reportResponse = (ReportResponse) data;

            Integer score = Integer.parseInt(reportResponse.getScore());
            holder.textViewDateTime.setText(reportResponse.getDateTime());
            holder.textViewScore.setText(score + "%");

            if (score >= 95) {
                holder.textViewScore.setTextColor(context.getResources().getColor(R.color.done_ckeck));
            } else if (score >= 90 && score <= 94) {
                holder.textViewScore.setTextColor(context.getResources().getColor(R.color.yellow));
            } else if (score >= 80 && score <= 89) {
                holder.textViewScore.setTextColor(context.getResources().getColor(R.color.colorAccent));
            } else if (score < 80) {
                holder.textViewScore.setTextColor(context.getResources().getColor(R.color.pending_ckeck));
            }

            if (reportResponse.getData() != null && !reportResponse.getData().isEmpty()) {
                Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> hashMap = gson.fromJson(reportResponse.getData(), type);
                if (hashMap.size() > 0) {
                    holder.linearLayoutDynamicData.removeAllViews();
                    for (Map.Entry entry : hashMap.entrySet()) {
                        //create the custom view
                        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_report_dynamic_view, holder.linearLayoutDynamicData, false);
                        TextView textView = layout.findViewById(R.id.textViewTitle);
                        textView.setText(entry.getKey().toString());
                        TextView textViewData = layout.findViewById(R.id.textViewData);
                        textViewData.setText(entry.getValue().toString());
                        holder.linearLayoutDynamicData.addView(layout);
                    }
                }
            }

            holder.imageViewDownload.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL) + "/Uploads/" + reportResponse.getLink()));
                context.startActivity(browserIntent);
            });

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object data = items.get(position);
        if (data instanceof VettingReport)
            return 1;
        else if (data instanceof ReportResponse)
            return 2;
        else
            return -1;
    }

    public class VettingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvVehicleid)
        TextView tvVehicleid;
        @BindView(R.id.tvVehicleType)
        TextView tvVehicleType;
        @BindView(R.id.tvDateTime)
        TextView tvDateTime;
        @BindView(R.id.rvCheckList)
        RecyclerView rvCheckList;
        @BindView(R.id.tvHeading)
        TextView tvHeading;
        @BindView(R.id.tvPdfLink)
        TextView tvPdfLink;
        @BindView(R.id.titleVehicleType)
        TextView titleVehicleType;
        @BindView(R.id.titleVehicleId)
        TextView titleVehicleId;
        @BindView(R.id.titleDateTime)
        TextView titleDateTime;

        public VettingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class ConfigReportViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textViewScore)
        TextView textViewScore;
        @BindView(R.id.textViewDateTime)
        TextView textViewDateTime;
        @BindView(R.id.imageViewDownload)
        ImageView imageViewDownload;
        @BindView(R.id.linearLayoutDynamicData)
        LinearLayout linearLayoutDynamicData;

        public ConfigReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
