package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

import java.util.List;

public class ComplexToken extends Token {
	
	private final List elements;
	
	private int length;
	
	public ComplexToken(List list, String text){
		this.elements=list;
		this.text = text;
		
		setLength();
	}
	
	private void setLength(){
		length = 0;
		for(Object obj : elements){
			Token token = (Token) obj;
			length += token.getLength();
		}
	}

	public List getElements() {
		return elements;
	}
	
	@Override
	public void setSourceId(int sourceRevisionId){
		super.setSourceId(sourceRevisionId);
		for(Object obj : elements){
			Token token = (Token) obj;
			token.setSourceId(sourceRevisionId);
		}
	}
	
	@Override
	public boolean equals(Object token) {
		return token instanceof ComplexToken 
				&& ((ComplexToken)token).getElements().equals(this.getElements())
				&& ((ComplexToken)token).getText().equals(this.getText());
	}
	
	@Override
	public String toString(){
		return "CT["+this.text+"]";
	}
	
	@Override
	public int hashCode(){
		return this.text.hashCode();
	}

	@Override
	public int getLength() {
		return length;
	}
	
}
