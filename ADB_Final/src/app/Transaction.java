package app;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private int transactionNumber;
    private int timestamp;
    private boolean isReadOnly;
    private Map<Operation, Site> variablesLocked = new HashMap<>(); //map<variable, site>

    public Transaction(int transactionNumber, boolean isReadOnly, int arrivalTime) {
        this.transactionNumber = transactionNumber;
        this.timestamp = arrivalTime;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String toString() {

        return "Transaction: " + this.transactionNumber + " began at: " + this.timestamp;
    }

    public int getTransactionNumber() {
        return this.transactionNumber;
    }

    public int getTimeStamp() {
        return this.timestamp;
    }

    public boolean isReadOnly(){
        return this.isReadOnly;
    }

    public Map<Operation, Site> getVariablesLocked() {
        return variablesLocked;
    }
    public void addVariableLocked(Operation op, Site s) {
        this.variablesLocked.put(op,s);
    }

}