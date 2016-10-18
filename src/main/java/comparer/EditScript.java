package comparer;

import java.util.ArrayList;
import java.util.Comparator;

public class EditScript extends ArrayList<Edit>{
	
	public int source;
	
	public int target;
	
	public EditScript(int source, int target)
	{
		this.source=source;
		this.target=target;
	}
	
	public void addEdit(int sourcePosition, int targetPosition, int lengthInWords)
	{
		this.add(new Edit(sourcePosition, targetPosition, lengthInWords));
	}
	
	public void sortPositions(){
		this.sort(new Comparator<Edit>(){
		
			public int compare(Edit e1, Edit e2) {
				if(e1.targetPosition<e2.targetPosition)
					return -1;
				if(e1.targetPosition==e2.targetPosition)
					return 0;
				else return 1;
			}
		});
	}
	
	
	public ArrayList<Edit> getMoveActions()
	{
		ArrayList<Edit> moves = new ArrayList<Edit>();
		for(Edit edit : this)
			if(edit.sourcePosition!=-1 && edit.targetPosition!=-1)
				moves.add(edit);
		return moves;
	}
	
	public ArrayList<Edit> getInsertions()
	{
		ArrayList<Edit> inserts = new ArrayList<Edit>();
		for(Edit edit : this)
			if(edit.sourcePosition==-1)
				inserts.add(edit);
		return inserts;
	}
	
	public ArrayList<Edit> getDeletions()
	{
		ArrayList<Edit> deletes = new ArrayList<Edit>();
		for(Edit edit : this)
			if(edit.targetPosition==-1)
				deletes.add(edit);
		return deletes;
	}
}