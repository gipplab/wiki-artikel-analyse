package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import java.util.ArrayList;
import java.util.List;

import ao.thesis.wikianalyse.model.RevisionID;


public class MathFormula extends ArrayList<MathToken> implements Token {
	
	private static final long serialVersionUID = -8447150306741131480L;
	
	private String text;
	
	private RevisionID sourceId;
	
	public MathFormula(String formula){
		text = formula;
	}

	public MathFormula(List<MathToken> formula){
		this.addAll(formula);
		
		text = "";
		formula.stream().forEachOrdered(t -> text += t.getText());
		text.trim();
	}
	
	public void setTokens(String buildXMLString) {
		this.clear();
		List<MathToken> tokens = new ArrayList<MathToken>();
		for(String text : buildXMLString.split("\\s+")){
			MathToken token = new MathToken();
			token.setText(text);
			tokens.add(token);
		}
		this.addAll(tokens);
		setSourceId(getSourceId());
	}
	
	public void setTokens(List<MathToken> list){
		this.clear();
		this.addAll(list);
		setSourceId(getSourceId()); // updates source in all new tokens
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
