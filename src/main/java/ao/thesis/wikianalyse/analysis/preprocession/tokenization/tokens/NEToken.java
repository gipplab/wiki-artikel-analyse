package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

import java.util.List;

public class NEToken extends ComplexToken {

	private final String namedEntity;
	
	public NEToken(List list, String text, String namedEntity) {
		super(list, text);
		this.namedEntity=namedEntity;
	}

	@Override
	public boolean equals(Object token) {
		return token instanceof NEToken 
				&& ((NEToken)token).getText().equals(this.getText())
				&& ((NEToken)token).getEntity().equals(this.getEntity());
	}
	
	@Override
	public String toString(){
		return "NET["+this.text+"; "+this.namedEntity+"]";
	}
	
	@Override
	public int hashCode(){
		return (this.text + this.namedEntity).hashCode();
	}

	public String getEntity() {
		return namedEntity;
	}
}
