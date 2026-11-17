
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.util.List;


public class GetCheckList {

    @Expose
    private List<Checklist> checklists;
    @Expose
    private int checklistcount;

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
}
