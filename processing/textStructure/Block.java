package processing.textStructure;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an arbitrary block of text within a file
 */
public class Block implements Serializable {
	public static final long serialVersionUID = 1L;

	private long startIdx;                  //index within the file where the block begins
	private long endIdx;                    //index within the file where the block ends
	private transient RandomAccessFile inputFile;     //the RAF object pointing to the physical file in the file
    // system
	private String fileName;
	private List<String> metaData;

	/**
	 * Constructor
	 * @param inputFile     the RAF object backing this block
	 * @param startIdx      start index of the block within the file
	 * @param endIdx        end index of the block within the file
	 */
	public Block(RandomAccessFile inputFile, long startIdx, long endIdx) {
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.inputFile = inputFile;
		this.fileName = "";
		this.metaData = new ArrayList<> (  );

	}

	///////// getters //////////
	/**
	 * @return start index
	 */
	public long getStartIndex() {
		return startIdx;
	}

	/**
	 * @return  end index
	 */
	public long getEndIndex() {
		return endIdx;
	}

	/**
	 * @return the RAF object for this block
	 */
	public RandomAccessFile getRAF () throws IOException{
		this.inputFile.seek ( this.startIdx );
		return this.inputFile;
	}

	/**
	 * Get the metadata of the block, if applicable for the parsing rule used
	 * @return  list of strings of all metadata.
	 */
	public List<String> getMetadata() {

	    return this.metaData;
	}


	/**
	 * The filename from which this block was extracted
	 * @return  filename
	 */
	public String getEntryName() {
		return this.fileName;
	}

	/**
	 * Convert an abstract block into a string
	 * @return  string representation of the block
	 */
	@Override
	public String toString() {
		//create a buffer for the number of chars in the block
		byte[] buffer = new byte[(int) (this.endIdx - this.startIdx)];

		// read all of the bytes in the block to byte array
		try{
			this.inputFile.seek ( this.startIdx );
			this.inputFile.read(buffer);
		} catch (IOException e) {
			//not supposed to happen
			System.out.println ("not suppposed to happen");
		}


		return new String ( buffer );
	}

	/**
	 * used to set the fileName by the Entry class. it is done so because we can't change the public API. it
	 * is set by the Entry class right after the Block instances are created (parsed)
	 * @param fileName - string of file name.
	 */
	void setFileName(String fileName){
		this.fileName = fileName;
	}

	/**
	 * setter for the metaData
	 * @param metaData - list of strings of metadata pertaining to to text in the block.
	 */
	public void setMetadata(List<String> metaData){
		this.metaData.addAll ( metaData );
	}

    void updateRAF(RandomAccessFile RAF){
	    this.inputFile = RAF;
    }
}

