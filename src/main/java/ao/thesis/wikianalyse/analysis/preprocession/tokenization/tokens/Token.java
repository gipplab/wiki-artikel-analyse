package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

public abstract class Token {

	protected String text;
	
	protected int sourceRevisionId = -1;
	
	public String getText(){
		return text;
	}
	
	public void setSourceId(int sourceRevisionId){
		this.sourceRevisionId = sourceRevisionId;
	}
	
	public int getSourceId(){
		return sourceRevisionId;
	}
	
	@Override
	public boolean equals(Object token) {
		return token instanceof Token && ((Token)token).getText().equals(this.getText());
	}
	
	@Override
	public String toString(){
		return "StrT["+this.text+"]";
	}
	
	@Override
	public int hashCode(){
		return this.text.hashCode();
	}

	public abstract int getLength();

}
