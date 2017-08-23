package ao.thesis.wikianalyse.analysis.preprocession.tokenization.entitytokenizer;

import java.util.List;

import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.NEToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NETokenizer {

	private CRFClassifier<CoreLabel> classifier = null;
	
//	private TextConverterWithStringTokens converter = null;
	private TextConverter converter = null;
	
	public void setClassifier(CRFClassifier<CoreLabel> classifier) {
		this.classifier=classifier;
	}
	
	private void setRevisionAsSourceID(List<Token> tokens, int sourceID){
		tokens.stream().forEach(t -> t.setSourceId((Integer)sourceID));
	}
	
	public List<NEToken> tokenize(EngProcessedPage epp, int sourceID) {
		
//		converter = new TextConverterWithStringTokens(classifier);
		converter = new TextConverter(classifier);
		
		List<NEToken> tokens = (List) converter.go(epp.getPage());
		
		setRevisionAsSourceID((List)tokens, sourceID);
		
		return tokens;
	}
	
}
