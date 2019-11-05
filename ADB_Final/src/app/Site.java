package app;

import java.util.ArrayList;

public class Site {

    private ArrayList<Integer> variablesForSite = new ArrayList<>(21);
    private boolean isUp;
    private int siteNo;

    public Site(int siteNo){
        this.siteNo = siteNo;
        this.isUp = true;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("siteNo:" + this.siteNo + " isUp:" + this.isUp);
        int size = variablesForSite.size();
        for (int i = 0; i < size; i++) {
            if (variablesForSite.get(i) != null) {
                str.append(" x" + i + ": " + variablesForSite.get(i) +",");
            }
        } 
        //Remove comma at the end
        return str.toString();
    }

    public void addVariableToSite(int index, int value){
        this.variablesForSite.add(index, value);
    }

    public void viewVariablesForSite(){
        if(this.isUp) {
            int size = variablesForSite.size();
            for (int i = 0; i < size; i++) {
                if (variablesForSite.get(i) != null) {
                    System.out.println("variable:" + i + " " + variablesForSite.get(i) + "\n");
                }
            } 
        }
        else {
            throw new IllegalArgumentException("The site is down cannot read");
        }
    }

    public int getVariable(int index){
        return variablesForSite.get(index);
    }

    public boolean isSiteUp(){
        return this.isUp;
    }

    public void siteFail(){
        this.isUp = false;
    }

    public void siteRecover(){
        this.isUp = true;
        //call DM for managing data
    }

    public int getSiteNo() {
        return siteNo;
    }

}
