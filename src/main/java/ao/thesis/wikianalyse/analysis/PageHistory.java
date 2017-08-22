package ao.thesis.wikianalyse.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Object that represents the history of one Wikipedia article. 
 * 
 * @author anna
 *
 * @param <T> revision datatype
 */
public class PageHistory<T extends AnalysisRevision> {
	
	private List<T> revisions;
	
	public PageHistory() {}

	public List<T> getRevisions(){
		return revisions;
	}
	
	public void setRevisions(List<T> revisions){
		this.revisions=revisions;
	}
	
//	/**
//	 * Setter for a sorted list of revisions that represents the history of one Wikipedia page.
//	 * @param revisions
//	 */
//	public void setRevisions(List<T> revisions){
//		this.revisions = revisions;
//	}
	
	/**
	 * Getter for a single revision in the list
	 * @param index position of the revision in the list to return
	 * @return the revision at the given index position or null if index was out of range
	 */
	public T getRevision(int index){
		if(checkBoundaries(index)){
			return revisions.get(index);
		} 
		return null;
	}
	
	/**
	 * 
	 * @param rev
	 * @param other
	 * @return true if one of the revisions has an anonym contributor or if the contributors do not match, false else.
	 */
	private boolean hasDifferentContributor(T rev, T other){
		return ("Anonymous").equals(rev.getContributorName())
				|| ("Anonymous").equals(other.getContributorName())
				|| !rev.getContributorName().equals(other.getContributorName());
	}
	
	private boolean checkBoundaries(int index){
		return revisions != null 
				&& !revisions.isEmpty() 
				&& revisions.size() > index 
				&& index >= 0;
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public T getPrevRevision(int index) {
		if(checkBoundaries(index) && checkBoundaries(index-1)){
			return revisions.get(index-1);
		} else {
			return null;
		}
		
//		if(checkBoundaries(index)){
//			T curr = revisions.get(index);
//			T prev;
//			ListIterator<T> it = revisions.listIterator(index);
//			while(it.hasPrevious()){
//				prev = it.previous();
//				if(prev.getContributorName() == null){
//					return prev;
//					//TODO redundant if filtered
//				} else if(prev.getContributorName().equals(curr.getContributorName())){
//					continue;
//				} else 
//					return prev;
//			}
//		}
//		return null;
	}
	
//	/**
//	 * 
//	 * @param index
//	 * @param maxLength
//	 * @return
//	 */
//	public List<T> getPrevRevisions(int index, int maxLength) {
//		if(!checkBoundaries(index) || maxLength <= 0){
//			return null;
//		}
//		if(index < maxLength){
//			return revisions.subList(0, index);
//		} else {
//			return revisions.subList(index-maxLength, index);
//		}
//	}
	
	/**
	 * 
	 * @param index
	 * @param maxLength
	 * @return cannot be null
	 */
	public List<T> getJudgedRevisions(int index, int maxLength) {
		List<T> judged = new ArrayList<>();
		if(!checkBoundaries(index) || maxLength <= 0){
			return judged;
		}
		T revision = revisions.get(index);
		ListIterator<T> it = revisions.listIterator(index);
		int counter = 0;
		T prev;
		while(counter<maxLength && it.hasPrevious()){
			prev = it.previous();
			// filter revisions with matching contributors
			if(hasDifferentContributor(revision, prev)){
				judged.add(prev);
				counter++;
			}
		}
		return judged;
	}
	
	/**
	 * 
	 * @param index
	 * @param maxLength
	 * @return cannot be null
	 */
	public List<T> getJudgingRevisions(int index, int maxLength) {
		List<T> judges = new ArrayList<>();
		if(!checkBoundaries(index) || maxLength <= 0){
			return judges;
		}
		T revision = revisions.get(index);
		Iterator<T> it = revisions.listIterator(index);
		int counter = 0;
		T next;
		if(it.hasNext()){
			// next is now the current revision
			next = it.next();
		}
		while(counter<maxLength && it.hasNext()){
			next = it.next();
			// filter revisions with matching contributors
			if(hasDifferentContributor(revision, next)){
				judges.add(next);
				counter++;
			}
		}
		return judges;
	}
	
	/**
	 * 
	 * @param index
	 * @param duration
	 * @return
	 */
	public T getRevisionAfterDuration(int index, Duration duration) {
		T revBefore;
		T revAfter;
		if((revBefore = getRevision(index)) != null){
			//TODO check if correct
			DateTime thisTimestamp = revBefore.getTimestamp().plus(duration);
			for(int i = index ; i < revisions.size() - 1 ; i++){
				if((revAfter = getRevision(i)).getTimestamp().compareTo(thisTimestamp) >= 0){
					return revAfter;
				}
			}
		}
		return null;
	}

	public List<T> getRevisionsAfterDuration(int index, Duration duration, int maxLength) {
		List<T> judges = new ArrayList<>();
		T revBefore;
		if((revBefore = getRevision(index)) != null){
			DateTime thisTimestamp = revBefore.getTimestamp().plus(duration);
			for(int i = index ; i < revisions.size() - 1 ; i++){
				
				if(getRevision(i).getTimestamp().compareTo(thisTimestamp) >= 0){
					Iterator<T> it = revisions.listIterator(i);
					int counter = 0;
					T next;
					while(counter < maxLength && it.hasNext()){
						next = it.next();
						// filter revisions with matching contributors
						if(hasDifferentContributor(revBefore, next)){
							judges.add(next);
							counter++;
						}
					}
				}
				
			}
		}
		return judges;
	}
	
	
	public List<T> getRevisionsBeforeDuration(int index, Duration duration, int maxLength) {
		List<T> judges = new ArrayList<>();
		T revAfter;
		if((revAfter = getRevision(index)) != null){
			DateTime thisTimestamp = revAfter.getTimestamp().minus(duration);
			for(int i = index ; i >= 0 ; i--){
				
				if(getRevision(i).getTimestamp().compareTo(thisTimestamp) <= 0){
					ListIterator<T> it = revisions.listIterator(i);
					int counter = 0;
					T prev;
					while(counter < maxLength && it.hasPrevious()){
						prev = it.previous();
						// filter revisions with matching contributors
						if(hasDifferentContributor(revAfter, prev)){
							judges.add(prev);
							counter++;
						}
					}
				}
				
			}
		}
		return judges;
	}
}
