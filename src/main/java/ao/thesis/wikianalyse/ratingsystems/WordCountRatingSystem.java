package ao.thesis.wikianalyse.ratingsystems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.io.OutputWriter;
import ao.thesis.wikianalyse.model.Rating;
import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.ratings.VandalismRating;
import ao.thesis.wikianalyse.utils.editanalyse.EditJudger;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.TextJudger;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/**Uses SwebleEditSystem and TokenCountEditSystem to rate revisions and editors 
 * and build one output.
 * 
 * @author anna
 *
 */
public class WordCountRatingSystem extends RatingSystem {
	
	private final static String folder = "/WordCountRatingSystem";
	
	private TokenCountRatingSystem mcrs;
	private SwebleEditRatingSystem sers;
	
	public WordCountRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		mcrs = new TokenCountRatingSystem(stopWords, classifier);
		sers = new SwebleEditRatingSystem();
	}
	
//	public WordCountRatingSystem(TokenCountRatingSystem mcrs, SwebleEditRatingSystem sers){
//		this.mcrs = mcrs;
//		this.sers = sers;
//	}
	
	
	public static void main(String[] args) throws Exception {
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output";
		
		String sw = System.getProperty("user.dir")+"/resources/engStopWords";
		String c = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(sw, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(c);
		
		RatingSystem system = new WordCountRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir + folder);
	}
	
	@Override
	public void setWikiOrga(WikiOrga orga){
		this.orga = orga;
		mcrs.setWikiOrga(orga);
		sers.setWikiOrga(orga);

		RatingBuilder rb = mcrs.getRatingBuilder();
		sers.setRatingBuilder(rb);
		setRatingBuilder(rb);
	}
	
	@Override
	public void setOutputWriter(OutputWriter ow){
		this.ow = ow;
		mcrs.setOutputWriter(ow);
		sers.setOutputWriter(ow);
	}
	
	
	@Override
	public void preprocess() {
		mcrs.preprocess();
		sers.preprocess();
	}

	@Override
	public void process() {
		mcrs.process();
		sers.process();
		
		/* WikiTrust defines vandalism as revisions that receive an edit longevity < -0.9 
		 * and a text decay quality < 0.05
		 */
		Map<RevisionID, Rating> vandalismRatings = new HashMap<RevisionID, Rating>();
		
		for(RevisionID id : orga.getChronologicalRevisions()){
			if(!id.isNullRevision()){
				
				double editLongevity = rb.getJudgingMeasureResult(id, 1);
				double textDecayQuality = rb.getJudgingMeasureResult(id, 0);
				
				VandalismRating rating = new VandalismRating();
				rating.setVandalism((editLongevity < -0.9) && (textDecayQuality < 0.05));
				vandalismRatings.put(id, rating);
			}
		}
		rb.rateRevisions(vandalismRatings, VandalismRating.buildOutputHeadlines());
	}

	@Override
	public void postprocess(){
		//TODO judge editors
	}


//	TextJudger getPreMatchedTextJudger(RevisionID id){
//		return mcrs.getPreMatchedTextJudger(id);
//	}
	
	TextJudger getPostMatchedTextJudger(RevisionID id){
		return mcrs.getPostMatchedTextJudger(id);
	}
	
	EditJudger getEditJudger(String title){
		return sers.getEditJudger(title);
	}
}