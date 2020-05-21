package processing.searchStrategies;

import processing.textStructure.Corpus;
import processing.textStructure.Word;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * factory class to instantiate a search strategy object according to the received arguments.
 */
public class SearchStrategyFactory {

	/**
	 * Instantiates a search strategy object acording to "strategyType" and setting the case sensitivity
	 * according to "isCaseSensitive".
	 * @param origin - Corpus object
	 * @param strategyType - String, the type of the strategy
	 * @return strategy - a search strategy object (implements IsearchStrategy)
	 */
	static public IsearchStrategy getSearchStrategy(Corpus origin, String strategyType){
		//todo maybe convert to singleton

		IsearchStrategy strategy;

		//todo improve switch to use imported enum

		switch (strategyType){
			case "NAIVE":
				strategy = new NaiveSearch ( origin );

			default:
				strategy = new NaiveSearch ( origin );


		}

		return strategy;
	}

}

