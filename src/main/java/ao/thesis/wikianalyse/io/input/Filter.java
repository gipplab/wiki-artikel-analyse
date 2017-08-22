package ao.thesis.wikianalyse.io.input;

import java.util.List;
import java.util.Objects;

import org.joda.time.DateTime;
import org.jsoup.helper.Validate;
import org.sweble.wikitext.dumpreader.model.Contributor;
import org.sweble.wikitext.dumpreader.model.Revision;

/**
 * Filter
 * 
 * This class filters Revision objects that are read by the InputReader.
 * 
 * @author Anna Opaska
 *
 */
class Filter {
	
	private DateTime limitDate;

	/**
	 * Constructor
	 * 
	 * @param limitYear 
	 */
	Filter(DateTime limitDate){	
		Validate.notNull(limitDate);
		this.limitDate = limitDate;
	}
	
	public DateTime getLimitDate(){
		return this.limitDate;
	}
	
	/**
	 * Filters the given List of Revision objects.
	 * 
	 * @param revisions	List of chronologically sorted Revision objects as read by the InputReader
	 * @param limitYear 
	 * @return the filtered chronologically sorted List of Revision objects
	 */
	List<Revision> filter(List<Revision> revisions){

		for(int index = 0; index < revisions.size(); index++){
			Revision rev = revisions.get(index);
			
			if(rev.getTimestamp().isAfter(limitDate)){
				return revisions.subList(0, index);
			}
			/*
			 * Revisions that are followed by a Revision by the same Contributor are filtered
			 * See also: WikiTrust
			 */
			Contributor curr = rev.getContributor();
			Contributor prev;
			
			if(index > 0 
					&& Objects.nonNull(curr) 
					&& Objects.nonNull(prev = revisions.get(index-1).getContributor()) 
					&& curr.getUsername().equals(prev.getUsername())){
				revisions.remove(index-1);
				index--;
			}
		}
		return revisions;
	}

}
