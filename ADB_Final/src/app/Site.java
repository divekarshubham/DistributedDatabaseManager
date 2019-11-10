package app;

import java.util.ArrayList;

public class Site {

    private ArrayList<Variable> variablesForSite = new ArrayList<>(21);
    private boolean isUp;
    private int siteNo;

    public Site(int siteNo) {
        this.siteNo = siteNo;
        this.isUp = true;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("siteNo:" + this.siteNo + " isUp:" + this.isUp);
        for (Variable var : variablesForSite) {
            if (var != null) {
                str.append(" x" + var.getIndex() + ": " + var.getValue() + ",");
            }
        }
        // Remove comma at the end
        return str.toString();
    }

    public void addVariableToSite(int index, Variable var) {
        this.variablesForSite.add(index, var);
    }

    public void viewVariablesForSite() {
        if (this.isUp) {
            for (Variable var : variablesForSite) {
                if (var != null) {
                    str.append(" x" + var.getIndex() + ": " + var.getValue() + ",");
                }
            }
        } else {
            throw new IllegalArgumentException("The site is down cannot read");
        }
    }

    public Variable getVariable(int index) {
        return variablesForSite.get(index);
    }

    public boolean isSiteUp() {
        return this.isUp;
    }

    public void siteFail() {
        this.isUp = false;
    }

    public void siteRecover() {
        this.isUp = true;
        // call DM for managing data
    }

    public int getSiteNo() {
        return siteNo;
    }

}
