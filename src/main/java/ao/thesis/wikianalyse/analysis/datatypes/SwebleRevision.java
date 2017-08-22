package ao.thesis.wikianalyse.analysis.datatypes;

import org.joda.time.DateTime;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

public class SwebleRevision extends PreprocessedRevision {
	
	/*
	 * Sweble and Edit Analysis Components
	 */
	
	private PageTitle pageTitle = null;
	
	private PageId pageId = null;
	
	private EngProcessedPage engProcessedPage = null;
	
	
	public SwebleRevision(int id, DateTime timestamp) {
		super(id, timestamp);
	}
	
	public SwebleRevision(PreprocessedRevision rev) {
		super(rev.getID(), rev.getTimestamp());
		super.setContributorName(rev.getContributorName());
	}
	
	
	public PageTitle getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(PageTitle pageTitle) {
		this.pageTitle = pageTitle;
	}

	public PageId getPageId() {
		return pageId;
	}

	public void setPageId(PageId pageId) {
		this.pageId = pageId;
	}

	public EngProcessedPage getEngProcessedPage() {
		return engProcessedPage;
	}

	public void setEngProcessedPage(EngProcessedPage engProcessedPage) {
		this.engProcessedPage = engProcessedPage;
	}


}
