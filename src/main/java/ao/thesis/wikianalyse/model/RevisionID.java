package ao.thesis.wikianalyse.model;

import java.math.BigInteger;

import org.joda.time.DateTime;
import org.sweble.wikitext.dumpreader.model.Revision;

/**
 * Identification object for a Wikipedia revision.
 * 
 * @author anna
 *
 */
public class RevisionID {
	
	private final BigInteger id;
	
	private final DateTime timestamp;
	
	private final BigInteger editorId;
	
	private final String editorIp;
	
	private final int index;
	
	private final String pageTitle;
	
	/** Constructor for an id of a revision that was contributed by a registered user with an id.
 	 * @param id			- revision id
	 * @param timestamp		- revision timestamp 
	 * @param editorId		- id of the contributor
	 * @param index			- index in page history
	 * @param pageTitle		- title of the associated page
	 */
	public RevisionID(BigInteger id, DateTime timestamp, BigInteger editorId, int index, String pageTitle){
		this.id=id;
		this.timestamp=timestamp;
		this.editorId=editorId;
		this.editorIp=null;
		this.index=index;
		this.pageTitle=pageTitle;
	}
	
	/** Constructor for an id of a revision that was contributed by an anonymous user without an id.
 	 * @param id			- revision id
	 * @param timestamp		- revision timestamp 
	 * @param editorIp		- ip of the contributor
	 * @param index			- index in page history
	 * @param pageTitle		- title of the associated page
	 */
	public RevisionID(BigInteger id, DateTime timestamp, String editorIp, int index, String pageTitle){
		this.id=id;
		this.timestamp=timestamp;
		this.editorId=null;
		this.editorIp=editorIp;
		this.index=index;
		this.pageTitle=pageTitle;
	}
	
	/** Constructor for an id of a revision using revision object.
	 * @param revision		- revision object
	 * @param index			- index in page history
	 * @param pageTitle		- title of the associated page
	 */
	public RevisionID(Revision revision, int index, String pageTitle){
		this.id=revision.getId();
		this.timestamp=revision.getTimestamp();
		
		if(revision.getContributor() != null){
			this.editorId=revision.getContributor().getId();
			this.editorIp=null;
		} else {
			this.editorIp=revision.getContributorIp();
			this.editorId=null;
		}
		
		this.index=index;
		this.pageTitle=pageTitle;
	}

	
	public BigInteger getId() {
		return id;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public boolean hasRegistredEditor() {
		return editorId != null;
	}

	public BigInteger getEditorId() {
		return editorId;
	}
	
	public String getEditorIp() {
		return editorIp;
	}

	public int getIndex() {
		return index;
	}

	public String getPageTitle() {
		return pageTitle;
	}

}
