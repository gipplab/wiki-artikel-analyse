package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;

public class WhitespaceRevision extends PreprocessedRevision {
	
	/*
	 * Text Analysis Components
	 */
	private List<StringToken> tokens = null;
	
	private HashMap<PrefixTuple, List<Integer>> prefixes = null;
	

	public WhitespaceRevision(int id, DateTime timestamp) {
		super(id, timestamp);
	}
	
	public WhitespaceRevision(PreprocessedRevision rev) {
		super(rev.getID(), rev.getTimestamp());
		super.setContributorName(rev.getContributorName());
	}


	public List<StringToken> getWhiteSpaceSegmentedTokens() {
		return tokens;
	}


	public void setWhiteSpaceSegmentedTokens(List<StringToken> tokens) {
		this.tokens = tokens;
	}


	public HashMap<PrefixTuple, List<Integer>> getWhiteSpaceSegmentedPrefixPositions() {
		return prefixes;
	}


	public void setWhiteSpaceSegmentedPrefixPositions(HashMap<PrefixTuple, List<Integer>> prefixes) {
		this.prefixes = prefixes;
	}
	
}
