package ao.thesis.wikianalyse.utils.editanalyse;

import java.util.List;

import ao.thesis.wikianalyse.model.ratings.EditOpRating;
import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.editscript.EditOp.Operation;


/**
 * Provides rating calculations for the given edit scripts using revision indices.
 * 
 * @author anna
 *
 */
public class EditJudger {
	
	/** List that stores for every revision all edit scripts for all previous revisions.
	 */
	private List<List<List<EditOp>>> editScripts;
	
	/** Provides rating calculations for the given edit scripts.
	 * @param editScripts	- 	edit scripts for a wikipedia page
	 */
	public EditJudger(List<List<List<EditOp>>> editScripts){
		this.editScripts = editScripts;
	}
	
	public double calculateEditDistance(int targetindex){
//		targetindex++;
		return calculateEditDistance(targetindex, targetindex-1);
	}
	
	public double calculateEditDistance(int targetindex, int sourceindex){
		targetindex++;
		sourceindex++;
		return calculateEditDistance(editScripts.get(targetindex).get(sourceindex));
	}
	
	public int getEditSize(int targetindex){
//		targetindex++;
		return getOpCount(targetindex, Operation.INSERT) + getOpCount(targetindex, Operation.DELETE)
				+ getOpCount(targetindex, Operation.MOVE) + getOpCount(targetindex, Operation.UPDATE);
	}
	
	public int getEditSize(int targetindex, int sourceindex){
//		targetindex++;
//		sourceindex++;
		return getOpCount(targetindex, sourceindex, Operation.INSERT) + getOpCount(targetindex, sourceindex, Operation.DELETE)
				+ getOpCount(targetindex, sourceindex, Operation.MOVE) + getOpCount(targetindex, sourceindex, Operation.UPDATE);
	}
	
	public int getOpCount(int targetindex, Operation op){
		targetindex++;
		return getOpCount(editScripts.get(targetindex).get(targetindex-1), op);
	}
	
	public int getOpCount(int targetindex, int sourceindex, Operation op){
		targetindex++;
		sourceindex++;
		return getOpCount(editScripts.get(targetindex).get(sourceindex), op);
	}

	/** Inserts edit ops into a EditOpRating object.
	 * @param index		- index of rated revision
	 * @param rating	- rating object
	 */
	public void setValuesInEditOpRating(int index, EditOpRating rating, int judgingDistance) {
//		index++;
		//TODO condition
		
		if(index < editScripts.size()-1){
			
//			List<List<EditOp>> editsToCurrent = editScripts.get(index);
//			List<EditOp> prevToCurrent = editsToCurrent.get(index-1);
			
			rating.setInserted(getOpCount(index, Operation.INSERT));
			rating.setDeleted(getOpCount(index, Operation.DELETE));
			rating.setMoved(getOpCount(index, Operation.MOVE));
			rating.setUpdated(getOpCount(index, Operation.UPDATE));
			rating.setEditLongevity(this.calculateEditLongevity(index, judgingDistance));
		}
	}
	
	/** Calculates edit longevity for a given revision index.
	 * @param revisionIndex		- index of revision to be judged
	 * @param distance			- distance between judged and judging revision
	 * @return edit longevity for the edits in the given revision
	 */
	public double calculateEditLongevity(int index, int distance){
		index++;
		if(index < editScripts.size()-1){
			
			int targetIndex = index + distance;
			if(targetIndex >= editScripts.size()){
				targetIndex = editScripts.size()-1;
			}
			
			List<List<EditOp>> editsToCurrent = editScripts.get(index);
			List<List<EditOp>> editsToTarget = editScripts.get(targetIndex);
			
			List<EditOp> prevToCurrent = editsToCurrent.get(index-1);
			List<EditOp> prevToTarget = editsToTarget.get(index-1);
			List<EditOp> currentToTarget = editsToTarget.get(index);
			
			return calculateEditLongevity(prevToCurrent, prevToTarget, currentToTarget);
			
		} else {
			return 0.0;
		}
	}	
	
	
	private int getOpCount(List<EditOp> editScript, Operation op){
		return (int) editScript.stream()
				.filter(e -> e.getType().equals(op))
				.count();
	}
	
	/** Source for calculating edit longevity: WikiTrust
	 * 
	 * @param prevToSource		- edits between judged revision and its previous revision
	 * @param prevToTarget		- edits between the previous revision and the judging revision
	 * @param sourceToTarget	- edits between the judged revision and the judging revision
	 * @return edit longevity for the given edit scripts
	 */
	private double calculateEditLongevity(List<EditOp> prevToSource, List<EditOp> prevToTarget, List<EditOp> sourceToTarget){
		
		double createdDistance;
		double survDistance;
		
		if((createdDistance = calculateEditDistance(prevToSource)) == 0.0) { 
			return 0.0;
			
		} else {
			survDistance = calculateEditDistance(prevToTarget) - calculateEditDistance(sourceToTarget);
			return (survDistance / createdDistance);
		}
	}
	
	public double calculateEditDistance(List<EditOp> edits){
		
		int inserts = getOpCount(edits, Operation.INSERT) + getOpCount(edits, Operation.UPDATE);
		int deletes = getOpCount(edits, Operation.DELETE) + getOpCount(edits, Operation.UPDATE);
		int moves = getOpCount(edits, Operation.MOVE);
		
		return (double) Math.max(inserts, deletes) - (0.5 * (double) Math.min(inserts, deletes)) + moves;
	}
	
}
