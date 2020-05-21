package dataStructures.dictionary;

import dataStructures.Aindexer;
import processing.parsingRules.IparsingRule;
import processing.searchStrategies.DictionarySearch;
import processing.textStructure.Block;
import processing.textStructure.Corpus;
import processing.textStructure.Entry;
import processing.textStructure.Word;
import utils.Stemmer;
import utils.Stopwords;
import utils.WrongMD5ChecksumException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of the abstract Aindexer class, backed by a simple hashmap to store words and their
 * locations within the files.
 */
public class DictionaryIndexer extends Aindexer<DictionarySearch> {

	private static final String WORD_FINDING_PATTERN = "[a-zA-Z]+";

	private static final Stemmer STEMMER = new Stemmer();
	public static final IndexTypes TYPE = IndexTypes.DICT;
    private static final String DOT_CACHE = ".cache";
    private static final String DOT = ".";
    private static final String UNDER_SCORE = "_";
    HashMap<Integer, List<Word>> dictionary;
	DictionarySearch searchStrategy;

	/**
	 * Basic constructor, sets origin Corpus and initializes backing hashmap
	 * @param origin    the Corpus to be indexed by this DS.
	 */
	public DictionaryIndexer(Corpus origin) {

		super ( origin );

		this.dictionary = new HashMap<> (  );

	}

    /**Tries to find and load a cache file holding a Corpus and the Dictionary.
     *
     * @throws WrongMD5ChecksumException if their is no cache file or if their is one but it does not match
     * the current checksum.
     * @throws IOException If had a problem when trying to load the cache file.
     */
    @SuppressWarnings("unchecked")
    @Override
	protected void readIndexedFile() throws WrongMD5ChecksumException, IOException {

        File cacheFile = findCashFile();
        if (cacheFile == null) {

            throw new WrongMD5ChecksumException();
        }
        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(cacheFile);
            ObjectInputStream in = new ObjectInputStream(file);

            String cachedmd5 = (String) in.readObject();

            if ( !this.origin.getChecksum().equals( cachedmd5 ) ) {

                    throw new WrongMD5ChecksumException();
            }

            this.origin = (Corpus) in.readObject();

            this.dictionary = (HashMap)in.readObject();

            in.close();
            file.close();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *Serializes the current Corpus & Dictionary
     */
    @Override
    protected void writeIndexFile() {
        File outputDirectory = getCacheFileAppropriateDir();
        String cacheFileName = getCacheFileName();

        String path = outputDirectory.getPath() + File.separator + cacheFileName;

        // Serialization
        try
        {

            FileOutputStream file = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(file);

            out.writeObject(this.origin.getChecksum());
            out.writeObject( this.origin );
            out.writeObject( this.dictionary );

            out.close();
            file.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    index the corpus files to Block objects
     */
    @Override
    protected void indexCorpus()  {

        Pattern wordFinder = Pattern.compile (WORD_FINDING_PATTERN);

        Matcher wordMatcher = wordFinder.matcher ( "" );

        for (Entry currentEntry: this.origin) {

            for (Block currentBlock : currentEntry) {
				try {
					this.indexBlock ( currentBlock, wordMatcher );
				} catch (Exception e) { System.out.println (e.toString ());}

            }
        }
    }

    /**Searches for a cache file, in the Corpus-Directory,
     * in the case where the corpus is 1 file then in the directory where the corpus is.
     *
     * @return a File representing the Cache file, Null if not found.
     */
    private File findCashFile(){

        String cacheFileName = getCacheFileName();
        File searchedDirectory = getCacheFileAppropriateDir();

        File[] cacheFiles = searchedDirectory.listFiles( file -> file.getName().equals( cacheFileName ) );
        if( cacheFiles == null || cacheFiles.length == 0  ){
            return null;
        }
        return cacheFiles[0];
        // Assuming their is only one file, and that is not in a sub-directory.
    }

    /*
    getter for a File object
     */
    private File getCacheFileAppropriateDir() {
        File cacheDir = new File( origin.getPath() );

        if ( !cacheDir.isDirectory() ){
            cacheDir = cacheDir.getParentFile();

        }
        return cacheDir;
    }
	/*
	getter for cache file name (string)
	 */
    private String getCacheFileName() {
        // Cashed file name pattern : < IndexerType > < ParseRule > < CorpusName > .cache

        String indexerType = TYPE.name();
        String parseRule = this.origin.getParsingRule().getClass().getName();
        // Trim parseRule
        parseRule = parseRule.substring( parseRule.lastIndexOf(DOT) + 1);

        String corpusName = origin.getPath().substring( origin.getPath().lastIndexOf(File.separatorChar) + 1);
        // Trim corpusName
        if ( corpusName.lastIndexOf(DOT) > 0){
            corpusName = corpusName.substring(0, corpusName.lastIndexOf(DOT));
        }
        return  indexerType + UNDER_SCORE + parseRule + UNDER_SCORE   + corpusName + DOT_CACHE;
    }

    /*
    index a certain block using regex like you like it... yeah....
     */
	private void indexBlock(Block block, Matcher wordMatcher) throws Exception{

		String blockText = (block.toString ()).toLowerCase ();

		wordMatcher.reset ( blockText );


		while (wordMatcher.find ()) {

			String matchedWord = wordMatcher.group ();

			//if match is a stop word, ignore it and continue to next match.
			if (Stopwords.isStopword ( matchedWord )) { continue; }

			long offsetWithinBlock = wordMatcher.start( );
			long endWithinBlock = wordMatcher.end ( );

			Word tempWord = new Word ( block, offsetWithinBlock, endWithinBlock );

			Integer matchStemmedHash = (STEMMER.stem ( matchedWord ) ).hashCode ();


			if (!this.dictionary.containsKey ( matchStemmedHash )) {

				this.dictionary.put ( matchStemmedHash, new LinkedList<Word> () );
			}

			this.dictionary.get ( matchStemmedHash ).add ( tempWord );
		}
	}

	/**
	 * getter for the IparsingRule obj
	 * @return IparsingRule
	 */
	@Override
	public IparsingRule getParseRule() { return this.origin.getParsingRule (); }

	/**
	 * getter for the DictionarySearch obj
	 * @return DictionarySearch
	 */
	@Override
	public DictionarySearch asSearchInterface() {
		return new DictionarySearch ( this.dictionary );
	
	}



}

