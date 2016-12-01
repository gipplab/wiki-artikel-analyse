package ao.thesis.wikianalyse.utils.textanalyse;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.*;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

/**
 * 
 * 
 * @author anna
 *
 */
public class Tokenizer {
	
	private static Logger logger = Logger.getLogger(Tokenizer.class);
	
	private List<String> stopWords;
	
	private CRFClassifier<CoreLabel> classifier;
	
	
	public Tokenizer(List<String> stopWords, CRFClassifier<CoreLabel> classifier){
		this.stopWords = stopWords;
		this.classifier = classifier;
	}
	
	
	/**Tokenizes the given revision, sets named entities if possible and sets the given id as 
	 * source revision for all tokens. For stop words all sources are replaced with an empty revision id.
	 * 
	 * @param epp		- EngProcessedPage object of the revision
	 * @param id		- id object of the revision
	 */
	public List<Token> tokenize(EngProcessedPage epp, RevisionID sourceID){
		
		logger.info("Tokenize revision on index "+sourceID.getIndex()+" with ID "+sourceID.getId());
		
		TextConverter converter = new TextConverter();
		
		List<Token> tokenizedRevision = (List<Token>) converter.go(epp.getPage());
		
		List<Token> revisionFormulas = tokenizedRevision.stream()
				.filter(o -> (o instanceof MathFormula))
				.collect(Collectors.toList());
		
		tokenizedRevision.stream().forEach(o -> o.setSourceId(sourceID));
		
		tokenizedRevision.stream()
			.filter(o -> ((o instanceof MarkupToken) && (stopWords.contains(o.getText()))))
			.forEach(o -> o.setSourceId(new RevisionID(BigInteger.ZERO, null, BigInteger.ZERO, -1, "")));
		
		String text = converter.sb.toString();
		
		if(tryNamedEntities(text, tokenizedRevision.size() - revisionFormulas.size())){
			setNamedEntities(text, tokenizedRevision);	
		} else {
			logger.error("Named entities could not be set.");
		}
		
		return tokenizedRevision;
	}
	
	
	private boolean tryNamedEntities(String text, int tokenCount){
		return classifier.classify(text).stream().mapToInt(sentence -> sentence.size()).sum() == tokenCount;
	}
	
	
	private void setNamedEntities(String text, List<Token> tokenizedRevision){
		
		List<Token> saveList = new ArrayList<Token>(tokenizedRevision);
		
		List<Triple<String, Integer, Integer>> neElement = classifier.classifyToCharacterOffsets(text);

		
		int neElementIndex = 0;
		int charAndSpaceCount = 0;
		
		for(int index = 0 ; index < tokenizedRevision.size() ; index++){
			
			if(neElementIndex >= neElement.size()){
				return;
			}
			
			if(tokenizedRevision.get(index) instanceof MathFormula){
				break;
			}
			
			boolean neSetCheck = false;

			do{
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
							
							/* all tokens in neTokens have the same source since no matching took place yet.
							 */
							neToken.setSourceId(neTokens.get(0).getSourceId());
							
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
						logger.error("Named Entity could not be set.");
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
