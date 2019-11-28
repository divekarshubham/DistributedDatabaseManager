package app;

import java.util.*;
import java.util.logging.Logger;

public class TransactionManager {

    private final static Logger LOGGER = Logger.getLogger(TransactionManager.class.getName());
    private Map<Integer, Transaction> activeTransactions;
    private Map<Integer, ArrayList<Integer>> readOnlyLastCommitedValue = new HashMap<>(); // transactionNumber,
                                                                                          // LastReadValue
    private Map<Integer, Variable> tempVariables; // make ints
    private DataManager dm;
    private ArrayList<Operation> waitQueue;
    private List<Operation> waitSiteDownQueue;
    private Graph waitForGraph;
    private int timer = 0;
    private boolean parsing = false;

    public TransactionManager() {
        dm = DataManager.getInstance();
        activeTransactions = new HashMap<>();
        tempVariables = new HashMap<>();
        waitQueue = new ArrayList<>();
        waitSiteDownQueue = new ArrayList<>();
        waitForGraph = new Graph();

        for (int i = 1; i < 21; i++) {
            tempVariables.put(i, new Variable(i, 10 * i));
        }


    }

    public void begin(int transactionNumber) {
        activeTransactions.put(transactionNumber, new Transaction(transactionNumber, false, timer++));
        LOGGER.fine("in begin");
    }

    public void beginRO(int transactionNumber) {
        activeTransactions.put(transactionNumber, new Transaction(transactionNumber, true, timer++));
        readOnlyLastCommitedValue.put(transactionNumber, dm.lastCommitedValuesForReadOnly());
        LOGGER.fine("in beginro");
    }

    public void end(int transactionNumber) {
        LOGGER.fine("in end");
        Transaction t = activeTransactions.get(transactionNumber);
        System.out.println(t.toString());
        System.out.println(waitQueue);
        Map<Operation, Site> lockedVariables = t.getVariablesLocked();
        for (Map.Entry<Operation, Site> entry : lockedVariables.entrySet()) {
            System.out.println(entry);
            if (!entry.getValue().isSiteUp()) {
                System.out.println("due to site down");
                abort(transactionNumber);
                return;
            }
        }
        commit(transactionNumber);

        // remove from wfg
        // execute from waitQ

    }

    private void parseWaitQueue(Set<Integer> updated_vars){
        parsing = true;
        ArrayList<Operation> waitTemp = new ArrayList<>();
        waitTemp = (ArrayList) waitQueue.clone();
        for(Operation op : waitTemp) {
            if (updated_vars.size() > 0) {
                if (updated_vars.contains(op.getVariableNumber())) {
                    addAndExecuteOperation(op);
                    if (!parsing) {
                        updated_vars.remove(op.getVariableNumber());
                        parsing = true;
                    }
                    else{
                        waitQueue.remove(op);
                    }

                }
            }
        }
    }

    private void commit(int transactionNumber) {
        LOGGER.fine("in commit");
        Transaction t = activeTransactions.get(transactionNumber);
        LOGGER.info("comminting trasaction" + t);
        Map<Operation, Site> lockedVariables = t.getVariablesLocked();
        Set<Integer> updated_vars = new HashSet<>();
        System.out.println(lockedVariables.keySet());
        for (Operation op : lockedVariables.keySet()) {
            updated_vars.add(op.getVariableNumber());
            if (op.getOperation() == OperationType.WRITE) {
                dm.updateVariableToSite(op.getVariableNumber(), op.getValue());
            }
            dm.removeLocks(op.getVariableNumber(), op.getTransaction());
        }
        dm.dump();

        // set wasDown to false
        // remove locks
        activeTransactions.remove(t);
        parseWaitQueue(updated_vars);
    }

    private void abort(int transactionNumber) {
        LOGGER.fine("in abort");
        Transaction t = activeTransactions.get(transactionNumber);
        Map<Operation, Site> lockedVariables = t.getVariablesLocked();
        Set<Integer> updated_vars = new HashSet<>();
        LOGGER.info("Aborting transaction " + t);
        for (Operation op : lockedVariables.keySet()) {
            updated_vars.add(op.getVariableNumber());
            if (op.getOperation() == OperationType.WRITE) {
                ArrayList<Site> sites = dm.getUpSites(op.getVariableNumber());
                if (sites.size() > 0) {
                    tempVariables.put(op.getVariableNumber(), sites.get(0).getVariable(op.getVariableNumber()));
                } else {
                    tempVariables.put(op.getVariableNumber(), null);
                }
            }
            dm.removeLocks(op.getVariableNumber(), op.getTransaction());
        }
        dm.dump();
         for(int tempV : tempVariables.keySet()){
         System.out.println(tempV+" "+tempVariables.get(tempV));
         }
        ArrayList<Operation> waitTemp = new ArrayList<>();
        waitTemp = (ArrayList) waitQueue.clone();
        for(Operation op : waitTemp) {
            if(op.getTransaction().getTransactionNumber() == transactionNumber){
                waitQueue.remove(op);
            }
        }
        System.out.println("waitq"+waitQueue);
        parseWaitQueue(updated_vars);
        activeTransactions.remove(t);
    }

