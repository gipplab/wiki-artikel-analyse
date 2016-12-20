package ao.thesis.wikianalyse.model;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Duration;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

/**
 * Interface for an object that organizes and provides revision ids and page information.
 * 
 * @author anna
 *
 */
public interface WikiOrga {
	
	/** Getter for ids of all revisions that were read.
	 * @return set of revision ids
	 */
	public Set<RevisionID> getRevisionIds();
	
	/** Getter for titles of all pages that were read.
	 * @return set of titles
	 */
	public Set<String> getTitles();
	
//	/** Getter for titles of all pages that were read.
//	 * @return set of titles
//	 */
//	public Set<Editor> getEditors();
	
	/** Getter for the page id object associated to the page title.
	 * @param title		- page title
	 * @return page id object
	 */
	public PageId getPageId(String title);
	
	/** Getter for revision ids representing the page history of the page with the given title.
	 * @param title		- page title
	 * @return list of all revisions in the page with the given title in chronological order
	 */
	public List<RevisionID> getSortedHistory(String title);

	/** Getter for all revision ids in chronological order.
	 * @return list of all revisions in chronological order
	 */
	public List<RevisionID> getChronologicalRevisions();
	
	/** Getter for a list of judging revisions (revision with a contributor different 
	 *  from the judged revision) that follow the revision with the given id.
	 * @param id		- id of the revision to be judged
	 * @param max		- maximal number of judgers
	 * @return list of judging revision ids
	 */
	public List<RevisionID> getJudgingRevisions(RevisionID id, int max);
	
	public List<RevisionID> getJudgedRevisions(RevisionID id, int max);
	
	/** Getter for EngProcessedPage objects associated with the given id.
	 * @param id		- revision id
	 * @return associated EngProcessedPage
	 */
	public EngProcessedPage getEngProcessedPage(RevisionID id);
	
	/** Getter for preprocessed wiki text.
	 * 
	 * @param id	- revision id
	 * @return associated wiki text
	 */
	public String getWikiText(RevisionID id);
	
	/** Getter for all EngProcessedPage objects associated with their id.
	 * @return all revisions mapped to their EngProcessedPage
	 */
	public Map<RevisionID, EngProcessedPage> getAllEngProcessedPages();

	/** Getter for the id of a revision that was created a duration after the revision 
	 *  with the given id.
	 * @param id			- revision id
	 * @param duration		- duration 
	 * @return id of revision after the duration
	 */
	public RevisionID getRevisionIDAfterDuration(RevisionID id, Duration duration);

	/** Getter for the number of wikipedia articles a(n registered) user has edited.
	 * @param editorId		- id of a registered user
	 * @return number of articles that the user has edited.
	 */
	public int getArticleCount(BigInteger editorId);
	
	public String getUserGroup(BigInteger editorId);
	
	/** Selects revision ids for each registered (!) editor in the given list and creates a map.
	 * @param revisions		- list of revision ids
	 * @return map that associates editors and revision ids from the list
	 */
	public Map<BigInteger, List<RevisionID>> mapWithEditorIds(List<RevisionID> revisions);
	
	/** Selects revision ids for each editor in the given list and creates a map.
	 * 
	 * 	Adds anonymous editors.
	 * 
	 * @param revisions		- list of revision ids
	 * @return map that associates editors and revision ids from the list
	 */
	public Map<String, List<RevisionID>> mapWithEditorName(List<RevisionID> revisions);

	public Collection<Editor> getEditors();
	
}
