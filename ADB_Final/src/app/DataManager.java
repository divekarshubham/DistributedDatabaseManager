/**
 * @file DataManager.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 * @brief Handles all operations on data, by directing each site
 * @version 0.1
 * @date 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */
package app;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {
    private final static Logger LOGGER = Logger.getLogger( DataManager.class.getName() );

    /**
     * Data-Structure to keep track of all the sites
     */
    private Map<Integer, Site> sites = new HashMap<Integer, Site>();

    private static DataManager instance = null;

    /**
     * @brief Initializes all the sites and assigns variables to them
     */
    public DataManager()
    {
        /** Initialize all the sites */
        for( int i = 1; i < 11; i++ )
        {
            this.sites.put( i, new Site( i ) );
        }

        /** Initialize the Variables */
        for( int i = 1; i <= 20; i++ )
        {
            if( i % 2 == 0 )
            {
                for( Integer siteNo : this.sites.keySet() )
                {
                    Variable var = new Variable( i, 10 * i );
                    sites.get( siteNo ).addVariableToSite( i, var );
                }
            }
            else
            {
                Variable var = new Variable( i, 10 * i );
                int si = ( i % 10 ) + 1;
                LOGGER.fine( "var is:" + i + " Site is:" + si );
                Site s = this.sites.get( si );
                s.addVariableToSite( i, var );
            }
        }
    }

    /**
     * @brief Singleton to create only one instace of the Data-Manager
     * @return instace of the DM
     */
    public static DataManager getInstance()
    {
        if( instance == null )
        {
            instance = new DataManager();
        }

        return instance;
    }

    /**
     *
     * @brief To change the value of the variable
     * @param variableNumber variable to update
     * @param value          updated value
     */
    public void updateVariableToSite( int variableNumber,
                                      int value )
    {
        if( variableNumber % 2 == 0 )
        {
            for( Site s : this.sites.values() )
            {
                if( s.isSiteUp() )
                {
                    Variable v = s.getVariable( variableNumber );
                    v.setValue( value );
                    v.setCorrupt( false );
                }
            }
        }
        else
        {
            Site s = this.sites.get( ( variableNumber % 10 ) + 1 );

            if( s.isSiteUp() )
            {
                Variable v = s.getVariable( variableNumber );
                v.setValue( value );
                v.setCorrupt( false );
            }
        }
    }

    /**
     * @brief Take down a particular site and delete its lock table
     * @param siteNo site to fail
     */
    public void onFail( int siteNo )
    {
        Site s = sites.get( siteNo );

        s.siteFail();
        s.makeVariablesCorruptAndDeleteLockTable();
    }

    /**
     * @brief Recover a particular site
     * @param siteNo site to recover
     */
    public Site onRecovery( int siteNo )
    {
        Site s = sites.get( siteNo );

        s.siteRecover();
        return s;
    }

    /**
     * @brief Displays all variables at all sites.
     */
    public void dump()
    {
        for( Integer siteNo : sites.keySet() )
        {
            System.out.println( "site " + siteNo + " -" + sites.get( siteNo ).toString() + "\n" );
        }
    }

    /**
     * @brief Makes a temporary storage for readonly variables that stores the
     *        values before the transaction began
     * @return List of values of all variables
     */
    public ArrayList<Integer> lastCommitedValuesForReadOnly()
    {
        ArrayList<Integer> lastCommitedValues = new ArrayList<>();

        for( int i = 1; i < 21; i++ )
        {
            lastCommitedValues.add( getVariableValue( i ) );
        }

        return lastCommitedValues;
    }

    /**
     * @brief check if variable is even/odd and fetch from corresponding site
     * @param variableNumber Variable to fetch
     * @return value fo the variable
     */
    public int getVariableValue( int variableNumber )
    {
        if( variableNumber % 2 == 0 )
        {
            for( Site site : sites.values() )
            {
                if( site.isSiteUp() )
                {
                    return site.getVariable( variableNumber ).getValue();
                }
            }
        }
        else
        {
            Site s = sites.get( ( variableNumber % 10 ) + 1 );

            if( s.isSiteUp() )
            {
                return s.getVariable( variableNumber ).getValue();
            }
        }

        return Integer.MIN_VALUE;
    }

    /**
     * @brief Fetch the object of a site
     * @param siteNo
     * @return reference to the site
     */
    public Site getSite( int siteNo )
    {
        return sites.get( siteNo );
    }

    /**
     * @brief to check if the variables are available we construct a list of all
     *        sites (corresponding to it) that are up
     * @param variableNumber
     * @return A list of available sites
     */
    public ArrayList<Site> getUpSites( int variableNumber )
    {
        ArrayList<Site> availSite = new ArrayList();

        if( variableNumber % 2 == 0 )
        {
            for( Site site : sites.values() )
            {
                if( site.isSiteUp() )
                {
                    availSite.add( site );
                }
            }
        }
        else
        {
            Site s = this.sites.get( ( variableNumber % 10 ) + 1 );

            if( s.isSiteUp() )
            {
                availSite.add( s );
            }
        }

        return availSite;
    }

    /**
     * @brief When a transaction ends we have to remove all its aquired locks
     * @param variableNumber Variable for which lock was acquired
     * @param transaction    Transaction that acquired the lock
     */
    public void removeLocks( int variableNumber,
                             Transaction transaction )
    {
        if( variableNumber % 2 == 0 )
        {
            for( Site site : sites.values() )
            {
                if( site.isSiteUp() )
                {
                    site.getVariable( variableNumber ).removeLockByTransaction( transaction );
                }
            }
        }
        else
        {
            Site s = this.sites.get( ( variableNumber % 10 ) + 1 );

            if( s.isSiteUp() )
            {
                s.getVariable( variableNumber ).removeLockByTransaction( transaction );
            }
        }
    }

    public void updateVariableToParticularSite( int variableNumber,
                                                int value,
                                                ArrayList<Site> upsites )
    {
        for( Site s : upsites )
        {
            if( s.isSiteUp() )
            {
                Variable v = s.getVariable( variableNumber );
                v.setValue( value );
                v.setCorrupt( false );
            }
        }
    }
}
