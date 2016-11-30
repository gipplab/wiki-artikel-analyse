package ao.thesis.wikianalyse.ratingsystems;

import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.ratings.TextPersistenceRating;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/**
 * Rates Editors by their text (!) persistence over time.
 * 
 * @author anna
 *
 */
public class TextPersistenceRatingSystem extends RatingSystem {
	
	private static Logger logger = Logger.getLogger(TextPersistenceRatingSystem.class);
	
	private MarkupCountRatingSystem mcrs;
	
	
	public TextPersistenceRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		mcrs = new MarkupCountRatingSystem(stopWords, classifier);
	}
	
	public TextPersistenceRatingSystem(MarkupCountRatingSystem mcrs){
		this.mcrs = mcrs;
	}
	
	@Override
	public void setWikiOrga(WikiOrga orga){
		
		this.orga = orga;
		
		mcrs.setWikiOrga(orga);
		rb = new RatingBuilder(this.orga.getRevisionIds());
	}
	
	
	public static void main(String[] args) throws Exception {
		
		//TODO setup in evaluation / in classes
		
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output"+"/WordCountSys";
		
		String sw = System.getProperty("user.dir")+"/resources/engStopWords";
		String c = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(sw, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(c);
		
		RatingSystem system = new TextPersistenceRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir);
		
	}
	
	
	@Override
	public void setEditVolume(String outputDir) {
		mcrs.setEditVolume(outputDir);
	}

	@Override
	public void associateEditors(String outputDir) {
		mcrs.associateEditors(outputDir);
	}
	
	@Override
	public void rateEdits(String outputDir) {
		
		logger.info("Rate Edits.");
		
//		Duration oneDay = new Duration(24L*60L*60L*1000L);
//		Duration oneWeek = new Duration(7L*24L*60L*60L*1000L);
		
		Duration twoWeeks = new Duration(14L*24L*60L*60L*1000L);
		Duration fourWeeks = new Duration(28L*24L*60L*60L*1000L);
		
		for(String title : orga.getTitles()){
			for(RevisionID id : orga.getSortedHistory(title)){
				
				TextPersistenceRating pr = new TextPersistenceRating();
				
				pr.setTextPersistenceAfterTwoWeeks(getPersistence(id, orga.getRevisionIDAfterDuration(id, twoWeeks), 0.75));
				pr.setTextPersistenceAfterFourWeeks(getPersistence(id, orga.getRevisionIDAfterDuration(id, fourWeeks), 0.75));
				
				rb.addRating(id, pr);
			}
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
	

	boolean getPersistence(RevisionID id, RevisionID laterId, double factor){
		
		//TODO improve measure
		
		if(laterId != null){
			return ((double) getInsertedTokens(laterId, id).size() >= (factor * ((double) getInsertedTokens(id, id).size())));
		} else {
			return false;
		}
	}

	
	List<Token> getInsertedTokens(RevisionID id, RevisionID sourceId){
		return mcrs.getInsertedTokens(id, sourceId);
	}

}
