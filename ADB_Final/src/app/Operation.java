package app;

public class Operation {


    private int valueToWrite;
    private OperationType operationPerformed;
    private Transaction transaction;
    private int variableNumber;

    public Operation(Transaction transaction, OperationType operationPerformed,int variableNumber,  int value) {
        this.transaction = transaction;
        this.operationPerformed = operationPerformed;
        this.variableNumber = variableNumber;
        this.valueToWrite = value;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public int getValue() {
        return this.valueToWrite;
    }

    public OperationType getOperation() {
        return this.operationPerformed;
    }

    public int getVariableNumber() {
        return variableNumber;
    }

}