package ao.thesis.wikianalyse.ratingsystems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.io.OutputWriter;
import ao.thesis.wikianalyse.model.Rating;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.ratings.TextPersistenceRating;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/**
 * @author anna
 *
 */
public class TextPersistenceRatingSystem extends RatingSystem {
	
	private static String folder = "/TextPersistenceRatingSystem";
	
	private final double survFactor = 0.6;
	
	private WordCountRatingSystem mcrs;
	
	List<Token> getInsertedTokens(RevisionID id, RevisionID sourceId){
		return mcrs.getPostMatchedTextJudger(id).getInsertedTokens(sourceId);
	}
	
	double getEditLongevity(RevisionID id, RevisionID sourceId){
		return mcrs.getEditJudger(id.getPageTitle()).calculateEditLongevity(sourceId.getIndex(), id.getIndex() - sourceId.getIndex());
	}
	
	public TextPersistenceRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		mcrs = new WordCountRatingSystem(stopWords, classifier);
	}
	
//	public TextPersistenceRatingSystem(WordCountRatingSystem mcrs){
//		this.mcrs = mcrs;
//	}
	
	@Override
	public void setWikiOrga(WikiOrga orga){
		this.orga = orga;
		mcrs.setWikiOrga(orga);
		setRatingBuilder(mcrs.getRatingBuilder());
	}
	
	@Override
	public void setOutputWriter(OutputWriter ow){
		this.ow = ow;
		mcrs.setOutputWriter(ow);
	}
	
	public static void main(String[] args) throws Exception {
		
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output";
		String sw = System.getProperty("user.dir")+"/resources/engStopWords";
		String c = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(sw, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(c);
		
		RatingSystem system = new TextPersistenceRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir + folder);
	}
	
	
	@Override
	public void preprocess() {
		mcrs.preprocess();
	}

	@Override
	public void process() {
		
//		uncomment to write sweble edit ratings as well
		mcrs.process();  
		
		Map<RevisionID, Rating> ratings = new HashMap<RevisionID, Rating>(orga.getRevisionIds().size());
		
		for(RevisionID id : orga.getChronologicalRevisions()){
			if(!id.isNullRevision()){
				
				TextPersistenceRating rating = new TextPersistenceRating();
				
				RevisionID afterDay = orga.getRevisionIDAfterDuration(id, TextPersistenceRating.oneDay);
				RevisionID afterWeek = orga.getRevisionIDAfterDuration(id, TextPersistenceRating.oneWeek);
				RevisionID afterTwoWeeks = orga.getRevisionIDAfterDuration(id, TextPersistenceRating.twoWeeks);
				RevisionID afterFourWeeks = orga.getRevisionIDAfterDuration(id, TextPersistenceRating.fourWeeks);
				
				rating.setOneDayTextPers(getTextPersistence(id, afterDay, survFactor));
				rating.setOneWeekTextPers(getTextPersistence(id, afterWeek, survFactor));
				rating.setTwoWeeksTextPers(getTextPersistence(id, afterTwoWeeks, survFactor));
				rating.setFourWeeksTextPers(getTextPersistence(id, afterFourWeeks, survFactor));
				rating.setOneDayEditPers(getEditPersistence(id, afterDay, survFactor));
				rating.setOneWeekEditPers(getEditPersistence(id, afterWeek, survFactor));
				rating.setTwoWeeksEditPers(getEditPersistence(id, afterTwoWeeks, survFactor));
				rating.setFourWeeksEditPers(getEditPersistence(id, afterFourWeeks, survFactor));
				
				ratings.put(id, rating);
			}
		}
		rb.rateRevisions(ratings, TextPersistenceRating.buildOutputHeadlines(survFactor));
	}
	
	
	boolean getTextPersistence(RevisionID id, RevisionID laterId, double factor){
		if(getInsertedTokens(id, id).size() == 0){
			return false;
		}
		if(laterId != null){
			return ((double) getInsertedTokens(laterId, id).size() >= (factor * ((double) getInsertedTokens(id, id).size())));
		} else {
			return false;
		}
	}
	
	boolean getEditPersistence(RevisionID id, RevisionID laterId, double factor){
		if(laterId != null){
			return ((double) getEditLongevity(laterId, id) >= factor);
		} else {
			return false;
		}
	}

	@Override
	public void postprocess(){
		orga.getChronologicalRevisions().stream().filter(id -> !id.isNullRevision()).forEachOrdered(id -> setEditorReputation(id));
	}
	
	private void setEditorReputation(RevisionID id){
		RevisionID judgingId = orga.getRevisionIDAfterDuration(id, TextPersistenceRating.twoWeeks);
		if(judgingId !=null){
			double update = rb.getJudgingMeasureResult(id, rb.size()-1);
			double judgingReputation = judgingId.getEditor().getReputation();
			id.getEditor().updateReputation(update, judgingReputation);
		}
//		System.out.println(id.getUsername()+" : "+id.getEditor().getReputation());
	}
	
}
