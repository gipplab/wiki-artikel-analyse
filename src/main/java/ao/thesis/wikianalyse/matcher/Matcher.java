package ao.thesis.wikianalyse.matcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.tokens.MathToken;

public class Matcher 
{			
	private int maxComparisations;
	private int minMatchLength;
	
	private List<List<Object>> revisions;
	private List<HashMap<Object, List<Integer>>> prefixPositions; 
	
	private static Logger logger = Logger.getLogger(Matcher.class);
	
	public Matcher(int maxComparisations, int minMatchLength) {
		
		this.maxComparisations = maxComparisations;
		this.minMatchLength = minMatchLength;
	}
	
	public List<List<Match>> match(List<List<Object>> revisions){
		
		this.revisions = revisions;
		
		this.prefixPositions = new ArrayList<HashMap<Object, List<Integer>>>(
				revisions.size());
		
		HashMap<Object, List<Integer>> prefixes;
		
		for(List<Object> revision : revisions){
			prefixes = buildPrefixHashMap(revision);
			prefixPositions.add(prefixes);
		}
		
		List<List<Match>> allBestMatches = new LinkedList<List<Match>>();
		
		if(!revisions.isEmpty()){
			
			List<Match> bestMatches;
			/* getBestMatches() compares every revision with previous revisions, 
			 * so it starts at targetNumber = 1.
			 */
			for(int targetNumber = 1 ; targetNumber < revisions.size() ; targetNumber++){
				bestMatches = getBestMatches(targetNumber);
				allBestMatches.add(bestMatches);
			}
		}
		return allBestMatches;
	}
	
	/**
	 * 
	 * @param targetIndex of revision to compare previous revisions with
	 * @return list of best matches for revisions that are older than the one at targetIndex
	 */
	private List<Match> getBestMatches(int targetIndex){
		
		logger.info("Get matches for revision "+targetIndex+".");
		
		List<Match> bestMatches = new LinkedList<Match>();
		List<Match> allMatches = new LinkedList<Match>();
		
		for(int sourceIndex = targetIndex-1 ; 0 <= sourceIndex ; sourceIndex--){
			allMatches.addAll(matchRevisions(sourceIndex, targetIndex));
			
			if(sourceIndex == targetIndex - maxComparisations - 1){
				break;
			}
		}
		
		Object targetElements[] = ((ArrayList<Object>) revisions.get(targetIndex)).toArray();
		
		if(!allMatches.isEmpty()){
			PriorityQueue<Match> queue = buildQueue(allMatches.size());
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

	private List<Match> matchRevisions(int sourceIndex, int targetIndex){
		
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
					
					if((length >= minMatchLength) || (currenttoken instanceof MathToken)){

						Match match = new Match(
								positionInSource, 
								positionInTarget, 
								length, sourceSize, 
								target.size(), 
								sourceIndex);
						
						matches.add(match);
					}
				}
			}
		}
		return matches;
	}
	
	private HashMap<Object, List<Integer>> buildPrefixHashMap(List<Object> elements)
	{
		HashMap<Object, List<Integer>> prefixPositions = new HashMap<Object, List<Integer>>();
		
		int position = 0;
		for(Object element : elements){
			if(prefixPositions.containsKey(element)){
				prefixPositions.get(element).add(position);
			} else {
				List<Integer> positions = new LinkedList<Integer>();
				positions.add(position);
				
				prefixPositions.put(element, positions);
			}
			position++;
		}
		return prefixPositions;
	}
	
	private PriorityQueue<Match> buildQueue(int size){
		
		PriorityQueue<Match> queue = new PriorityQueue<Match>(size, 
				(new Comparator<Match>(){
						public int compare(Match m1, Match m2){
							return m1.compareTo(m2);
						}
				}));
		return queue;
	}
}
