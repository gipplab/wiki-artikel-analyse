package ao.thesis.wikianalyse.tokens;

public class MathToken extends Token {
	
	final private String completeFormula;
	final private int positionInFormula;

	public MathToken(String content, String completeFormula,
			int positionInFormula){
		super(content);
		this.completeFormula=completeFormula;
		this.positionInFormula=positionInFormula;
	}
	
	public String getFormula(){
		return completeFormula;
	}
	
	public int getPositionInFormula(){
		return positionInFormula;
	}
	
	@Override
	public boolean equals(Object c) {
		return ((c instanceof MathToken) &&
				super.equals((Token)c) &&
				//this.completeFormula.equals(((MathToken)c).completeFormula) &&
				this.positionInFormula == ((MathToken)c).positionInFormula);
	};

}
