/**
 * @file main.c
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 * @brief Data-Structure to store all the information of a variable.
 * @version 0.1
 * @date 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */
package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Variables for the site is created using this class
 */
public class Variable {
    private int value;
    /* A variable can have multiple transactions holding read-locks on it */
    private List<Transaction> lockedByTransactions;
    private LockType lockType;
    private boolean isLock;
    private int index;
    /* When the site fails all the variables are marked corrupt to reflect they are unreadable */
    private boolean isCorrupt;

    /**
     * Creates variable object
     * @param index
     * @param value
     */
    public Variable( int index,
                     int value )
    {
        this.index = index;
        this.value = value;
        this.isLock = false;
        this.isCorrupt = false;
        lockedByTransactions = new ArrayList<>();
        lockType = null;
    }

    @Override
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append( "index:" + this.index + " value:" + this.value + " isLock:" + this.isLock + " lockedByTrans: " );

        if( !lockedByTransactions.isEmpty() )
        {
            for( Transaction trans : lockedByTransactions )
            {
                str.append( " " + trans.getTransactionNumber() );
            }

            str.append( " locktype:" + lockType );
        }

        return str.toString();
    }

    public int getIndex()
    {
        return index;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue( int value )
    {
        this.value = value;
    }

    public List<Transaction> getLockedByTransaction()
    {
        return lockedByTransactions;
    }

    public LockType getLockType()
    {
        return lockType;
    }

    public boolean isLock()
    {
        return isLock;
    }

    public boolean isCorrupt()
    {
        return isCorrupt;
    }

    public void setCorrupt( boolean corrupt )
    {
        this.isCorrupt = corrupt;
    }

    public void setLock( Transaction lockedByTransaction,
                         LockType lockType )
    {
        this.lockedByTransactions.add( lockedByTransaction );
        this.lockType = lockType;
        this.isLock = true;
    }

    /**
     * @brief used when a transaction wants to convert a readlock to writelock
     */
    public void promoteLock()
    {
        this.lockType = LockType.WRITELOCK;
    }

    public void removeLockByTransaction( Transaction transaction )
    {
        lockedByTransactions.remove( transaction );

        if( lockedByTransactions.size() == 0 )
        {
            this.isLock = false;
            this.lockType = null;
        }
    }

    /**
     * @brief used a site fails and all the locks need to be cleared
     */
    public void removeAllLocks()
    {
        lockedByTransactions.clear();
        isLock = false;
        lockType = null;
    }
}
