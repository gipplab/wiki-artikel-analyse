package ao.thesis.wikianalyse.judger;

import java.util.HashMap;
import java.util.List;

import de.fau.cs.osr.hddiff.editscript.EditOp;


public class EditJudger{
	
	HashMap<Integer, List<List<EditOp>>> editScripts;

	public EditJudger(HashMap<Integer, List<List<EditOp>>> editScripts){
		this.editScripts=editScripts;
	}
	
	/**Calculates the average edit longevity for the given revisions.
	 * @param revisionIndices	- list of revision indices
	 * @param distance			- distance between judged and judging revision
	 * @return average edit longevity for given revisions
	 */
	public double calculateAverageEditLongevity(List<Object> revisionIndices, int distance){
		
		int size;
		if((size = revisionIndices.size()) == 0){
			return 0.0;
		}
		
		double sumLongevity = 0.0;
		int notJudgedRevisions = 0;
		
		for(Object revision : revisionIndices){
			
			if(revision instanceof Integer){
				
				double longevity = 0.0;
				int indexInEditScripts = (Integer) revision + 1;
				
				try{
					longevity = calculateEditLongevityForRevision(indexInEditScripts, distance);
					
				} catch (IllegalArgumentException e){
					notJudgedRevisions++;
					break;
				}
				sumLongevity += longevity;
			}
		}
		if((size - notJudgedRevisions) == 0){
			return 0.0;
		} else {
			return (sumLongevity / (size - notJudgedRevisions));
		}
	}	
	
	/** Calculates edit longevity for a given revision index.
	 * @param revisionIndex		- index of revision to be judged
	 * @param distance			- distance between judged and judging revision
	 * @return edit longevity for the edits in the given revision
	 */
	private double calculateEditLongevityForRevision(int revisionIndex, int distance){
		
		if(revisionIndex >= editScripts.size()-1){
			throw new IllegalArgumentException("Index not in revisions or to new to be judged.");
		}
		
		int targetIndex;
		if((targetIndex = revisionIndex + distance) >= editScripts.size()){
			targetIndex = editScripts.size()-1;
		}
		
		List<List<EditOp>> editsToCurrent = editScripts.get(revisionIndex);
		List<List<EditOp>> editsToTarget = editScripts.get(targetIndex);
		
		List<EditOp> prevToCurrent = editsToCurrent.get(revisionIndex-1);
		List<EditOp> prevToTarget = editsToTarget.get(revisionIndex-1);
		List<EditOp> currentToTarget = editsToTarget.get(revisionIndex);
		
		return calculateEditLongevity(prevToCurrent, prevToTarget, currentToTarget);
	}	
	
	/** Source for calculating edit longevity: WikiTrust
	 * 
	 * @param prevToSource		- edits between judged revision and its previous revision
	 * @param prevToTarget		- edits between the previous revision and the judging revision
	 * @param sourceToTarget	- edits between the judged revision and the judging revision
	 * @return edit longevity for the given edit scripts
	 */
	private double calculateEditLongevity(List<EditOp> prevToSource,
			List<EditOp> prevToTarget, List<EditOp> sourceToTarget)
	{
		double createdDistance;
		double survDistance;
		
		if((createdDistance = calculateEditDistance(prevToSource)) == 0.0) { 
			return 0.0;
			
		} else {
			survDistance = calculateEditDistance(prevToTarget) - calculateEditDistance(sourceToTarget);
			
			return (survDistance / createdDistance);
		}
	}
	
	/**Source for calculating edit distance: WikiTrust 
	 * 
	 * @param edits		- list of edit operations
	 * @return edit distance for the given edit script
	 */
	private double calculateEditDistance(List<EditOp> edits){

		int inserts =0;
		int deletes=0;
		int moves =0;
		
		for(EditOp op : edits){
			
			switch(op.getType()){
				case DELETE:
					deletes++; 
					break;
				case INSERT:
					inserts++; 
					break;
				case MOVE:
					moves++; 
					break;
				default:
					break;
			}
		}
		return Math.max(inserts, deletes) 
				- (0.5 * Math.min(inserts, deletes)) 
				+ moves;
	}
}
