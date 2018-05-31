# Maintaining-Demo-BlockChain
In this work I have implemented a node that’s part of a block-chain-based distributed consensus 
protocol. Specifically, the code will receive incoming transactions and blocks and maintain an
updated block chain.

#Assumptions
● A new genesis block won’t be mined. If you receive a block which claims to be a genesis block
(parent is a null hash) in the ​ addBlock(Block b) ​ function, you can return ​ false ​ .
● If there are multiple blocks at the same height, return the oldest block in
getMaxHeightBlock() ​ ​ function.
● Assume for simplicity that a coinbase transaction of a block is available to be spent in the next
block mined on top of it. (This is contrary to the actual Bitcoin protocol when there is a
“maturity” period of 100 confirmations before it can be spent).
● Maintain only one global Transaction Pool for the block chain and keep adding transactions to
it on receiving transactions and remove transactions from it if a new block is received or
created. It’s okay if some transactions get dropped during a block chain reorganization, i.e.,
when a side branch becomes the new longest branch. Specifically, transactions present in the
original main branch (and thus removed from the transaction pool) but absent in the side
branch might get lost.
● The coinbase value is kept constant at 25 bitcoins whereas in reality it halves roughly every 4
years and is currently 12.5 BTC.
● When checking for validity of a newly received block, just checking if the transactions form a
valid set is enough. The set need not be a maximum possible set of transactions. Also, you
needn’t do any proof-of-work checks.
