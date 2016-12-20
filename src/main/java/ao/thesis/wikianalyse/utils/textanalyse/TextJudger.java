package ao.thesis.wikianalyse.utils.textanalyse;

import java.util.List;
import java.util.stream.Collectors;

import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.ratings.MarkupRating;
import ao.thesis.wikianalyse.model.ratings.TextCountRating;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MarkupToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.NEToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;

/**
 * Provides measures to rate text.
 * 
 * @author anna
 *
 */
public class TextJudger {
	
	List<Token> tokens;
	RevisionID id;

	public TextJudger(List<Token> tokens, RevisionID id){
		this.tokens = tokens;
		this.id = id;
	}
	
	public List<Token> getInsertedTokens(){
		return getInsertedTokens(id);
	}
	
	public List<Token> getInsertedTokens(RevisionID sourceId){
		if (!id.getPageTitle().equals(sourceId.getPageTitle())){
			return null;
		} else return tokens.stream().filter(o -> o.getSourceId().equals(sourceId)).collect(Collectors.toList());
	}
	
	public void setRating(TextCountRating rating){
		rating.setTextCount(getInsertedTokens().size());
	}
	
	public void setRating(MarkupRating rating){
		
		int insertedMathFormulas = 0;
		int insertedNETokens = 0;	
		
		int insertedLinks = 0;	
		int insertedCategories = 0;	
		int insertedFiles = 0;
		int insertedHeaderWords = 0;
		
		for(Object token : getInsertedTokens()){
			
			if(token instanceof MathFormula){
				insertedMathFormulas++;
				
			} else if(token instanceof NEToken){
				insertedNETokens++;
				
			} else if(token instanceof MarkupToken){
				
				//TODO count instead link refs, category refs, etc.
				
				switch (((MarkupToken) token).getMarkup()){
					case LINK :
						insertedLinks++;
						break;
					case BOLD:
						break;
					case CATEGORY:
						insertedCategories++;
						break;
					case EXTERNLINK:
						insertedLinks++;
						break;
					case FILE:
						insertedFiles++;
						break;
					case HEADER:
						insertedHeaderWords++;
						break;
					case ITALIC:
						break;
					case TEXT:
						break;
					default:
						break;
				}
			}
		}
		rating.setInsertedMathFormulas(insertedMathFormulas);
		rating.setInsertedNETokens(insertedNETokens);
		
		rating.setInsertedFiles(insertedFiles);
		rating.setInsertedLinks(insertedLinks);
		rating.setInsertedCategories(insertedCategories);
		rating.setInsertedHeaderWords(insertedHeaderWords);
	}
	
	/** Calculates text decay quality (source WikiTrust).
	 * @param insertedWords				- number of inserted words
	 * @param survivedWords				- number of words that survived in all judging revisions
	 * @param numberOfJudgingRevisions	- number of judging revisions
	 * @return text decay quality of inserted text
	 */
	public static double calculateDecayQuality(int insertedWords, int survivedWords, int numberOfJudgingRevisions){
		
		double textDecayQuality = 0.0;
		
		//TODO rate only revisions, that have enough judging revisions
		if(insertedWords > 0 && numberOfJudgingRevisions > 0){
			textDecayQuality = calculateNewtonMethod(textDecayQuality, 
					insertedWords, 
					survivedWords+insertedWords, 
					numberOfJudgingRevisions, 
					5);
		}
		return textDecayQuality;
	}
	
	private static double calculateNewtonMethod(double result, int insertedWords, int totalSurvivedWords, 
			int numberOfJudgingRevisions, int limit){
		
		for(int j = 0 ; j < limit ; j++){
			double factor = Math.pow(result, numberOfJudgingRevisions + 1) - 1;
			double function = (factor * insertedWords) + ((1 - result) * totalSurvivedWords);
			
			double factor_1 = (numberOfJudgingRevisions + 1) * Math.pow(result, numberOfJudgingRevisions);
			double function_1 = (factor_1 * insertedWords) - totalSurvivedWords;
			
			result = result - (function / function_1);
		}
		return result;
	}
}