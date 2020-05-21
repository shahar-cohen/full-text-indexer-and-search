import dataStructures.Aindexer;
import dataStructures.IndexerFactory;
import processing.parsingRules.IparsingRule;
import processing.searchStrategies.IsearchStrategy;
import processing.textStructure.Corpus;
import processing.textStructure.WordResult;
import utils.MatchesNotFoundException;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Scanner;

/**
 * The main program - A text searching module that indexes and queries large corpses for
 * strings or word groups.
 */
public class TextSearcher {

	static final String EMPTY_STRING = "";

    static final String EXCEPTION_MSG_WRONG_TOKEN =
            "Expected %s token received %s ";
    static final String EXCEPTION_INCORRECT_NUM_OF_ARGS =
            " incorrect number of arguments, should be only one ";

    static final String TITLE_WHEN_PRINTING_RESULTS =
            "The top %d results for query \"%s\" are:";

    /** Each entry starts with a separator line of 256 ”=” signs */
    static final String ENTRY_SEPERATOR = "=".repeat( 256 );

    /** The arguments file's pattern is IndexerTypeIdentifier \n requested IndexerTypeIdentifier
     *          used by initIndexer()   */
    enum IndexerTypeIdentifier {CORPUS, INDEXER, PARSE_RULE, QUERY}

    private Aindexer indexer;
    private String query;

    private void initIndexer(String filePath) throws IOException {

        Scanner argsFile = new Scanner(new File ( filePath ) );

        String corpusPath = getArgumentName( argsFile, IndexerTypeIdentifier.CORPUS );
        String indexerName = getArgumentName( argsFile, IndexerTypeIdentifier.INDEXER );
        String parserName = getArgumentName( argsFile, IndexerTypeIdentifier.PARSE_RULE );

        // optional
        if ( argsFile.hasNextLine() ){
            this.query = getArgumentName( argsFile, IndexerTypeIdentifier.QUERY);
        }
        this.indexer = IndexerFactory.getIndexer( new Corpus(corpusPath, parserName), indexerName );

    }

    /** A helper of initIndexer, parses the arguments. the first line is the type the second the what
     *      we probably should have used Regex to get some more practice */
    private String getArgumentName( Scanner arguments, IndexerTypeIdentifier expectedIdentifier) throws IllegalArgumentException {

        String expected = expectedIdentifier.name();
        String receivedToken = arguments.nextLine().trim();

        if ( !receivedToken.equals( expected ) ) {
            throw new IllegalArgumentException(String.format(EXCEPTION_MSG_WRONG_TOKEN,
                                                            expected, receivedToken));
        }

        return arguments.nextLine().trim();
    }

    void run( String pathToSearchParametersFile ) throws Exception{


        initIndexer( pathToSearchParametersFile );

        // init the search DS
        this.indexer.index();

        if ( this.query != null && this.query != EMPTY_STRING ){

            // gets the searcher of the Indexed DS
            IsearchStrategy searcher = this.indexer.asSearchInterface();

            // search for wanted query and return a list of the results
            List<? extends WordResult> results = searcher.search(this.query);

            System.out.println(String.format( TITLE_WHEN_PRINTING_RESULTS, results.size(), this.query));

            IparsingRule rule = this.indexer.getParseRule();



            for (WordResult result : results) {
                System.out.println(ENTRY_SEPERATOR);
                rule.printResult( result ); }



        } else {throw new MatchesNotFoundException ("search of empty query."); }

    }


    /** Used when args file is not in the correct format and when the program encounters IO.
     * Does not deal with problematic txt formats, expects them to be as described in the definition
     * exercise.
     *  */
    private static void exitGracefully(String message) {
        System.out.println( message );
        System.exit( -1 );
    }



    /**
     * Main method. Reads and parses a command file and if a query exists, prints the results.
     * @param args
     */
    public static void main(String[] args) {

        if ( args != null && ( args.length == 1 ) ){

            TextSearcher search = new TextSearcher();

            try {
                search.run( args[0]);
            } catch (Exception e) {
            	exitGracefully(e.getMessage()); }
        }
        else{
            exitGracefully(EXCEPTION_INCORRECT_NUM_OF_ARGS);
        }

    }
}
