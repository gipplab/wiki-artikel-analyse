package ao.thesis.wikianalyse.utils.textanalyse;

import java.util.List;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.utils.textanalyse.matcher.Match;
import ao.thesis.wikianalyse.utils.textanalyse.matcher.MathMatch;

import ao.thesis.wikianalyse.utils.textanalyse.matcher.MathMatcher;
import ao.thesis.wikianalyse.utils.textanalyse.matcher.TextMatcher;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;


/**
 * Matches revision texts and math formulas and sets source revisions.
 * 
 * @author anna
 *
 */
public class EditorAssociation {

	private static Logger logger = Logger.getLogger(EditorAssociation.class);
	
	
	List<List<Token>> tokenizedRevisions;
	List<List<Token>> mathFormulas;

	
	public void associateTextEditors(List<List<Token>> orderedTokenizedRevisions, TextMatcher matcher){
		this.tokenizedRevisions = orderedTokenizedRevisions;
		updateTokenSources(matcher);
	}
	
	public void associateMathEditors(List<List<Token>> orderedMathFormulas, MathMatcher mathMatcher){
		this.mathFormulas = orderedMathFormulas;
		updateMathTokens(mathMatcher);
	}
	
	/** Matches texts and math formula (if there are any).
	 * @param tokenizedRevisions	- list of tokens per revision in page history order
	 * @param mathFormulas			- list of math formulas (list of math tokens) per revision in page history order
	 * @param matcher				- matcher to match texts
	 * @param mathMatcher			- matcher to match math formulas
	 */
	public void associateTextAndMathEditors(
			List<List<Token>> orderedTokenizedRevisions, 
			List<List<Token>> orderedMathFormulas,
			TextMatcher matcher, 
			MathMatcher mathMatcher){
		
		associateTextEditors(orderedTokenizedRevisions, matcher);
		associateMathEditors(orderedMathFormulas, mathMatcher);
	}
	
	
	@SuppressWarnings("unchecked")
	private void updateTokenSources(TextMatcher matcher){
		
		if(!tokenizedRevisions.isEmpty()){
			
			List<List<Match>> bestMatches = matcher.match((List) tokenizedRevisions);
			
			if(!bestMatches.isEmpty()){
				for(int index = 1 ; index < tokenizedRevisions.size() ; index++){
					
					logger.info("Set sources for revision "+index+".");
					List<Match> bestTargetMatches = bestMatches.get(index-1);
					List<Object> targetRevision = (List<Object>) ((List) tokenizedRevisions).get(index);
					bestTargetMatches.stream().forEach(match -> updateTokenSourcesFromMatch(match, targetRevision));
				}
			}
		} else {
			logger.warn("Revision list is empty.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateTokenSourcesFromMatch(Match match, List<Object> targetRevision) {
		
		for(int currlength = 0 ; currlength < match.length ; currlength++){
			
			int sPosition = match.sourcePosition + currlength;
			int tPosition = match.targetPosition + currlength;

			if(match.sourceIndex < tokenizedRevisions.size()){
				
				List<Object> sourceRevision = (List<Object>) ((List) tokenizedRevisions).get(match.sourceIndex);
				Object originalToken = sourceRevision.get(sPosition);
				Object copyToken = targetRevision.get(tPosition);
				
				if(copyToken instanceof Token && originalToken instanceof Token){
					((Token) copyToken).setSourceId(((Token) originalToken).getSourceId());
				}
				
			} else {
				throw new IllegalArgumentException();
			}
		}	
	}
	
	
	@SuppressWarnings("unchecked")
	/* TODO this method fails in some cases. The math token sources of a revision have to be set 
	 * right after the text sources.
	 */
	private void updateMathTokens(MathMatcher mathMatcher){
		
		int revIndex = 0;
		int formulaIndex = 0;
		
		for(List<Token> revisionFormulas : mathFormulas){
			for(Object item : revisionFormulas){
				MathFormula formula = (MathFormula) item;
				if(formula.getSourceId().getIndex() == revIndex && revIndex != 0){
					
					List<Match> formulaMatches = mathMatcher.match(
							(List) mathFormulas.subList(0, revIndex), 
							formula, 
							formulaIndex, 
							revIndex);
					
					if(!formulaMatches.isEmpty()){
						String formulaText = formula.getText();
						if(formula.getText().length() > 10){
							formulaText = formulaText.substring(0,10)+"...";
						}
						logger.info("Update MathTokens for formula "+formulaText+" in revision "+revIndex);
						
						formulaMatches.stream().forEachOrdered(match -> updateMathSourcesFromMatch((MathMatch) match, formula));
					}
				}
				formulaIndex++;
			}
			formulaIndex = 0;
			revIndex++;
		}
	}
	
	
	private void updateMathSourcesFromMatch(MathMatch match, MathFormula formula) {
		
		for(int currlength = 0 ; currlength < match.length ; currlength++){
			
			int sPosition = match.sourcePosition + currlength;
			int tPosition = match.targetPosition + currlength;
			
			if(match.sourceIndex < mathFormulas.size()){
				
				MathFormula sourceFormula = (MathFormula) mathFormulas.get(match.sourceIndex).get(match.sourceFormulaIndex);
				Object originalToken = sourceFormula.get(sPosition);
				Object copyToken = formula.get(tPosition);
				
				if(copyToken instanceof MathToken && originalToken instanceof MathToken){
					((Token) copyToken).setSourceId(((Token) originalToken).getSourceId());
				}
			} else {
				throw new IllegalArgumentException();
			}
		}	
	}
}
