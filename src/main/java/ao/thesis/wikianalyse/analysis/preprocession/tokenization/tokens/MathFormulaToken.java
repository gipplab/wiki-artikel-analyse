package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

import java.util.List;
import java.util.Map;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;


public class MathFormulaToken extends ComplexToken {
	
	public MathFormulaToken(List list, String text, int position) {
		super(list, text);
		this.setPosition(position);
	}
	
	private Map<PrefixTuple, List<Integer>> prevsPrefixPositions;
	
	private int position = -1;
	
	@Override
	public boolean equals(Object token) {
		return token instanceof MathFormulaToken 
				&& ((MathFormulaToken)token).getElements().equals(this.getElements())
				&& ((MathFormulaToken)token).getText().equals(this.getText());
	}
	
	@Override
	public String toString(){
		return "MathT["+this.text+"]";
	}
	
	@Override
	public int hashCode(){
		return this.text.hashCode();
	}

	@Override
	public String getText() {
		return text;
	}

	public Map<PrefixTuple, List<Integer>> getPrevsPrefixPositions() {
		return prevsPrefixPositions;
	}

	public void setPrevsPrefixPositions(Map<PrefixTuple, List<Integer>> prevsPrefixPositions) {
		this.prevsPrefixPositions = prevsPrefixPositions;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
