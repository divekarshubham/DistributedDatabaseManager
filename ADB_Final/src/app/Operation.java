package app;

public class Operation {

    enum operations {
        READ, READONLY, WRITE;
    }

    private int valueToWrite;
    private operations operationPerformed;
    private Transaction transaction;
    
    public Operation(Transaction transaction, operations operationPerformed, int value) {
        this.transaction = transaction;
        this.operationPerformed = operationPerformed;
        this.valueToWrite = value;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public int getValue() {
        return this.valueToWrite;
    }

    public operations getOperation() {
        return this.operationPerformed;
    }

}