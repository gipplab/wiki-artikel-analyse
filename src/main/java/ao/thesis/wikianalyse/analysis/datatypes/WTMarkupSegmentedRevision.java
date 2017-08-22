package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;

public class WTMarkupSegmentedRevision extends MarkupSegmentedRevision{

	private WhitespaceRevision revision;
	
	public WTMarkupSegmentedRevision(int id, DateTime timestamp) {
		super(id, timestamp);
		revision = new WhitespaceRevision(id, timestamp);
	}
	
	public WTMarkupSegmentedRevision(MarkupSegmentedRevision rev) {
		super(rev.getID(), rev.getTimestamp());
		revision = new WhitespaceRevision(rev.getID(), rev.getTimestamp());
		
		super.setContributorName(rev.getContributorName());
		super.setEngProcessedPage(rev.getEngProcessedPage());
		super.setPageId(rev.getPageId());
		super.setPageTitle(rev.getPageTitle());
		super.setMarkupSegmentedPrefixPositions(rev.getMarkupSegmentedPrefixPositions());
		super.setMarkupSegmentedTokens(rev.getMarkupSegmentedTokens());
		
		super.math = rev.math;
	}
	
	public WTMarkupSegmentedRevision(MarkupSegmentedRevision rev1, WhitespaceRevision rev2) {
		super(rev1.getID(), rev1.getTimestamp());
		revision = rev2;
		
		super.setContributorName(rev1.getContributorName());
		super.setEngProcessedPage(rev1.getEngProcessedPage());
		super.setPageId(rev1.getPageId());
		super.setPageTitle(rev1.getPageTitle());
		super.setMarkupSegmentedPrefixPositions(rev1.getMarkupSegmentedPrefixPositions());
		super.setMarkupSegmentedTokens(rev1.getMarkupSegmentedTokens());
		
		super.math = rev1.math;
	}
	
	public WhitespaceRevision getWhitespaceRevision(){
		return revision;
	}
	
	public List<StringToken> getWhiteSpaceSegmentedTokens() {
		return revision.getWhiteSpaceSegmentedTokens();
	}


	public void setWhiteSpaceSegmentedTokens(List<StringToken> tokens) {
		revision.setWhiteSpaceSegmentedTokens(tokens);
	}


	public HashMap<PrefixTuple, List<Integer>> getWhiteSpaceSegmentedPrefixPositions() {
		return revision.getWhiteSpaceSegmentedPrefixPositions();
	}


	public void setWhiteSpaceSegmentedPrefixPositions(HashMap<PrefixTuple, List<Integer>> prefixes) {
		revision.setWhiteSpaceSegmentedPrefixPositions(prefixes);
	}

}
