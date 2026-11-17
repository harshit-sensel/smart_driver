
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.util.List;


public class Checklist {

    @Expose
    private String checklistName;
    @Expose
    private List<Rule> rules;

    //for internal use
    private Boolean listStatus = true;

    public String getChecklistName() {
        return checklistName;
    }

    public void setChecklistName(String checklistName) {
        this.checklistName = checklistName;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public Boolean getListStatus() {
        return listStatus;
    }

    public void setListStatus(Boolean listStatus) {
        this.listStatus = listStatus;
    }
}
