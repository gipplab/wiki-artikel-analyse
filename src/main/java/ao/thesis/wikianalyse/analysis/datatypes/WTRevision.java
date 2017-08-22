package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;

public class WTRevision extends SwebleRevision{
	
	private WhitespaceRevision revision;

	public WTRevision(int id, DateTime timestamp) {
		super(id, timestamp);
		revision = new WhitespaceRevision(id, timestamp);
	}
	
	public WTRevision(SwebleRevision rev) {
		super(rev.getID(), rev.getTimestamp());
		revision = new WhitespaceRevision(rev.getID(), rev.getTimestamp());
		
		super.setContributorName(rev.getContributorName());
		super.setEngProcessedPage(rev.getEngProcessedPage());
		super.setPageId(rev.getPageId());
		super.setPageId(rev.getPageId());
	}
	
	public WTRevision(SwebleRevision rev1, WhitespaceRevision rev2) {
		super(rev1.getID(), rev1.getTimestamp());
		revision = rev2;
		
		super.setContributorName(rev1.getContributorName());
		super.setEngProcessedPage(rev1.getEngProcessedPage());
		super.setPageId(rev1.getPageId());
		super.setPageId(rev1.getPageId());
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
