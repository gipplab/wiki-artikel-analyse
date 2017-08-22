package ao.thesis.wikianalyse.analysis.preprocession.matcher.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

public class PrefixHashMapBuilder {
	
	private PrefixHashMapBuilder(){}
	
	static public Map getPrefixHashMap(List<Token> tokens, int prefixlength){
		
		HashMap<PrefixTuple, List<Integer>> prefixPositions = new HashMap<>();
		
		for(int i=0; (i+prefixlength) < tokens.size(); i++){
			Token[] prefix = new Token[prefixlength];
			for(int j=0; j<prefixlength; j++){
				prefix[j] = tokens.get(i+j);
			}
			PrefixTuple prefixTuple = new PrefixTuple(prefix);
			if(!prefixPositions.containsKey(prefixTuple)){
				prefixPositions.put(prefixTuple, new LinkedList<Integer>());
			}
			prefixPositions.get(prefixTuple).add(i);
		}
		return prefixPositions;
	}
	
//	static public Map getPrefixHashMapWithComplexTokens(List<Token> elements, int prefixlength){
//		
//		HashMap<PrefixTuple, List<Integer>> prefixPositions = new HashMap<>();
//		
//		int wordindex = 0;
//		for(int i = 0 ; (i + prefixlength) < elements.size() ; i++){
//			
//			Token token = elements.get(i);
//			if(token instanceof ComplexToken){
//				addTokensFromComplex(wordindex, (ComplexToken)token, prefixPositions);
//				wordindex += token.getLength();
//				continue;
//			}
//			
//			Token[] prefix = new Token[prefixlength];
//			for(int j = 0 ; j < prefixlength ; j++){
//				token = elements.get(i + j);
//				if(token instanceof ComplexToken){
//					prefix = Arrays.copyOf(prefix, j);
//					break;
//				}
//				prefix[j] = token;
//			}
//
//			PrefixTuple prefixTuple = new PrefixTuple(prefix);
//			if(!prefixPositions.containsKey(prefixTuple)){
//				prefixPositions.put(prefixTuple, new LinkedList<Integer>());
//			}
//			prefixPositions.get(prefixTuple).add(wordindex);
//			
//			wordindex++;
//		}
//		return prefixPositions;
//	}
//	
//	
//	private static void addTokensFromComplex(int wordindex, ComplexToken complexToken, HashMap<PrefixTuple, List<Integer>> prefixPositions){
//		
//		// add complex token
//		PrefixTuple prefixTuple = new PrefixTuple(new Token[]{complexToken});
//		if(!prefixPositions.containsKey(prefixTuple)){
//			prefixPositions.put(prefixTuple, new LinkedList<Integer>());
//		}
//		prefixPositions.get(prefixTuple).add(wordindex);
//		
//		// add every token in list
//		int index = 0;
//		for(Object element : complexToken.getElements()){
//			Token token = (Token) element;
//			if(token instanceof ComplexToken){
//				addTokensFromComplex(wordindex + index, (ComplexToken)token, prefixPositions);
//			}
//			prefixTuple = new PrefixTuple(new Token[]{token});
//			if(!prefixPositions.containsKey(prefixTuple)){
//				prefixPositions.put(prefixTuple, new LinkedList<Integer>());
//			}
//			prefixPositions.get(prefixTuple).add(wordindex + index);
//			index++;
//		}
//	}
}
