package ao.thesis.wikianalyse.model;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.sweble.wikitext.dumpreader.model.Revision;

/**
 * Identification object for a Wikipedia revision.
 * 
 * @author anna
 *
 */
public class RevisionID implements Comparable<RevisionID>{
	
	private final BigInteger id;
	
	private final DateTime timestamp;

	private final Editor editor;
	
	private final int index;
	
	private final String pageTitle;
	
	//TODO editorReputationWhenCreated
	private Map<String, Double> editorReputationWhenCreated = new HashMap<String, Double>();

	
	/** Constructor for an id of a revision that was contributed by a registered user with an id.
 	 * @param id			- revision id
	 * @param timestamp		- revision timestamp 
	 * @param editorId		- id of the contributor
	 * @param index			- index in page history
	 * @param pageTitle		- title of the associated page
	 */
	public RevisionID(BigInteger id, DateTime timestamp, String editorUsernameOrIp, BigInteger editorId, int index, String pageTitle){
		this.id=id;
		this.timestamp=timestamp;
		editor = new Editor(editorUsernameOrIp, editorId);
		this.index=index;
		this.pageTitle=pageTitle;
	}
	
	/** Constructor for an id of a revision using revision object.
	 * @param revision		- revision object
	 * @param index			- index in page history
	 * @param pageTitle		- title of the associated page
	 */
	public RevisionID(Revision revision, Editor editor, int index, String pageTitle){
		this.id = revision.getId();
		this.timestamp = revision.getTimestamp();
		this.editor = editor;
		this.index=index;
		this.pageTitle=pageTitle;
	}
	
	public static RevisionID getNullRevision(String title){
		return new RevisionID(BigInteger.ZERO, new DateTime(), "", null, -1, title);
	}
	
	public boolean isNullRevision(){
		return this.index == -1;
	}

	
	public BigInteger getId() {
		return id;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public boolean hasRegistredEditor() {
		return editor.getId() != BigInteger.ZERO;
	}

	public BigInteger getEditorId() {
		return editor.getId();
	}

	public int getIndex() {
		return index;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public String getUsername() {
		return editor.getUsername();
	}
	
	public Editor getEditor(){
		return editor;
	}
	
//	/** Sets a new reputation value for the editor of this revision.
//	 * @param name		- reputation name
//	 * @param value		- reputation value
//	 */
//	public void updateEditorReputation(String name, double value){
//		editor.setReputation(name, value);
//	}
	
//	/** Stores reputation values of the editor when the revision was created.
//	 * @param name		- reputation name
//	 * @param value		- reputation value
//	 */
//	public void setReputationWhenCreated(String name, double value){
//		editorReputationWhenCreated.put(name, value);
//	}
	
	public String[] getOutputInfoLine(){
		return new String[]{
				String.valueOf(getIndex()),
				String.valueOf(getPageTitle()),
				String.valueOf(getTimestamp()),
				getUsername()};
	}
	
	public boolean equals(RevisionID id){
		return this.getId().equals(id.getId()) 
				&& this.getPageTitle().equals(id.getPageTitle());
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}

	@Override
	public int compareTo(RevisionID other) {
		if(getPageTitle().equals(other.getPageTitle())){
			return getId().compareTo(other.getId());
		} else return getTimestamp().compareTo(other.getTimestamp());
	}
	
	@Override
	public String toString() {
		return "[RevID: "+getId()+"("+getPageTitle()+", "+getIndex()+") Cond:"+this.getUsername()+"]";
	}

}
