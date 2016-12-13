package ao.thesis.wikianalyse.ratingsystems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sweble.wikitext.engine.EngineException;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.Rating;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.ratings.EditOpRating;
import ao.thesis.wikianalyse.utils.editanalyse.EditJudger;
import ao.thesis.wikianalyse.utils.editanalyse.HDDiffEditScriptBuilder;


/**Rates revisions by counting edit activities and calculating an edit distance to the previous revision.
 * Rates editors by calculating an edit longevity for all their revisions. Updates the editor reputation 
 * using the revision quality and the reputation value of the judging editor.
 * 
 * @author anna
 */
public class SwebleEditRatingSystem extends RatingSystem {
	
	private final static String folder = "/SwebleEditRatingSystem";
	private final static int judgingDistance = 10;
	
	private Map<String, EditJudger> pageJudgers = new HashMap<String, EditJudger>();
	
	public EditJudger getEditJudger(String title) {
		return pageJudgers.get(title);
	}
	
	public static void main(String[] args) throws Exception {
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output";
		(new SwebleEditRatingSystem()).run(inputdir, outputdir + folder);	
	}

	@Override
	/**Preprocesses every page by building edit scripts for all revisions.
	 */
	public void preprocess(){
		orga.getTitles().forEach(title -> preprocessPage(title));
	}
	
	/**Builds edit scripts for a page and sets a page judger.
	 * @param title		- title of the page to preprocess
	 */
	private void preprocessPage(String title){
		HDDiffEditScriptBuilder esb = new HDDiffEditScriptBuilder(orga.getPageId(title), logger);
		try {
			pageJudgers.put(title, new EditJudger(esb.buildAllEditScripts(
											orga.getSortedHistory(title).stream()
												.map(id -> orga.getEngProcessedPage(id))
												.collect(Collectors.toList()))));
		} catch (EngineException e) {
			logger.error("Page judger could not be build for page \""+title+"\".");
		}
	}
	
	@Override
	/**Processes every revision by building and storing a rating object.
	 */
	public void process(){
		if(!pageJudgers.isEmpty()){
			/* Map that stores a rate for every revision.
			 */
			Map<RevisionID, Rating> editOpRatings = new HashMap<RevisionID, Rating>(orga.getRevisionIds().size());
			for(RevisionID id : orga.getChronologicalRevisions()){
				if(!id.isNullRevision()){
					editOpRatings.put(id, rateRevision(id));
				}
			}
			rb.rateRevisions(editOpRatings, EditOpRating.revRatingHeadlines());
		} else {
			logger.error("No page judgers are set. Processing can not be done.");
		}
	}

	private EditOpRating rateRevision(RevisionID id){
		EditOpRating rating = new EditOpRating();
		pageJudgers.get(id.getPageTitle()).setValuesInEditOpRating(id.getIndex(), rating, judgingDistance);
		return rating;
	}
	
	@Override
	/**Rates every editor using the revision ratings.
	 */
	public void postprocess(){
		orga.getChronologicalRevisions().stream().filter(id -> !id.isNullRevision()).forEachOrdered(id -> setEditorReputation(id));
	}
	
	private void setEditorReputation(RevisionID id){
		List<RevisionID> judgingRevisions = orga.getJudgingRevisions(id, judgingDistance);
		if(judgingRevisions.size() == judgingDistance){
			double update = rb.getJudgingMeasureResult(id, 0);
			double judgingReputation = judgingRevisions.get(judgingDistance-1).getEditor().getReputation();
			id.getEditor().updateReputation(update, judgingReputation);
		}
//		System.out.println(id.getUsername()+" : "+id.getEditor().getReputation());
	}
}
