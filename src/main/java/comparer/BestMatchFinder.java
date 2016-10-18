package comparer;

import java.util.ArrayList;
import java.util.HashMap;

import main.Main;
import output.EditScriptWriter;
import comparer.Edit;
import comparer.EditScript;
import tokenizer.Token;

public class BestMatchFinder {
	
	private static BestMatchFinder finder = new BestMatchFinder();
	
	private static EditScriptWriter writer = EditScriptWriter.getWriter();
	
	public static BestMatchFinder getEditorFinder(){return finder;}
	
	private BestMatchFinder(){}
	
	
	public HashMap<Integer, ArrayList<EditScript>> buildEditScriptsAndSetEditors(String pageTitle, ArrayList<ArrayList<Token>> revisions)
	{
		//TODO save all edits
		
		Matcher matcher = new Matcher(revisions);
		HashMap<Integer, ArrayList<EditScript>> edits = matcher.compareAndGetEditScripts();
		
		if(Main.WRITE_EDIT_SCRIPTS)
			for(ArrayList<EditScript> targetScripts : edits.values())
				for(EditScript edit : targetScripts)
					writer.write(pageTitle, edit);
		
		
		for(int targetNumber=2; targetNumber<revisions.size();targetNumber++)
			for(int sourceNumber=1; sourceNumber<targetNumber;sourceNumber++)
			{
				EditScript script = edits.get(targetNumber).get(sourceNumber);
				for(Edit edit : script.getMoveActions())
					for(int length=0; length<edit.length; length++)
					{
						Token movedToken = revisions.get(targetNumber).get(edit.targetPosition+length);
						Token sourceToken = revisions.get(sourceNumber).get(edit.sourcePosition+length);
						
						movedToken.setSource(
								sourceToken.getContributorID(), 
								sourceToken.getSourceRevision(), 
								sourceToken.getSourcePosition());
					}
			}
		return edits;
	}
}
