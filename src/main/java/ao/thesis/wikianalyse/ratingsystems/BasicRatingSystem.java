package ao.thesis.wikianalyse.ratingsystems;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.RevisionID;


/**
 * Rates editors by counting their revisions.
 * 
 * @author anna
 *
 */
public class BasicRatingSystem extends RatingSystem {
	
	private static Logger logger = Logger.getLogger(BasicRatingSystem.class);

	
	@Override
	public void setEditVolume(String outputDir) {
	}

	@Override
	public void associateEditors(String outputDir) {
	}

	@Override
	public void rateEdits(String outputDir) {
	}

	@Override
	public void rateEditors(String outputDir) {
		
		logger.info("Rate Editors.");
		
		for(RevisionID id : orga.getChronologicalRevisions()){
			if(id.hasRegistredEditor()){
				
				//TODO editor ratings (needed?) and output
				
			}
		}
	}

}
