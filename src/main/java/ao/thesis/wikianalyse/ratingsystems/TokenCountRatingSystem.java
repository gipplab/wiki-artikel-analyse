package ao.thesis.wikianalyse.ratingsystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ao.thesis.wikianalyse.RatingSystem;
import ao.thesis.wikianalyse.model.Rating;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.ratings.TokenCountRating;
import ao.thesis.wikianalyse.utils.textanalyse.EditorAssociation;
import ao.thesis.wikianalyse.utils.textanalyse.StopWordReader;
import ao.thesis.wikianalyse.utils.textanalyse.TextJudger;
import ao.thesis.wikianalyse.utils.textanalyse.Tokenizer;
import ao.thesis.wikianalyse.utils.textanalyse.matcher.Matcher;
import ao.thesis.wikianalyse.utils.textanalyse.matcher.TextMatcher;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/**Rates revisions by counting markup and text tokens. Rates editors by calculating a text decay quality 
 * for all their revisions. Updates the editor reputation using the revision quality and the reputation 
 * value of several judging editors.
 * Prints an token overview.
 * 
 * TODO: does not yet set the text decay quality in the rating class
 * 
 * @author anna
 *
 */
public class TokenCountRatingSystem extends RatingSystem {
	
	private final static String folder = "/TokenCountRatingSystem";
	private final static int judgingDistance = 10;
	
	private final List<String> stopWords;
	private final CRFClassifier<CoreLabel> classifier;
	
	private boolean writeTokens = true;
	private boolean filterStopWords = true;
	private boolean setNEs = true;

//	/* Stores a judger for text and markup token before they are matched with previous revisions.
//	 */
//	private Map<RevisionID, TextJudger> preMatchedTextJudger = new HashMap<RevisionID, TextJudger>();
//	public TextJudger getPreMatchedTextJudger(RevisionID id) {
//	return preMatchedTextJudger.get(id);
//	}
	
	private Map<RevisionID, TextJudger> postMatchedTextJudger = new HashMap<RevisionID, TextJudger>();
	private Map<RevisionID, List<MathFormula>> mathFormulas = new HashMap<RevisionID, List<MathFormula>>();
	
	public TextJudger getPostMatchedTextJudger(RevisionID id) {
		return postMatchedTextJudger.get(id);
	}
	
	public List<MathFormula> getPostMatchedMathFormulas(RevisionID id) {
		return mathFormulas.get(id);
	}
	
	public TokenCountRatingSystem(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		this.stopWords=stopWords;
		this.classifier=classifier;
	}
	
	public static void main(String[] args) throws Exception {
		String inputdir = System.getProperty("user.dir")+"/input";
		String outputdir = System.getProperty("user.dir")+"/output";
		String stopWordDir = System.getProperty("user.dir")+"/resources/engStopWords";
		String classifierDir = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
		
		List<String> stopWords = (new StopWordReader()).readStopWords(stopWordDir, logger);
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(classifierDir);
		(new TokenCountRatingSystem(stopWords, classifier)).run(inputdir, outputdir + folder);
	}
	
	@Override
	public void preprocess(){
		
		Tokenizer tokenizer = new Tokenizer(stopWords, classifier, logger);
		EditorAssociation ea = new EditorAssociation();
		
		Map<String, List<List<Token>>> revisions = new HashMap<String, List<List<Token>>>();
		Map<String, List<List<MathFormula>>> mathFormulas = new HashMap<String, List<List<MathFormula>>>();
		
		Map<RevisionID, Rating> wordAmountRatings = new HashMap<RevisionID, Rating>();
		
		for(String title : orga.getTitles()){
			
			revisions.put(title, new ArrayList<List<Token>>());
			mathFormulas.put(title, new ArrayList<List<MathFormula>>());
			
			for(RevisionID id : orga.getSortedHistory(title)){
				if(!id.isNullRevision()){
					preprocessRevision(id, 
							tokenizer, 
							revisions.get(title), 
							mathFormulas.get(title), 
							wordAmountRatings);
				}
			}
			// TODO text and math formulas must be matched together, otherwise it fails
			ea.associateTextEditors(revisions.get(title), new TextMatcher(10, 3, Matcher.getDefaultComparator()));
			
			if(writeTokens){
				writeTokens(revisions.get(title), title);
			}
		}
		/* Rating with all token count information
		 */
		rb.rateRevisions(wordAmountRatings, TokenCountRating.buildOutputHeadlines("ALL"));
		
		/* Rating with new inserted token count information
		 */
		setInsertedRatings(revisions, mathFormulas);
	}
	
	public void writeTokens(List<List<Token>> tokens, String title){
		logger.info("Write Token Output.");
		try {
			ow.writeTokenOutput(tokens, title, "Tokens");
		} catch (IOException e) {
			logger.error("Token Output could not be written.");
		}
	};
	
	private void preprocessRevision(RevisionID id, 
			Tokenizer tokenizer, 
			List<List<Token>> revisions, 
			List<List<MathFormula>> mathFormulas, 
			Map<RevisionID, Rating> wordAmountRatings){
		
		/* Tokenization
		 */
		List<Token> revision = tokenizer.tokenize(orga.getEngProcessedPage(id), id, setNEs, filterStopWords);
		revisions.add(revision);
		List<MathFormula> revisionFormulas = (List) revision.stream().filter(o -> (o instanceof MathFormula)).collect(Collectors.toList());
		mathFormulas.add(revisionFormulas);
		
		/* Rating
		 */
		TextJudger judger = new TextJudger(revision, id);
		TokenCountRating counts = new TokenCountRating();
		judger.setTokenCountRating(counts);
		wordAmountRatings.put(id, counts);
	}
	
	private void setInsertedRatings(
			Map<String, List<List<Token>>> revisions,
			Map<String, List<List<MathFormula>>> mathFormulas){
		
		Map<RevisionID, Rating> insertedWordRatings = new HashMap<RevisionID, Rating>();
		
		for(RevisionID id : orga.getChronologicalRevisions()){
			if(!id.isNullRevision()){
				
				TokenCountRating counts = new TokenCountRating();
				TextJudger judger = new TextJudger(revisions.get(id.getPageTitle()).get(id.getIndex()), id);
				judger.setTokenCountRating(counts);
				insertedWordRatings.put(id, counts);
				postMatchedTextJudger.put(id, judger);
				this.mathFormulas.put(id, mathFormulas.get(id.getPageTitle()).get(id.getIndex()));
			}
		}
		rb.rateRevisions(insertedWordRatings, TokenCountRating.buildOutputHeadlines("IN"));
	}
	

	@Override
	public void process() {
		for(RevisionID id : orga.getChronologicalRevisions()){
			if(!id.isNullRevision()){
				//TODO calculate text decay quality in rating class
				rb.getJudgingMeasureResult(id, 1); // 1 : index of the inserted word ratings
			}
		}
	}
	
	
	@Override
	public void postprocess(){
		orga.getChronologicalRevisions().stream().filter(id -> !id.isNullRevision()).forEachOrdered(id -> setEditorReputation(id));
	}
	
	//TODO does not yet rate editors; no text decay quality is calculated
	private void setEditorReputation(RevisionID id){
		List<RevisionID> judgingRevisions = orga.getJudgingRevisions(id, judgingDistance);
		if(judgingRevisions.size() == judgingDistance){
			double update = rb.getJudgingMeasureResult(id, 1);
			double judgingReputation = judgingRevisions.get(judgingDistance-1).getEditor().getReputation();
			id.getEditor().updateReputation(update, judgingReputation);
		}
//		System.out.println(id.getUsername()+" : "+id.getEditor().getReputation());
	}
}
