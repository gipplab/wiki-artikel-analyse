package ao.thesis.wikianalyse.judger;

public class TextJudger {

	
	public double calculateDecayQuality(int insertedTextAmount, Integer[] survivedWordAmount) 
	{
		double textDecayQuality = 0.0;
		
		if(insertedTextAmount != 0){
			
			int length = 0;
			int totalSurvivedWords = insertedTextAmount;
			
			for(int index = 0 ; index < survivedWordAmount.length ; index++){
				
				if(survivedWordAmount[index] != null){
					totalSurvivedWords += survivedWordAmount[index];
					length++;
				}
			}
			/* WikiTrust uses limit of 5
			 */
			textDecayQuality = calculateNewtonMethod(textDecayQuality, insertedTextAmount,
					totalSurvivedWords, length, 5);
		}
		return textDecayQuality;
	}
	
	public double calculateNewtonMethod(double result, int insertedTextAmount,
			int totalSurvivedWords, int n, int limit){
		
		for(int j = 0 ; j < limit ; j++)
		{
			double factor1 = (Math.pow(result, n + 1) - 1);
			double factor2 = (1 - result);
			
			double function = factor1 * insertedTextAmount 
					 		+ factor2 * totalSurvivedWords;
			
			double function_1 = insertedTextAmount * (n + 1) * Math.pow(result, n) 
					- totalSurvivedWords;
			
			result = result - (function / function_1);
		}
		return result;
	}
}