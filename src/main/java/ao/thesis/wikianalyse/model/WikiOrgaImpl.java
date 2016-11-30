package ao.thesis.wikianalyse.model;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

/**
 * WikiOrga Implementation
 * 
 * @author anna
 *
 */
public class WikiOrgaImpl implements WikiOrga {
	
	private Set<RevisionID> revisionIds;
	
	private Map<String, PageId> pageInfos;
	
	private Map<RevisionID, EngProcessedPage> engProcessedPages;
	
	/**
	 * Used by InputReader
	 */
	public void setPageTitleAndId(Map<String, PageId> pageInfos) {
		this.pageInfos=pageInfos;
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
	

	@Override
	public List<RevisionID> getSortedHistory(String pageTitle) {
		return revisionIds.stream()
			.filter(id -> id.getPageTitle().equals(pageTitle))
			.sorted(new Comparator<RevisionID>(){
				@Override
				public int compare(RevisionID id1, RevisionID id2) {
					return Integer.compare(id1.getIndex(),id2.getIndex());
				}})
			.collect(Collectors.toList());
	}

	@Override
	public List<RevisionID> getChronologicalRevisions() {
		return revisionIds.stream()
			.sorted(new Comparator<RevisionID>(){
				@Override
				public int compare(RevisionID id1, RevisionID id2) {
					return id1.getTimestamp().compareTo(id2.getTimestamp());
				}})
			.collect(Collectors.toList());
	}

	@Override
	public List<RevisionID> getJudgingRevisions(RevisionID id, int max) {
		return getSortedHistory(id.getPageTitle()).stream()
			.skip(id.getIndex())
			.filter(i -> !i.getEditorId().equals(id.getEditorId()))
			.limit(max)
			.collect(Collectors.toList());
	}
	
	@Override
	public RevisionID getRevisionIDAfterDuration(RevisionID id, Duration duration) {
		List<RevisionID> history = getSortedHistory(id.getPageTitle());
		
		DateTime thisTimestamp = new DateTime();
		thisTimestamp = history.get(id.getIndex()).getTimestamp().plus(duration);
		
		for(int index = id.getIndex() ; index < history.size() ; index++){
			if(history.get(index).getTimestamp().compareTo(thisTimestamp) >= 0){
				return history.get(index);
			}
		}
		return null;
	}
	
	@Override
	public Map<BigInteger, List<RevisionID>> mapWithEditorIds(List<RevisionID> revisions) {
		return revisions.stream()
				.collect(Collectors
						.toMap(id -> (BigInteger)id.getEditorId(), id -> (List<RevisionID>) filterRevisionsbyEditor(revisions, id.getEditorId())));
	}
	
	private List<RevisionID> filterRevisionsbyEditor(List<RevisionID> revisions, BigInteger editorId){
		return revisions.stream().filter(id -> id.hasRegistredEditor())
				.filter(id -> id.getEditorId().equals(editorId))
				.collect(Collectors.toList());
	}


	@Override
	public int getArticleCount(BigInteger editorId) {
		return (int) pageInfos.keySet().stream()
			.map(title -> getSortedHistory(title).stream().anyMatch(id -> id.getEditorId().equals(editorId)))
			.filter(matches -> matches==true)
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
		return pageInfos.keySet();
	}

	@Override
	public PageId getPageId(String title) {
		return pageInfos.get(title);
	}

	@Override
	public Set<RevisionID> getRevisionIds() {
		return revisionIds;
	}

}
