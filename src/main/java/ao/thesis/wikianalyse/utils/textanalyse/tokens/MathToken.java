package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import ao.thesis.wikianalyse.model.RevisionID;

public class MathToken implements Token {

	final private String text;
	
	private RevisionID sourceId;

	public MathToken(String text){
		this.text=text;
	}

	@Override
	public boolean equals(Object c) {
		return ((c instanceof MathToken) && text.equals(((MathToken)c).getText()));
	}

	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public int hashCode() {
		return text.hashCode();
	}

	public RevisionID getSourceId() {
		return sourceId;
	}

	public void setSourceId(RevisionID sourceId) {
		this.sourceId = sourceId;
	}

}
