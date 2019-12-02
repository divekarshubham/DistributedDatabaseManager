/**
 * @file Transaction.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 * @brief Represents and stores the information related to a Transaction
 * @version 0.1
 * @date 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */
package app;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private int transactionNumber;
    private int timestamp;
    private boolean isReadOnly;
    private Map<Operation, ArrayList<Site> > variablesLocked = new HashMap<>(); /*map<variable, site> */

    public Transaction( int transactionNumber,
                        boolean isReadOnly,
                        int arrivalTime )
    {
        this.transactionNumber = transactionNumber;
        this.timestamp = arrivalTime;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String toString()
    {
        return "Transaction: " + this.transactionNumber + " began at: " + this.timestamp;
    }

    public int getTransactionNumber()
    {
        return this.transactionNumber;
    }

    public int getTimeStamp()
    {
        return this.timestamp;
    }

    public boolean isReadOnly()
    {
        return this.isReadOnly;
    }

    public Map<Operation, ArrayList<Site> > getVariablesLocked()
    {
        return variablesLocked;
    }

    public void addVariableLocked( Operation op,
                                   ArrayList<Site> s )
    {
        this.variablesLocked.put( op, s );
    }
}
