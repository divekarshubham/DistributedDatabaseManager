package app;

import java.util.List;

public class Variable {

    private int value;
    private List<Transaction> lockedByTransactions;
    private LockType lockType;
    private boolean isLock;
    private int index;
    private boolean isCorrupt;

    public Variable(int index, int value){
        this.index = index;
        this.value = value;
        this.isLock = false;
        this.isCorrupt = false;
    }

    @Override
    public String toString() {
        return "index:"+this.index+" value:"+this.value+" isLock:"+this.isLock+" lockedByTrans: "+ lockedByTransactions.get(0).getTransactionNumber()+" locktype:"+lockType;
    }

    public int getIndex() {
        return index;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public List<Transaction> getLockedByTransaction() {
            return lockedByTransactions;
    }


    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public boolean isLock() {
        return isLock;
    }

    public boolean isCorrupt(){
        return isCorrupt;
    }

    public void setCorrupt(boolean corrupt){
        this.isCorrupt = corrupt;
    }

    public void setLock(Transaction lockedByTransaction, LockType lockType){
        this.lockedByTransactions.add(lockedByTransaction);
        this.lockType = lockType;
        this.isLock = true;
    }

    public void unlock(){
        if(this.isLock)
            this.isLock = false;
        else {
            throw new IllegalArgumentException("variable has no lock");
        }
    }
}
