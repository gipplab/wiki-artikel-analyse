package ao.thesis.wikianalyse.ratingsystems;

import java.util.List;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.utils.editanalyse.EditJudger;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.TextJudger;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import ao.thesis.wikianalyse.model.Rating;
import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.ratings.WikiTrustRating;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/** 
 * Rates editors by calculating edit longevity and text decay quality as proposed in the WikiTrust-System.
 * 
 * @author anna
 *
 */
public class WikiTrustRatingSystem extends RatingSystem {

	private static Logger logger = Logger.getLogger(WikiTrustRatingSystem.class);
	
	private WordCountRatingSystem wcrs;
	
	
	public WikiTrustRatingSystem(WordCountRatingSystem wcrs, SwebleEditRatingSystem sers){
		this.wcrs=wcrs;
	}
	
	public WikiTrustRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		wcrs = new WordCountRatingSystem(stopWords, classifier);
	}
	
	@Override
	public void setWikiOrga(WikiOrga orga){
		
		this.orga = orga;
		
		wcrs.setWikiOrga(orga);
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
		
		RatingSystem system = new WikiTrustRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir);
		
	}
	
	@Override
	public void setEditVolume(String outputDir) {
		wcrs.setEditVolume(outputDir);
	}

	@Override
	public void associateEditors(String outputDir) {
		wcrs.associateEditors(outputDir);
	}
	
	@Override
	public void rateEdits(String outputDir) {
		
		//wcrs.rateEdits(outputDir);
		
		logger.info("Rate Edits.");
		
		TextJudger textjudger = new TextJudger();
		
		for(String title : orga.getTitles()){
			for(RevisionID id : orga.getSortedHistory(title)){
				
				EditJudger editjudger = wcrs.getEditJudger(title);
				
				WikiTrustRating wtr = new WikiTrustRating();
				
				List<RevisionID> judging = orga.getJudgingRevisions(id, 10);
				
				List<Token> inserted = wcrs.getInsertedTokens(id, id);
				int survivedText = judging.stream().mapToInt(i -> wcrs.getInsertedTokens(i, id).size()).sum();
				
				wtr.setEditLongevity(editjudger.calculateEditLongevityForRevision(id.getIndex(), judging.size()));
				wtr.setTextDecayQuality(textjudger.calculateDecayQuality(inserted.size(), survivedText, judging.size()));

				rb.addRating(id, (Rating) wtr);
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
}
