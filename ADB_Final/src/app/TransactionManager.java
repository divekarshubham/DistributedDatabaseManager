package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TransactionManager {

    private final static Logger LOGGER = Logger.getLogger(DataManager.class.getName());
    private Map<Integer, Transaction> activeTransactions;
    private List<Variable> tempVariables;
    private DataManager dm;
    private List<Operation> waitQueue;
    private List<Operation> waitSiteDownQueue;

    public TransactionManager() {
        dm = DataManager.getInstance();
        activeTransactions = new HashMap<>();
        tempVariables = new ArrayList<>(21);
        waitQueue = new ArrayList<>();
        waitSiteDownQueue = new ArrayList<>();

        for(int i = 1;i <21; i++){
            tempVariables.add(null);
            tempVariables.add(i, new Variable(i,10*i));
        }
    }

    public void begin(int transactionNumber) {
        activeTransactions.put(transactionNumber, new Transaction(transactionNumber, false));
        System.out.println("in begin");
    }

    public void beginRO(int transactionNumber) {
        activeTransactions.put(transactionNumber, new Transaction(transactionNumber, true));
        System.out.println("in beginro");
    }

    public void end(int transactionNumber) {
        System.out.println("in end");
    }

    public void write(Operation op) {
        System.out.println("in write");
    }

    public  Transaction getActiveTransactions(int transNumber) {
        return activeTransactions.get(transNumber);
    }

    public void setActiveTransactions(Map<Integer, Transaction> activeTransactions) {
        this.activeTransactions = activeTransactions;
    }

    public void addAndExecuteOperation(Operation oper){
        if(oper.getOperation() == OperationType.READ){
            read(oper);
        }
        else if(oper.getOperation() == OperationType.READONLY){
            readOnly(oper);
        }
        else{
            write(oper);
        }
    }

    private int isLockedCount(ArrayList<Site> upSites, Operation op){
        int countLocked = 0;
        for(Site site : upSites){
            if( site.getVariable(op.getVariableNumber()).isLock()){
                countLocked++;
            }
        }
        if(countLocked == upSites.size())
            return 1; //All locked
        else if(countLocked == 0)
            return 0; //All unlocked
        else
            return -1; //some locked
    }

    private void setLock(ArrayList<Site> upSites, Operation op){
        for(Site site: upSites){
            if(op.getOperation() == OperationType.READ)
                site.getVariable(op.getVariableNumber()).setLock(op.getTransaction(), LockType.READLOCK);
            else
                site.getVariable(op.getVariableNumber()).setLock(op.getTransaction(), LockType.WRITELOCK);
        }
    }

    public void read(Operation op){
        System.out.println("in read");
        ArrayList<Site> upSites = dm.getUpSites(op.getVariableNumber());
        if(upSites.isEmpty()){
            waitSiteDownQueue.add(op);
            return;
        }
        else{
            Variable variable = upSites.get(0).getVariable(op.getVariableNumber());

            if(isLockedCount(upSites, op) == 0){ //all unlocked
                setLock(upSites, op);
                System.out.println("x"+op.getVariableNumber()+" : "+variable.getValue());
            }
            else if(variable.getLockType() == LockType.WRITELOCK && op.getTransaction() != variable.getLockedByTransaction().get(0)){
                //parsing
                waitQueue.add(op);
                //Add to waitforgraph
                //deadlock detection
            }
            else if(variable.getLockType() == LockType.WRITELOCK && op.getTransaction() == variable.getLockedByTransaction().get(0)){
                System.out.println("x"+tempVariables.get(op.getVariableNumber())+" : "+tempVariables.get(op.getVariableNumber()).getValue());
            }
            else if(variable.getLockType() == LockType.READLOCK){
                if(variable.getLockedByTransaction().contains(op.getTransaction()))
                    System.out.println("x"+tempVariables.get(op.getVariableNumber())+" : "+tempVariables.get(op.getVariableNumber()).getValue());
                else{
                    boolean inWaitQueue = false;
                    for(Operation o: waitQueue) {
                        if (o.getVariableNumber() == op.getVariableNumber()) {
                            inWaitQueue = true;
                            break;
                        }
                    }
                    if (inWaitQueue) {
                        //parsing
                        waitQueue.add(op);
                        //Add to waitforgraph
                        //deadlock detection
                    }
                    else {
                        setLock(upSites, op);
                        System.out.println("x" + op.getVariableNumber() + " : " + variable.getValue());
                    }

                }
            }

        }

    }
    public void readOnly(Operation op){
        System.out.println("in readonly");

    }


//    public void read(int transactionNumber, int variableNumber) {
//        // for readonly transaction - multiversion read
//        if (activeTransactions.get(transactionNumber).isReadOnly()) {
//            System.out.println("x" + variableNumber + ": " + dm.getValueOfVariable(variableNumber));
//        } else {
//            // Check for deadlock
//
//            // for normal read
//            Variable var = variableLocks.get(variableNumber);
//            if (var.isLock()) {
//                if(var.getLockedByTransaction().getTransaction() == transactionNumber)
//                    System.out.println("x" + variableNumber + ": " + var.getValue());
//                else if(var.getLockType() == LockType.READLOCK)
//                    System.out.println("x" + variableNumber + ": " + dm.getValueOfVariable(variableNumber));
//                else{}
//                    // Wait for lock
//
//            } else {
//                var.setLock(activeTransactions.get(transactionNumber), LockType.READLOCK);
//                System.out.println("x" + variableNumber + ": " + dm.getValueOfVariable(variableNumber));
//            }
//        }
//    }

}