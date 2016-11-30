package ao.thesis.wikianalyse.ratingsystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.ratings.EditOpRating;
import ao.thesis.wikianalyse.utils.editanalyse.EditJudger;
import ao.thesis.wikianalyse.utils.editanalyse.HDDiffEditScriptBuilder;


/** 
 * Rates editors by their edit activities using sweble hddiff.
 * 
 * @author anna
 *
 */
public class SwebleEditRatingSystem extends RatingSystem {
	
	private static Logger logger = Logger.getLogger(SwebleEditRatingSystem.class);
	
	
	private Map<String, EditJudger> pageJudgers = new HashMap<String, EditJudger>();
	
	private static WikiConfig config = DefaultConfigEnWp.generate(); //TODO setup in WikiOrga
	private static WtEngineImpl engine = new WtEngineImpl(config); //TODO setup in WikiOrga
	
	
	public static void main(String[] args) throws Exception {
		
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output"+"/SwebleEditSys";
		
		RatingSystem system = new SwebleEditRatingSystem();
		system.run(inputdir, outputdir);
		
	}

	@Override
	public void setEditVolume(String outputDir) {
		
		logger.info("Start setting processed pages.");
		
		for(String title : orga.getTitles()){

			HDDiffEditScriptBuilder esb = new HDDiffEditScriptBuilder(orga.getPageId(title), outputDir, logger);
			
			List<EngProcessedPage> engProcessedPages = new ArrayList<EngProcessedPage>();

			try {
				//TODO setup in WikiOrga
				engProcessedPages.add(engine.postprocess(orga.getPageId(title),"", null));
				
				engProcessedPages.addAll(orga.getSortedHistory(title).stream().map(id -> orga.getEngProcessedPage(id)).collect(Collectors.toList()));
				
				pageJudgers.put(title, new EditJudger(esb.buildAllEditScripts(engProcessedPages)));
			
			} catch (EngineException e) {
				logger.error("Edit judgers could not be set.");
				break;
			}
		}
	}

	@Override
	public void associateEditors(String outputDir) {
		
	}

	@Override
	public void rateEdits(String outputDir) {
		
		if(!pageJudgers.isEmpty()){
			
			logger.info("Rate Edits.");
			
			for(String title : orga.getTitles()){
				
				for(RevisionID id : orga.getSortedHistory(title)){
					
					EditOpRating eor = new EditOpRating();
	
					pageJudgers.get(title).setEditOpRating(id.getIndex(), eor);
					
					rb.addRating(id, eor);
				}
			}
		} else {
			logger.error("No pages are set.");
		}
	}

	@Override
	public void rateEditors(String outputDir) {
		
		logger.info("Rate Editors.");
		
		for(RevisionID id : orga.getChronologicalRevisions()){
			if(id.hasRegistredEditor()){
				
				//TODO editor ratings and output
				
			}
		}
	}

	public EditJudger getEditJudger(String title) {
		
		if(!pageJudgers.isEmpty()){
			
			return pageJudgers.get(title);
		} else {
			
			logger.error("EditJudger is not set yet.");
			return null;
		}
	}
}
