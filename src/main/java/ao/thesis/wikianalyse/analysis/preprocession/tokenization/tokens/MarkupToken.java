package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

import java.util.List;

public class MarkupToken extends ComplexToken{
	
	private final Markup markup;
	
	private String linkreference = "";
	
	public MarkupToken(List list, String text, Markup markup){
		super(list, text);
		this.markup=markup;
	}
	
	public MarkupToken(List list, String text, Markup markup, String reference){
		super(list, text);
		this.markup=markup;
		this.linkreference=reference;
	}

	public Markup getMarkup(){
		return this.markup;
	}
	
	public String getLinkReference(){
		return linkreference;
	}
	
	@Override
	public String toString(){
		return "MT["+this.text+"; "+this.markup+"; "+this.linkreference+"]";
	}
	
	/* 
	 * Using this method leads to different editor associations;
	 * when an editor puts a link around already existing words
	 * he gets credit for them
	 */
	@Override
	public boolean equals(Object token) {
		return token instanceof MarkupToken 
				&& ((MarkupToken)token).getText().equals(this.getText())
				&& ((MarkupToken)token).getMarkup().equals(this.getMarkup())
				&& ((MarkupToken)token).getLinkReference().equals(this.getLinkReference());
	}
	
	@Override
	public int hashCode(){
		return (this.text + this.linkreference + this.markup.toString()).hashCode();
	}

}
