package ao.thesis.wikianalyse.analysis.preprocession.tokenization.markupstringtokenizer;

import java.util.Collection;
import java.util.List;

import org.jsoup.helper.Validate;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

import ao.thesis.wikianalyse.analysis.datatypes.SwebleRevision;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

/**
 * MarkupStringTokenizer
 * 
 * In his dissertation Thomas Adler states that the OCAML version of the WikiTrust-Algorithm also makes use of markup information
 * in addition to the white space separation in order to identify single words.
 * 
 * This class provides a version of the TextConverter that iterates over all markup elements and and stores every word inside a list.
 * This tries to reproduce the output of the OCAML version.
 * 
 * @author Anna Opaska
 *
 */
public class MarkupStringTokenizer {
	
	private Collection<String> stopWords = null;
	
	public List<MathFormulaToken> formulas = null;
	
	public MarkupStringTokenizer(){}

	public void setStopWords(Collection<String> stopWords) {
		this.stopWords = stopWords;
	}

	public List<Token> tokenize(SwebleRevision revision){
		int id = revision.getID();
		EngProcessedPage epp = revision.getEngProcessedPage();
		
		Validate.notNull(epp);
		
		TextConverter converter = new TextConverter();
		List<Token> tokens = (List) converter.go(epp.getPage());
		setRevisionAsSourceID((List)tokens, id);
		
		formulas = converter.mathtokens;
		
		setRevisionAsSourceID((List)formulas, id);
		for(MathFormulaToken formula : formulas){
			setRevisionAsSourceIDForFormula(formula.getElements(), id);
		}
		
		return tokens;
	}
	
	private void setRevisionAsSourceID(List<Token> tokens, int sourceID){
		if((this.stopWords != null) && (!this.stopWords.isEmpty())){
			tokens.stream()
				.filter(t -> !stopWords.contains(t.getText().toLowerCase()) && !t.getText().matches("\\p{Punct}"))
				.forEach(t -> t.setSourceId(sourceID));
		} else if(((this.stopWords == null) || (this.stopWords.isEmpty()))){
			tokens.stream()
			.forEach(t -> t.setSourceId(sourceID));
		}
	}
	
	private void setRevisionAsSourceIDForFormula(List<Token> tokens, int sourceID){
		tokens.stream()
		.forEach(t -> t.setSourceId(sourceID));
	}


}
