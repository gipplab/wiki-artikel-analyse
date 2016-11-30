package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import ao.thesis.wikianalyse.model.RevisionID;

public interface Token {

	public String getText();

	public void setSourceId(RevisionID id);
	
	public RevisionID getSourceId();

	public int hashCode();
	
	public boolean equals(Object c);
	
}
