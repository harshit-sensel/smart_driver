
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;


public class Option {

    @Expose
    private String optionName;
    @Expose
    private String score;

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

}
