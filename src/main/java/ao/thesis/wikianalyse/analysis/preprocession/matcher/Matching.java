package ao.thesis.wikianalyse.analysis.preprocession.matcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.NEToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

/**
 * Matching
 * 
 * Class that controls the revision matching and updates sources of each token.
 * 
 * @author Anna Opaska
 *
 */
public class Matching {

	private static final Logger LOGGER = Logger.getLogger(Matching.class);
	
	private Matching(){}
	
	
	/**
	 * Processes a List of PreprocessedRevisions
	 * 
	 * @param revisions
	 * @param matcher
	 */
	public static void matchNEBagOfWords(
			NERevision revision, 
			List<NERevision> prevs){
		try{
			Validate.notNull(revision);
			Validate.notNull(prevs);
		} catch(IllegalArgumentException e){
			LOGGER.error(e);
			return;
		}
		List<NEToken> tokens = (List) revision.getNETokens();
		
		for(NERevision prev : prevs){
			List<NEToken> prevtokens = (List) prev.getNETokens();
			
			for(NEToken token : tokens){
				for(NEToken prevtoken : prevtokens){
					if(prevtoken.equals(token) && (token.getSourceId()==revision.getID())){
						token.setSourceId(prevtoken.getSourceId());
					}
				}
			}
		}
		
	}
	
	
	/**
	 * Processes a List of PreprocessedRevisions
	 * 
	 * @param revisions
	 * @param matcher
	 */
	public static void matchNEsWithGreedy(NERevision revision, List<NERevision> prevs, TextMatcher matcher){
		try{
			Validate.notNull(revision);
			Validate.notNull(prevs);
			Validate.notNull(matcher);
		} catch(IllegalArgumentException e){
			LOGGER.error(e);
			return;
		}
		matcher.matchAndUpdate(revision, prevs);
	}
	
	
	/**
	 * Processes a List of PreprocessedRevisions
	 * 
	 * @param revisions
	 * @param matcher
	 */
	public static void matchPreprocessedRevision(
			PreprocessedRevision revision, 
			List<PreprocessedRevision> prevs,
			TextMatcher matcher){
		try{
			Validate.notNull(revision);
			Validate.notNull(prevs);
			Validate.notNull(matcher);
		} catch(IllegalArgumentException e){
			LOGGER.error(e);
			return;
		}
		matcher.matchAndUpdate(revision, prevs);
		
		
	}
	
	/**
	 * Processes a List of PreprocessedRevisions
	 * 
	 * @param revisions
	 * @param matcher
	 */
	public static void matchPreprocessedRevision(
			PreprocessedRevision revision, 
			List<PreprocessedRevision> prevs,
			TextMatcher matcher, 
			FormulaMatcher formulamatcher){
		try{
			Validate.notNull(revision);
			Validate.notNull(prevs);
			Validate.notNull(matcher);
		} catch(IllegalArgumentException e){
			LOGGER.error(e);
			return;
		}
		
		matcher.matchAndUpdate(revision, prevs);
		
		int id = revision.getID();
		
		formulamatcher.matchAndUpdate(
				revision.getMathFormulas().stream()
					.filter(formula-> formula.getSourceId()==id).collect(Collectors.toList()), 
				prevs.stream()
					.map(rev -> rev.getMathFormulas()).collect(Collectors.toList()));
		
		for(MathFormulaToken formula : revision.getMathFormulas()){
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for(Object obj : formula.getElements()){
				Token token = (Token) obj;
				
				if(map.containsKey(token.getSourceId())){
					map.put(token.getSourceId(), (Integer) map.get(token.getSourceId())+1);
				} else {
					map.put(token.getSourceId(), 1);
				}
			}
			int fid = formula.getSourceId();
			int biggest = 0;
			for(Integer key : map.keySet()){
				if(map.get(key) > biggest){
					fid = map.get(key);
				}
			}
			formula.setSourceId(fid);
		}
		
	}
	
//	/**
//	 * Processes a List of PreprocessedRevisions
//	 * 
//	 * @param revisions
//	 * @param matcher
//	 */
//	public static void matchPreprocessedRevision(
//			PreprocessedRevision revision, 
//			List<PreprocessedRevision> prevs,
//			TokenMatcher matcher){
//		try{
//			Validate.notNull(revision);
//			Validate.notNull(prevs);
//			Validate.notNull(matcher);
//		} catch(IllegalArgumentException e){
//			LOGGER.error(e);
//			return;
//		}
//		matcher.matchAndUpdate(revision, prevs);
//	}

}