package processing.textStructure;

import utils.Stemmer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.*;

/**
 * This class defines a query result for multiple non-consecutive words.
 */
public class MultiWordResult extends WordResult implements Comparable<MultiWordResult> {

    private static final String ANSI_RESET = " \u001B[0m ";
	private static final String ANSI_GREEN = " \u001B[32m ";
	private static final String SPACING = "			";
	private static final String NEW_LINE = "\n";
	private static final int FIRST_INDEX = 0;

	private long[] sortedWordEndings;
	private long blockOffset;
	private long[] wordPositions;
	private int confidence;

	private MultiWordResult(Block blk, String[] query, long idx) {
		super(blk, query, idx);
		this.blockOffset = idx;
	}

	/**
	 * Constructor
	 * @param query The list of query words
	 * @param block The block where this result came from
	 * @param locs  The indices of the words in the block
	 */
	public MultiWordResult(String[] query, Block block, long[] locs) throws Exception {
		this(block, query, getBlockOffset (locs));

		this.wordPositions = locs;

		Arrays.sort ( this.wordPositions );

		this.sortedWordEndings = this.wordPositions.clone ();

		this.calcConfidence ();
	}


	/**
	 * Calculate the confidence level of a result, defined by the sum of word distances.
	 * If i could change the public API i would have made the constructor of this class receive word objects
	 * so it would be easier to mannage
	 */
	private void calcConfidence() throws Exception {
		long sum = 0;

		long blockAbsoluteLocation = this.location.getStartIndex ();

		int prevWordEnd = (int) this.wordPositions[FIRST_INDEX];


		RandomAccessFile blockRAF = this.location.getRAF ();

		// for each Index of word beginning check where the word ends and calculate the distance to the next
		// word -----I SWEAR i would have used RandomAccessFile methods like .read() if they had worked for
		// me---
		for(int i = 0 ; i < this.wordPositions.length; i++) {

			long wordPosition = this.wordPositions[i];

			blockRAF.seek ( blockAbsoluteLocation + wordPosition );

			String s = blockRAF.readLine ();

			int wordLength = s.split ( " " )[FIRST_INDEX].length ();

			sum += ((int) wordPosition) - prevWordEnd;

			prevWordEnd = ((int) wordPosition) + wordLength;

			this.sortedWordEndings[i] = prevWordEnd;

		}

		this.confidence = (int) sum;
	}

	/**
	 * Comparator for multy-word results
	 * @param o The other result to compare against
	 * @return  int representing comparison result, according to the comparable interface.
	 */
	@Override
	public int compareTo(MultiWordResult o) {
		int result = this.confidence - o.confidence;

		int otherResult = (int) (this.getBlock().getStartIndex() - o.getBlock().getStartIndex()) ;

		return (result == 0 ? otherResult : result);
	}


	/**
	 * Extract a string that contains all words in the multy-word-result
	 * This should be a sentance starting at the word with the minimal location (index) and ending
	 * at the first line-break after the last word
	 * EVEN COLORS THE WORDS IN GREEN :)
	 * @return  A piece of text containing all query words
	 */
	@Override
	public String resultToString() throws IOException {

		long lastEnd =  this.sortedWordEndings[this.wordPositions.length - 1];

		String beginningSubString = (this.getBlock().toString()).substring(0, (int) lastEnd);

		String[] split = beginningSubString.substring((int) lastEnd).split(NEW_LINE);

		String endSubstring = split[split.length - 1];

		if (beginningSubString.endsWith(NEW_LINE)) {endSubstring = "";}

		StringBuilder resultBuilder = new StringBuilder();

		resultBuilder.append(beginningSubString);

		for(int i = (wordPositions.length - 1) ; i >= 0 ; i--){
			resultBuilder.insert ( (int) (this.sortedWordEndings[i]), ANSI_RESET );
			resultBuilder.insert ( (int) (this.wordPositions[i]), ANSI_GREEN );
		}
		String result = resultBuilder.toString().substring((int) blockOffset);

		Stemmer s = new Stemmer();


		return SPACING + (resultBuilder.toString()).substring((int) blockOffset);
	}

	/*
	getter for the smallest (long) index in the list received in the constructor
	did this as i wanted to make this class work even if the indices weren't sorted.
	 */
	private static long getBlockOffset(long[] longList){
		long smallestNum = longList[FIRST_INDEX];

		for (long num: longList) { if (smallestNum > num) { smallestNum = num;} }

		return smallestNum;
	}

	/*
	getter for the absolute index in the file (long) by a relative idex in the block (long).
	 */
	private long absoluteIndex(long relativeIndex) {return this.location.getStartIndex () + relativeIndex; }

}
