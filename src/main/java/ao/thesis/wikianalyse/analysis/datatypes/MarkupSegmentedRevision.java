package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;


public class MarkupSegmentedRevision extends SwebleRevision {
	
	/*
	 * Text Analysis Components
	 */
	private List<Token> tokens = null;
	
	private HashMap<PrefixTuple, List<Integer>> prefixes = null;
	

	public MarkupSegmentedRevision(int id, DateTime timestamp) {
		super(id, timestamp);
	}
	
	public MarkupSegmentedRevision(SwebleRevision rev) {
		super(rev.getID(), rev.getTimestamp());
		super.setContributorName(rev.getContributorName());
		super.setEngProcessedPage(rev.getEngProcessedPage());
		super.setPageId(rev.getPageId());
		super.setPageTitle(rev.getPageTitle());
	}


	public List<Token> getMarkupSegmentedTokens() {
		return tokens;
	}


	public void setMarkupSegmentedTokens(List<Token> tokens) {
		this.tokens = tokens;
	}


	public HashMap<PrefixTuple, List<Integer>> getMarkupSegmentedPrefixPositions() {
		return prefixes;
	}


	public void setMarkupSegmentedPrefixPositions(HashMap<PrefixTuple, List<Integer>> prefixes) {
		this.prefixes = prefixes;
	}

}
