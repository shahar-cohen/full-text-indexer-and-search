package dataStructures.naive;

import dataStructures.Aindexer;
import processing.parsingRules.IparsingRule;
import processing.searchStrategies.NaiveSearch;
import processing.searchStrategies.NaiveSearchRK;
import processing.textStructure.Corpus;
import utils.MD5;
import utils.WrongMD5ChecksumException;

import java.lang.invoke.WrongMethodTypeException;

/**
 * A "naive" indexer. This approach forgoes actually preprocessing the file, and simply loads the text and searches directly on it.
 */
public class NaiveIndexer extends Aindexer<NaiveSearch> {

	public static final IndexTypes TYPE_NAME = IndexTypes.NAIVE;
	private final boolean isRK;

	/**
	 * Basic constructor
	 * @param corpus    The corpus to search over
	 * @param RK        Whether or not to use Rabin-Karp search strategy
	 */
	public NaiveIndexer(Corpus corpus, boolean RK){
		super(corpus);
		this.isRK = RK;
	}


	/**
	 * Basicer constructorer
	 * @param corpus    The corpus to search over
	 */
	public NaiveIndexer(Corpus corpus) {
		super(corpus);
		this.isRK = false;
	}

	@Override
	protected void indexCorpus() {
		// does nothing
	}

	@Override
	protected void readIndexedFile() throws WrongMD5ChecksumException {
		throw new WrongMD5ChecksumException ();
	}

	@Override
	protected void writeIndexFile() {
		//does nothing
	}

	/**
	 * getter for the Corpus obj
	 * @return corpus
	 */
	public Corpus getOrigin() {
		return this.origin;
	}

	/**
	 * getter for the IparsingRule obj
	 * @return IparsingRule
	 */
	@Override
	public IparsingRule getParseRule() {
		return this.origin.getParsingRule();
	}

	/**
	 * getter for the NaiveSearch obj
	 * @return NaiveSearch
	 */
	@Override
	public NaiveSearch asSearchInterface() {
		return this.isRK ? new NaiveSearchRK(this.origin): new NaiveSearch(this.origin);
    }


}
