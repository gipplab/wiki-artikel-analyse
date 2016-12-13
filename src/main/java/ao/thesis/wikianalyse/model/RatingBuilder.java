package ao.thesis.wikianalyse.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;

/**
 * Associates ratings with revision ids. 
 * 
 * @author anna
 *
 */
public class RatingBuilder {
	
	private TreeMap<RevisionID, List<Rating>> ratings;
	
	private int size = 0;
	
	private String[] headlines = new String[0];
	
	/** Associates ratings with revision ids. 
	 * @param ids	- set of ids from all revisions that should be rated.
	 */
	public RatingBuilder(Set<RevisionID> ids){
		
		this.ratings = new TreeMap<RevisionID, List<Rating>>();
		ids.stream().filter(id -> !id.isNullRevision()).forEach(id -> ratings.put(id, new ArrayList<Rating>()));
	}
	
	/**Rates all revisions.
	 * @param ratings
	 * @return true if every revision was rated, else false.
	 */
	public boolean rateRevisions(Map<RevisionID, Rating> ratings, String[] headlines){
		if(ratings.keySet().containsAll(this.ratings.keySet())){ //ensures that every revision gets a Rating
			
			ratings.entrySet().stream().forEach(e -> addRatings(e.getKey(), e.getValue()));
			updateHeadlines(headlines);
			
			size++;
			return true;
		} else 
			return false;
	}
	
	private void updateHeadlines(String[] headlines){
		if(headlines.length == 0){
			this.headlines = headlines;
		} else {
			this.headlines = (String[]) ArrayUtils.addAll(this.headlines, headlines);
		}
	}
	
	private void addRatings(RevisionID id, Rating rating){
		ratings.get(id).add(rating);
	}
	
	public double getJudgingMeasureResult(RevisionID id, int ratingIndex) {
		return ratings.get(id).get(ratingIndex).getReputationMeasureResult();
	}

	
//	/** Getter for ratings of all revisions in the given list.
//	 * @param ids		- list of revision ids
//	 * @return rating list
//	 */
//	public List<List<Rating>> getAllRatings(List<RevisionID> ids) {
//		return ids.stream()
//				.sorted(new Comparator<RevisionID>(){
//					@Override
//					public int compare(RevisionID id1, RevisionID id2) {
//						return id1.compareTo(id2);
//					}})
//				.map(id -> getRatings(id)).collect(Collectors.toList());
//	}
	
//	/** Getter for all ratings of one (registered) editor.
//	 * @param editorid	- id of an editor
//	 * @return rating list
//	 */
//	public List<List<Rating>> getEditorRatings(BigInteger editorid){
//		return getAllRatings(ratings.keySet().stream()
//			.filter(id -> id.getEditorId().equals(editorid))
//			.sorted(new Comparator<RevisionID>(){
//				@Override
//				public int compare(RevisionID id1, RevisionID id2) {
//					return id1.compareTo(id2);
//				}})
//			.collect(Collectors.toList()));
//	}
	
	public String[] getHeadLines(){
		return headlines;
	}
	
	/** Getter for rating output lines.
	 * @param ids		- list of revision ids
	 * @return
	 */
	public Map<RevisionID, String[]> getOutput(List<RevisionID> ids){
		return 
//				TODO TreeMap<RevisionID, String[]> 
				ids.stream()
					.filter(id -> !id.isNullRevision())
					.collect(Collectors.toMap(id -> id, id -> getOutputLine(id)));
	}
	
	private String[] getOutputLine(RevisionID id){
		String[] revOutput = null;
		List<Rating> revRatings = ratings.get(id);
		
		if(!revRatings.isEmpty()){
			revOutput = revRatings.get(0).buildOutputLine();
			
			for(int index = 1 ; index < revRatings.size() ; index++){
				revOutput = ((String[]) ArrayUtils.addAll(revOutput, revRatings.get(index).buildOutputLine()));
			}
		} else {
			return new String[0];
		}
		return revOutput;
	}

	public int size() {
		return size;
	}
}
