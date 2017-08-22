package ao.thesis.wikianalyse.analysis.preprocession.tokenization;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;

public class StringTokenizer {
	
	public static final String whitespacePattern = "\\s+|\\p{Z}+";

	private Collection<String> stopWords = null;
	
	public StringTokenizer(){}

	public void setStopWords(Collection<String> stopWords) {
		this.stopWords = stopWords;
	}

	public List<StringToken> tokenize(String text, int sourceID){
		List<String> words = Arrays.asList(text.split(whitespacePattern));
		List<StringToken> tokens = words.stream().map(word -> new StringToken(word)).collect(Collectors.toList());
		setRevisionAsSourceID(tokens, sourceID);
		return tokens;
	}
	
	private void setRevisionAsSourceID(List<StringToken> tokens, int sourceID){
		if((this.stopWords != null) && (!this.stopWords.isEmpty())){
			tokens.stream()
				.filter(t -> !stopWords.contains(t.getText().toLowerCase()))
				.forEach(t -> t.setSourceId(sourceID));
		} else if(((this.stopWords == null) || (this.stopWords.isEmpty()))){
			tokens.stream()
			.forEach(t -> t.setSourceId(sourceID));
		}
	}

}
