package app;

import java.util.Date;
import java.text.SimpleDateFormat;  

public class Transaction {
    private int transactionNumber;
    private Date timestamp;
    private boolean isReadOnly;

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

    public int getTransaction() {
        return this.transactionNumber;
    }

    public Date getTimeStamp() {
        return this.timestamp;
    }

    public boolean isReadOnly(){
        return this.isReadOnly;
    }


}