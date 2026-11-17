
package com.sensel.driverapp.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;


public class AutocompleteDropdownResponse {

    @Expose
    public String Value;
    @Expose
    public String DisplayValue;

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getDisplayValue() {
        return DisplayValue;
    }

    public void setDisplayValue(String displayValue) {
        DisplayValue = displayValue;
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayValue();
    }
}
