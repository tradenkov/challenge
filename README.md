# challenge

## This solution is OK for in-memory account storage, but if we want to use real DB is not appropriate. 
Using real DB will require adding transactions. We should add all operations in one transaction and because of potential concurrency problems we should not lock on account object, but lock DB row itself. 
## This solutions is not appropriate using  


## Using the DB to lock the Accounts as they are in DB (Not the objects in the memory), because if we use ORM there could be 2 different objects corresponding to same DB row, so there will be concurrency issue. 
## Use optimistic locking mechanism, blocking certain amount from the account, waiting transaction to finish and next made the transfer / roll-back the transaction. Your account will able to receive/send money even while certain amount is blocked. 
## Keep Track of all transfers