# DistributedDatabaseManager
# TO FIX
DataManager has two methods updateVariables to sites and particular site
Uncrustify
why are there comments in end/commit/abort for removing variables etc

# Changed
changed dump
changed read from site to siteNO

# Algorithms used for the Database Manager

#### For read intstruction R(T, V):
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

read():
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
```
#### For write instruction W(T, V, x):
```
write():
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
```
#### Checking deadlock after adding to waitForGraph 
```
DeadlockDetection(waitForGraph):
    check for cycle similar to https://www.geeksforgeeks.org/detect-cycle-in-a-graph/ using HashMap
    list<transaction> = all transaction in the cycle
    if (!null(list)){
        get the youngest transaction T
        abort(T)
        remove transaction from waitforgraph
    }
```

#### For ending the transaction 
```
end(T):
    //validate the transaction
    if(valid){
        commit(T)
        Print T commits
    }
    else{
        abort(T)
        Print T aborts
    }

commit(T):
    vars = get all locked variables for T
    update all available corresponding sites with above vars and set wasDown[vars] to false
    release all locks 
    remove T from waitForGraph
    Parse(vars)
    remove T from activeTransactions //delete object

abort(T):
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
#### Site Handling operations
```
//if the site was downn then the next read operation will find the site to be down
isSiteUp(operationType,variableNumber):
    if(siteUp){
        if(operationType == READ){
            if(wasDown[variableNumeber]) //wasDown is boolean[20] 
                return false
            else
                return true
        }
        else{
            return true
        }
    }
    else
        return false


recover(Site s)
    s.isSiteUp = true
    for(i: replicated variables)
        s.wasDown[i] = true 
    for (operation op: waitSiteDownQueue)
        if(checkSiteforVariable(op.variable) == s)
            execute the transaction

fail(Site s)
    s.isSiteUp = false
    {
        make an accessedAtSite List in the Transaction class
        at commit time if any of the above site is down then abort the transaction
    }

```