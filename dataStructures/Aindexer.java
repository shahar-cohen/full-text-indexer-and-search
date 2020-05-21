package dataStructures;

import processing.parsingRules.IparsingRule;
import processing.searchStrategies.IsearchStrategy;
import processing.textStructure.Corpus;
import utils.MatchesNotFoundException;
import utils.WrongMD5ChecksumException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The abstract class describing the necessary methods and common implementations of all indexing data
 * structures.
 * @param <T>   The search stratagy used by this indexing engine. Can be any class that implements the
 *           IsearchStrategy interface.
 */
public abstract class Aindexer<T extends IsearchStrategy> {
	public static final long serialVersionUID = 1L;


	public static enum IndexTypes {DICT, NAIVE, NAIVE_RK, SUFFIX_TREE}
    IndexTypes dataStructType;
    protected Corpus origin;


	/**
	 * Basic constructor accepting origin Corpus
	 * @param origin    The corpus indexed in this DS.
	 */
	public Aindexer(Corpus origin){
        this.origin = origin;
    }


	/**
	 * Main indexing method. Common implementation trying to read indexed cache file
	 * This method can be edited, but is enough assuming the other methods are implemented well.
	 */
	public void index() throws IOException, MatchesNotFoundException {
    	try {
			readIndexedFile();
			origin.updateRAFs();
			System.out.println("success on reading index from file");
		} catch (FileNotFoundException | WrongMD5ChecksumException e) {
			origin.populate();
            indexCorpus();
            writeIndexFile();
            System.out.println("success on indexing file");

        }
    	
    }

	/**
	 * get the backing search interface.
	 * @return  The search interface implementation used by this indexer.
	 */
	public abstract T asSearchInterface();

	/**
	 * Index the Corpus internally using the parsing rules defined for this datastructure.
	 */
    protected abstract void indexCorpus() throws MatchesNotFoundException;

	/**
	 * Try to read a cached index file if one already exists.
	 * @throws FileNotFoundException
	 * @throws WrongMD5ChecksumException
	 */
	protected abstract void readIndexedFile() throws IOException, WrongMD5ChecksumException;


	/**
	 * Getter for the cached index file path.
	 * @return  the path to the cached index file.
	 */
	protected String getIndexedPath() { return this.origin.getPath (); }

	/**
	 * Write the indernal index into file.
	 */
	protected abstract void writeIndexFile();



	/**
	 * Extract the parsing rule used for indexing this data structure.
	 * @return  an instance of a parser implementing IparsingRule
	 */
	public abstract IparsingRule getParseRule();


	/**
	 * simple getter
	 * @return  Regerence to the origin Corpus
	 */
	public Corpus getCorpus(){return  this.origin;}
}
