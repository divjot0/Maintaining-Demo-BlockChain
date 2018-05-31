import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {
	
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool myPool;	

    public TxHandler(UTXOPool utxoPool) {
		myPool = new UTXOPool(utxoPool);
    }

	public UTXOPool getUTXOPool() {
		return myPool;
    }


    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        int oIndex = 0;
        int iIndex = 0;
        double iSum = 0;
        double oSum = 0;
        for (Transaction.Input in : inputs) {
            byte[] prevTxHash = in.prevTxHash;
            UTXO utxo = new UTXO(prevTxHash, in.outputIndex);
            if (!myPool.contains(utxo)) {
            	return false;
            }
            Crypto check = new Crypto();
            if (!check.verifySignature(myPool.getTxOutput(utxo).address, tx.getRawDataToSign(iIndex), inputs.get(iIndex).signature)) {
            	return false;
            }
            iIndex++;
            iSum = iSum + myPool.getTxOutput(utxo).value;
        }
        iIndex = 0;
        for (Transaction.Input in : inputs) {
            byte[] prevTxHash = in.prevTxHash;
            UTXO utxo = new UTXO(prevTxHash, in.outputIndex);
            for (int j=iIndex+1; j<inputs.size(); j++) {
            	UTXO u = new UTXO(inputs.get(j).prevTxHash, inputs.get(j).outputIndex);
            	if (u.equals(utxo)) {
            		return false;
            	}
            }
            iIndex++;
        }

        for (Transaction.Output op : outputs) {
        	if (op.value<0) {
        		return false;
        	}
        	oSum = oSum + op.value;
        }

        if (oSum > iSum) {
        	return false;
        }

    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	ArrayList<Transaction> valid = new ArrayList<Transaction>();
        for (int i=0; i<possibleTxs.length; i++) {
        	if (isValidTx(possibleTxs[i]))
        	{
        		possibleTxs[i].finalize();
        		byte[] hash = possibleTxs[i].getHash();
        		ArrayList<Transaction.Input> inputs = possibleTxs[i].getInputs();
		        ArrayList<Transaction.Output> outputs = possibleTxs[i].getOutputs();
		        int oIndex = 0;
		        int iIndex = 0;
		        double iSum = 0;
		        double oSum = 0;
		        for (Transaction.Input in : inputs) {
		            byte[] prevTxHash = in.prevTxHash;
		            UTXO utxo = new UTXO(prevTxHash, in.outputIndex);
		            myPool.removeUTXO(utxo);
		       	}
		        for (Transaction.Output op : outputs) {
		        	UTXO ut = new UTXO(hash, oIndex);
		        	myPool.addUTXO(ut, op);
		        	oIndex++;
		        }
		        valid.add(possibleTxs[i]);
        	}
        }
        Transaction[] accepted = new Transaction[valid.size()];
        int i = 0;
        for (Transaction sb : valid)
            accepted[i++] = sb;
        return accepted;
    }
}
