package ao.thesis.wikianalyse.ratingsystems;

import java.util.List;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.utils.editanalyse.EditJudger;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/** 
 * Rates editors by counting text, markup and edit information.
 * 
 * @author anna
 *
 */
public class WordCountRatingSystem extends RatingSystem {
	
	private static Logger logger = Logger.getLogger(WordCountRatingSystem.class);
	
	
	private MarkupCountRatingSystem mcrs;
	
	private SwebleEditRatingSystem sers;
	
	
	public WordCountRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		
		mcrs = new MarkupCountRatingSystem(stopWords, classifier);
		sers = new SwebleEditRatingSystem();
	}
	
	public WordCountRatingSystem(MarkupCountRatingSystem mcrs, SwebleEditRatingSystem sers){
		
		this.mcrs = mcrs;
		this.sers = sers;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		//TODO setup in evaluation / in classes
		
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output"+"/WordCountSys";
		
		String sw = System.getProperty("user.dir")+"/resources/engStopWords";
		String c = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(sw, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(c);
		
		RatingSystem system = new WordCountRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir);
		
	}
	
	@Override
	public void setWikiOrga(WikiOrga orga){
		this.orga = orga;
		
		mcrs.setWikiOrga(orga);
		sers.setWikiOrga(orga);
		
		rb = new RatingBuilder(this.orga.getRevisionIds());
	}
	
	
	@Override
	public void setEditVolume(String outputDir) {
		mcrs.setEditVolume(outputDir);
		sers.setEditVolume(outputDir);
	}

	@Override
	public void associateEditors(String outputDir) {
		mcrs.associateEditors(outputDir);
//		sers.associateEditors(outputDir);
	}

	@Override
	public void rateEdits(String outputDir) {
		mcrs.rateEdits(outputDir);
		sers.rateEdits(outputDir);
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

	List<Token> getInsertedTokens(RevisionID id, RevisionID sourceId){
		return mcrs.getInsertedTokens(id, sourceId);
	}
	
	EditJudger getEditJudger(String title){
		return sers.getEditJudger(title);
	}
}
