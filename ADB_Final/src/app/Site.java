package app;

import java.util.ArrayList;
import java.util.HashMap;

public class Site {

    private HashMap<Integer, Variable> variablesForSite = new HashMap<>(21);
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
        for (Integer i : variablesForSite.keySet()) {
            str.append(" x" + i + ": " + variablesForSite.get(i) + ",");
        }
        // Remove comma at the end
        return str.toString();
    }

    public void addVariableToSite(int index, Variable var) {
        this.variablesForSite.put(index, var);
    }

    public void viewVariablesForSite() {
        StringBuffer str = new StringBuffer();
        if (this.isUp) {
            for (Integer i : variablesForSite.keySet()) {
                str.append(" x" + i + ": " + variablesForSite.get(i) + ",");
            }
            System.out.println(str);
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

    public void makeVariablesCorruptAndDeleteLockTable(){
        for(Variable var: variablesForSite.values()){
            var.setCorrupt(true);
            var.removeAllLocks();
        }
    }

    public void makeVariableNonCorrupt(Variable var){
        var.setCorrupt(false);
    }

    public int getSiteNo() {
        return siteNo;
    }

    public boolean checkVariableIsCorrupt(int variableNo) {
        return variablesForSite.get(variableNo).isCorrupt();
    }

}
