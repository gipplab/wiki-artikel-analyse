package ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes;


public class FormulaMatch extends Match {
	
	final private int formulaIndex;
	final private int formulaTargetPos;
	final private int formulaSourcePos;
	
	final private double jaccard;
	
	public FormulaMatch(
			int length, 
			int sPos, 
			int tPos, 
			int sTotalLen, 
			int tTotalLen, 
			int chunk, 
			int formulaIndex, 
			int targetPos, 
			int sourcePos, 
			double jaccard) {
		super(length, sPos, tPos, sTotalLen, tTotalLen, chunk);
		this.formulaIndex = formulaIndex;
		this.formulaTargetPos = targetPos;
		this.formulaSourcePos = sourcePos;
		this.jaccard=jaccard;
	}

	public int compareTo(FormulaMatch other) {
		int result;
		if((result = Integer.compare(this.getLength(), other.getLength())) == 0){
			if((result = Double.compare(this.getJaccard(), other.getJaccard())) == 0){
				if((result = Integer.compare(this.getChunk(), other.getChunk())) == 0){
					return Double.compare(this.getRelPos(), other.getRelPos());
				} else {
					return result;
				}
			} else {
				return -result;
			}
		} else {
			return -result;
		}
		
	}
	
	public boolean equals(FormulaMatch other)
	{
		return (this.getLength()==other.getLength()
				&& this.getRelPos()==other.getRelPos()
				&& this.getChunk()==other.getChunk()
				&& this.getSourcePos()==other.getSourcePos()
				&& this.getTargetPos()==other.getTargetPos()
				&& this.formulaIndex==other.getFormulaChunk()
				&& this.getFormulaSourcePos()==other.getFormulaSourcePos()
				&& this.getFormulaTargetPos()==other.getFormulaTargetPos());
	}

	public int getFormulaChunk() {
		return formulaIndex;
	}

	public int getFormulaSourcePos() {
		return formulaSourcePos;
	}

	public int getFormulaTargetPos() {
		return formulaTargetPos;
	}

	public double getJaccard() {
		return jaccard;
	}

}
