package app;

public class Variable {

    enum LockType
    {
        READLOCK, WRITELOCK;
    }

    private int value;
    private Transaction lockedByTransaction;
    private LockType lockType;
    private boolean isLock;
    private int index;

    public Variable(int index, int value){
        this.index = index;
        this.value = value;
        this.isLock = false;
    }

    @Override
    public String toString() {
        return "index:"+this.index+" value:"+this.value+" isLock:"+this.isLock+" lockedByTrans: "+lockedByTransaction.getTransaction()+" locktype:"+lockType;
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

    public Transaction getLockedByTransaction() {
        return lockedByTransaction;
    }

    public void setLockedByTransaction(Transaction lockedByTransaction) {
        this.lockedByTransaction = lockedByTransaction;
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

    public void setLock(Transaction lockedByTransaction, LockType lockType){
        this.lockedByTransaction = lockedByTransaction;
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
