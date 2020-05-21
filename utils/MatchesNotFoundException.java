package utils;

/**
 * an Exception to be raised when no results were found by the query or when the query is empty.
 */
public class MatchesNotFoundException extends Exception {
	public MatchesNotFoundException(String situation){
		super("No search result were found during " + situation);
	}
}
