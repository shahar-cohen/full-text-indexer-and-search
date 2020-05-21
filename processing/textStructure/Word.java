package processing.textStructure;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Wrapper class for a single word containing relevant attributes for distance calculation and string extraction from
 * the containing block or file.
 */
//this class is being used for storing word result in the preproccessing step
//	i.e. the "indexer" in the dictionary approach
public class Word implements Serializable {
	public static final long serialVersionUID = 1L;

	/**
	 * A reference for the contaning Block object.
	 */
	private final Block srcBlk;
	/**
	 * The offset of the word within the block
	 */
	private final long srcBlkOffset;
	/**
	 * Length of the word
	 */
	private final int length;
	/**
	 * Hash of the word - for quick dictionary querying without unnecessary extraction and conversion.
	 */
	private final int wordHash;

	private String extractedWord;

	/**
	 * The constructor.
	 *
	 * @param source   The Block where this word resides.
	 * @param startIdx The offset within the block where the word starts.
	 * @param endIdx   The  offset within the block where the word ends.
	 */
	public Word(Block source, long startIdx, long endIdx) throws Exception {
		this.srcBlk = source;
		this.srcBlkOffset = startIdx;
		this.length = (int) (endIdx - startIdx);
		this.wordHash = extractWord().hashCode();
		this.extractedWord = null;
	}

	/**
	 * Simple getter
	 *
	 * @return The source block
	 */
	public Block getSrcBlk() {
		return this.srcBlk;
	}

	/**
	 * Get the actual string of the word from within the block.
	 *
	 * @return The word in String format.
	 */
	protected String extractWord() throws Exception{

		if (extractedWord == null) {

			try {
				RandomAccessFile randomAccessFile = this.srcBlk.getRAF ();

				randomAccessFile.seek ( this.srcBlkOffset + this.srcBlk.getStartIndex ());

				String readLine = (randomAccessFile.readLine ());

				this.extractedWord = readLine.substring ( 0,  this.length );


			} catch (IOException e) {
				throw new IOException ( "File could not be read due to IO error." );
			} catch (NullPointerException e) {
				throw new NullPointerException ( "Attempted to read file into null byte array." );
			} catch ( IndexOutOfBoundsException e) {
				throw new IndexOutOfBoundsException ( "Attempted to read file with inappropriate index or " +
														"size of array");
			}

		}

		return this.extractedWord;
	}

	/**
	 * Get the hashCode of the word
	 *
	 * @return The wordHash.
	 */
	public int getHash() {
		return this.wordHash;
	}

	/**
	 * The source block offset within the file + the offset of the word within the block = offset within an
	 * entry!
	 *
	 * @return offset within the entire FILE where the word resides.
	 */
	public long getEntryIndex() {
		return this.srcBlk.getStartIndex() + this.srcBlkOffset;
	}

	/**
	 * getter for the string representation of the Word as found in the text.
	 * @return
	 */
	@Override
	public String toString() {
		try{
			return (extractWord ());
		} catch (Exception e){
			return "";
		}

	}

}
