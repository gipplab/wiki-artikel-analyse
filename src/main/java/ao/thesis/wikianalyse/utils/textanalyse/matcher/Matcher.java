package ao.thesis.wikianalyse.utils.textanalyse.matcher;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


public abstract class Matcher {
	
	protected List<HashMap<Object, List<Integer>>> prefixPositions;
	
	protected List<List<HashMap<Object, List<Integer>>>> mathPrefixPositions;
	
	
	public Comparator<MathMatch> getDefaultMathComparator(){
		return new Comparator<MathMatch>(){
			public int compare(MathMatch m1, MathMatch m2){
				return m1.compareTo(m2);
			}
		};
	}
	
	public Comparator<Match> getDefaultComparator(){
		return new Comparator<Match>(){
			public int compare(Match m1, Match m2){
				return m1.compareTo(m2);
			}
		};
	}
	
	protected HashMap<Object, List<Integer>> buildPrefixHashMap(List<Object> elements){
		HashMap<Object, List<Integer>> prefixPositions = new HashMap<Object, List<Integer>>();
		
		int position = 0;
		for(Object element : elements){
			if(!prefixPositions.containsKey(element)){
				prefixPositions.put(element, new LinkedList<Integer>());
			}
			prefixPositions.get(element).add(position);
			position++;
		}
		return prefixPositions;
	}
	
	protected PriorityQueue<Match> buildQueue(int size, Comparator<Match> comparator){
		return new PriorityQueue<Match>(size, comparator);
	}

}
