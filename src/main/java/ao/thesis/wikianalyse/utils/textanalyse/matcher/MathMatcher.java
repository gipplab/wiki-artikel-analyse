package ao.thesis.wikianalyse.utils.textanalyse.matcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;

/**
 * Matcher for math formulas in revisions.
 * 
 * @author anna
 *
 */
public class MathMatcher extends Matcher {
	
	private List<List<Object>> prevFormulas;
	
	private MathFormula target;
	
	@SuppressWarnings("unchecked")
	public List<Match> match(List<List<Object>> prevFormulas, MathFormula target, int targetIndex, 
			int targetRevisionIndex, Comparator<Match> comparator){
		
		List<Match> allBestMatches = new LinkedList<Match>();
		
		if(!prevFormulas.isEmpty()){
			this.prevFormulas = prevFormulas;
			
			this.target = target;
			
			this.mathPrefixPositions = new ArrayList<List<HashMap<Object, List<Integer>>>>(prevFormulas.size());
			
			this.prevFormulas.stream().forEach(formula -> mathPrefixPositions.add(buildMathPrefixHashMap((List) formula)));
			
			allBestMatches = getBestMatches(targetIndex, targetRevisionIndex, comparator);
		}
		
		return allBestMatches;
	}
	
	private List<Match> getBestMatches(int targetFormulaIndex, int targetRevIndex, Comparator<Match> comparator){
		
		List<Match> bestMatches = new LinkedList<Match>();
		List<Match> allMatches = new LinkedList<Match>();
		
		for(int sourceRevIndex = prevFormulas.size() - 1 ; 0 <= sourceRevIndex ; sourceRevIndex--){
			for(int sourceFormulaIndex = 0 ; sourceFormulaIndex < prevFormulas.get(sourceRevIndex).size(); sourceFormulaIndex++){
				
				allMatches.addAll(matchFormulas(sourceFormulaIndex, sourceRevIndex, targetFormulaIndex, targetRevIndex));
				
				if(sourceRevIndex == targetRevIndex - 10 - 1){
					break;
				}
			}
		}
		
		Object targetElements[] = target.stream().toArray();
		
		if(!allMatches.isEmpty()){
			
			PriorityQueue<Match> queue = buildQueue(allMatches.size(), comparator);
			
			queue.addAll(allMatches);
			
			while (!queue.isEmpty()){
				
				Match currentMatch = queue.peek();
				
				boolean completeMatch = true;
				for(int matchlength = 0 ; matchlength < currentMatch.length ; matchlength++){
					
					if(targetElements[currentMatch.targetPosition + matchlength] == null){
						
						completeMatch = false;
						break;
					}
				}
				
				if(completeMatch){
					
					bestMatches.add(currentMatch);
					
					for(int matchlength = 0 ; matchlength < currentMatch.length ; matchlength++){
						
						targetElements[currentMatch.targetPosition + matchlength] = null;
					}
				}
				queue.poll();
			}
		}
		return bestMatches;
	}
	
	
	
	private List<Match> matchFormulas(int sourceFormulaIndex, int sourceRevIndex, int targetFormulaIndex, int targetRevIndex){
		
		List<Match> matches = new LinkedList<Match>();
		
		HashMap<Object, List<Integer>> source = mathPrefixPositions.get(sourceRevIndex).get(sourceFormulaIndex);
		
		int sourceSize = ((List)prevFormulas.get(sourceRevIndex).get(sourceFormulaIndex)).size();
		
		int length=0;
		
		for(int positionInTarget=0 ; positionInTarget < target.size() ; positionInTarget++){
			
			Object currenttoken = target.get(positionInTarget);
			
			if(source.containsKey(currenttoken)){
				
				for(Integer positionInSource : source.get(currenttoken)){
					
					length = 1;
					while(positionInSource + length < sourceSize 
							&& positionInTarget + length < target.size() 
							&& source.containsKey(target.get(positionInTarget + length)) 
							&& source.get(target.get(positionInTarget + length))
								.contains(positionInSource + length)){
						
						length++;
					}

					//TODO condition
					if(length >= 3) {
						
					Match match = new MathMatch(
							positionInSource, 
							positionInTarget, 
							length, 
							sourceSize, 
							target.size(), 
							sourceRevIndex, 
							sourceFormulaIndex);
					
					matches.add(match);
					}
				}
			}
		}
		return matches;
	}
	
	private List<HashMap<Object, List<Integer>>> buildMathPrefixHashMap(List<List<Object>> allFormulas){
		
		List<HashMap<Object, List<Integer>>> mathPrefixPositions = new ArrayList<HashMap<Object, List<Integer>>>();
		
		allFormulas.stream().forEach(revFormulas -> mathPrefixPositions.add(buildPrefixHashMap(revFormulas)));

		return mathPrefixPositions;
	}
}
