
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.util.List;


public class DynamicCheckListResponse {

    @Expose
    private List<Checklist> checklists;

    @Expose
    private int checklistcount;

    @Expose
    private List<Field> fields;

    public List<Checklist> getChecklists() {
        return checklists;
    }

    public void setChecklists(List<Checklist> checklists) {
        this.checklists = checklists;
    }

    public int getChecklistcount() {
        return checklistcount;
    }

    public void setChecklistcount(int checklistcount) {
        this.checklistcount = checklistcount;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
