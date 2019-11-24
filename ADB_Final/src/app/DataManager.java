package app;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DataManager {

    private final static Logger LOGGER = Logger.getLogger(DataManager.class.getName());

    private List<Site> sites = new ArrayList<Site>();
    //    private HashMap<Integer, ArrayList<Site>> getUpSites = new HashMap<>();
    private static DataManager instance = null;

    public DataManager() {
        // initialize all the sites
        this.sites.add(null);
        for (int i = 1; i < 11; i++) {
            this.sites.add(new Site(i));
        }
        // initialize all the variables
        for(Site s : sites)
            for(int i = 1; i <= 20; i++)
                if(s != null)
                s.addVariableToSite(i,null);

        for (int i = 1; i <= 20; i++) { //for all variables
            if (i % 2 == 0) {
                for (Site site : this.sites) {
                    Variable var = new Variable(i, 10 * i);
                    if(site != null)
                        site.addVariableToSite(i, var);
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
            for (Site site : this.sites) {
                site.getVariable(variableNumber).setValue(value);
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
        for (Site site : sites) {
            if(site != null)
            System.out.println("site " + site.getSiteNo() + " -" + site.toString() + "\n");
        }
    }

    public void recover(int siteNumber) {
        LOGGER.info("in recover" +siteNumber);
    }

    public void fail(int siteNumber) {
        LOGGER.info("infail" +siteNumber);
    }

    public ArrayList<Site> getUpSites(int variableNumber) {
        ArrayList<Site> availSite = new ArrayList();
        if (variableNumber % 2 == 0) {
            for (Site site : this.sites) {
                if (site.isSiteUp()) {
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