# DistributedDatabaseManager
```
decideRead():
if (transactiontype == read)
    read()
else
    readOnly()

readOnly():
if(none of the sites are up)
    Add T to waitSiteDownQueue
else 
    //read last commited value

For read intstruction R(T, V):
if(none of the sites are up)
    Add T to waitSiteDownQueue
else 
{
    if(!isLock){
        Assign readlock on all available sites
        print value
    }
    else{
        if(lockType == writeLock by another Transaction T2){
            if(!parsing)
                Add to waitQueue
                add in waitForGraph T->T2
                DeadlockDetection
            else
                    parsing = false
        }
        else if(lockType == writeLock by T)
            print value //intermediate or commited
        else if( Transaction has readlock on variable)
            print value
        else if(locktype == readlock){
            if(waitQueue doesnt contain V){
                Assign readlock on all available sites
                print value
            }
            else{ // if(waitQueue contains V){
                if(!parsing)
                    Add to waitQueue
                    add in waitForGraph T->T2
                    DeadlockDetection
                else
                    parsing = false
            }
        }
    }
}
For write instruction W(T, V, x):
if(none of the sites are up)
    Add T to waitSiteDownQueue
else 
{
    if(!isLock){
        Assign writelock on all available sites
        update value in variableTempQueue
    }
    else{
        if(lockType == writeLock by T)
            update value in variableTempQueue
        else if(lockType == writeLock by another Transaction T2){
            if(!parsing)
                if(!parsing)
                Add to waitQueue
                add in waitForGraph T->T2
                DeadlockDetection
            else
                parsing = false
            
        }
        else if(locktype == readlock only by T){
            Promote readlock to writelock
            update value in variableTempQueue
        }
        else if(locktype == readlock by multiple transactions){ //may include T
            if(!parsing)
                Add to waitQueue
                add in waitForGraph T->Ti (for i in readlocktransctions(i != T))
                DeadlockDetection
            else
                parsing = false
        }
    }
}

checking deadlock DeadlockDetection(waitForGraph):
check for cycle similar to https://www.geeksforgeeks.org/detect-cycle-in-a-graph/ using HashMap
list<transaction> = all transaction in the cycle
if (!null(list)){
    get the youngest transaction T
    abort(T)
    remove transaction from waitforgraph
}

for ending the transaction end(T):
//validate the transaction
if(valid){
    commit(T)
}
else{
    abort(T)
}

for commit(T):
vars = get all locked variables for T
update all available corresponding sites with above vars
release all locks 
remove T from waitForGraph
Parse(vars)
remove T from activeTransactions //delete object

for abort(T):
vars = get all locked variables for T
update variableTempQueue for vars with last commited values
release all locks
remove T from waitForGraph
Parse(vars)
remove T from activeTransactions //delete object

Parse(vars):
boolean parsing = true
for(operation: waitQueue){
    if(operation.var in vars){
        execute operation
        if(!parsing)
            remove operation.var from vars
            parsing = true;
        else
            remove opertion from waitQueue
    }
}



```