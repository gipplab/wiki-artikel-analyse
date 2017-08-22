package ao.thesis.wikianalyse.analysis.preprocession.matcher.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ao.thesis.wikianalyse.analysis.datatypes.MarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.WhitespaceRevision;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.NEToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

public class WordNumberHashMapBuilder {
	
	private WordNumberHashMapBuilder(){}
	
	static public HashMap<Integer, Integer> getWordNumberMap(WhitespaceRevision revision){
		
		HashMap<Integer, Integer> wordNumbers = new HashMap<>();
		
		if(Objects.nonNull(revision.getWhiteSpaceSegmentedTokens())){
			revision.getWhiteSpaceSegmentedTokens().forEach(token -> {
				if(wordNumbers.containsKey(token.getSourceId())){
					wordNumbers.put(token.getSourceId(), wordNumbers.get(token.getSourceId())+1);
				} else {
					wordNumbers.put(token.getSourceId(), 1);
				}
			});
		}
		return wordNumbers;
	}
	
	static public HashMap<Integer, Integer> getWordNumberMap(MarkupSegmentedRevision revision){
		
		HashMap<Integer, Integer> wordNumbers = new HashMap<>();
		
		if(Objects.nonNull(revision.getMarkupSegmentedTokens())){
			revision.getMarkupSegmentedTokens().forEach(token -> {
				if(wordNumbers.containsKey(token.getSourceId())){
					wordNumbers.put(token.getSourceId(), wordNumbers.get(token.getSourceId())+1);
				} else {
					wordNumbers.put(token.getSourceId(), 1);
				}
			});
		}
		return wordNumbers;
	}
	
	//NES

	public static HashMap<Integer, Integer> getNewNENumberMaps(NERevision revision, String entity) {
		
		HashMap<Integer, Integer> wordNumbers = new HashMap<>();
		
		Set<Token> uniqueTokens = new HashSet<Token>(revision.getNETokens());
		
		if(Objects.nonNull(revision.getNETokens())){
			uniqueTokens.forEach(token -> {
				if(token instanceof NEToken){
					if((((NEToken)token).getEntity()).equals(entity)){
						if(wordNumbers.containsKey(token.getSourceId())){
							wordNumbers.put(token.getSourceId(), wordNumbers.get(token.getSourceId())+1);
						} else {
							wordNumbers.put(token.getSourceId(), 1);
						}
					}
				}
			});
		}
		return wordNumbers;
	}
	
	public static HashMap<Integer, Integer> getAllNENumberMaps(NERevision revision, String entity) {
		
		HashMap<Integer, Integer> wordNumbers = new HashMap<>();
		
		if(Objects.nonNull(revision.getNETokens())){
			revision.getNETokens().forEach(token -> {
				if(token instanceof NEToken){
					if((((NEToken)token).getEntity()).equals(entity)){
						if(wordNumbers.containsKey(token.getSourceId())){
							wordNumbers.put(token.getSourceId(), wordNumbers.get(token.getSourceId())+1);
						} else {
							wordNumbers.put(token.getSourceId(), 1);
						}
					}
				}
			});
		}
		return wordNumbers;
	}

	public static HashMap<Integer, Integer> getWordNumberMap(List<MathFormulaToken> formulas , int id) {
		
		HashMap<Integer, Integer> wordNumbers = new HashMap<>();
		
		if(Objects.nonNull(formulas)){
			formulas.forEach(formula -> formula.getElements().forEach(token -> {
						if(wordNumbers.containsKey(((Token) token).getSourceId())){
							wordNumbers.put(((Token) token).getSourceId(), wordNumbers.get(((Token) token).getSourceId())+1);
						} else {
							wordNumbers.put(((Token) token).getSourceId(), 1);
						}
				}));
			}
		return wordNumbers;
	}

}
