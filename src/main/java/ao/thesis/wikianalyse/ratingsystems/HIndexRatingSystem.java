package ao.thesis.wikianalyse.ratingsystems;

import java.math.BigInteger;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.ratings.HIndexRating;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/** 
 * Rates editors by their h-index and p-ratio.
 * 
 * @author anna
 *
 */
public class HIndexRatingSystem extends RatingSystem {
	
	private static Logger logger = Logger.getLogger(TextPersistenceRatingSystem.class);
	
	
	private WordCountRatingSystem wcrs;
	
	
	public HIndexRatingSystem(WordCountRatingSystem wcrs){
		this.wcrs=wcrs;
	}
	
	public HIndexRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		wcrs = new WordCountRatingSystem(stopWords, classifier);
	}
	
	public static void main(String[] args) throws Exception {
		
		//TODO setup in evaluation / in classes
		
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output"+"/WordCountSys";
		
		String sw = System.getProperty("user.dir")+"/resources/engStopWords";
		String c = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(sw, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(c);
		
		RatingSystem system = new HIndexRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir);
		
	}
	
	@Override
	public void setWikiOrga(WikiOrga orga){
		this.orga = orga;
		
		wcrs.setWikiOrga(orga);
		rb = new RatingBuilder(this.orga.getRevisionIds());
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
		wcrs.rateEdits(outputDir);
	}
	
	private boolean isPositiveRated(RevisionID id, RevisionID judgingId, double value){
		
		int inserted = wcrs.getInsertedTokens(id, id).size();
		int survived = wcrs.getInsertedTokens(judgingId, id).size();
		
		return (((inserted - survived) / inserted) >= value);
	}

	@Override
	public void rateEditors(String outputDir) {
		
		logger.info("Rate Editors.");
		
		for(RevisionID id : orga.getChronologicalRevisions()){
			
			int times = 0;
			for(Entry<BigInteger, List<RevisionID>> judgingEditor : orga.mapWithEditorIds(orga.getJudgingRevisions(id, 10)).entrySet()){
				
				for(RevisionID revId : judgingEditor.getValue()){
					
					if(isPositiveRated(id, revId, 0.5)){
						times++;
					}
					
					//set positive feedback if user rates positive two times
					if(times == 2){
						((HIndexRating) rb.getRating(id)).addPositiveFeedback();
						break;
					}
				}
			}
			((HIndexRating) rb.getRating(id)).setHIndex(orga.getArticleCount(id.getEditorId()));
			((HIndexRating) rb.getRating(id)).setPRatio(orga.getArticleCount(id.getEditorId()));
		}
		
		//TODO output
	}

}
