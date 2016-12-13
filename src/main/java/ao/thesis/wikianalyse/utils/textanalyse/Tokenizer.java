package ao.thesis.wikianalyse.utils.textanalyse;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.*;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

/**
 * 
 * 
 * @author anna
 *
 */
public class Tokenizer {
	
	private Logger logger;
	
	private List<String> stopWords;
	private CRFClassifier<CoreLabel> classifier;
	
	public Tokenizer(List<String> stopWords, CRFClassifier<CoreLabel> classifier, Logger logger){
		this.stopWords = stopWords;
		this.classifier = classifier;
		this.logger = logger;
	}
	
	/**Tokenizes the given revision, sets named entities if possible and sets the given id as 
	 * source revision for all tokens. For stop words all sources are replaced with an empty revision id.
	 * 
	 * @param epp		- EngProcessedPage object of the revision
	 * @param id		- id object of the revision
	 */
	public List<Token> tokenize(EngProcessedPage epp, RevisionID sourceID, boolean setNEs, boolean filterSWs){
		
		logger.info("Tokenize revision on index "+sourceID.getIndex()+" with ID "+sourceID.getId());
		
		TextConverter converter = new TextConverter();
		
		List<Token> tokenizedRevision = (List<Token>) converter.go(epp.getPage());
		
		tokenizedRevision.stream().forEach(o -> o.setSourceId(sourceID));
		
		if(filterSWs){
			tokenizedRevision.stream()
				.filter(o -> ((o instanceof MarkupToken) && (stopWords.contains(o.getText()))))
				.forEach(o -> o.setSourceId(RevisionID.getNullRevision(sourceID.getPageTitle())));
		}
		
		if(setNEs){
			String text = converter.sb.toString();
			
			//TODO define markup fitting sentences
			List<String> sentences;
			
			List<Token> revisionFormulas = tokenizedRevision.stream()
					.filter(o -> (o instanceof MathFormula))
					.collect(Collectors.toList());
			
			if(tokensFitClassifierResult(text, tokenizedRevision.size() - revisionFormulas.size())){
				setNamedEntities(text, tokenizedRevision, sourceID);	
				
			} else {
				logger.warn("Named entities could not be set.");
				System.out.println(classifier.classifyToString(text));
				System.out.println(text);
			}
		}
		return tokenizedRevision;
	}
	
	/* I was not able to find out yet, how the classifier segments the input text, since the already segmented
	 * tokens should be classified in their sentence context.
	 * The splitting pattern* that I use in the TextConverter seems to work in most cases. I had some different 
	 * segmentations with math formulas and since they do not need classification the TextConverter does not add 
	 * them to the text.
	 * 
	 *  	*(\\s+|\\p{Z}+|(?=(?U)\\p{Punct})|(?<=(?U)\\p{Punct})|(?=\\p{Punct})|(?<=\\p{Punct}))
	 */
	private boolean tokensFitClassifierResult(String text, int tokenCount){
		
		return classifier.classify(text).stream().mapToInt(sentence -> sentence.size()).sum() == tokenCount;
	}
	
	/* The classifier does not provide a method that returns the content of one entity. 
	 * To use instead:
	 * 		- 	classifier.classifyWithInlineXML(text) returns the text with tags around each entity
	 * 			that can be parsed.
	 * 		- 	classifier.classifyToCharacterOffsets(text) returns character offsets for each entity
	 * 			including white spaces.
	 */
	private void setNamedEntities(String text, List<Token> tokenizedRevision, RevisionID sourceID){
		
		List<Token> saveList = new ArrayList<Token>(tokenizedRevision);
		
		List<Triple<String, Integer, Integer>> neElement = classifier.classifyToCharacterOffsets(text.replaceAll("\\s+", " "));
		
		if(neElement.isEmpty()){
			return;
		}
		int neElementIndex = 0;
		int charAndSpaceCount = 0;
		for(int index = 0 ; index < tokenizedRevision.size() ; index++){
			if(neElementIndex >= neElement.size()){
				return;
			}
			boolean neSetCheck = false;
			do{
				if(tokenizedRevision.get(index) instanceof MathFormula){
					index++;
					continue;
				}
				if(charAndSpaceCount == neElement.get(neElementIndex).second()){
					
					int length = (neElement.get(neElementIndex).third() - neElement.get(neElementIndex).second());
					int tokenCounter = 0;
					
					List<Token> neTokens;
					int neCharAndSpaceCount = 0;
					
					do{
						tokenCounter++;
						neTokens = new ArrayList<Token>(tokenizedRevision.subList(index, index + tokenCounter));
						
						if(length == (neCharAndSpaceCount = neTokens.stream().mapToInt(t -> t.getText().length()).sum() + neTokens.size() - 1)){

							charAndSpaceCount += neCharAndSpaceCount;
							Token neToken = new NEToken(neTokens, neElement.get(neElementIndex).first());
							neToken.setSourceId(sourceID);
							tokenizedRevision.set(index, neToken);
							
							while(tokenCounter > 1){
								tokenizedRevision.remove(index + 1);
								tokenCounter--;
							}
							neSetCheck = true;
							neElementIndex++;
						}
						
					}
					while(length > neCharAndSpaceCount);
					
					if(neSetCheck){
						charAndSpaceCount++;
						break;
						
					} else {
						logger.warn("Named Entity could not be set with offsets.");
						System.out.println(classifier.classifyToString(text));
						System.out.println(text);
						tokenizedRevision = saveList;
						return;
					}
				}
				
				if(neSetCheck){
					break;
					
				} else {
					String tokentext = tokenizedRevision.get(index).getText();
					charAndSpaceCount += (tokentext.length() + 1);
					index++;
				}

			}
			while(charAndSpaceCount <= neElement.get(neElementIndex).second());
		}
	}
}
