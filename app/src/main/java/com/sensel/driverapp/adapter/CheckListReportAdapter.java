package com.sensel.driverapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensel.driverapp.R;
import com.sensel.driverapp.response.report.ChecklistValue;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CheckListReportAdapter extends RecyclerView.Adapter<CheckListReportAdapter.ViewHolder> {


    private Context context;
    private LayoutInflater layoutInflater;
    private List<ChecklistValue> item;


    public CheckListReportAdapter(Context context, List<ChecklistValue> items) {
        this.context = context;
        this.item = items;
        layoutInflater = LayoutInflater.from(context);


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ViewHolder viewHolder;
        View view = layoutInflater.inflate(R.layout.item_report_checklist, viewGroup, false);

        viewHolder = new ViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChecklistValue checklistValue = item.get(position);

        holder.tvHeading.setText(checklistValue.getChecklistHeader());


        if (checklistValue.getPercentage() >= 95) {
            holder.llGreenProgress.setVisibility(View.VISIBLE);
            holder.llYellowProgress.setVisibility(View.GONE);
            holder.llOrangeProgress.setVisibility(View.GONE);
            holder.llRedProgress.setVisibility(View.GONE);
            holder.progressBarGreen.setProgress(checklistValue.getPercentage());
            holder.tvGreenVotePrercentage.setText(checklistValue.getPercentage() + "%");

        } else if (checklistValue.getPercentage() >= 90 && checklistValue.getPercentage() <= 94) {
            holder.llGreenProgress.setVisibility(View.GONE);
            holder.llYellowProgress.setVisibility(View.VISIBLE);
            holder.llOrangeProgress.setVisibility(View.GONE);
            holder.llRedProgress.setVisibility(View.GONE);

            holder.progressBarYellow.setProgress(checklistValue.getPercentage());
            holder.tvYellowVotePrercentage.setText(checklistValue.getPercentage() + "%");

        } else if (checklistValue.getPercentage() >= 80 && checklistValue.getPercentage() <= 89) {
            holder.llGreenProgress.setVisibility(View.GONE);
            holder.llYellowProgress.setVisibility(View.GONE);
            holder.llOrangeProgress.setVisibility(View.VISIBLE);
            holder.llRedProgress.setVisibility(View.GONE);

            holder.progressBarOrange.setProgress(checklistValue.getPercentage());
            holder.tvOrangeVotePrercentage.setText(checklistValue.getPercentage() + "%");

        } else if (checklistValue.getPercentage() < 80) {
            holder.llGreenProgress.setVisibility(View.GONE);
            holder.llYellowProgress.setVisibility(View.GONE);
            holder.llOrangeProgress.setVisibility(View.GONE);
            holder.llRedProgress.setVisibility(View.VISIBLE);

            holder.progressBarRed.setProgress(checklistValue.getPercentage());
            holder.tvRedVotePrercentage.setText(checklistValue.getPercentage() + "%");

        }


    }


    @Override
    public int getItemCount() {
        return item.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvHeading)
        TextView tvHeading;
        @BindView(R.id.progressBarGreen)
        ProgressBar progressBarGreen;
        @BindView(R.id.tvGreenVotePrercentage)
        TextView tvGreenVotePrercentage;
        @BindView(R.id.llGreenProgress)
        LinearLayout llGreenProgress;
        @BindView(R.id.carview_container)
        LinearLayout carviewContainer;

        @BindView(R.id.viewLine)
        View viewLine;
        @BindView(R.id.progressBarYellow)
        ProgressBar progressBarYellow;
        @BindView(R.id.viewLineYellow)
        View viewLineYellow;
        @BindView(R.id.tvYellowVotePrercentage)
        TextView tvYellowVotePrercentage;
        @BindView(R.id.llYellowProgress)
        LinearLayout llYellowProgress;
        @BindView(R.id.progressBarOrange)
        ProgressBar progressBarOrange;
        @BindView(R.id.viewLineOrange)
        View viewLineOrange;
        @BindView(R.id.tvOrangeVotePrercentage)
        TextView tvOrangeVotePrercentage;
        @BindView(R.id.llOrangeProgress)
        LinearLayout llOrangeProgress;
        @BindView(R.id.progressBarRed)
        ProgressBar progressBarRed;
        @BindView(R.id.viewLineRed)
        View viewLineRed;
        @BindView(R.id.tvRedVotePrercentage)
        TextView tvRedVotePrercentage;
        @BindView(R.id.llRedProgress)
        LinearLayout llRedProgress;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


        }

    }


}
