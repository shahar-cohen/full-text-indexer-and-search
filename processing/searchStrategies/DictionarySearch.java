package processing.searchStrategies;

import dataStructures.dictionary.ResultsAggregator;
import dataStructures.dictionary.WordAndQuery;
import processing.textStructure.Block;
import processing.textStructure.MultiWordResult;
import processing.textStructure.Word;
import processing.textStructure.WordResult;
import utils.MatchesNotFoundException;
import utils.Stemmer;
import utils.Stopwords;

import java.util.*;

/**
 * Dictionary search strategy for text files.
 */
public class DictionarySearch implements IsearchStrategy {

//============================ Constants ===================================================================
	private static final int MAX_RESULTS = 10;
	private final HashMap<Integer, List<Word>> dictionary;
	private final Stemmer STEMMER = new Stemmer ();

//============================ Vars ===================================================================
	private ResultsAggregator aggregator;
	private TreeSet<MultiWordResult> topTenResultCombinations;
	private ArrayList<String> queryList;
	private HashSet<WordAndQuery> wordsFromBlock;
	private int numOfQueries;

	/**
	 * constructor for the class
	 *
	 * @param dict - Hashmap mapping Integers to lists of Word objects.
	 */
	public DictionarySearch(HashMap<Integer, List<Word>> dict) {
		this.dictionary = dict;
	}

	/**
	 * Search function for indexed search files.
	 *
	 * @param query The query string to search for.
	 * @return a list of WordResults objects that represent valid results.
	 * @throws NullPointerException
	 */
	@Override
	public List<? extends WordResult> search(String query) throws NullPointerException {
		// init data structs
		this.queryList = new ArrayList<> ();

		this.wordsFromBlock = new HashSet<>();

		this.topTenResultCombinations = new TreeSet<> ();

		// parse query to single words
		this.processQuery ( query );

		// init data structure for partial results
		this.aggregator = new ResultsAggregator ( this.numOfQueries );


		try {
			// Parse the results to data struct
			this.parseQueryResults ();

			generatePossibleWordResults();

		} catch (Exception e) {
			throw new NullPointerException ( e.getMessage () );
		}

		return new ArrayList<> ( this.topTenResultCombinations );
	}

	/*
	get a list of Word objects in the dictionary that match a certain word
 	*/
	private ArrayList<Word> dictMatchesForQuery(String queryWord) throws NullPointerException {
		// generate a key from the query word
		int stemmedQueryHash = STEMMER.stem ( queryWord ).hashCode ();

		//init list
		List<Word> resultList;

		resultList = this.dictionary.get ( stemmedQueryHash );

		if (resultList == null || resultList.size () <= 0) {
			throw new NullPointerException ( "No results found for at least one query." );
		}

		return new ArrayList<> ( resultList );
	}




	/*
	parse query to single query word stings
	 */
	private void processQuery(String rawQuery) {
		String[] rawQueryWords = rawQuery.toLowerCase ().trim ().split ( " " );

		for (String queryWord : rawQueryWords) {
			if (!Stopwords.isStopword ( queryWord )) {
				this.queryList.add ( queryWord );
			}
		}

		this.numOfQueries = queryList.size ();
	}

	/*
	parse the results to resultAggregator
	 */
	private void parseQueryResults() throws Exception {

		for (String queryWord : this.queryList) {

			ArrayList<Word> queryResults = dictMatchesForQuery ( queryWord );

			if (queryResults.isEmpty ()) {
				throw new MatchesNotFoundException ( "parsing single query results." );
			}

			for (Word word : queryResults) {
				this.aggregator.addWord ( word, queryWord );
			}
		}
	}

    private void generatePossibleWordResults() throws Exception{
        HashSet<Block> validBlocks = new HashSet<>(this.aggregator.getAllValidBlocks());

        for (Block block : validBlocks) {
            this.wordsFromBlock = new HashSet<>(aggregator.getWordsByBlock(block));

            addPossiblePermutationsToSet(new Word[numOfQueries], 0);
        }
    }

	/*
	recursively add permutation of word combinations (one from each query) to data sturcture.
	 */
	private void addPossiblePermutationsToSet(Word[] words, int queryNumber) throws Exception {
		// stopping condition
		if (queryNumber == this.numOfQueries) { this.addPossibleResult ( words );
			return;
		}

		for (Word word : matchesOfQueryNumber ( queryNumber )) {
			words[queryNumber] = word;

			addPossiblePermutationsToSet ( words, queryNumber + 1 );
		}
	}


	// add permutation to multiwordresult tree
	private void addPossibleResult(Word[] words) throws Exception {
		MultiWordResult multiWordResult = this.generateMultiWordResult ( words );

		this.topTenResultCombinations.add ( multiWordResult );

		if (this.topTenResultCombinations.size () > MAX_RESULTS) {
			this.topTenResultCombinations.pollLast ();
		}
	}

	// generate multiwordresult
	private MultiWordResult generateMultiWordResult(Word[] words) throws Exception {
		long[] indexArray = new long[this.numOfQueries];

		for (int i = 0; i < this.numOfQueries; i++) {
			indexArray[i] = words[i].getEntryIndex () - words[i].getSrcBlk().getStartIndex ();
		}

		return new MultiWordResult ( this.queryList.toArray ( new String[0] ), words[0].getSrcBlk (), indexArray );
	}

	/*
	get a set of Word objects matching a number given to each query word.
	 */
	private HashSet<Word> matchesOfQueryNumber(int queryNumber) throws Exception {

		HashSet<Word> resultByQuery = new HashSet<> ();

		String query = this.queryList.get ( queryNumber );

		for (WordAndQuery wordAndQuery: wordsFromBlock) {
            if (wordAndQuery.query.equals(query)) {
                resultByQuery.add(wordAndQuery.word);
            }
        }
		return resultByQuery;
	}


}
