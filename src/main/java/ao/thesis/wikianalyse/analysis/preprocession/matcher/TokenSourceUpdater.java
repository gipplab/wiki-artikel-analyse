package ao.thesis.wikianalyse.analysis.preprocession.matcher;

import java.util.List;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.FormulaMatch;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.Match;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

public class TokenSourceUpdater {
	
	private TokenSourceUpdater(){}

	public static void update(List<Match> matches, List<Token> targetTokens, List<List<Token>> prevs) {

		for(Match match : matches){
			for(int currlength = 0 ; currlength < match.getLength() ; currlength++){
				
				int sPosition = match.getSourcePos() + currlength;
				int tPosition = match.getTargetPos() + currlength;
				
				Token originalToken = prevs.get(match.getChunk()).get(sPosition);
				Token copyToken = targetTokens.get(tPosition);
				
				copyToken.setSourceId(originalToken.getSourceId());
				
				if(copyToken instanceof MathFormulaToken && originalToken instanceof MathFormulaToken){
					for(int i = 0; i < ((MathFormulaToken)copyToken).getElements().size(); i++){
						((Token)((MathFormulaToken)copyToken).getElements().get(i)).setSourceId(((Token)((MathFormulaToken)originalToken).getElements().get(i)).getSourceId());
					}
				}
				
			}	
		}
	}

	public static void updateMathTokens(List<List<FormulaMatch>> matches, List<MathFormulaToken> revisionformulas,
			List<List<MathFormulaToken>> allprevsformulas) {
		
		MathFormulaToken curr;
		
		int index = 0;
		for(List<FormulaMatch> matchlist : matches){
			
			curr = revisionformulas.get(index);
			
			for(FormulaMatch match : matchlist){
				for(int currlength = 0 ; currlength < match.getLength() ; currlength++){
					
					int sPosition = match.getSourcePos() + currlength;
					int tPosition = match.getTargetPos() + currlength;
					
					Token originalToken = (Token) allprevsformulas.get(match.getChunk()).get(match.getFormulaChunk()).getElements().get(sPosition);
					Token copyToken = (Token) curr.getElements().get(tPosition);
					
					copyToken.setSourceId(originalToken.getSourceId());
					
////					if(copyToken instanceof MathFormulaToken && originalToken instanceof MathFormulaToken){
//						for(int i = 0; i < ((MathFormulaToken)copyToken).getElements().size(); i++){
//							((Token)((MathFormulaToken)copyToken).getElements().get(i)).setSourceId(((Token)((MathFormulaToken)originalToken).getElements().get(i)).getSourceId());
//						}
////					}
				}	
			}
			
			index++;
		}
	}
}
