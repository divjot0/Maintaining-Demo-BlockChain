// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BlockChain {
	public HashMap<byte[],TxHandler> hm;
	public HashMap<byte[],Integer> hm2;  
    public static final int CUT_OFF_AGE = 10;
	public UTXOPool maxHeightPool;
    public TransactionPool txPool;
	public int maxHeight;
	public int rootHeight;
	public Block maxHeightBlock;
    /**
     * create an empty block chain with just a genesis block. Assume {@cod genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
		hm = new HashMap<byte[],TxHandler>();
		hm2 = new HashMap<byte[],Integer>();
		rootHeight = 1;
		maxHeight = 1;
		int oIndex = 0;
		UTXOPool myPool = new UTXOPool();
		maxHeightPool = new UTXOPool();
		txPool = new TransactionPool();
		TxHandler txh = new TxHandler(myPool);
		
		ArrayList<Transaction> possibleTxs = genesisBlock.getTransactions();
		possibleTxs.add(genesisBlock.getCoinbase());
		for (int i=0; i<possibleTxs.size(); i++) {
			possibleTxs.get(i).finalize();
       		byte[] hash = possibleTxs.get(i).getHash();	        
			ArrayList<Transaction.Output> outputs = possibleTxs.get(i).getOutputs();
	        for (Transaction.Output op : outputs) {
	        	UTXO ut = new UTXO(hash, oIndex);
	        	myPool.addUTXO(ut, op);
	        	oIndex++;
	        }
	    }
		ArrayList<Transaction> txs = genesisBlock.getTransactions();
		Transaction txss[]=txs.toArray(new Transaction[txs.size()]);

		maxHeightPool = new UTXOPool(myPool);
		Transaction ts[] = txh.handleTxs(txss);	

		maxHeightBlock = genesisBlock;
		TxHandler t = new TxHandler(maxHeightPool);
		hm.put(genesisBlock.getHash(), t);
		hm2.put(genesisBlock.getHash(), 1);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
		return maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
		ArrayList<Transaction> txs = txPool.getTransactions();
		TransactionPool tPool = new TransactionPool();
		TxHandler txh = hm.get(maxHeightBlock.getHash());
		UTXOPool pool = txh.getUTXOPool();
		UTXOPool pool2 = new UTXOPool(pool);
		Transaction txss[]=txs.toArray(new Transaction[txs.size()]);
		Transaction[] ac = txh.handleTxs(txss);
		for (int i=0; i<ac.length; i++) {
			tPool.addTransaction(ac[i]);
		}
		
		TxHandler th = new TxHandler(pool2);
		hm.put(maxHeightBlock.getHash(), th);
        return tPool;
    }

    /**
     * Add {@cod block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@cod height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@cod
     * CUT_OFF_AGE + 1}. As soon as {@cod height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
		if (block.getPrevBlockHash()==null)
			return false;
		TxHandler txhandler = hm.get(block.getPrevBlockHash());
		if (!hm.containsKey(block.getPrevBlockHash())) {
			return false;
		}
		int num = hm2.get(block.getPrevBlockHash());	
		if (maxHeight-num-1>=CUT_OFF_AGE)
			return false;
		ArrayList<Transaction> txs = block.getTransactions();
		UTXOPool pool = new UTXOPool();
		pool = txhandler.getUTXOPool();
		UTXOPool pool2 = new UTXOPool(pool);
//		txs.add(block.getCoinbase());
/*		for (Transaction tx: txs) {
			if(!txhandler.isValidTx(tx))
				return false;
		}
*/		
		
		Transaction txss[]=txs.toArray(new Transaction[txs.size()]);
		UTXOPool Pool = new UTXOPool();
		Pool = txhandler.getUTXOPool();
		TxHandler txh = new TxHandler(pool);
		Transaction ts[] = txh.handleTxs(txss);
		ArrayList<Transaction> tss = new ArrayList<Transaction>(Arrays.asList(ts));
		for (Transaction div : txss) {
			if (!tss.contains(div)) {
				TxHandler th = new TxHandler(pool2);
				hm.put(block.getPrevBlockHash(), th);
//				System.out.println("Bloc");
				return false;
			}
		}		
		UTXOPool fina = new UTXOPool(txh.getUTXOPool());
		UTXO ut = new UTXO(block.getCoinbase().getHash(), 0);
		fina.addUTXO(ut, block.getCoinbase().getOutputs().get(0));
		txh = new TxHandler(fina);
		hm.put(block.getHash(), txh);
		hm2.put(block.getHash(), num+1);
		if(num+1>maxHeight) {
			maxHeight = num+1;
			maxHeightPool = new UTXOPool(fina);
			maxHeightBlock = block;
		}
		
		TxHandler th = new TxHandler(pool2);
		hm.put(block.getPrevBlockHash(), th);
		return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    }
}
