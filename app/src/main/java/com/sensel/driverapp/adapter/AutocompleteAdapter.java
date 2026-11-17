package com.sensel.driverapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.sensel.driverapp.R;
import com.sensel.driverapp.response.AutocompleteDropdownResponse;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteAdapter extends ArrayAdapter {

    private int itemLayout;
    private ListFilter listFilter = new ListFilter();
    private List<AutocompleteDropdownResponse> dataList;
    private List<AutocompleteDropdownResponse> dataListAllItems;

    public AutocompleteAdapter(Context context, int resource, List<AutocompleteDropdownResponse> storeDataLst) {
        super(context, resource, storeDataLst);
        dataList = storeDataLst;
        itemLayout = resource;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public AutocompleteDropdownResponse getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        }
        CheckedTextView textViewAutoComplete = view.findViewById(R.id.textViewAutoComplete);
        textViewAutoComplete.setText(getItem(position).toString());
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class ListFilter extends Filter {
        private Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (dataListAllItems == null) {
                synchronized (lock) {
                    dataListAllItems = new ArrayList<AutocompleteDropdownResponse>(dataList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = dataListAllItems;
                    results.count = dataListAllItems.size();
                }
            } else {
                final String searchStrLowerCase = prefix.toString().toLowerCase();

                ArrayList<AutocompleteDropdownResponse> matchValues = new ArrayList<AutocompleteDropdownResponse>();

                for (AutocompleteDropdownResponse dataItem : dataListAllItems) {
                    if (dataItem.getDisplayValue().toLowerCase().contains(searchStrLowerCase)) {
                        matchValues.add(dataItem);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                dataList = (ArrayList<AutocompleteDropdownResponse>) results.values;
            } else {
                dataList = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}