package ao.thesis.wikianalyse.ratingsystems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.ratings.MarkupCountRating;
import ao.thesis.wikianalyse.utils.textanalyse.EditorAssociation;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.Tokenizer;
import ao.thesis.wikianalyse.utils.textanalyse.matcher.MathMatcher;
import ao.thesis.wikianalyse.utils.textanalyse.matcher.TextMatcher;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MarkupToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.NEToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/** 
 * Rates editors by their text and markup edits. 
 * 
 * @author anna
 *
 */
public class MarkupCountRatingSystem extends RatingSystem {
	
	private static Logger logger = Logger.getLogger(MarkupCountRatingSystem.class);

	
	private List<List<Token>> tokenizedRevisions = new ArrayList<List<Token>>();
	private List<List<Token>> mathFormulas = new ArrayList<List<Token>>();

	private List<String> stopWords;
	private CRFClassifier<CoreLabel> classifier;
	
	
	public MarkupCountRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		this.stopWords=stopWords;
		this.classifier=classifier;
	}
	
	public static void main(String[] args) throws Exception {
		
		//TODO setup in evaluation / in classes
		
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output"+"/WordCountSys";
		
		String sw = System.getProperty("user.dir")+"/resources/engStopWords";
		String c = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(sw, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(c);
		
		RatingSystem system = new MarkupCountRatingSystem(stopWords, classifier);
		system.run(inputdir, outputdir);
		
	}
	
	
	@Override
	public void setEditVolume(String outputDir) {
		
		logger.info("Start tokenizing input and setting edit scripts.");
		
		for(String title : orga.getTitles()){
			for(RevisionID id : orga.getSortedHistory(title)){
				
				Tokenizer tokenizer = new Tokenizer(stopWords, classifier);
				
				List<Token> tokenizedRevision = tokenizer.tokenize(orga.getEngProcessedPage(id), id);
				List<Token> revisionFormulas = tokenizedRevision.stream().filter(o -> (o instanceof MathFormula)).collect(Collectors.toList());
				
				tokenizedRevisions.add(tokenizedRevision);
				mathFormulas.add(revisionFormulas);
			}
		}
	}
	

	@Override
	public void associateEditors(String outputDir) {
		
		logger.info("Associate editors.");
		
		EditorAssociation ea = new EditorAssociation();
		ea.associateTextAndMathEditors(tokenizedRevisions, mathFormulas, new TextMatcher(10, 3), new MathMatcher());
	}
	

	@Override
	public void rateEdits(String outputDir) {
		
		logger.info("Rate Edits.");
		
		for(String title : orga.getTitles()){
			for(RevisionID id : orga.getSortedHistory(title)){
				
				MarkupCountRating counts = new MarkupCountRating();
				updateMarkupCounts(counts, getInsertedTokens(id,id));
				rb.addRating(id, counts);
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
	
	
	List<Token> getInsertedTokens(RevisionID id, RevisionID sourceId){
		return tokenizedRevisions.get(id.getIndex()).stream()
				.filter(o -> o.getSourceId().equals(sourceId))
				.collect(Collectors.toList());
	}

	
	private void updateMarkupCounts(MarkupCountRating counts, List<Token> inserted){
		int insertedMathTokens = 0;	
		int insertedNETokens = 0;	
		int insertedLinks = 0;	
		int insertedCategories = 0;	
		int insertedFiles = 0;
		int insertedHeaderWords = 0;
		
		for(Object token : inserted){

			if(token instanceof MarkupToken){
				switch (((MarkupToken) token).getMarkup()){
				case LINK :
					insertedLinks++;
					break;
				case BOLD:
					break;
				case CATEGORY:
					insertedCategories++;
					break;
				case EXTERNLINK:
					insertedLinks++;
					break;
				case FILE:
					insertedFiles++;
					break;
				case HEADER:
					insertedHeaderWords++;
					break;
				case ITALIC:
					break;
				case TEXT:
					break;
				default:
					break;
				}
			} else if(token instanceof MathFormula){
				insertedMathTokens += ((MathFormula) token).stream().count();
			} else if(token instanceof NEToken){
				insertedNETokens++;
			}
		}
		counts.setInsertedMathTokens(insertedMathTokens);
		counts.setInsertedNETokens(insertedNETokens);
		counts.setInsertedFiles(insertedFiles);
		counts.setInsertedLinks(insertedLinks);
		counts.setInsertedCategories(insertedCategories);
		counts.setInsertedHeaderWords(insertedHeaderWords);
	}

}