    public Transaction getActiveTransactions(int transNumber) {
        return activeTransactions.get(transNumber);
    }

    public void setActiveTransactions(Map<Integer, Transaction> activeTransactions) {
        this.activeTransactions = activeTransactions;
    }

    public void addAndExecuteOperation(Operation oper) {
        if (oper.getOperation() == OperationType.READ) {
            read(oper);
        } else if (oper.getOperation() == OperationType.READONLY) {
            readOnly(oper);
        } else {
            write(oper);
        }
    }

    private int isLockedCount(ArrayList<Site> upSites, Operation op) {
        int countLocked = 0;
        for (Site site : upSites) {
            if (site.getVariable(op.getVariableNumber()).isLock()) {
                countLocked++;
            }
        }
        if (countLocked == upSites.size())
            return 1; // All locked
        else if (countLocked == 0)
            return 0; // All unlocked
        else
            return -1; // some locked
    }

    private void setLock(ArrayList<Site> upSites, Operation op) {
        LOGGER.fine("Adding Lock...");
        for (Site site : upSites) {
            if (op.getOperation() == OperationType.READ)
                site.getVariable(op.getVariableNumber()).setLock(op.getTransaction(), LockType.READLOCK);
            else
                site.getVariable(op.getVariableNumber()).setLock(op.getTransaction(), LockType.WRITELOCK);
        }
    }

    private void promoteLock(ArrayList<Site> upSites, Operation op) {
        LOGGER.fine("Promoting Lock...");
        for (Site site : upSites) {
            site.getVariable(op.getVariableNumber()).promoteLock();
        }
    }

    private void detectDeadlock(){
        List<Integer> deadlockedVertices = waitForGraph.hasCycle();
        System.out.println(deadlockedVertices);
        int minTimestamp = Integer.MIN_VALUE;
        int youngestTransaction = 0;
        if(!deadlockedVertices.isEmpty()){
            for (Integer transNumber : deadlockedVertices) {

                Transaction current = activeTransactions.get(transNumber);
                System.out.println(current.getTransactionNumber()+"......................"+current.getTimeStamp());
                if (current.getTimeStamp() > (minTimestamp)){
                    minTimestamp = current.getTimeStamp();
                    youngestTransaction = current.getTransactionNumber();
                }
            }
            System.out.println("Aborting deadlock......");
            abort(youngestTransaction);
        }

    }

    public void read(Operation op) {
        LOGGER.fine("in read");
        ArrayList<Site> upSites = dm.getUpSites(op.getVariableNumber());
        if (upSites.isEmpty()) {
            LOGGER.fine("All corresponding sites down, adding [" + op + "] to wait for sites waitQueue");
            waitSiteDownQueue.add(op);
            return;
        } else {
            Variable variable = upSites.get(0).getVariable(op.getVariableNumber());
            if (isLockedCount(upSites, op) == 0) { // all unlocked
                setLock(upSites, op);
                op.getTransaction().addVariableLocked(op, upSites.get(0));
                LOGGER.info("x" + op.getVariableNumber() + " : " + variable.getValue());
            } else if (variable.getLockType() == LockType.WRITELOCK
                    && op.getTransaction() != variable.getLockedByTransaction().get(0)) { // if not locked same
                                                                                          // transaction, write lock
                                                                                          // only has 1 transaction
                if(!parsing) {
                    LOGGER.fine("WRITE LOCK BY" + variable.getLockedByTransaction().get(0) + ", Adding[" + op
                            + "] to waitQueue");
                    waitQueue.add(op);
                    // Adding to waitforgraph & deadlock detection
                    waitForGraph.addEdge(op.getTransaction().getTransactionNumber(),
                            variable.getLockedByTransaction().get(0).getTransactionNumber());
                    detectDeadlock();
                }
                else{
                    parsing =false;
                }
            } else if (variable.getLockType() == LockType.WRITELOCK
                    && op.getTransaction() == variable.getLockedByTransaction().get(0)) {
                LOGGER.info(
                        "x" + op.getVariableNumber() + " : " + tempVariables.get(op.getVariableNumber()).getValue());
            } else if (variable.getLockType() == LockType.READLOCK) {
                if (variable.getLockedByTransaction().contains(op.getTransaction()))
                    LOGGER.info("x" + op.getVariableNumber() + " : "
                            + tempVariables.get(op.getVariableNumber()).getValue());
                else { // all readlocks
                    boolean inWaitQueue = false;
                    for (Operation o : waitQueue) {
                        if (o.getVariableNumber() == op.getVariableNumber()) { // dont skip writes
                            inWaitQueue = true;
                            break;
                        }
                    }
                    if (inWaitQueue) {
                        if(!parsing) {
                            LOGGER.fine("WRITE transaction waiting for lock, Adding[" + op + "] to waitQueue");
                            waitQueue.add(op);
                            for (Transaction t : variable.getLockedByTransaction()) {
                                waitForGraph.addEdge(op.getTransaction().getTransactionNumber(), t.getTransactionNumber());
                            }
                            for(Operation operation: waitQueue){
                                if(op.getVariableNumber() == operation.getVariableNumber() && operation.getOperation() == OperationType.WRITE)
                                    waitForGraph.addEdge(op.getTransaction().getTransactionNumber(), operation.getTransaction().getTransactionNumber());
                            }

                            detectDeadlock();
                        }
                        else{
                            parsing=false;
                        }
                    } else {
                        setLock(upSites, op);
                        op.getTransaction().addVariableLocked(op, upSites.get(0));
                        LOGGER.info("x" + op.getVariableNumber() + " : " + variable.getValue());
                    }

                }
            }
        }
    }

