package ao.thesis.wikianalyse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.judger.TextJudger;
import ao.thesis.wikianalyse.matcher.Match;
import ao.thesis.wikianalyse.matcher.Matcher;
import ao.thesis.wikianalyse.tokenizer.TextConverter;
import ao.thesis.wikianalyse.tokens.MathToken;
import ao.thesis.wikianalyse.tokens.Token;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class TextAnalysis {
	
	private static Logger logger = Logger.getLogger(TextAnalysis.class);
	
	private Matcher matcher;
	private CRFClassifier<CoreLabel> classifier;
	private List<String> stopWordList;
	
	private List<List<Object>> tokenizedRevisions;
	private List<List<CoreLabel>> namedEntities;
	
	private TextJudger textJudger;
	
	
	public TextAnalysis(Matcher matcher, CRFClassifier<CoreLabel> classifier, 
			List<String> stopWordList){

		this.matcher=matcher;
		this.classifier=classifier;
		this.stopWordList=stopWordList;
		
		tokenizedRevisions = new ArrayList<List<Object>>();
		textJudger = new TextJudger();
	}
	
	public int getTokenizedRevisionSize(){
		return this.tokenizedRevisions.size();
	}
	
	/**Tokenizes the given revision, sets named entities if possible and sets the given id as 
	 * source revision for all tokens.
	 * @param epp	- revision
	 * @param id	- revision id
	 */
	@SuppressWarnings("unchecked")
	public void tokenize(EngProcessedPage epp, BigInteger id){
		logger.info("Tokenize revision "+id);
		
		TextConverter converter = new TextConverter();
		List<Object> tokenizedRevision = (List<Object>) converter.go(epp.getPage());
		
		for(Object token : tokenizedRevision){
			if(token instanceof Token){
				((Token) token).setSourceID(id);
			}
		}
		
		String processedText = converter.sb.toString();
		if(tryNamedEntities(processedText, tokenizedRevision.size())){
			int index = 0;
			for(List<CoreLabel> sentence : namedEntities){
				for(CoreLabel entity : sentence){
					String annotation = entity.get(CoreAnnotations.AnswerAnnotation.class);
					if(tokenizedRevision.get(index) instanceof Token){
						((Token) tokenizedRevision.get(index)).setEntity(annotation);
					}
					index++;
				}
			}
		} else {
			logger.error("Named entities could not be set.");
		}
		tokenizedRevisions.add(tokenizedRevision);
	}
	
	private boolean tryNamedEntities(String text, int tokenCount){
		
		namedEntities = classifier.classify(text);
		int neCount=0;
		for(List<CoreLabel> sentence : namedEntities){
			neCount+=sentence.size();
		}
		return (neCount == tokenCount);
	}
	
	/**Updates the source revisions of all tokens using matches.
	 * Deletes source revisions of tokens that are stop words.
	 */
	public void updateSources(){
		
		if(tokenizedRevisions.isEmpty()){
			logger.error("No tokenized revisions found.");
			return;
		}
		
		List<List<Match>> bestMatches = matcher.match(tokenizedRevisions);
		
		if(!bestMatches.isEmpty()){
			for(int index = 1 ; index < tokenizedRevisions.size() ; index++){
				logger.info("Set sources for revision "+index+".");
				
				List<Match> bestTargetMatches = bestMatches.get(index-1);
				List<Object> targetRevision = tokenizedRevisions.get(index);
				
				setSourceIds(bestTargetMatches, targetRevision);
			}
		}
		
		deleteSourcesFromStopWords();
	}

	/**Updates the source ids of all tokens of a given revision with the given matches
	 * @param matches			- matches to update the tokens with
	 * @param targetRevision	- updated revision
	 */
	private void setSourceIds(List<Match> matches, List<Object> targetRevision)
	{
		try {
			for(Match match : matches)
			{
				for(int currlength = 0 ; currlength < match.length ; currlength++)
				{
					int sPosition = match.sourcePosition + currlength;
					int tPosition = match.targetPosition + currlength;
					
					if(match.sourceIndex < tokenizedRevisions.size()){
						
						List<Object> sourceRevision = tokenizedRevisions
								.get(match.sourceIndex);
						
						Object originalToken = sourceRevision.get(sPosition);
						Object copyToken = targetRevision.get(tPosition);
						
						if(copyToken instanceof Token && originalToken instanceof Token){
							((Token) copyToken).setSourceID(
									((Token) originalToken).getSourceID());
						}
						
					} else {
						throw new IllegalArgumentException();
					}
				}	
			}
		} catch (IllegalArgumentException e) {
			logger.error("Wrong matches for given revision.", e);
			return;
		}
	}
	
	private void deleteSourcesFromStopWords() {
		for(List<Object> revision : tokenizedRevisions){
			for(Object item : revision){
				String content = ((Token)item).getTextContent();
				if(stopWordList.contains(content) && !(item instanceof MathToken)){
					((Token)item).setSourceID(BigInteger.ZERO);
				}
			}
		}
	}
	
	//-------------------------------------------------------
	
	/**Counts tokens that have a ne-annotation and the given source id.
	 * @param index		- index of revision to count named entities in
	 * @param source	- source id that the tokens should have
	 * @return number of ne-tokens with the given source id
	 */
	public int countNamedEntities(int index, BigInteger source){
		int counter = 0;
		if(index < getTokenizedRevisionSize()){
			for(Object item : tokenizedRevisions.get(index)){
				if(!((Token)item).getEntity().equals("O")
						&& ((Token)item).getSourceID().equals(source)){
					counter++;
				}
			}
		}
		return counter;
	}
	
	/**Counts tokens with the given source id.
	 * @param index		- index of revision to count tokens in
	 * @param source	- source id that the tokens should have
	 * @return number of tokens with the given source id
	 */
	public int countWords(int index, BigInteger source){
		int counter = 0;
		if(index < getTokenizedRevisionSize()){
			for(Object item : tokenizedRevisions.get(index)){
				if(((Token)item).getSourceID().equals(source)){
					counter++;
				}
			}
		}
		return counter;
	}
	
	public int countMathTokens(int index, BigInteger source){
		int counter = 0;
		if(index < getTokenizedRevisionSize()){
			for(Object item : tokenizedRevisions.get(index)){
				if(item instanceof MathToken){
					if(((Token)item).getSourceID().equals(source)){
						counter++;
					}
				}
			}
		}
		return counter;
	}

	/**Getter for tokenized revision.
	 * @param revisionIndex
	 * @return list of tokens
	 */
	public List<Object> getTokens(int revisionIndex){
		try{
			return tokenizedRevisions.get(revisionIndex);
		} catch(NullPointerException e){
			logger.error("Index not in revision list.",e);
			return null;
		}
	}
	
	/**Calculates the decay quality of the text in a revision
	 * @param revisionIndex		- index of the revision to be judged
	 * @param id				- id of the revision to be judged
	 * @param judgingRevisions	- list of indices of judging revisions
	 * @return decay quality of the text that was inserted in the given revision
	 */
	public double calculateDecayQuality(int revisionIndex, BigInteger id,
			List<Object> judgingRevisions){
		
		int insertedText = getWordCountById(revisionIndex, id);
		Integer[] allSurvivedTexts = new Integer[judgingRevisions.size()];
		int surIndex=0;
		
		for(Object judgingRevision : judgingRevisions){
			
			if(judgingRevision instanceof Integer){
				int surText = getWordCountById((Integer)judgingRevision, id);
				allSurvivedTexts[surIndex] = surText;
				surIndex++;
			}
		}
		return textJudger.calculateDecayQuality(insertedText, allSurvivedTexts);
	}
	
	/**Returns the number of tokens that have the given revision id as source id.
	 * @param index		- index of the revision to count tokens
	 * @param id		- revision id
	 * @return number of tokens with id as source id
	 */
	private int getWordCountById(int index, BigInteger id){
		int words = 0;
		
		if(!tokenizedRevisions.isEmpty() && index < getTokenizedRevisionSize()){
			List<Object> revision = tokenizedRevisions.get(index);
			
			for(Object token : revision){
				
				if(token instanceof Token 
						&& ((Token)token).getSourceID().equals(id)){
					words++;
				}
			}
		} else {
			logger.error("Index not in revision list.");
		}
		return words;
	}
	
}
