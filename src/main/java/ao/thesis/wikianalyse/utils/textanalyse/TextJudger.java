package ao.thesis.wikianalyse.utils.textanalyse;

/**
 * Provides measures to rate text.
 * 
 * @author anna
 *
 */
public class TextJudger {

	/** Calculates text decay quality (source WikiTrust).
	 * @param insertedWords				- number of inserted words
	 * @param survivedWords				- number of words that survived in all judging revisions
	 * @param numberOfJudgingRevisions	- number of judging revisions
	 * @return text decay quality of inserted text
	 */
	public double calculateDecayQuality(int insertedWords, int survivedWords, int numberOfJudgingRevisions){
		
		double textDecayQuality = 0.0;
		if(insertedWords > 0){
			textDecayQuality = calculateNewtonMethod(textDecayQuality, insertedWords, survivedWords+insertedWords, 
					numberOfJudgingRevisions, 5);
		}
		return textDecayQuality;
	}
	
	private double calculateNewtonMethod(double result, int insertedWords, int totalSurvivedWords, 
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