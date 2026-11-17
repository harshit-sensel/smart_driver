
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Rule {

    @Expose
    private String coefficient;
    @Expose
    private List<Option> options;
    @Expose
    private String ruleId;
    @Expose
    private String ruleName;

    String imagePath = "";
    String remark = "";
    String option = "";

    String Score;


    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }


}
