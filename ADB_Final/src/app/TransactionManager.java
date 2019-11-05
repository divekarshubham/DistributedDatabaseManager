package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.Variable.LockType;

public class TransactionManager {
    private Map<Integer, Transaction> activeTransactions;
    private List<Variable> variableLocks;
    private List<Operation> operationQueue;
    private DataManager dm;

    public TransactionManager() {
        dm = DataManager.getInstance();
        activeTransactions = new HashMap<>();
        variableLocks = new ArrayList<>(20);
        operationQueue = new ArrayList<>();
    }

    public void begin(int transactionNumber) {
        activeTransactions.put(transactionNumber, new Transaction(transactionNumber, false));
    }

    public void beginRO(int transactionNumber) {
        activeTransactions.put(transactionNumber, new Transaction(transactionNumber, true));
    }

    public void end(int transactionNumber) {
    }

    public void write(int transactionNumber, int variableNumber) {
    }

    public void read(int transactionNumber, int variableNumber) {
        // for readonly transaction - multiversion read
        if (activeTransactions.get(transactionNumber).isReadOnly()) {
            System.out.println("x" + variableNumber + ": " + dm.getValueOfVariable(variableNumber));
        } else {
            // Check for deadlock

            // for normal read
            Variable var = variableLocks.get(variableNumber);
            if (var.isLock()) {
                if(var.getLockedByTransaction().getTransaction() == transactionNumber)
                    System.out.println("x" + variableNumber + ": " + var.getValue());
                else if(var.getLockType() == LockType.READLOCK)
                    System.out.println("x" + variableNumber + ": " + dm.getValueOfVariable(variableNumber));
                else{}
                    // Wait for lock

            } else {
                var.setLock(activeTransactions.get(transactionNumber), LockType.READLOCK);
                System.out.println("x" + variableNumber + ": " + dm.getValueOfVariable(variableNumber));
            }
        }
    }

}