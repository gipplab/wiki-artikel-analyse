package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

public class StringToken extends Token {

	public StringToken(String text){
		this.text = text;
	}

	@Override
	public boolean equals(Object token) {
		return token instanceof StringToken && ((StringToken)token).getText().equals(this.getText());
	}
	
	@Override
	public String toString(){
		return "StrT["+this.text+"]";
	}
	
	@Override
	public int hashCode(){
		return this.text.hashCode();
	}

	@Override
	public int getLength() {
		return 1;
	}

}
