package ao.thesis.wikianalyse.matcher;

public class Match
{
	public int sourcePosition;
	
	public int targetPosition;

	public int length;
	
	public double relPos;
	
	public int sourceIndex;
	
	int sourceTotalSize;
	
	int targetTotalSize;
	
	public Match(int sourcePosition, 
			int targetPosition, 
			int length, 
			int sourceTotalSize, 
			int targetTotalSize, 
			int sourceIndex)
	{
		this.sourcePosition=sourcePosition;
		this.targetPosition=targetPosition;
		this.targetTotalSize=targetTotalSize;
		this.sourceTotalSize=sourceTotalSize;
		this.length=length;
		
		this.relPos=calculateBlockMovement(sourceTotalSize, targetTotalSize);
		this.sourceIndex=sourceIndex;
	}
	
	private double calculateBlockMovement(int sourceSize, int targetSize)
	{
		double first = (sourcePosition+(double)(length/2))/sourceSize;
		double snd = (targetPosition+(double)(length/2))/targetSize;
		
		return -Math.abs(first-snd);
	}
	
	public int compareTo(Match other)
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
	
	public boolean equals(Match other)
	{
		return (this.length==other.length
				&& this.relPos==other.relPos
				&& this.sourceIndex==other.sourceIndex
				&& this.sourcePosition==other.sourcePosition
				&& this.targetPosition==other.targetPosition);
	}
	
	public String toString()
	{
		return ("Match["+this.sourcePosition+", "
					+this.targetPosition+", "
					+this.length+", "
					+this.sourceTotalSize+", "
					+this.targetTotalSize+", "
					+this.sourceIndex+"]");
	}
}

