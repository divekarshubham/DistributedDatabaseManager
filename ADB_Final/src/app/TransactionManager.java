package app;

import java.util.*;
import java.util.logging.Logger;

/**
 * @file TransactionManager.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 *  Main driver of the program which is responsible for executing the operations and queing them if needed. It also manages the life cycle of each transaction.
 * It can perform transactions like read, write, end, commit and abort.
 * It also has an instance of data manager to access data form the sites
 * @version 0.1
 * Date: 2019-12-02
 *
 * @copyright Copyright (c) 2019
 *
 */

/**
 *
 */
public class TransactionManager {
    private final static Logger LOGGER = Logger.getLogger( TransactionManager.class.getName() );
    private Map<Integer, Transaction> activeTransactions;
    private Map<Integer, ArrayList<Integer> > readOnlyLastCommitedValue = new HashMap<>();
    private Map<Integer, Variable> tempVariables;
    private DataManager dm;
    private ArrayList<Operation> waitQueue;
    private HashSet<Operation> waitSiteDownQueue;
    private Graph waitForGraph;
    private int timer = 0;
    private boolean parsing = false;
    ArrayList<Transaction> abortTransaction = new ArrayList<>();

    /**
     * This constructor instializes all the waitqueue, sitedownqueue, waitfor graph and active transactions.
     * It also creates an instance of datamanager
     * It creates a tempVariable cache to cache variables before they are written to site
     */
    public TransactionManager()
    {
        dm = DataManager.getInstance();
        activeTransactions = new HashMap<>();
        tempVariables = new HashMap<>();
        waitQueue = new ArrayList<>();
        waitSiteDownQueue = new HashSet<>();
        waitForGraph = new Graph();

        for( int i = 1; i < 21; i++ )
        {
            tempVariables.put( i, new Variable( i, 10 * i ) );
        }
    }

    /**
     * This function is called during recover operation.
     * It executes on recovery function and tries to execute the operations that were in queue due to site down
     * @param siteNumber site to recover
     */
    public void recover( int siteNumber )
    {
        LOGGER.info( "Site " + siteNumber + " recovered\n" );
        Site s = dm.onRecovery( siteNumber );
        executeSiteDownQueueOperations( s );
    }

    /**
     * This function is called during fail operation.
     * It aborts the transactions that used this site.
     * It also performs on fail operations(mentioned in onFail() function)
     * @param siteNumber site to fail
     */
    public void fail( int siteNumber )
    {
        LOGGER.info( "Site " + siteNumber + " failed\n" );

        for( Transaction t: activeTransactions.values() )
        {
            Map<Operation, ArrayList<Site> > lockedVariables = t.getVariablesLocked();

            for( Operation op : lockedVariables.keySet() )
            {
                if( dm.getUpSites( op.getVariableNumber() ).contains( dm.getSite( siteNumber ) ) )
                {
                    abortTransaction.add( t );
                    break;
                }
            }
        }

        dm.onFail( siteNumber );
    }

    /**
     * Exceutes the transactions that were waiting for a site to recover
     * @param s recovered site
     */
    public void executeSiteDownQueueOperations( Site s )
    {
        HashSet<Operation> waitRecovTemp = new HashSet<>();

        waitRecovTemp = ( HashSet ) waitSiteDownQueue.clone();

        for( Operation op: waitRecovTemp )
        {
            if( dm.getUpSites( op.getVariableNumber() ).contains( s ) )
            {
                addAndExecuteOperation( op );
                waitSiteDownQueue.remove( op );
            }
        }
    }

    /**
     * This function is called when transaction begins
     * It creates the transaction and adds the transaction in the active transaction queue
     * @param transactionNumber new transaction number
     */
    public void begin( int transactionNumber )
    {
        activeTransactions.put( transactionNumber, new Transaction( transactionNumber, false, timer++ ) );
        LOGGER.fine( "Begin transaction " + transactionNumber );
    }

    /**
     * This function is called when read only transaction begins
     * It creates the transaction and adds the transaction in the active transaction queue
     * It also saves the last commited values for this operation
     * @param transactionNumber new transaction number
     */
    public void beginRO( int transactionNumber )
    {
        activeTransactions.put( transactionNumber, new Transaction( transactionNumber, true, timer++ ) );
        readOnlyLastCommitedValue.put( transactionNumber, dm.lastCommitedValuesForReadOnly() );
        LOGGER.fine( "Begin readonly transaction " + transactionNumber );
    }

