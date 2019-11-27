package app;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private int transactionNumber;
    private Date timestamp;
    private boolean isReadOnly;
    private Map<Operation, Site> variablesLocked = new HashMap<>(); //map<variable, site>

    public Transaction(int transactionNumber, boolean isReadOnly) {
        this.transactionNumber = transactionNumber;
        this.timestamp = new Date();
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
        return "Transaction: " + this.transactionNumber + " began at: " + formatter.format(this.timestamp);
    }

    public int getTransactionNumber() {
        return this.transactionNumber;
    }

    public Date getTimeStamp() {
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