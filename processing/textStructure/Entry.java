package processing.textStructure;

import processing.parsingRules.IparsingRule;
import processing.textStructure.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a single file within a Corpus.
 * The Blocks are injected through populate() (Not through the constructor)
 * It basically represents just a container for the blocks.
 */
public class Entry implements Iterable<Block>, Serializable {
	public static final long serialVersionUID = 1L;

    private final List<Block> blocks;
    private final String entryFilePath;
    private final IparsingRule parsingRule;
    private transient RandomAccessFile randomAccessFile;


    /**
	 * Main constructor - expects a valid filepath as corpus already checked it.
	 * @param filePath  The path to the file this entry represents
	 * @param parseRule The parsing rule to be used for this entry
	 */
    public Entry(String filePath, IparsingRule parseRule) {
    	this.entryFilePath = filePath;
    	this.blocks = new ArrayList<Block> (  );
    	this.parsingRule = parseRule;

    }

    /** Used to order the Entry to initialize the blocks by using the parsing rule. */
    void populate() throws IOException {
        this.createRAF();

        this.blocks.addAll ( this.parsingRule.parsedFile ( this.randomAccessFile ) );

        for (Block block : this.blocks) { block.setFileName ( this.entryFilePath ); }
	}


    /**
     * Iterate over Block objects in the Entry
     * @return  A Block object iterator
     */
    @Override
    public Iterator<Block> iterator() {
        return blocks.iterator();
    }

    /** If the Entry is Deserialize then the RAF needs to be injected back into the Entry.
     * ( beacuse it is not serializable */
    void updateRAF() throws IOException{
		createRAF();
		blocks.forEach(block -> block.updateRAF(this.randomAccessFile));
    	}


    String getEntryFilePath() {
        return entryFilePath;
    }

    /** initiates an RAF from the path */
    private void createRAF() throws FileNotFoundException{ this.randomAccessFile = new RandomAccessFile ( this.entryFilePath , "r"); }


}

