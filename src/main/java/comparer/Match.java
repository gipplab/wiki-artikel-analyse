package comparer;

public class Match
{
	public int sourcePosition;
	
	public int targetPosition;

	public int length;
	
	public double relPos;
	
	public int sourceNumber;
	
	//-----------------------------------
	
	public Match(int sourcePosition, 
			int targetPosition, 
			int length, 
			int sourceTotalSize, 
			int targetTotalSize, 
			int sourceNumber)
	{
		this.sourcePosition=sourcePosition;
		this.targetPosition=targetPosition;
		this.length=length;
		
		this.relPos=calculateBlockMovement(sourceTotalSize, targetTotalSize);
		this.sourceNumber=sourceNumber;
	}
	
	private double calculateBlockMovement(int sourceSize, int targetSize)
	{
		double first = (sourcePosition+(double)(length/2))/sourceSize;
		double snd = (targetPosition+(double)(length/2))/targetSize;
		
		return -Math.abs(first-snd);
	}
}

