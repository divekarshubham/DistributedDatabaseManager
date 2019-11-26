package app;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class DataManager {

    private final static Logger LOGGER = Logger.getLogger(DataManager.class.getName());

    private Map<Integer, Site> sites = new HashMap<Integer, Site>();
    //    private HashMap<Integer, ArrayList<Site>> getUpSites = new HashMap<>();
    private static DataManager instance = null;

    public DataManager() {
        // initialize all the sites
        for (int i = 1; i < 11; i++) {
            this.sites.put(i, new Site(i));
        }

        for (int i = 1; i <= 20; i++) { //for all variables
            if (i % 2 == 0) {
                for (Integer siteNo : this.sites.keySet()) {
                    Variable var = new Variable(i, 10 * i);
                    sites.get(siteNo).addVariableToSite(i, var);
                }
            } else {
                Variable var = new Variable(i, 10 * i);
                int si = (i % 10) + 1;
                LOGGER.info("var is:" + i +" Site is:" + si);
                Site s = this.sites.get(si);
                s.addVariableToSite(i, var);
            }
        }
    }

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    public void updateVariableToSite(int variableNumber, int value) {
        if (variableNumber % 2 == 0) {
            for (Integer siteNo : this.sites.keySet()) {
                sites.get(siteNo).getVariable(variableNumber).setValue(value);
            }
        } else {
            this.sites.get((variableNumber % 10) + 1).getVariable(variableNumber).setValue(value);
        }
    }

    public void onFail(Site s) {

    }

    public void onRecovery(Site s) {

    }

    public void dump() {
        for (Integer siteNo : sites.keySet()) {
            System.out.println("site " + siteNo + " -" + sites.get(siteNo).toString() + "\n");
        }
    }

    public void recover(int siteNumber) {
        LOGGER.info("in recover" +siteNumber);
    }

    public void fail(int siteNumber) {
        LOGGER.info("infail" +siteNumber);
    }

    public ArrayList<Integer> lastCommitedValuesForReadOnly(){
        ArrayList<Integer> lastCommitedValues = new ArrayList<>();
        for(int i=1; i<21; i++){
            lastCommitedValues.add(getVariableValue(i));
        }
        return lastCommitedValues;
    }

    public int getVariableValue(int variableNumber){
        if(variableNumber%2 == 0) {
            for (Site site : sites.values()) {
                if (site.isSiteUp())
                    return site.getVariable(variableNumber).getValue();
            }
        }
        else {
            Site s = sites.get((variableNumber % 10) + 1);
            if(s.isSiteUp())
                return s.getVariable(variableNumber).getValue();
        }
        return Integer.MIN_VALUE;
    }

    public ArrayList<Site> getUpSites(int variableNumber) {
        ArrayList<Site> availSite = new ArrayList();
        if (variableNumber % 2 == 0) {
            for (Site site : sites.values()) {
                if (site.isSiteUp() ) {
                    availSite.add(site);
                }
            }
        } else {
            Site s =this.sites.get((variableNumber % 10) + 1);
            if(s.isSiteUp()){
                availSite.add(s);
            }
        }
        return availSite;
    }
}