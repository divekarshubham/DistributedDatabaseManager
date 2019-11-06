# DistributedDatabaseManager
For read intstruction R(T,V):
if(!isLock){
    Assign readlock on all available sites
    print value
}
else{
    if(lockType == writeLock by another Transaction)
        Add to waitQueue
    else if(lockType == writeLock by T)
        print value //intermediate or commited
    else if( Transaction has readlock on variable)
        print value
    else if(locktype == readlock){
        if(waitQueue doesnt contain V){
            Assign readlock on all available sites
            print value
        }
        else{
            Add to waitQueue
        }
    }
}