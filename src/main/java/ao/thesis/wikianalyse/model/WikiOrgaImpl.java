package ao.thesis.wikianalyse.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.io.BarnstarReader;
import ao.thesis.wikianalyse.io.UserGroupReader;

/**
 * WikiOrga Implementation
 * 
 * @author anna
 *
 */
public class WikiOrgaImpl implements WikiOrga {
	
	private Set<RevisionID> revisionIds;
	
	private Map<String, PageId> pageIds; //title, pageId
	
	private Map<RevisionID, EngProcessedPage> engProcessedPages;
	
	private UserGroupReader userGroupReader;
	
	/**
	 * Used by InputReader
	 */
	public void setPageTitleAndId(Map<String, PageId> pageIds) {
		this.pageIds=pageIds;
	}

	/**
	 * Used by InputReader
	 */
	public void setEngProcessedPages(Map<RevisionID, EngProcessedPage> engProcessedPages) {
		this.engProcessedPages=engProcessedPages;
	}
	
	/**
	 * Used by InputReader
	 */
	public void setRevisionIDs(Set<RevisionID> revisionIds) {
		this.revisionIds=revisionIds;
	}
	
	
	public void setUsergroupReader(UserGroupReader userGroupReader) {
		this.userGroupReader=userGroupReader;
	}
	

	@Override
	public List<RevisionID> getSortedHistory(String pageTitle) {
		return revisionIds.stream()
			.filter(id -> id.getPageTitle().equals(pageTitle))
			.sorted(new Comparator<RevisionID>(){
				@Override
				public int compare(RevisionID id1, RevisionID id2) {
					return id1.compareTo(id2);
				}})
			.collect(Collectors.toList());
	}

	@Override
	public List<RevisionID> getChronologicalRevisions() {
		return revisionIds.stream()
			.sorted(new Comparator<RevisionID>(){
				@Override
				public int compare(RevisionID id1, RevisionID id2) {
					return id1.compareTo(id2);
				}})
			.collect(Collectors.toList());
	}

	@Override
	public List<RevisionID> getJudgingRevisions(RevisionID id, int max) {
		if(id.isNullRevision()) 
			return new ArrayList<RevisionID>();
		else 
		return getSortedHistory(id.getPageTitle()).stream()
				.filter(i -> !i.isNullRevision())
				.skip(id.getIndex())
				.filter(i -> !i.getEditorId().equals(id.getEditorId()))
				.limit(max)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<RevisionID> getJudgedRevisions(RevisionID id, int max) {
		if(id.isNullRevision()) 
			return new ArrayList<RevisionID>();
		else 
			return getSortedHistory(id.getPageTitle()).stream()
				.filter(i -> !i.isNullRevision())
				.limit(id.getIndex())
				.sorted(new Comparator<RevisionID>(){
					@Override
					public int compare(RevisionID id1, RevisionID id2) {
						return id2.compareTo(id1);
					}})
				.filter(i -> !i.getEditorId().equals(id.getEditorId()))
				.limit(max)
				.collect(Collectors.toList());
	};
	
	@Override
	public RevisionID getRevisionIDAfterDuration(RevisionID id, Duration duration) {
		if(!id.isNullRevision()){
			List<RevisionID> history = getSortedHistory(id.getPageTitle());
			
			DateTime thisTimestamp = new DateTime();
			thisTimestamp = history.get(id.getIndex()).getTimestamp().plus(duration);
			
			for(int index = id.getIndex() ; index < history.size()-1 ; index++){
				if(!history.get(index).isNullRevision()){
					if(history.get(index).getTimestamp().compareTo(thisTimestamp) >= 0){
						return history.get(index);
					}
				}
			}
			return null;
		}
		return null;
	}
	
	@Override
	public Map<BigInteger, List<RevisionID>> mapWithEditorIds(List<RevisionID> revisions) {
		return revisions.stream().collect(Collectors.groupingBy(RevisionID::getEditorId, Collectors.toList()));
	}
	
	@Override
	public Map<String, List<RevisionID>> mapWithEditorName(List<RevisionID> revisions) {
		return revisions.stream().collect(Collectors.groupingBy(RevisionID::getUsername, Collectors.toList()));
	}
	
//	@Override
//	public Map<String, List<RevisionID>> mapWithEditorName(List<RevisionID> revisions) {
//		return revisions.stream().collect(Collectors
//						.groupingBy(id -> (String) id.getUsername(), id -> (List<RevisionID>) filterRevisionsbyEditorName(revisions, id.getUsername())));
//	}
	
//	private List<RevisionID> filterRevisionsbyEditorID(List<RevisionID> revisions, BigInteger editorId){
//		return revisions.stream().filter(id -> id.hasRegistredEditor())
//				.filter(id -> id.getEditorId().equals(editorId))
//				.collect(Collectors.toList());
//	}
//	
//	private List<RevisionID> filterRevisionsbyEditorName(List<RevisionID> revisions, String username){
//		return revisions.stream().filter(id -> id.getUsername().equals(username))
//				.collect(Collectors.toList());
//	}


	@Override
	public int getArticleCount(BigInteger editorId) {
		return (int) pageIds.keySet().stream()
				.map(title -> getSortedHistory(title).stream()
						.filter(id -> !id.isNullRevision() && id.hasRegistredEditor())
						.anyMatch(id -> id.getEditorId().equals(editorId)))
				.count();
	}

	@Override
	public EngProcessedPage getEngProcessedPage(RevisionID id) {
		return engProcessedPages.get(id);
	}

	@Override
	public Map<RevisionID, EngProcessedPage> getAllEngProcessedPages() {
		return engProcessedPages;
	}

	@Override
	public Set<String> getTitles() {
		return pageIds.keySet();
	}

	@Override
	public PageId getPageId(String title) {
		return pageIds.get(title);
	}

	@Override
	public Set<RevisionID> getRevisionIds() {
		return revisionIds;
	}
	
	@Override
	public String getUserGroup(BigInteger editorId){
		return this.userGroupReader.getUsergroup(editorId);
	}

	@Override
	public Collection<Editor> getEditors() {
		// TODO Auto-generated method stub
		return null;
	}

}
