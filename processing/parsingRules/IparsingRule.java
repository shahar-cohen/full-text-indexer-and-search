package processing.parsingRules;

import processing.textStructure.Block;
import processing.textStructure.WordResult;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;

/**
 * An interface describing the contract any parser should maintain, as well as possible default methods.
 *
 * We changed a bit the given API.
 * The Entry meta Data is dealt with the way we were told to.
 *      but we changed the way we are dealing with the block metaData,
 *      instead of parsing and injecting the metaData of each block in to the block,
 *      we only inject the File Meta-Data to each Block.
 *
 *      and then only if a result is found in a block then we parse the meta-data of that block.
 *
 *      The logic behind this change:
 *            We do not have the #quick query modifier
 *                    => we do not need Indexer-DS's that hold only Meta-Data,
 *                    => we do not need the Meta-data parsed till after the search.
 *                    now we have at most 10 results so parsing their Meta-Data will be very quick
 *                    and will not decrease owr chances to hit the buzzer in time :)
 *
 *                    We changed it back to how it was supposed to be.
 *
 */
public interface IparsingRule extends Serializable {
	enum ParserTypes {SIMPLE, ST_MOVIE, ST_TV}
	int MAXLINELENGTH = 256;



	/**
	 * A parser for a single block of text
	 * @param inputFile A RandomAccessFile from which we are reading
	 * @param startPos  The starting position of the block within the file
	 * @param endPos    The end position of the block within the file
	 * @return          A Block Object.
	 */
	Block parseRawBlock(RandomAccessFile inputFile, long startPos, long endPos);

	/**
	 * A parser for the entire file.
	 * @param inputFile The RandomAccessFile from which we are reading.
	 * @return  A list of Block objects describing the file.
	 */
	List<Block> parsedFile(RandomAccessFile inputFile) throws IOException;

	/**
	 * Print a WordResult object according to the parsing rules.

	 * @param wordResult    The WordResult to be printed
	 * @throws IOException  If the RAF misbehaves.
	 */
	void printResult(WordResult wordResult) throws IOException;


    /**
     * Utility method to create a matcher regex for an arbitrary list of words.
     * @param qWords    an array of Strings representing the query words.
     * @return  A regex String to be compiled into a pattern.
     */
    default String getMatcherRegex(String[] qWords) {
        StringBuilder matchAllWordsRegex = new StringBuilder();
        for (String word : qWords){
            matchAllWordsRegex.append("((").append(word).append(")).*?");
        }
        matchAllWordsRegex.append("\n");
        return matchAllWordsRegex.toString();
    }





}


