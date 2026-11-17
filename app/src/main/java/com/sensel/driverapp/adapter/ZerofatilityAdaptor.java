package com.sensel.driverapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sensel.driverapp.R;
import com.sensel.driverapp.Zerofatality;

import java.util.Arrays;
import java.util.List;

public class ZerofatilityAdaptor extends RecyclerView.Adapter<ZerofatilityAdaptor.ViewHolder> {
    private LayoutInflater layoutInflater;
    public List<String> data;
    public List<String> color;
    public  List<String> result;
    Context mcontext;
    public Integer size;
    public  String[] default_values;

    public ZerofatilityAdaptor(Context context, List<String> data, List<String> color){
        this.layoutInflater=LayoutInflater.from(context);
        this.data=data;
        this.color=color;
        this.mcontext=context;
        default_values = new String[data.size()];
        Arrays.fill(default_values, "nill");
        result = Arrays.asList(default_values);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=layoutInflater.inflate(R.layout.zerofatility_card,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ZerofatilityAdaptor.ViewHolder holder, int position) {
        String layoutcolor=color.get(position);
        String rulenametext=data.get(position);

        holder.layout.setBackgroundColor(Color.parseColor(layoutcolor));
        holder.textView.setText(rulenametext);

        holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i){
                    case R.id.radio_yes:
                        result.set(holder.getAdapterPosition(),"Yes");
                        Log.e("result",result.toString()+holder.getAdapterPosition());

                        break;
                    case R.id.radio_no:
                        result.set(holder.getAdapterPosition(),"No");
                        Log.e("result",result.toString()+holder.getAdapterPosition());
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        Log.e("getItemCount()", "called");
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout layout;
        RadioGroup radioGroup;
        RadioButton radioButton;
        public ViewHolder(View itemView) {
            super(itemView);
            layout=itemView.findViewById(R.id.zfclinearlayout);
            textView=itemView.findViewById(R.id.zerofatility_text);
            radioGroup=itemView.findViewById(R.id.radio_group);

        }
    }
}
