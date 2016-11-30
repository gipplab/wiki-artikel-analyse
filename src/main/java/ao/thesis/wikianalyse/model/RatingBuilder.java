package ao.thesis.wikianalyse.model;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Associates ratings with revision ids. 
 * 
 * @author anna
 *
 */
public class RatingBuilder {
	
	private HashMap<RevisionID, Rating> ratings;
	
	/** Associates ratings with revision ids. 
	 * @param ids	- set of ids from all revisions that should be rated.
	 */
	public RatingBuilder(Set<RevisionID> ids){
		
		this.ratings = new HashMap<RevisionID, Rating>();
		ids.stream().forEach(id -> ratings.put(id, null));
	}

	public void addRating(RevisionID id, Rating rating) {
		ratings.put(id, rating);
	}
	
	public Rating getRating(RevisionID id) {
		return ratings.get(id);
	}
	
//	public List<Rating> getEditorRatingsInArticle(BigInteger editorid, String title){
//		return getRevisionRatings(ratings.keySet().stream()
//			.filter(id -> (id.getEditorId().equals(editorid) && id.getPageTitle().equals(title)))
//			.sorted(new Comparator<RevisionID>(){
//				@Override
//				public int compare(RevisionID id1, RevisionID id2) {
//					return id1.getTimestamp().compareTo(id2.getTimestamp());
//				}})
//			.collect(Collectors.toList()));
//	}
	
	/** Getter for all ratings of one (registered) editor.
	 * @param editorid	- id of an editor
	 * @return rating list
	 */
	public List<Rating> getEditorRatings(BigInteger editorid){
		return getRevisionRatings(ratings.keySet().stream()
			.filter(id -> id.getEditorId().equals(editorid))
			.sorted(new Comparator<RevisionID>(){
				@Override
				public int compare(RevisionID id1, RevisionID id2) {
					return id1.getTimestamp().compareTo(id2.getTimestamp());
				}})
			.collect(Collectors.toList()));
	}
	
	/** Getter for ratings of all revisions in the given list.
	 * @param ids		- list of revision ids
	 * @return rating list
	 */
	public List<Rating> getRevisionRatings(List<RevisionID> ids) {
		return ratings.entrySet().stream()
			.filter(e -> ids.contains(e.getKey()))
			.map(e -> e.getValue()).collect(Collectors.toList());
	}
	
//	public void output(List<RevisionID> ids){
//		
//		List<String[]> list = new ArrayList<String[]>();
//		
//		ArrayList<String[]> headlines = (ArrayList<String[]>) ratings.get(0).stream()
//				.map(r -> r.buildOutputHeadlines())
//				.collect(Collectors.toList());
//		
//		list.add((String[]) headlines.toArray());
//		
//		for(RevisionID id : ids){
//			String[] column = new String[headlines.size()];
//			int pos = 0;
//			for(Rating rating : ratings.get(id)){
//				//TODO
//				column[pos] = rating.buildOutputLine()[pos];
//			}
//			list.add(column);
//		}
//	}
}
