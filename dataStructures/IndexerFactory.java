package dataStructures;

import dataStructures.dictionary.DictionaryIndexer;
import dataStructures.naive.NaiveIndexer;
import dataStructures.naive.NaiveIndexerRK;
import processing.textStructure.Corpus;

import static dataStructures.Aindexer.*;

/**
 * A factory class for AIndexer objects
 */
public class IndexerFactory {

	public static final String INDEXER_NOT_ADDED_TO_FACTORY_MSG =
			" It Seems As if the new indexer type was not added to the SwitchCase";


	/**Returns wanted Indexer.
	 * @param indexerName the name must be exactly the enum that represents the wanted indexer.
	 * @return Notice the two reasons that this method can throw IllegalArgumentException.
	 *      1. the wanted enum does not exist (and hopefully their for the wanted Paring Rule does not exist)
	 *      2. the wanted enum exists but was not updated here in the factory.
	 *      ( basically the enum system seems cool but not easily extendable )
	 */
	public static Aindexer getIndexer(Corpus corpus, String indexerName) {


		IndexTypes wantedEnum = IndexTypes.valueOf(indexerName);

		Aindexer newIndexer;

		switch (wantedEnum) {

			case NAIVE:
				newIndexer = new NaiveIndexer (corpus);
				break;

			case NAIVE_RK:
				newIndexer = new NaiveIndexerRK (corpus);
				break;

			case DICT:
				newIndexer = new DictionaryIndexer (corpus);
				break;

			default:
				throw new IllegalArgumentException( INDEXER_NOT_ADDED_TO_FACTORY_MSG);

		}
		return newIndexer;
	}

}