    public void write(Operation op) {
        LOGGER.fine("in write");

        ArrayList<Site> upSites = dm.getUpSites(op.getVariableNumber());
        if (upSites.isEmpty()) {
            waitSiteDownQueue.add(op);
            LOGGER.fine("All corresponding sites down, adding [" + op + "] to wait for sites waitQueue");
            return;
        } else {
            Variable tvariable = tempVariables.get(op.getVariableNumber());
            Variable variable = upSites.get(0).getVariable(op.getVariableNumber());

            if (isLockedCount(upSites, op) == 0) { // all unlocked
                setLock(upSites, op);
                op.getTransaction().addVariableLocked(op, upSites.get(0));
                tvariable.setValue(op.getValue());
                LOGGER.info(op + " from value " + variable.getValue());
            } else {
                if (variable.getLockType() == LockType.WRITELOCK
                        && op.getTransaction() == variable.getLockedByTransaction().get(0)) {
                    LOGGER.info(op + " from value " + tvariable.getValue());
                    tvariable.setValue(op.getValue());
                    op.getTransaction().addVariableLocked(op, upSites.get(0));
                } else if (variable.getLockType() == LockType.WRITELOCK
                        && op.getTransaction() != variable.getLockedByTransaction().get(0)) {
                    if(!parsing) {
                        waitQueue.add(op);
                        LOGGER.fine("WRITE LOCK BY" + variable.getLockedByTransaction().get(0) + ", Adding[" + op
                                + "] to waitQueue");
                        waitForGraph.addEdge(op.getTransaction().getTransactionNumber(),
                                variable.getLockedByTransaction().get(0).getTransactionNumber());
                        detectDeadlock();
                    }
                    else{
                        parsing=false;
                    }
                } else if (variable.getLockType() == LockType.READLOCK) {
                    boolean inWaitQueue = false;
                    for (Operation o : waitQueue) {
                        if (o.getVariableNumber() == op.getVariableNumber()) { // dont skip writes
                            inWaitQueue = true;
                            break;
                        }
                    }
                    if (variable.getLockedByTransaction().size() == 1 && variable.getLockedByTransaction().get(0)
                            .getTransactionNumber() == op.getTransaction().getTransactionNumber() && !inWaitQueue) {
                        promoteLock(upSites, op);
                        op.getTransaction().addVariableLocked(op, upSites.get(0));
                        LOGGER.info(op + " from value " + tvariable.getValue());
                        tvariable.setValue(op.getValue());
                    } else {
                        if(!parsing) {
                            LOGGER.fine("READ LOCK by multiple transactions/other transaction, Adding [" + op
                                    + "] to waitQueue");
                            waitQueue.add(op);
                            for (Transaction t : variable.getLockedByTransaction()) {
                                waitForGraph.addEdge(op.getTransaction().getTransactionNumber(), t.getTransactionNumber());
                            }
                            for(Operation operation: waitQueue){
                                if(op.getVariableNumber() == operation.getVariableNumber())
                                    waitForGraph.addEdge(op.getTransaction().getTransactionNumber(), operation.getTransaction().getTransactionNumber());
                            }
                            detectDeadlock();
                        }
                        else{
                            parsing=false;
                        }
                    }
                }

            }

        }
    }

    public void readOnly(Operation op) {
        LOGGER.fine("in readOnly");
        int readValue = readOnlyLastCommitedValue.get(op.getTransaction().getTransactionNumber())
                .get(op.getVariableNumber() - 1);
        if (readValue != Integer.MIN_VALUE) {
            LOGGER.info("x" + op.getVariableNumber() + " : " + readValue);
        } else {
            LOGGER.fine("All sites down, Adding [" + op + "] to waitQueue");
            waitSiteDownQueue.add(op);
        }
    }

}