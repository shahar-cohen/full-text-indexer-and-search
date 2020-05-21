package processing.textStructure;

import processing.parsingRules.*;
import utils.MD5;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.FileSystems.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**`
 * This class represents a body of works - anywhere between one and thousands of documents sharing the same structure and that can be parsed by the same parsing rule.
 */
public class Corpus implements Iterable<Entry>, Serializable {
	public static final long serialVersionUID = 1L;



    private List<Entry> entryList;
    private IparsingRule parsingRule;
    private String corpusPath;
    private ArrayList<Path> pathsList;

    //made this because i wanted to make sure the sorting was lexicographical
    private static final Comparator<Entry> LEXICOMPARATOR =
											(x, y)->x.getEntryFilePath().compareTo(y.getEntryFilePath());


    /**
     * constructor for Corpus
     * @param path String file path
     * @param parserName String name of parser
     * @throws IOException
     */
    public Corpus(String path, String parserName) throws IOException {

        this.parsingRule = ParsingRuleFactory.StringToRule( parserName );
        this.entryList = new LinkedList<>();
        this.corpusPath = getCorrectPath(path);
        this.pathsList = new ArrayList<>();

        this.addAllEntriesToList();
    }

    /**
     * This method populates the Block lists for each Entry in the corpus.
     */
    public void populate() throws IOException { for (Entry entry: this.entryList) { entry.populate (); } }

    /**
     * The path to the corpus folder
     * @return A String representation of the absolute path to the corpus folder
     */
    public String getPath() { return this.corpusPath;}

    /**
     * Return the parsing rule used for this corpus
     * @return the parsing rule used for this corpus
     */
    public IparsingRule getParsingRule() { return this.parsingRule; }


    /**
     * Iterate over Entry objects in the Corpus
     * @return An entry iterator
     */
    @Override
    public Iterator<Entry> iterator() { return this.entryList.iterator(); }


    /**
     * Return the checksum of the entire corpus. This is an MD5 checksum which represents all the files in the corpus.
     * @return A string representing the checksum of the corpus.
     * @throws IOException if any file is invalid.
     */
    public String getChecksum() throws IOException {
    	//init string builder
    	StringBuilder concatChecksum = new StringBuilder (  );

    	//for each entry in the list
		for (Entry entry: this.entryList) {

		    String content = new String(Files.readAllBytes(Paths.get(entry.getEntryFilePath())));

            concatChecksum.append(MD5.getMd5(content));
		}
		//return the md5 checksum of the concatenated checksums.
		return MD5.getMd5 ( concatChecksum.toString () );
    }

    /**
     * Update the RandomAccessFile objects for the Entries in the corpus, if it was loaded from cache.
     */
    public void updateRAFs () throws IOException {
		try{
			for (Entry entry: this.entryList) {
				entry.updateRAF ();
			}
		} catch (IOException e) {
		    throw new IOException("RandomAccessFile update failed.");
		}
    }

    /*
    Adds Entry object to list by the file/directory specified by the corpus path.
     */
    private void addAllEntriesToList() throws IOException {

        Path nioPath = Paths.get(this.corpusPath);

        if (! Files.isDirectory(nioPath)) {
            //got only file
            this.entryList.add(new Entry(nioPath.toAbsolutePath().toString(), this.parsingRule));
        } else {
            try (Stream<Path> walk = Files.walk(nioPath)) {

                walk.filter(Files::isRegularFile).forEach(file -> this.entryList.add(new Entry(file.toAbsolutePath().toString(), this.parsingRule)));

            } catch (FileNotFoundException e) { }
        }
        //sort lexicographically so the result would be sorted as ex6 description required.
        this.entryList.sort(LEXICOMPARATOR);
    }

    /*
    Makes absolutely doubly sure that the file indeed exists and edits it if it doesn't, and then checks
    again!
     */
    private String getCorrectPath(String path) throws IOException {
        Path nioPath = Paths.get(path);
        try {
            nioPath = nioPath.toRealPath();
        } catch (IOException e) {
            String separator = System.getProperty("file.separator");

            String srcFolderFilePath = "src".concat(separator).concat(path);

            try {
                nioPath = Paths.get(srcFolderFilePath).toRealPath();
            } catch (IOException e1) {

                throw new FileNotFoundException("The file doesn't seem to exist.");

            }
        }

        return nioPath.toString();
    }
}
