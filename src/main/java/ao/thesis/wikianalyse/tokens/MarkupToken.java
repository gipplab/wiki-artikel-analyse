package ao.thesis.wikianalyse.tokens;

public class MarkupToken extends Token {
	
	final private Markup markup;
	final private String linkreference;
	
	public MarkupToken(String content, Markup markup, String linkreference){
		super(content);
		this.markup=markup;
		this.linkreference=linkreference;
	}
	
	public Markup getMarkup(){
		return this.markup;
	}
	
	public String getLinkReference(){
		return linkreference;
	}
	
	/* Using this method leads to different editor associations;
	 * when an editor puts a link around already existing words
	 * he gets the credit for them
	 */
	@Override
	public boolean equals(Object c) {
		return ((c instanceof MarkupToken) &&
				super.equals((Token)c)
//				&& this.markup.equals(((MarkupToken)c).markup)
//				&& this.linkreference.equals(((MarkupToken)c).linkreference)
				);
	};
}
