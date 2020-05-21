package dataStructures.naive;

import processing.textStructure.Corpus;

/**
 * public class of a naive search Indexer for the naive search strategy based on Rabin Karp algorithm.
 */
public class NaiveIndexerRK extends NaiveIndexer {

    public static final IndexTypes TYPE_NAME = IndexTypes.NAIVE_RK;

    public NaiveIndexerRK(Corpus corpus) {
        super(corpus, true);
    }

}
