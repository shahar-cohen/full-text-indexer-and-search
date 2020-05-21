package processing.textStructure;

import java.io.IOException;
import java.io.RandomAccessFile;



/**
 * This wrapper class describes a query result for a single word.
 */
public class WordResult {
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_GREEN = "\u001B[32m";
	/**
	 * Fields
	 */
	private long idxInBlk;  // The offset of the word within the block
	Block location;       // The block in which this word was found
	protected String[] content; // The word(s) that were found

	/**
	 * private constructor for word result
	 * @param loc - Block - the block of the word result
	 * @param word - a list of strings containing the matching word(s)
	 */
	private WordResult(Block loc, String[] word) {
		this.location = loc;
		this.content = word;
	}

	/**
	 * public constructor for wordresult
	 * @param blk Block - the block of the word result
	 * @param word a list of strings containing the matching word(s)
	 * @param idx long - offset of the result in the block
	 */
	public WordResult(Block blk, String[] word, long idx) {
		this ( blk, word );
		this.idxInBlk = idx;
	}

	/**
	 * get the block of this word result
	 * @return Block - the block of this wordresult
	 */
	public Block getBlock() {
		return this.location;
	}

	/**
	 * get the list of strings of words in the result
	 * @return a list of strings containing the matching word(s)
	 */
	public String[] getWord() {
		return this.content;
	}

	/**
	 * return a string representing the result
	 * @return a string representing the result
	 * @throws IOException - in case the file doesn't exist.
	 */
	public String resultToString() throws IOException {
		RandomAccessFile blockRAF = this.location.getRAF ();

		blockRAF.seek ( this.idxInBlk );

		StringBuilder s = new StringBuilder();

		s.append(blockRAF.readLine ());

		s.insert( (this.content[0].length()), ANSI_RESET);

		s.insert(0, ANSI_GREEN);

		return s.toString();
	}
}