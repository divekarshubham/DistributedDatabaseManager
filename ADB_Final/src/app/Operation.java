/**
 * @file Operation.java
 * @author Shubham Divekar, Himani Shah (sjd451@nyu.edu, has482@nyu.edu)
 * @brief Corresponds to each action performed by a transaction on a variable.
 * @version 0.1
 * @date 2019-12-02
 * 
 * @copyright Copyright (c) 2019
 * 
 */
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

    @Override
    public String toString(){
        return "Operation details: "+this.transaction+" operation type:" + this.operationPerformed +" variable:" + this.variableNumber+ (this.operationPerformed==OperationType.WRITE? " Val:"+this.valueToWrite: " ");
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