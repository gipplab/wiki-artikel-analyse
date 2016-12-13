package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import ao.thesis.wikianalyse.model.RevisionID;

public class MathToken implements Token {

	private String mathText = "";
	
	private RevisionID sourceId;

	public void setText(String text){
		this.mathText=text;
	}

	@Override
	public boolean equals(Object c) {
		return ((c instanceof MathToken) && mathText.equals(((MathToken)c).getText()));
	}

	@Override
	public String getText() {
		return mathText;
	}
	
	@Override
	public int hashCode() {
		return mathText.hashCode();
	}

	public RevisionID getSourceId() {
		return sourceId;
	}

	public void setSourceId(RevisionID sourceId) {
		this.sourceId = sourceId;
	}

}
