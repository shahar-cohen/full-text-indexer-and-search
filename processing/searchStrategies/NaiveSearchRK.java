package processing.searchStrategies;

import processing.textStructure.Block;
import processing.textStructure.Corpus;
import processing.textStructure.Entry;
import processing.textStructure.WordResult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * a class representing a naive search strategy based on RK algorithm.
 */
public class NaiveSearchRK extends NaiveSearch {

    private static final int NUMBER_OF_WANTED_RESULTS = 10;

	/**
	 * Constuctor for the NaiveSearchRK class
	 * @param origin
	 */
	public NaiveSearchRK(Corpus origin) {
        super(origin);
    }

	/**
	 * search a query and get a list of WordResult objects.
	 * @param query The query string to search for.
	 * @return a list of WordResult objects.
	 */
	@Override
    public List<WordResult> search(String query) {

        List<WordResult> results = new ArrayList<>();

        //get entry iterator from corpus
        Iterator<Entry> entryIterator = origin.iterator ();

        //loop over entries
        while ( entryIterator.hasNext () && results.size() < NUMBER_OF_WANTED_RESULTS ) {

            //get next entry
            Entry entry = entryIterator.next ();

            //get block iterator from entry:
            Iterator<Block> blockIterator = entry.iterator ();

            //loop over blocks
            while (blockIterator.hasNext ()) {
                Block currentBlock = blockIterator.next ();


                searchBlock(currentBlock, results , query);
            }
        }

        return results;
    }

	/*
	search in a certain block
	 */
    private void searchBlock(Block blk, List<WordResult> results, String query) {

        char[] queryCharList = query.toCharArray();
        char[] blockCharArray = blk.toString().toCharArray();

        int querySize = queryCharList.length;
        int blockCharNum = blockCharArray.length;

        long prime = getBiggerPrime(querySize);

        long base = 1;
	    /**
	     * Implement the base calculation?
	     */
	    for (int i = 0; i < querySize - 1; i++) {
            base *= 2;
            base = base % prime;
        }

        long[] rolLHashArr = new long[blockCharNum];
        rolLHashArr[0] = 0;

        long queryFingerPrint = 0;

	    /**
	     * Implement the fingerprint calculation?
	     */
	    for (int j = 0; j < querySize; j++) {
            rolLHashArr[0] = (2 * rolLHashArr[0] + blockCharArray[j]) % prime;
            queryFingerPrint = (2 * queryFingerPrint + queryCharList[j]) % prime;
        }


        int i;  // = 0
        boolean passed; // = false

        int diff = blockCharNum - querySize;
        for (i = 0; i <= diff; i++) {
            if (rolLHashArr[i] == queryFingerPrint) {
                passed = true;
                for (int k = 0; k < querySize; k++) {
                    if (blockCharArray[i + k] != queryCharList[k]) {
                        passed = false;
                        break;
                    }
                }

                if (passed) {
                    results.add(new WordResult(blk, new String[]{query}, i));
                }
            }

            if (i < diff) {
                long value = 2 * (rolLHashArr[i] - base * blockCharArray[i]) + blockCharArray[i + querySize];
                rolLHashArr[i + 1] = ((value % prime) + prime) % prime;
            }
        }

    }
	/*
	getter for bigger prim (long)
	 */
    private static long getBiggerPrime(int m) {
        BigInteger prime = BigInteger.probablePrime(getNumberOfBits(m) + 1, new Random());
        return prime.longValue();
    }
	/*
	getter for number of bits (int)
		 */
    private static int getNumberOfBits(int number) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(number);
    }
}
