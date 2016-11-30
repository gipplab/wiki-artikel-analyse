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
public class EditJudger{
	
	/** List that stores for every revision all edit scripts for all previous revisions.
	 */
	private List<List<List<EditOp>>> editScripts;
	
	private long inserts;
	
	private long deletes;
	
	private long moves;
	
	private long updates;
	
	/** Provides rating calculations for the given edit scripts.
	 * @param editScripts	- 	edit scripts for a wikipedia page
	 */
	public EditJudger(List<List<List<EditOp>>> editScripts){
		
		this.editScripts=editScripts;
	}
	
	
	public void rateWordCounts(int targetindex, int sourceindex, EditOpRating rating) {
		
		List<List<EditOp>> editsToCurrent = editScripts.get(sourceindex);
		List<List<EditOp>> editsToTarget = editScripts.get(targetindex);
		
		List<EditOp> prevToCurrent = editsToCurrent.get(sourceindex-1);
		List<EditOp> prevToTarget = editsToTarget.get(sourceindex-1);
		
		getAmountOfPersistentEdits(prevToCurrent, prevToTarget);

	}
	
	private long getAmountOfPersistentEdits(List<EditOp> prevToCurrent, List<EditOp> prevToTarget) {

		long i = prevToCurrent.stream().filter(e -> e.getType().equals(Operation.INSERT)).count();
		long d = prevToCurrent.stream().filter(e -> e.getType().equals(Operation.DELETE)).count();
		long m = prevToCurrent.stream().filter(e -> e.getType().equals(Operation.MOVE)).count();
		long u = prevToCurrent.stream().filter(e -> e.getType().equals(Operation.UPDATE)).count();
		
		long insDiff = Math.abs(i - prevToTarget.stream().filter(e -> e.getType().equals(Operation.INSERT)).count());
		long delDiff = Math.abs(d - prevToTarget.stream().filter(e -> e.getType().equals(Operation.DELETE)).count());
		long movDiff = Math.abs(m - prevToTarget.stream().filter(e -> e.getType().equals(Operation.MOVE)).count());
		long updDiff = Math.abs(u - prevToTarget.stream().filter(e -> e.getType().equals(Operation.UPDATE)).count());
		
		return insDiff + delDiff + movDiff + updDiff;
	}

	/** Inserts values into a EditOpRating object.
	 * @param index		- index of rated revision
	 * @param rating	- rating object
	 */
	public void setEditOpRating(int givenIndex, EditOpRating rating) {
		
		int index = givenIndex + 1;
		
		//TODO condition
		if(index < editScripts.size()-1){
			
			List<List<EditOp>> editsToCurrent = editScripts.get(index);
			List<EditOp> prevToCurrent = editsToCurrent.get(index-1);
			
			setEditOps(prevToCurrent);
			
			rating.setInserted(inserts);
			rating.setDeleted(deletes);
			rating.setMoved(moves);
			rating.setUpdated(updates);
		}
	}
	
	private void setEditOps(List<EditOp> edits){
		inserts = edits.stream().filter(e -> e.getType().equals(Operation.INSERT)).count();
		deletes = edits.stream().filter(e -> e.getType().equals(Operation.DELETE)).count();
		moves = edits.stream().filter(e -> e.getType().equals(Operation.MOVE)).count();
		updates = edits.stream().filter(e -> e.getType().equals(Operation.UPDATE)).count();
	}
	
	/** Calculates edit longevity for a given revision index.
	 * @param revisionIndex		- index of revision to be judged
	 * @param distance			- distance between judged and judging revision
	 * @return edit longevity for the edits in the given revision
	 */
	public double calculateEditLongevityForRevision(int givenIndex, int distance){
		//TODO should maybe also store it
		
		int revisionIndex = givenIndex + 1;
		
		if(revisionIndex < editScripts.size()-1){
			
			int targetIndex = revisionIndex + distance;
			if(targetIndex >= editScripts.size()){
				targetIndex = editScripts.size()-1;
			}
			
			List<List<EditOp>> editsToCurrent = editScripts.get(revisionIndex);
			List<List<EditOp>> editsToTarget = editScripts.get(targetIndex);
			
			List<EditOp> prevToCurrent = editsToCurrent.get(revisionIndex-1);
			List<EditOp> prevToTarget = editsToTarget.get(revisionIndex-1);
			List<EditOp> currentToTarget = editsToTarget.get(revisionIndex);
			
			return calculateEditLongevity(prevToCurrent, prevToTarget, currentToTarget);
			
		} else {
			//throw new IllegalArgumentException("Index not in revisions or to new to be judged.");
			return 0.0;
		}
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
			//throw new IllegalArgumentException("Index not in revisions or to new to be judged.");
			return 0.0;
			
		} else {
			survDistance = calculateEditDistance(prevToTarget) - calculateEditDistance(sourceToTarget);
			
			return (survDistance / createdDistance);
		}
	}

	private double calculateEditDistance(List<EditOp> edits){
		
		setEditOps(edits);
		
		return (double) Math.max(inserts, deletes) - (0.5 * (double) Math.min(inserts, deletes)) + moves;
	}
	
}
