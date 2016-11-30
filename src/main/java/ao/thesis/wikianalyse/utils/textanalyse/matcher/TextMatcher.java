package ao.thesis.wikianalyse.utils.textanalyse.matcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;

/**
 * Matcher for text in revisions.
 * 
 * @author anna
 *
 */
public class TextMatcher extends Matcher {		
	
	private static Logger logger = Logger.getLogger(TextMatcher.class);
	
	private final int maxComparisations;
	
	private final int minMatchLength;
	
	private List<List<Object>> revisions;
	
	public TextMatcher(int maxComparisations, int minMatchLength) {
		this.maxComparisations = maxComparisations;
		this.minMatchLength = minMatchLength;
	}
	
	public List<List<Match>> matchAll(List<List<Object>> objects, Comparator<Match> comparator) {
		
		List<List<Match>> allBestMatches = new LinkedList<List<Match>>();
		
		if(!objects.isEmpty()){
			
			this.prefixPositions = new ArrayList<HashMap<Object, List<Integer>>>(objects.size());
			
			objects.stream().forEach(o -> buildPrefixHashMap(o, prefixPositions));
				
			/* getBestMatches() compares every revision with previous revisions, 
			 * so it starts at targetNumber = 1.
			 */
			for(int targetNumber = 1 ; targetNumber < objects.size() ; targetNumber++){
				allBestMatches.add(getBestMatches(targetNumber, objects, comparator));
			}
		}
		return allBestMatches;
	}

	public List<List<Match>> matchToTarget(List<List<Object>> objects, List<Object> target, Comparator<Match> comparator) {
		List<List<Match>> allBestMatches = new LinkedList<List<Match>>();
		
		if(!objects.isEmpty()){
			
			objects.add(target);
			
			this.prefixPositions = new ArrayList<HashMap<Object, List<Integer>>>(objects.size());
			
			objects.stream().forEach(o -> buildPrefixHashMap(o, prefixPositions));

			allBestMatches.add(getBestMatches(objects.size()-1, objects, comparator));
		}
		return allBestMatches;
	}
	
	private void buildPrefixHashMap(List<Object> o, List<HashMap<Object, List<Integer>>> prefixPositions) {
		prefixPositions.add(buildPrefixHashMap(o));
	}

	public List<List<Match>> match(List<List<Object>> revisions, Comparator<Match> comparator){
		
		List<List<Match>> allBestMatches = new LinkedList<List<Match>>();
		
		if(!revisions.isEmpty()){
			
			this.revisions = revisions;
			
			this.prefixPositions = new ArrayList<HashMap<Object, List<Integer>>>(revisions.size());
			
			revisions.stream().forEach(revision -> prefixPositions.add(buildPrefixHashMap(revision)));
			
			/* getBestMatches() compares every revision with previous revisions, 
			 * so it starts at targetNumber = 1.
			 */
			for(int targetNumber = 1 ; targetNumber < revisions.size() ; targetNumber++){
				allBestMatches.add(getBestMatches(targetNumber, revisions, comparator));
			}
		}
		return allBestMatches;
	}
	

	private List<Match> getBestMatches(int targetIndex, List<List<Object>> objects, Comparator<Match> comparator){
		
		logger.info("Get matches for revision "+targetIndex+".");
		
		List<Match> bestMatches = new LinkedList<Match>();
		List<Match> allMatches = new LinkedList<Match>();
		
		for(int sourceIndex = targetIndex - 1 ; 0 <= sourceIndex ; sourceIndex--){
			
			allMatches.addAll(matchElements(sourceIndex, targetIndex));
			
			if(sourceIndex == targetIndex - maxComparisations - 1){
				break;
			}
		}
		
		Object targetElements[] = ((ArrayList<Object>) objects.get(targetIndex)).toArray();
		
		if(!allMatches.isEmpty()){
			
			PriorityQueue<Match> queue = buildQueue(allMatches.size(), comparator);
			
			queue.addAll(allMatches);
			
			while (!queue.isEmpty()){
				
				Match currentMatch = queue.peek();
				
				boolean completeMatch = true;
				
				for(int matchlength = 0 ; matchlength < currentMatch.length ; matchlength++){
					
					if(targetElements[currentMatch.targetPosition+matchlength] == null){
						completeMatch = false;
						break;
					}
				}
				
				if(completeMatch){
					
					bestMatches.add(currentMatch);
					
					for(int matchlength = 0 ; matchlength < currentMatch.length ; matchlength++){
						
						targetElements[currentMatch.targetPosition+matchlength] = null;
					}
				}
				queue.poll();
			}
		}
		return bestMatches;
	}

	
	private List<Match> matchElements(int sourceIndex, int targetIndex){
		
		List<Match> matches = new LinkedList<Match>();
		List<Object> target = revisions.get(targetIndex);
		
		HashMap<Object, List<Integer>> source = prefixPositions.get(sourceIndex);
		
		int sourceSize = revisions.get(sourceIndex).size();
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
					
					//TODO update condition for ne tokens
					if((length >= minMatchLength) || (currenttoken instanceof MathFormula)){

						Match match = new Match(
								positionInSource, 
								positionInTarget, 
								length, 
								sourceSize, 
								target.size(), 
								sourceIndex);
						
						matches.add(match);
					}
				}
			}
		}
		return matches;
	}

}
