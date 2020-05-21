package dataStructures.dictionary;

import processing.textStructure.Block;
import processing.textStructure.Word;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * a class that gathers Word object matching a certain query in a data structure that enables pulling results
 * that are relevant by the query word or the block they partain to.
 */
public class ResultsAggregator {

	// set of WordAndQuery obj
	private HashSet<WordAndQuery> words;

	// Map of block to the query matches found in it.
	private HashMap<Block, HashSet<String>> blockQueryCount;

    private int numOfQueries;

	/**
	 * Constructure for the ResultsAggregator class
	 * @param numOfQueries - int - number of (non stop word) query words.
	 */
	public ResultsAggregator(int numOfQueries){

		this.words = new HashSet<WordAndQuery> (  );

		this.numOfQueries = numOfQueries;

		this.blockQueryCount = new HashMap<>();

	}

	/**
	 * Add a Word object to data structure.
	 * @param word - A word object - represents a word in the text matching a single query word.
	 * @param query - String - single query word
	 */
	public void addWord(Word word, String query){

		if (! this.blockQueryCount.containsKey ( word.getSrcBlk () )){

		    this.blockQueryCount.put ( word.getSrcBlk (), new HashSet<>() );
		}

		this.blockQueryCount.get(word.getSrcBlk()).add(query);

		this.words.add(new WordAndQuery(word, query));
		}

	/**
	 * getter for the set of BlockResultsGroup classes held by ResultsAggregator.
	 * @return Collection<BlockResultsGroup> existingBlocks - set of  BlockResultsGroups
	 */
	public Collection<Block> getAllValidBlocks() {
	    HashSet<Block> validBlockSet = new HashSet<>();

        for (Block block: blockQueryCount.keySet()) {
            if (blockQueryCount.get(block).size() == numOfQueries) { validBlockSet.add(block); }
        }

        return validBlockSet;
	}

	/**
	 * getter for a collection of WordAndQuery objects matching a particular Block obj.
	 * @param block - a block obj
	 * @return HashSet<WordAndQuery>  wordAndQueries
	 */
    public Collection<WordAndQuery> getWordsByBlock(Block block) {
        HashSet<WordAndQuery>  wordAndQueries = new HashSet<>();

        for (WordAndQuery wordQuery: words) {
            if (wordQuery.block == block) {
                wordAndQueries.add(wordQuery); }
        }

        return wordAndQueries;
    }

}