    /**
     * This function is called when transaction ends
     *
     * If site was previously failed, it aborts that transaction
     * It also aborts the transaction if no sites that were used to perform operation are available for that transaction
     * Else it commits the transaction
     * @param transactionNumber
     */
    public void end( int transactionNumber )
    {
        LOGGER.fine( "waitqueue:" + waitQueue );
        LOGGER.fine( "waitsite downqueue:" + waitSiteDownQueue );
        LOGGER.fine( "in end" );
        Transaction t = activeTransactions.get( transactionNumber );

        /** Performing validations */
        if( abortTransaction.contains( t ) )
        {
            LOGGER.info( "Aborting the transaction " + transactionNumber + " since a corresponding site previously failed\n" );
            abort( t.getTransactionNumber() );
            abortTransaction.remove( t );
            return;
        }

        Map<Operation, ArrayList<Site> > lockedVariables = t.getVariablesLocked();

        for( Map.Entry<Operation, ArrayList<Site> > entry : lockedVariables.entrySet() )
        {
            if( !checkIsSiteUp( entry.getValue() ) )
            {
                LOGGER.info( "Aborting the transaction since no sites are available\n" );
                abort( transactionNumber );
                return;
            }
        }

        /**Commit if everything is okay */
        commit( transactionNumber );
    }

