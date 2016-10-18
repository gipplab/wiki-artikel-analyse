package comparer;

public class Edit {
	
	public int sourcePosition; //-1 if edit is insertion
	
	public int targetPosition; //-1 if edit is deletion
	
	public int length;
	
	public Edit(int sourcePosition, int targetPosition, int length)
	{
		this.sourcePosition=sourcePosition;
		this.targetPosition=targetPosition;
		this.length=length;
	}
	
}
