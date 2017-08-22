package ao.thesis.wikianalyse.analysis.preprocession.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.FormulaMatch;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.utils.MatchComparator;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

/**
 * Matcher for math formulas in revisions.
 * 
 * @author anna
 *
 */
public class FormulaMatcher {		
	
	private static final Logger LOGGER = Logger.getLogger(FormulaMatcher.class);
	
	private final int maxComparisations;
	private final int minMatchLength;
	
	private Comparator<FormulaMatch> comparator = MatchComparator.getDefaultMathComparator();
	
	private List<MathFormulaToken> revisionformulas;
	
	private MathFormulaToken formula;
	
	private List<List<MathFormulaToken>> allprevsformulas;
	
	private List<MathFormulaToken> prevsformulas;
	
	private List<List<Map<PrefixTuple, List<Integer>>>> prevsPrefixPositions;
	
	
	public FormulaMatcher(int maxComparisations, int minMatchLength) {
		
		validate(maxComparisations, 1);
		validate(minMatchLength, 1);
		
		this.maxComparisations = maxComparisations;
//		this.minMatchLength = minMatchLength;
		
		this.allprevsformulas = new ArrayList<>(maxComparisations);
		this.prevsPrefixPositions = new ArrayList<>(maxComparisations);
		
		this.minMatchLength = 5;
	}
	
	/**
	 * Validates the given value by checking if it is not null and bigger or equal the second argument.
	 * 
	 * @param value		integer to be validated
	 * @param min		smallest possible value
	 * @throws IllegalArgumentException	if value is null or smaller than min
	 */
	private void validate(int value, int min){
		if(Objects.isNull(value) || value < min){
			throw new IllegalArgumentException();
		}
	}
	
	private void init(List<MathFormulaToken> revisionformulas, List<List<MathFormulaToken>> allprevsformulas) {
		this.allprevsformulas.clear();
		this.prevsPrefixPositions.clear();
		
		for(int i = 0; i< allprevsformulas.size(); i++){
			prevsPrefixPositions.add(new ArrayList<>());
		}
		
		setTargetFormulas(revisionformulas);
		
		this.allprevsformulas = allprevsformulas;
		int index = 0;
		for(List<MathFormulaToken> revFormulas : allprevsformulas){
			setRevFormulas(revFormulas, index);
			index++;
		}
	}
	
	
	private void setRevFormulas(List<MathFormulaToken> rev, int index) {
		int formulaindex = 0;
		for(MathFormulaToken revFormulas : rev){
			setSourceFormula(revFormulas, index, formulaindex);
			formulaindex++;
		}
	}
	
	private void setSourceFormula(MathFormulaToken rev, int index, int formulaindex) {
		if(rev != null){
			prevsPrefixPositions.get(index).add(rev.getPrevsPrefixPositions());
		}
	}
	
	private void setTargetFormulas(List<MathFormulaToken> revisionformulas){
		if(revisionformulas != null){
			this.revisionformulas = revisionformulas;
		}
	}
	
	public void matchAndUpdate(List<MathFormulaToken> revisionformulas, List<List<MathFormulaToken>> allprevsformulas){
		if(revisionformulas != null && !revisionformulas.isEmpty() && allprevsformulas != null && !allprevsformulas.isEmpty()){
				
			init(revisionformulas, allprevsformulas);
			
			if(prevsPrefixPositions != null){
				long timeStart = System.currentTimeMillis();
				TokenSourceUpdater.updateMathTokens(findBestMatches(), this.revisionformulas, this.allprevsformulas);
				LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
			}
		}
	}
	


