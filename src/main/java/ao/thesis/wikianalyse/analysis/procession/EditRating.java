package ao.thesis.wikianalyse.analysis.procession;

import java.util.List;

import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.SwebleRevision;
import ao.thesis.wikianalyse.utils.HDDiffUtils;
import ao.thesis.wikianalyse.utils.SwebleException;
import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.editscript.EditOp.Operation;
import de.fau.cs.osr.hddiff.editscript.EditOpDelete;
import de.fau.cs.osr.hddiff.editscript.EditOpInsert;
import de.fau.cs.osr.hddiff.editscript.EditOpMove;
import de.fau.cs.osr.hddiff.editscript.EditOpUpdate;
import de.fau.cs.osr.utils.visitor.VisitingException;
import joptsimple.internal.Strings;

public class EditRating {
	
	/**
	 * Average Edit Longevity
	 * 
	 * Used for Precision Recall
	 * 
	 * @param revision
	 * @param prev
	 * @param processedRevisions
	 * @param pageTitle
	 * @return
	 * @throws VisitingException
	 * @throws SwebleException
	 */
	public static double calculateAverageEditLongevity(ProcessedRevision revision, ProcessedRevision prev, ProcessedRevision[] processedRevisions, PageTitle pageTitle) 
			throws VisitingException, SwebleException{
		
		if(processedRevisions.length != 0){
			double quality = 0.0;
			List<EditOp> prevToCurrent = HDDiffUtils.buildEditScript(prev, revision, pageTitle);
			
			for(int index = 0; index < processedRevisions.length ; index++){
				ProcessedRevision judge = processedRevisions[index];
				List<EditOp> currentToJudging = HDDiffUtils.buildEditScript(revision, judge, pageTitle);
				List<EditOp> prevToJudging = HDDiffUtils.buildEditScript(prev, judge, pageTitle);
				
				quality += calculateEditLongevity(prevToCurrent, currentToJudging, prevToJudging);
			}
			
			return quality/processedRevisions.length;
		} else {
			return 0.0;
		}
	}
	
	public static double calculateEditLongevity(SwebleRevision revision, SwebleRevision prev, SwebleRevision judge, PageTitle pageTitle) 
			throws VisitingException, SwebleException{
		List<EditOp> prevToCurrent = HDDiffUtils.buildEditScript(prev, revision, pageTitle);
		List<EditOp> currentToJudging = HDDiffUtils.buildEditScript(revision, judge, pageTitle);
		List<EditOp> prevToJudging = HDDiffUtils.buildEditScript(prev, judge, pageTitle);
		return calculateEditLongevity(prevToCurrent, currentToJudging, prevToJudging);
	}
	
	/**
	 * Edit Longevity
	 * 
	 * (-1; +1)
	 * 
	 * @param prevToCurrent
	 * @param currentToJudging
	 * @param prevToJudging
	 * @return
	 */
	public static double calculateEditLongevity(List<EditOp> prevToCurrent, List<EditOp> currentToJudging, List<EditOp> prevToJudging) {
		double createdDistance;
		
		if(Double.compare(createdDistance = calculateAlternativeEditDistance(prevToCurrent), 0.0) == 0) { 
			return 0.0;
		}
		double futureDistance = calculateAlternativeEditDistance(currentToJudging);
		double totalDistance = calculateAlternativeEditDistance(prevToJudging);
		double survivedDistance = totalDistance - futureDistance;
		
		double longevity = survivedDistance / createdDistance;
		
		if(Double.compare(longevity, 1.0) > 0){
			return 1.0;
		} else if(Double.compare(longevity, -1.0) < 0){
			return -1.0;
		} else {
			return longevity;
		}
	}
	
	public static double calculateAlternativeEditDistance(List<EditOp> script) {
		
		int inserts = 0;
		int deletes = 0;
		int moves = 0;
		
		for(EditOp op : script){
				switch(op.getType()){
				case INSERT : 
					try{
						inserts += ((EditOpInsert)op).getInsertedNode().getTextContent().split("\\s+").length; 
					} catch (Exception e) {
						
					}
					break;
				case DELETE : 
					try{
						deletes += ((EditOpDelete)op).getDeletedNode().getTextContent().split("\\s+").length; 
					} catch (Exception e) {
						
					}
					break;
				case MOVE : 
					moves++;
					break;
				case UPDATE : 
					String text = "";
					String nat = "";
					try{
						
						text = ((EditOpUpdate)op).getUpdatedNode().getTextContent(); 
						nat = ((EditOpUpdate)op).getUpdatedNode().getPartner().getTextContent(); 
						
						int textL = 0;
						int natL = 0;
						
						if(Strings.isNullOrEmpty(text.trim()) && Strings.isNullOrEmpty(nat.trim())){
							break;
						} else if(Strings.isNullOrEmpty(text.trim()) && !Strings.isNullOrEmpty(nat.trim())){
							inserts += nat.split("\\s+").length;
						} else if(!Strings.isNullOrEmpty(text.trim()) && Strings.isNullOrEmpty(nat.trim())){
							deletes += text.split("\\s+").length;
						} else {
							if ((natL = nat.split("\\s+").length) == (textL = text.split("\\s+").length)){
								// update
								break;
							}
							else if ((natL = nat.split("\\s+").length) != (textL = text.split("\\s+").length)){
								inserts += nat.split("\\s+").length;
								deletes += text.split("\\s+").length;
							}
						}
						
						((EditOpInsert)op).getInsertedNode().getTextContent(); 
					} catch (Exception e) {
						
					}
				break;
			}
		}
		return (double) inserts + deletes + moves;
	}
}
