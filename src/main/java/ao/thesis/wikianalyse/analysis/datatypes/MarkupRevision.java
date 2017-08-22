package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

//public class MarkupRevision extends SwebleRevision {
//
//
//	public MarkupRevision(int id, DateTime timestamp) {
//		super(id, timestamp);
//	}
//
//	private List<StringToken> markupStringTokenizedText = null;
//	
//	private HashMap<PrefixTuple, List<Integer>> markupStringTokenPrefixPositions = null; //text representation from whitespace word segmentation
//	
//	private List<Token> tokenizedText = null;
//	
//	private HashMap<PrefixTuple, List<Integer>> tokenPrefixPositions = null; //text representation from markup supported word segmentation
//	
//	public List<StringToken> getMarkupStringTokenizedText() {
//		return markupStringTokenizedText;
//	}
//
//	public void setMarkupStringTokenizedText(List<StringToken> markupStringTokenizedText) {
//		this.markupStringTokenizedText = markupStringTokenizedText;
//	}
//
//	public HashMap<PrefixTuple, List<Integer>> getMarkupStringTokenPrefixPositions() {
//		return markupStringTokenPrefixPositions;
//	}
//
//	public void setMarkupStringTokenPrefixPositions(HashMap<PrefixTuple, List<Integer>> markupStringTokenPrefixPositions) {
//		this.markupStringTokenPrefixPositions = markupStringTokenPrefixPositions;
//	}
//	
//
//	public List<Token> getTokenizedText() {
//		return tokenizedText;
//	}
//
//	public void setTokenizedText(List<Token> tokens) {
//		this.tokenizedText = tokens;
//	}
//}