	private List<List<FormulaMatch>> findBestMatches(){
		
		List<List<FormulaMatch>> bestRevisionMatches = new LinkedList<>();
		
		for(MathFormulaToken revFormula : revisionformulas){
			
			List<FormulaMatch> bestMatches = new LinkedList<>();
			List<FormulaMatch> allMatches = new LinkedList<>();
			
			formula = revFormula;
			
			for(int sourceIndex = 0 ; sourceIndex < allprevsformulas.size() ; sourceIndex++){
				
				prevsformulas = allprevsformulas.get(sourceIndex);
				
				for(int formulaindex = 0 ; formulaindex < prevsformulas.size() ; formulaindex++){
					List<FormulaMatch> currMatches = findMatches(sourceIndex, formulaindex);
					allMatches.addAll(currMatches);
				}
				if(sourceIndex == allprevsformulas.size() - maxComparisations - 1){
					break;
				}
			}
		
			if(!allMatches.isEmpty()){
				Object[] objects = formula.getElements().toArray();
				Token[] tokens = Arrays.copyOf(objects, objects.length, Token[].class);
				PriorityQueue<FormulaMatch> queue = new PriorityQueue<>(allMatches.size(), comparator);
				queue.addAll(allMatches);
		
				filterBestMatches(queue, tokens, bestMatches);
			}
			bestRevisionMatches.add(bestMatches);
		}
		
		return bestRevisionMatches;
	}
	
	
	private void filterBestMatches(PriorityQueue<FormulaMatch> queue, Token[] targetElements, List<FormulaMatch> bestMatches){
		
		while (!queue.isEmpty()){
			FormulaMatch currentMatch = queue.peek();
			
			boolean matchRelevant = false;
			boolean completeMatch = true;
			
			int subMatchLength = 0;
					
			for(int matchlength = 0 ; matchlength < currentMatch.getLength() ; matchlength++){	
				
				if(targetElements[currentMatch.getTargetPos() + matchlength] != null){
					subMatchLength++;
					matchRelevant = true;
				}
				if(targetElements[currentMatch.getTargetPos() + matchlength] == null){					
					completeMatch = false;
					if(matchRelevant){
						FormulaMatch subMatch = new FormulaMatch(
								subMatchLength,
								currentMatch.getSourcePos() + (matchlength + 1) - subMatchLength,
								currentMatch.getTargetPos() + (matchlength + 1) - subMatchLength,
								currentMatch.getTotalSourceLength(),
								currentMatch.getTotalTargetLength(),
								currentMatch.getChunk(),
								currentMatch.getFormulaChunk(),
								currentMatch.getFormulaTargetPos(),
								currentMatch.getFormulaSourcePos(),
								currentMatch.getJaccard());
						queue.add(subMatch);
						subMatchLength = 0;
						matchRelevant = false;
					}
				}
			}
			if(completeMatch){
				bestMatches.add(currentMatch);			
				for(int matchlength = 0 ; matchlength < currentMatch.getLength() ; matchlength++){
					targetElements[currentMatch.getTargetPos() + matchlength] = null;
				}
				
				boolean everythingMatched = true;
				for(int i = 0 ; i < targetElements.length ; i++){	
					if(targetElements[i] != null){
						everythingMatched = false;
						break;
					}
				} 
				if (everythingMatched){
					return;
				}
				
			} else if(matchRelevant){
				FormulaMatch subMatch = new FormulaMatch(
						subMatchLength,
						currentMatch.getSourcePos() + currentMatch.getLength() - subMatchLength,
						currentMatch.getTargetPos() + currentMatch.getLength() - subMatchLength,
						currentMatch.getTotalSourceLength(),
						currentMatch.getTotalTargetLength(),
						currentMatch.getChunk(),
						currentMatch.getFormulaChunk(),
						currentMatch.getFormulaTargetPos(),
						currentMatch.getFormulaSourcePos(),
						currentMatch.getJaccard());
				queue.add(subMatch);
			}
			queue.poll();
		}
	}
	

