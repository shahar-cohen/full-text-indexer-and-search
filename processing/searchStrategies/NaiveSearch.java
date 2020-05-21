package processing.searchStrategies;

import processing.textStructure.Block;
import processing.textStructure.Corpus;
import processing.textStructure.Entry;
import processing.textStructure.WordResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * a class implementing a naive (direct comparison iterated over every possible possition in the text) search
 * strategy.
 */
public class NaiveSearch implements IsearchStrategy {
	protected Corpus origin;
	private List<WordResult> resultList;
	private boolean isCaseSensitive;

	public NaiveSearch(Corpus origin) {
		this.origin = origin;
		this.resultList = new ArrayList<>();
		this.isCaseSensitive = true;
	}

	/**
	 * The main search method to comply with the IsearchStrategy interface
	 * @param query The query string to search for.
	 * @return  A list of wordResults
	 */
	@Override
	public List<WordResult> search(String query) {

		//get entry iterator from corpus
		Iterator<Entry> entryIterator = origin.iterator ();

		//loop over entries
		while (entryIterator.hasNext ()) {

			//get next entry
			Entry entry = entryIterator.next ();

			//get block iterator from entry:
			Iterator<Block> blockIterator = entry.iterator ();

			//loop over blocks
			while (blockIterator.hasNext ()) {
				Block currentBlock = blockIterator.next ();

				searchRAF(currentBlock, query);
			}
		}

		return this.resultList;
	}

	// adds a WordResult for each match of "query" in the text in the given block
	private void searchRAF(Block block, String query){

		String text = block.toString ();

		int querySize = query.length();


		// application of naive search of string (as shown in readme)
		for(int i = 0; i < text.length () - querySize + 1; i++){

			int j;


			for(j = 0 ; j < querySize; j++){
				if (text.charAt(i + j) != query.charAt(j)) { break;}
			}

			if (j == querySize ) {
				// if all chars were the same (break wasn't called) add result to list
				String[] newList = new String[1];
				newList[0] = query;
				this.resultList.add(new WordResult(block, newList, i));
			}
		}
	}
}
