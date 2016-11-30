package ao.thesis.wikianalyse.utils.textanalyse.matcher;

/**
 * Represents a text match (word based) between two math formulas from different revisions. 
 * 
 * @author anna
 *
 */
public class MathMatch extends Match {
	
	public int sourceFormulaIndex;
	
	/** Represents a match between two revisions.
	 * @param sourcePosition		- starting position (word based) in source revision
	 * @param targetPosition		- starting position (word based) in target revision
	 * @param length				- length (number of words) of the match
	 * @param sourceTotalSize		- size (number of words) of source revision
	 * @param targetTotalSize		- size (number of words) of target revision
	 * @param sourceIndex			- index of the source revision in the page history
	 * @param sourceFormulaIndex	- index of the formula, representing its position in the text
	 */
	public MathMatch(
			int sourcePosition, 
			int targetPosition, 
			int length, 
			int sourceTotalSize, 
			int targetTotalSize,
			int sourceIndex,
			int sourceFormulaIndex) {
		super(sourcePosition, targetPosition, length, sourceTotalSize, targetTotalSize, sourceIndex);
		this.sourceFormulaIndex = sourceFormulaIndex;
	}
	
	public int compareTo(MathMatch other)
	{
		if(this.sourceIndex > other.sourceIndex)
			return -1;
		if(this.sourceIndex == other.sourceIndex)
		{
			if(this.length > other.length)
				return -1;
			if (other.length == this.length)
			{
				if(this.relPos < other.relPos)
					return -1;
				if(this.relPos == other.relPos)
					return 0;
			}
		}
		return 1;
	}
	
	public boolean equals(MathMatch other)
	{
		return (this.length==other.length
				&& this.relPos==other.relPos
				&& this.sourceIndex==other.sourceIndex
				&& this.sourcePosition==other.sourcePosition
				&& this.targetPosition==other.targetPosition
				&& this.sourceFormulaIndex==other.sourceFormulaIndex);
	}
}