	public List<FormulaMatch> findMatches(int sourceIndex, int formulaindex){

		MathFormulaToken target = formula;
		MathFormulaToken source = prevsformulas.get(formulaindex);
		int targetSize = target.getElements().size();
		int sourceSize = source.getElements().size();
		Map<PrefixTuple, List<Integer>> sourcePrefixPositions = prevsPrefixPositions.get(sourceIndex).get(formulaindex);
		
		double jaccard = new JaccardSimilarity().apply(target.getText(), source.getText());
		
		List<FormulaMatch> matches = new LinkedList<>();
		
		/*
		 * optimization header and trailer
		 */
//		int matchStart = checkHeader(matches, target, source, targetSize, sourceSize, sourceIndex);
//		int matchEnd = checkTrailer(matches, target, source, targetSize, sourceSize, sourceIndex);

		// check matches for all elements in target
		for(int posT = 0; (posT + minMatchLength - 1) < targetSize ; posT++){
		
			int longestlength = 1;
			
			List tokenlist = new ArrayList<>();
			for(int i = 0; i < minMatchLength; i++){
				Token token = (Token) target.getElements().get(posT+i);
				tokenlist.add(token);
			}
			
			PrefixTuple currPrefix = buildPrefixTuple(tokenlist, 0, minMatchLength);
			
			if(sourcePrefixPositions.containsKey(currPrefix)){
				
//				/*
//				 * optimization max matches
//				 */
//				if(sourcePrefixPositions.get(currPrefix).size() > 50){
//					currPrefix.setNoMatching(); // sets source id to -1 which resembles stop word handling
//					continue;
//				}
				
				// check matches for all prefix positions in source
				for(int posS : sourcePrefixPositions.get(currPrefix)){
					
					int length = minMatchLength;
					while(posS + length < sourceSize 
							&& posT + length < targetSize // matchEnd < targetSize
							&& source.getElements().get(posS + length).equals(target.getElements().get(posT + length))){
						length++;
					}
					/*
					 * optimization longest match
					 */
//					while(length >= minMatchLength){
					FormulaMatch match = new FormulaMatch(
								length,
								posS,
								posT,
								sourceSize, 
								targetSize, 
								sourceIndex,
								formulaindex,
								target.getPosition(),
								source.getPosition(),
								jaccard);
						matches.add(match);
						
						if(length > longestlength){
							longestlength = length;
						}
//					length--;
//					}
				}
			}
			posT+=(longestlength-1);
		}
		return matches;
	}
	
	
	private PrefixTuple buildPrefixTuple(List<Token> target, int pos, int length){
		Token[] prefix = new Token[length];
		for(int i=0; i < length; i++){
			prefix[i] = target.get(pos+i);
		}
		return new PrefixTuple(prefix);
	}
	
	
//	/*
//	 * optimization
//	 */
//	private int checkHeader(List<Match> matches, List<Token> target, List<Token> source, int targetSize, int sourceSize, int sourceIndex){
//		int matchStart = 0;
//
//		int counter = 0;
//		while(counter < sourceSize 
//				&& counter < targetSize 
//				&& target.get(counter).equals(source.get(counter))){
//			matchStart++;
//			counter++;
//		}
//		if(counter >= minMatchLength){
//			Match header = new Match(counter, 0, 0, sourceSize, targetSize, sourceIndex);
//			matches.add(header);
//		}
//		return matchStart;
//	}
	
	
//	/*
//	 * optimization
//	 */
//	private int checkTrailer(List<Match> matches, List<MathFormulaToken> target, List<Token> source, int targetSize, int sourceSize, int sourceIndex){
//		int matchEnd = targetSize;
//		int counter = 0;
//		while(targetSize-counter > 1 
//				&& sourceSize-counter > 1 
//				&& target.get(targetSize-1-counter).equals(source.get(sourceSize-1-counter))){
//			counter++;
//			matchEnd--;
//		}
//		if(counter >= minMatchLength){
//			Match trailer = new Match(counter, sourceSize-counter, targetSize-counter, sourceSize, targetSize, sourceIndex);
//			matches.add(trailer);
//		}
//		return matchEnd;
//	}
	
}
