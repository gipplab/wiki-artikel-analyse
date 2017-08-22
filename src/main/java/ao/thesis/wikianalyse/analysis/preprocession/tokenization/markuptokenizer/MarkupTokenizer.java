package ao.thesis.wikianalyse.analysis.preprocession.tokenization.markuptokenizer;


import java.util.Collection;
import java.util.List;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;
/**
 * 
 * 
 * @author anna
 *
 */
public class MarkupTokenizer {
	
	private Collection<String> stopWords = null;
	private TextConverter converter = null;
	
	public Collection<String> getStopWords() {
		return stopWords;
	}

	public void setStopWords(Collection<String> stopWords) {
		this.stopWords = stopWords;
	}
	
	public TextConverter getTextConverter() {
		return converter;
	}

	public List<Token> tokenize(EngProcessedPage epp, int sourceID){
		converter = new TextConverter();
		List<Token> tokens = (List<Token>) converter.go(epp.getPage());
		
		setRevisionAsSourceID(tokens, sourceID);
		return tokens;
	}
	
	private void setRevisionAsSourceID(List<Token> tokens, int sourceID){
		if((this.stopWords != null) && (!this.stopWords.isEmpty())){
			tokens.stream()
				.filter(t -> !stopWords.contains(t.getText().toLowerCase()) && !t.getText().matches("\\p{Punct}"))
				.forEach(t -> t.setSourceId(sourceID));
		} else if(((this.stopWords == null) || (this.stopWords.isEmpty()))){
			tokens.stream()
			.filter(t -> !t.getText().matches("\\p{Punct}"))
			.forEach(t -> t.setSourceId(sourceID));
		}
	}
}
	