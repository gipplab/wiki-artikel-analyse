package ao.thesis.wikianalyse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.diffs.HDDiffEditScriptBuilder;
import ao.thesis.wikianalyse.judger.EditJudger;

import de.fau.cs.osr.hddiff.editscript.EditOp;

public class EditAnalysis {

	private WtEngineImpl engine;
	private static Logger logger = Logger.getLogger(EditAnalysis.class);
	
	private List<EngProcessedPage> processedRevisions;
	private HashMap<Integer, List<List<EditOp>>> editScripts;
	private HDDiffEditScriptBuilder esb;
	
	private EditJudger editJudger;
	
	public EditAnalysis(PageId pageId, WtEngineImpl engine){
		
		this.engine = engine;
		this.esb = new HDDiffEditScriptBuilder(pageId);
		
		processedRevisions = new ArrayList<EngProcessedPage>();
		addEmptyFirstRevision(pageId);
	}
	
	private void addEmptyFirstRevision(PageId pageId){
		try {
			EngProcessedPage epp = engine.postprocess(pageId, "", null);
			processedRevisions.add(epp);
			
		} catch (EngineException e) {
			logger.error("EngProcessedPage could not be generated.", e);
		}
	}

	public void addProcessedPage(EngProcessedPage epp){
		processedRevisions.add(epp);
	}

	//----------------------------------------------------------

	public void setEditScripts(){
		logger.info("Set edit scripts.");
		
		if(processedRevisions.size() == 1){
			logger.error("No processed revisions set.");
			return;
		}
		try {
			editScripts = esb.buildAllEditScripts(processedRevisions);
			
			if(editScripts.isEmpty()){
				throw new Exception("No edit scripts were created.");
			}
		} catch (Exception e) {
			logger.error("HDDiffs could not be generated.", e);
			editScripts = new HashMap<Integer, List<List<EditOp>>>();
		}
		editJudger = new EditJudger(editScripts);
		
		logger.info("Edit judger ready.");
	}
	
	//----------------------------------------------------
	
	/**Calculates an average edit quality for the given revision.
	 * @param allEditorRevisions 	- list of all revision indices (!) to be judged
	 * @param max					- distance between judged and judging revision
	 * @return average quality of the edits in the given revisions
	 */
	public double calculateEditQuality(List<Object> allEditorRevisions, int distance){
		return editJudger.calculateAverageEditLongevity(allEditorRevisions, distance);
	}
}