    /**
     * Checks if a all sites in a list are up
     * @param sites list to sites
     * @return True if all sites are up
     */
    private boolean checkIsSiteUp( ArrayList<Site> sites )
    {
        for( Site s: sites )
        {
            if( !s.isSiteUp() )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Tries to run the operations in wait queue after every timestamp based on the updated variables
     * It also sets parsing to true if the operation to be performed next is from the wait queue.
     * If the operation got executed, it is removed from the waitqueue
     * @param updated_vars
     */
    private void parseWaitQueue( Set<Integer> updated_vars )
    {
        parsing = true;
        ArrayList<Operation> waitTemp = new ArrayList<>();
        waitTemp = ( ArrayList ) waitQueue.clone();

        if( updated_vars.size() > 0 )
        {
            for( Operation op : waitTemp )
            {
                if( updated_vars.size() > 0 )
                {
                    if( updated_vars.contains( op.getVariableNumber() ) )
                    {
                        addAndExecuteOperation( op );

                        if( !parsing )
                        {
                            updated_vars.remove( op.getVariableNumber() );
                            parsing = true;
                        }
                        else
                        {
                            waitQueue.remove( op );
                        }
                    }
                }
            }
        }
        else
        {
            for( Operation op : waitTemp )
            {
                addAndExecuteOperation( op );

                if( !parsing )
                {
                    updated_vars.remove( op.getVariableNumber() );
                    parsing = true;
                }
                else
                {
                    waitQueue.remove( op );
                }
            }
        }

        parsing = false;
    }

    /**
     * The transaction with the transaction number is committed.
     *
     * If the operation was a write operation, its corresponding variables on upsites are updated.
     * Once the transaction commits all the corresponding locks on the variables are removed.
     * It also executes the operation in waitqueue if it was waiting for recovered sites variable to be written
     * It also parses the waitqueue to executes operations waiting for this transaction to complete
     * Also removes the transaction from active transactions
     * @param transactionNumber
     */
    private void commit( int transactionNumber )
    {
        LOGGER.fine( "in commit" );
        waitForGraph.removeEdges( transactionNumber );
        Transaction t = activeTransactions.get( transactionNumber );
        LOGGER.info( "Commiting trasaction " + t +"\n");
        Map<Operation, ArrayList<Site> > lockedVariables = t.getVariablesLocked();
        Set<Integer> updated_vars = new HashSet<>();

        for( Operation op : lockedVariables.keySet() )
        {
            updated_vars.add( op.getVariableNumber() );
            boolean temp = false;

            if( op.getOperation() == OperationType.WRITE )
            {
                temp = dm.updateVariableToParticularSite( op.getVariableNumber(), op.getValue(), lockedVariables.get( op ) );
            }

            dm.removeLocks( op.getVariableNumber(), op.getTransaction() );

            if( temp )
            {
                HashSet<Operation> waitRecovTemp = new HashSet<>();
                waitRecovTemp = ( HashSet<Operation> ) waitSiteDownQueue.clone();

                for( Operation oper: waitRecovTemp )
                {
                    if( oper.getVariableNumber() == op.getVariableNumber() )
                    {
                        addAndExecuteOperation( oper );
                        waitSiteDownQueue.remove( oper );
                    }
                }
            }
        }

        activeTransactions.remove( t );
        parseWaitQueue( updated_vars );
    }

    /**
     * It aborts this transaction
     * Once the transaction commits all the corresponding locks on the variables are removed.
     * It updates temperory cached variables to old values, if any transaction aborts
     * It also parses the waitqueue to executes operations waiting for this transaction to complete
     * Also removes the transaction from active transactions
     * @param transactionNumber
     */
    private void abort( int transactionNumber )
    {
        LOGGER.fine( "in abort" );
        waitForGraph.removeEdges( transactionNumber );
        Transaction t = activeTransactions.get( transactionNumber );
        Map<Operation, ArrayList<Site> > lockedVariables = t.getVariablesLocked();
        Set<Integer> updated_vars = new HashSet<>();
        LOGGER.info( "Aborting transaction " + t +"\n");

        for( Operation op : lockedVariables.keySet() )
        {
            updated_vars.add( op.getVariableNumber() );

            if( op.getOperation() == OperationType.WRITE )
            {
                ArrayList<Site> sites = dm.getUpSites( op.getVariableNumber() );

                if( sites.size() > 0 )
                {
                    Variable v = new Variable( op.getVariableNumber(), sites.get( 0 ).getVariable( op.getVariableNumber() ).getValue() );
                    tempVariables.put( op.getVariableNumber(), v );
                }
                else
                {
                    tempVariables.put( op.getVariableNumber(), null );
                }
            }

            dm.removeLocks( op.getVariableNumber(), op.getTransaction() );
        }

        ArrayList<Operation> waitTemp = new ArrayList<>();
        waitTemp = ( ArrayList ) waitQueue.clone();

        for( Operation op : waitTemp )
        {
            if( op.getTransaction().getTransactionNumber() == transactionNumber )
            {
                waitQueue.remove( op );
            }
        }

        HashSet<Operation> waitSiteTemp = new HashSet();
        waitSiteTemp = ( HashSet<Operation> ) waitSiteDownQueue.clone();

        for( Operation op : waitSiteTemp )
        {
            if( op.getTransaction().getTransactionNumber() == transactionNumber )
            {
                waitSiteDownQueue.remove( op );
            }
        }

        activeTransactions.remove( t );
        parseWaitQueue( updated_vars );
    }

    /**
     * Returns active transactions based on transaction number
     * @param transNumber
     * @return  active transactions
     */
    public Transaction getActiveTransactions( int transNumber )
    {
        return activeTransactions.get( transNumber );
    }

    public void setActiveTransactions( Map<Integer, Transaction> activeTransactions )
    {
        this.activeTransactions = activeTransactions;
    }

    /**
     * Based on the operation, performs read/write/read only
     * @param oper operation
     */
    public void addAndExecuteOperation( Operation oper )
    {
        if( oper.getOperation() == OperationType.READ )
        {
            read( oper );
        }
        else if( oper.getOperation() == OperationType.READONLY )
        {
            readOnly( oper );
        }
        else
        {
            write( oper );
        }
    }

    /**
     * Returns lock count on a variable on all the sites having it
     * @param upSites
     * @param op
     * @return total locks
     */
    private int isLockedCount( ArrayList<Site> upSites,
                               Operation op )
    {
        int countLocked = 0;

        for( Site site : upSites )
        {
            if( site.getVariable( op.getVariableNumber() ).isLock() )
            {
                countLocked++;
            }
        }

        if( countLocked == upSites.size() )
        {
            return 1; /* All locked */
        }
        else if( countLocked == 0 )
        {
            return 0; /* All unlocked */
        }
        else
        {
            return -1; /* some locked */
        }
    }

    /**
     * Assigns READLOCK or WRITELOCK to all the variables on all the sites for that operation
     * @param upSites
     * @param op
     */
    private void setLock( ArrayList<Site> upSites,
                          Operation op )
    {
        LOGGER.fine( "Adding Lock..." );

        for( Site site : upSites )
        {
            if( op.getOperation() == OperationType.READ )
            {
                site.getVariable( op.getVariableNumber() ).setLock( op.getTransaction(), LockType.READLOCK );
            }
            else
            {
                site.getVariable( op.getVariableNumber() ).setLock( op.getTransaction(), LockType.WRITELOCK );
            }
        }
    }

    /**
     * Promotes lock from read to write lock for all the sites if variable already has readlock for that variable
     * @param upSites
     * @param op
     */
    private void promoteLock( ArrayList<Site> upSites,
                              Operation op )
    {
        LOGGER.fine( "Promoting Lock..." );

        for( Site site : upSites )
        {
            site.getVariable( op.getVariableNumber() ).promoteLock();
        }
    }

    /**
     * Deadlock detection algorithm has been added. It looks for cycles and removes the youngest transaction if deadlock is detected
     */
    private void detectDeadlock()
    {
        List<Integer> deadlockedVertices = waitForGraph.hasCycle();
        int minTimestamp = Integer.MIN_VALUE;
        int youngestTransaction = 0;

        if( !deadlockedVertices.isEmpty() )
        {
            LOGGER.info( "Deadlock detected in " + deadlockedVertices +"\n");

            for( Integer transNumber : deadlockedVertices )
            {
                Transaction current = activeTransactions.get( transNumber );

                if( current.getTimeStamp() > ( minTimestamp ) )
                {
                    minTimestamp = current.getTimeStamp();
                    youngestTransaction = current.getTransactionNumber();
                }
            }

            boolean temp = waitForGraph.removeEdges( youngestTransaction );
            abort( youngestTransaction );

            if( temp )
            {
                parseWaitQueue( new HashSet<>() );
                detectDeadlock();
            }
        }
    }

    /**
     * Performs read operation.
     * It gets a valid upsite for the operation, if upsite is not found, operation is added to waitsitesdownqueue
     * It gets readlock if variable has no locks and performs read
     * It doesnot read from site that has corrupt variables(site recovered but not written)
     * If variable only has readlocks it assigns readlocks and does read operation if waitqueue has not writes waiting for the locks to be released
     * If there is a write lock by another transaction, operation is added to wait queue and in waitForGraph, deadlock detection is done
     * If write is by same transaction, it does the read
     * If transaction already has readlock on variable it does the read
     * If variable only has readlocks adds opeartion to waitqueue if waitqueue has writes waiting for the locks to be released, also the operation is added to waitforqueue and deadlock detection is done
     * @param op
     */
    public void read( Operation op )
    {
        LOGGER.fine( "in read" );
        Site validSite = null;
        ArrayList<Site> upSites = dm.getUpSites( op.getVariableNumber() );

        if( upSites.isEmpty() )
        {
            LOGGER.info( "All corresponding sites down, adding [" + op + "] to wait for sites waitQueue\n" );
            waitSiteDownQueue.add( op );
            return;
        }
        else if( ( upSites.size() == 1 ) && ( op.getVariableNumber() % 2 != 0 ) )
        {
            validSite = upSites.get( 0 );
        }
        else
        {
            for( Site s : upSites )
            {
                if( !s.checkVariableIsCorrupt( op.getVariableNumber() ) )
                {
                    validSite = s;
                    break;
                }
            }
        }

        if( validSite != null )
        {
            Variable variable = validSite.getVariable( op.getVariableNumber() );

            if( isLockedCount( upSites, op ) == 0 ) /* all unlocked */
            {
                setLock( upSites, op );
                op.getTransaction().addVariableLocked( op, upSites );
                LOGGER.info( "x" + op.getVariableNumber() + " : " + variable.getValue() + " from site " + validSite.getSiteNo() +"\n");
            }
            else if( ( variable.getLockType() == LockType.WRITELOCK ) &&
                     ( op.getTransaction() != variable.getLockedByTransaction().get( 0 ) ) ) /* if not locked same */
                                                                                             /* transaction, write lock */
                                                                                             /* only has 1 transaction */
            {
                if( !parsing )
                {
                    LOGGER.info( "WRITE LOCK BY Transaction " + variable.getLockedByTransaction().get( 0 ).getTransactionNumber() + ", Adding[" + op
                                 + "] to waitQueue.\n" );
                    waitQueue.add( op );
                    /* Adding to waitforgraph & deadlock detection */
                    waitForGraph.addEdge( op.getTransaction().getTransactionNumber(),
                                          variable.getLockedByTransaction().get( 0 ).getTransactionNumber(), op.getVariableNumber() );
                    detectDeadlock();
                }
                else
                {
                    LOGGER.fine( "in parsing" );
                    parsing = false;
                }
            }
            else if( ( variable.getLockType() == LockType.WRITELOCK ) &&
                     ( op.getTransaction() == variable.getLockedByTransaction().get( 0 ) ) )
            {
                LOGGER.info(
                    "x" + op.getVariableNumber() + " : " + tempVariables.get( op.getVariableNumber() ).getValue() + " from site " + validSite.getSiteNo() +"\n");
            }
            else if( variable.getLockType() == LockType.READLOCK )
            {
                if( variable.getLockedByTransaction().contains( op.getTransaction() ) )
                {
                    LOGGER.info( "x" + op.getVariableNumber() + " : "
                                 + tempVariables.get( op.getVariableNumber() ).getValue() + " from site " + validSite.getSiteNo() +"\n");
                }
                else /* all readlocks */
                {
                    LOGGER.fine( "in else" );
                    boolean inWaitQueue = false;

                    for( Operation o : waitQueue )
                    {
                        if( o.getVariableNumber() == op.getVariableNumber() ) /* dont skip writes */
                        {
                            inWaitQueue = true;
                            break;
                        }
                    }

                    if( inWaitQueue )
                    {
                        if( !parsing )
                        {
                            LOGGER.info( "WRITE transaction waiting for lock, Adding[" + op + "] to waitQueue\n" );
                            waitQueue.add( op );

                            if( !inWaitQueue )
                            {
                                for( Transaction t : variable.getLockedByTransaction() )
                                {
                                    waitForGraph.addEdge( op.getTransaction().getTransactionNumber(), t.getTransactionNumber(), op.getVariableNumber() );
                                }
                            }
                            else
                            {
                                Operation temp = null;

                                for( Operation operation: waitQueue )
                                {
                                    if( ( op.getVariableNumber() == operation.getVariableNumber() ) && ( operation.getOperation() == OperationType.WRITE ) )
                                    {
                                        temp = operation;
                                    }
                                }

                                waitForGraph.addEdge( op.getTransaction().getTransactionNumber(), temp.getTransaction().getTransactionNumber(), op.getVariableNumber() );
                            }

                            detectDeadlock();
                        }
                        else
                        {
                            parsing = false;
                        }
                    }
                    else
                    {
                        setLock( upSites, op );
                        op.getTransaction().addVariableLocked( op, upSites );
                        LOGGER.info( "x" + op.getVariableNumber() + " : " + variable.getValue() + " from site " + validSite.getSiteNo() +"\n");
                    }
                }
            }
        }
        else
        {
            LOGGER.info( "Adding [" + op + "] to site down queue\n" );
            waitSiteDownQueue.add( op );
        }
    }

    /**
     * Performs write operation.
     * It gets a valid upsite for the operation, if upsite is not found, operation is added to waitsitesdownqueue
     * It gets writelock if variable has no locks and updates tempvariables cache
     * If there is a readlock or write lock by another transaction, operation is added to wait queue and in waitForGraph, deadlock detection is done
     * If write is by same transaction, it does the write
     * If transaction has readlock by the same transaction it promotes the lock to write lock
     * @param op
     */
    public void write( Operation op )
    {
        LOGGER.fine( "in write" );

        ArrayList<Site> upSites = dm.getUpSites( op.getVariableNumber() );

        if( upSites.isEmpty() )
        {
            waitSiteDownQueue.add( op );
            LOGGER.info( "All corresponding sites down, adding [" + op + "] to wait for sites waitQueue\n" );
            return;
        }
        else
        {
            Variable tvariable = tempVariables.get( op.getVariableNumber() );
            Variable variable = upSites.get( 0 ).getVariable( op.getVariableNumber() );

            if( isLockedCount( upSites, op ) == 0 ) /* all unlocked */
            {
                setLock( upSites, op );
                op.getTransaction().addVariableLocked( op, upSites );
                LOGGER.info( op + " from value " + variable.getValue() +"\n");
                tvariable.setValue( op.getValue() );
            }
            else
            {
                if( ( variable.getLockType() == LockType.WRITELOCK ) &&
                    ( op.getTransaction() == variable.getLockedByTransaction().get( 0 ) ) )
                {
                    LOGGER.info( op + " from value " + tvariable.getValue() +"\n");
                    tvariable.setValue( op.getValue() );
                    op.getTransaction().addVariableLocked( op, upSites );
                }
                else if( ( variable.getLockType() == LockType.WRITELOCK ) &&
                         ( op.getTransaction() != variable.getLockedByTransaction().get( 0 ) ) )
                {
                    if( !parsing )
                    {
                        waitQueue.add( op );
                        LOGGER.info( "WRITE lock by transaction " + variable.getLockedByTransaction().get( 0 ).getTransactionNumber() + ", Adding[" + op
                                     + "] to waitQueue" +"\n");
                        waitForGraph.addEdge( op.getTransaction().getTransactionNumber(),
                                              variable.getLockedByTransaction().get( 0 ).getTransactionNumber(),
                                              op.getVariableNumber() );
                        detectDeadlock();
                    }
                    else
                    {
                        parsing = false;
                    }
                }
                else if( variable.getLockType() == LockType.READLOCK )
                {
                    boolean inWaitQueue = false;

                    for( Operation o : waitQueue )
                    {
                        if( ( o.getVariableNumber() == op.getVariableNumber() ) && ( o != op ) ) /* dont skip writes */
                        {
                            inWaitQueue = true;
                            break;
                        }
                    }

                    if( ( variable.getLockedByTransaction().size() == 1 ) && ( variable.getLockedByTransaction().get( 0 )
                                                                                  .getTransactionNumber() == op.getTransaction().getTransactionNumber() ) && !inWaitQueue )
                    {
                        promoteLock( upSites, op );
                        op.getTransaction().addVariableLocked( op, upSites );
                        LOGGER.info( op + " from value " + tvariable.getValue() +"\n");
                        tvariable.setValue( op.getValue() );
                    }
                    else
                    {
                        if( !parsing )
                        {
                            LOGGER.info( "READ LOCK by multiple transactions/other transaction, Adding [" + op
                                         + "] to waitQueue\n" );
                            waitQueue.add( op );

                            /* if no transaction in waitQ, wait for all the transactions to give up their read locks*/
                            if( !inWaitQueue )
                            {
                                for( Transaction t : variable.getLockedByTransaction() )
                                {
                                    waitForGraph.addEdge( op.getTransaction().getTransactionNumber(), t.getTransactionNumber(), op.getVariableNumber() );
                                }
                            }
                            else
                            {
                                Operation temp = null;

                                for( Operation operation : waitQueue )
                                {
                                    if( ( op.getVariableNumber() == operation.getVariableNumber() ) && ( op.getTransaction().getTransactionNumber() != operation.getTransaction().getTransactionNumber() ) )
                                    {
                                        temp = operation;
                                    }
                                }

                                waitForGraph.addEdge( op.getTransaction().getTransactionNumber(), temp.getTransaction().getTransactionNumber(), op.getVariableNumber() );
                            }

                            detectDeadlock();
                        }
                        else
                        {
                            parsing = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs raedonly operation
     * It alwas reads the last commited value before transaction begun from the readOnlyLastCommitedValue HashMap
     * @param op
     */
    public void readOnly( Operation op )
    {
        LOGGER.fine( "in readOnly" );
        int readValue = Integer.MIN_VALUE;
        ArrayList<Site> upSites = dm.getUpSites( op.getVariableNumber() );

        if( !upSites.isEmpty() )
        {
            readValue = readOnlyLastCommitedValue.get( op.getTransaction().getTransactionNumber() )
                           .get( op.getVariableNumber() - 1 );
        }

        if( readValue != Integer.MIN_VALUE )
        {
            LOGGER.info( "x" + op.getVariableNumber() + " : " + readValue );
        }
        else
        {
            LOGGER.info( "All sites down, Adding [" + op + "] to waitQueue" );
            waitSiteDownQueue.add( op );
        }
    }
}
