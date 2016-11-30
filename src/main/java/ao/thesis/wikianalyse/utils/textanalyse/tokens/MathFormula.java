package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import java.util.ArrayList;
import java.util.List;

import ao.thesis.wikianalyse.model.RevisionID;


public class MathFormula extends ArrayList<MathToken> implements Token {
	
	private static final long serialVersionUID = -8447150306741131480L;
	
	private String text;
	
	private RevisionID sourceId;

	public MathFormula(List<MathToken> formula){
		this.addAll(formula);
		
		text = "";
		formula.stream().forEach(t -> text += t.getText()+" ");
		text.trim();
	}
	
	@Override
	/**
	 * Sets source for all tokens in the formula.
	 */
	public void setSourceId(RevisionID id) {
		this.sourceId=id;
		for(Token token : this){
			token.setSourceId(id);
		}
	}

	@Override
	public RevisionID getSourceId() {
		return sourceId;
	}
	
	@Override
	public boolean equals(Object c) {
		return ((c instanceof MathFormula) && (((MathFormula)c).getText().equals(this.text)));
	};
	
	@Override
	public String toString() {
		return text;
	}
	
	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public String getText() {
		return text;
	}
}
