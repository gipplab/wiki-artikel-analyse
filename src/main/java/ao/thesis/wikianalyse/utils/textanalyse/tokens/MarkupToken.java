package ao.thesis.wikianalyse.utils.textanalyse.tokens;

import ao.thesis.wikianalyse.model.RevisionID;

public class MarkupToken implements Token {
	
	final private Markup markup;
	final private String linkreference;
	final private String text;
	
	private RevisionID sourceId;
	
	public MarkupToken(String text, Markup markup, String linkreference){
		this.text=text;
		this.markup=markup;
		this.linkreference=linkreference;
	}
	
	public Markup getMarkup(){
		return this.markup;
	}
	
	public String getLinkReference(){
		return linkreference;
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public void setSourceId(RevisionID id) {
		this.sourceId=id;
		
	}

	@Override
	public RevisionID getSourceId() {
		return sourceId;
	}
	
	/* Using this method leads to different editor associations;
	 * when an editor puts a link around already existing words
	 * he gets credit for them
	 */
	@Override
	public boolean equals(Object c) {
		return ((c instanceof MarkupToken) &&
				text.equals(((MarkupToken)c).getText())
//				&& this.markup.equals(((MarkupToken)c).markup)
//				&& this.linkreference.equals(((MarkupToken)c).linkreference)
				);
	}
}
