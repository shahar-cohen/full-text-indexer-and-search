package dataStructures.dictionary;

import processing.textStructure.Block;
import processing.textStructure.Word;

/**
 * A class that stores a Word Object and query string that match eachother and the block they belong to.
 */
public class WordAndQuery {

	public Word word;

	public String query;

	public Block block;


	/**
	 * constructor for a WordAndQuery object
     *
	 * @param word - A word object - represents a word in the text matching a single query word.
	 * @param query - String - single query word
	 */
	WordAndQuery(Word word, String query){
		this.word = word;
		this.query = query;
		this.block = word.getSrcBlk();
	}

}
