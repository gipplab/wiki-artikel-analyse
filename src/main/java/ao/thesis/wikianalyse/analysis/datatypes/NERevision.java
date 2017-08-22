package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

public class NERevision extends SwebleRevision {

	public NERevision(int id, DateTime timestamp) {
		super(id, timestamp);
	}
	
	public NERevision(SwebleRevision rev) {
		super(rev.getID(), rev.getTimestamp());
		super.setContributorName(rev.getContributorName());
		super.setEngProcessedPage(rev.getEngProcessedPage());
		super.setPageId(rev.getPageId());
		super.setPageTitle(rev.getPageTitle());
	}

	private List<Token> NETokens = null;
	
	private HashMap<PrefixTuple, List<Integer>> prefixes = null;
	
	public List<Token> getNETokens() {
		return NETokens;
	}

	public void setNETokens(List<Token> tokens) {
		NETokens = tokens;
	}
	
	public void setPrefixPositions(HashMap<PrefixTuple, List<Integer>> prefixes) {
		this.prefixes = prefixes;
	}

	public Map getPrefixPositions() {
		return prefixes;
	}
}
