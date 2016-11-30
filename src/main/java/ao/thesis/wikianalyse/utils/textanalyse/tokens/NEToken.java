package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import java.util.ArrayList;
import java.util.List;

import ao.thesis.wikianalyse.model.RevisionID;

public class NEToken extends ArrayList<Token> implements Token {
	
	private static final long serialVersionUID = -6873626600885479729L;

	private final String namedEntity;
	
	private String text;

	private RevisionID sourceId;
	

	public NEToken(List<Token> tokens, String namedEntity){
		
		this.addAll(tokens);
		
		text = "";
		tokens.stream().forEach(t -> text += t.getText()+" ");
		text.trim();
		
		this.namedEntity = namedEntity;
	}
	
	public String getEntity() {
		return namedEntity;
	}

	@Override
	public boolean equals(Object c) {
		return ((c instanceof NEToken) 
				&& (this.getText().equals(((NEToken)c).getText()))
				&& (this.namedEntity.equals(((NEToken)c).getEntity())));
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setSourceId(RevisionID id) {
		this.sourceId=id;
		
	}

	@Override
	public RevisionID getSourceId() {
		return sourceId;
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}
}
