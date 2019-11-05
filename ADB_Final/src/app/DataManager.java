package app;

import java.util.List;
import java.util.ArrayList;

public class DataManager {

    private List<Site> sites = new ArrayList<Site>();
    private static DataManager instance = null;

    public DataManager() {
        // initialize all the sites
        for (int i = 1; i < 11; i++) {
            this.sites.add(new Site(i));
        }
        // initialize all the variables
        for (int i = 1; i <= 20; i++) {
            if (i % 2 == 0) {
                for (Site site : this.sites) {
                    site.addVariableToSite(i, 10 * i);
                }
            } else {
                this.sites.get((i % 10) + 1).addVariableToSite(i, 10 * i);
            }
        }
    }

    public static DataManager getInstance(){
        if(instance == null)
            instance = new DataManager();
        return instance;
    }

    public void updateVariableToSite(int variableNumber, int value) {
        if (variableNumber % 2 == 0) {
            for (Site site : this.sites) {
                site.addVariableToSite(variableNumber, value);
            }
        } else {
            this.sites.get((variableNumber % 10) + 1).addVariableToSite(variableNumber, value);
        }
    }

    public void onFail(Site s) {

    }

    public void onRecovery(Site s) {

    }

    public void dump() {
        for (Site site : sites) {
            System.out.println("site " + site.getSiteNo() + " -" + site.toString() + "\n");
        }
    }

	public void recover(int siteNumber) {
	}

	public void fail(int siteNumber) {
	}

    public int getValueOfVariable(int variableNumber){
        if (variableNumber % 2 == 0) {
            for (Site site : this.sites) {
                if(site.isSiteUp())
                    return site.getVariable(variableNumber);
            }
        } else {
            return this.sites.get((variableNumber % 10) + 1).getVariable(variableNumber);
        }
        return -1;
    }
}