package app;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
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
            for (Site s : this.sites.values()) {
                if(s.isSiteUp()) {
                    Variable v = s.getVariable(variableNumber);
                    v.setValue(value);
                    v.setCorrupt(false);
                }
            }
        } else {
            Site s =this.sites.get((variableNumber % 10) + 1);
            if(s.isSiteUp()) {
                Variable v = s.getVariable(variableNumber);
                v.setValue(value);
                v.setCorrupt(false);
            }
        }
    }

    public void onFail(int siteNo) {
        Site s = sites.get(siteNo);
        s.siteFail();
        s.makeVariablesCorruptAndDeleteLockTable();
    }

    public Site onRecovery(int siteNo) {
        Site s = sites.get(siteNo);
        s.siteRecover();
        return s;
    }

    public void dump() {
        for (Integer siteNo : sites.keySet()) {
            System.out.println("site " + siteNo + " -" + sites.get(siteNo).toString() + "\n");
        }
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

    public Site getSite(int siteNo){
        return sites.get(siteNo);
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

    public void removeLocks(int variableNumber, Transaction transaction){
        if (variableNumber % 2 == 0) {
            for (Site site : sites.values()) {
                if (site.isSiteUp() ) {
                    site.getVariable(variableNumber).removeLockByTransaction(transaction);
                }
            }
        } else {
            Site s =this.sites.get((variableNumber % 10) + 1);
            if(s.isSiteUp()){
                s.getVariable(variableNumber).removeLockByTransaction(transaction);
            }
        }
    }
}