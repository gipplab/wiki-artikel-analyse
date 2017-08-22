package ao.thesis.wikianalyse.analysis.preprocession;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.sweble.wikitext.dumpreader.model.Contributor;
import org.sweble.wikitext.dumpreader.model.Revision;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.datatypes.MarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.SwebleRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTMarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WhitespaceRevision;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.utils.PrefixHashMapBuilder;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.StringTokenizer;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.entitytokenizer.NETokenizer;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.markupstringtokenizer.MarkupStringTokenizer;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.NEToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;
import ao.thesis.wikianalyse.utils.SwebleException;
import ao.thesis.wikianalyse.utils.SwebleUtils;

/**
 * Preprocession
 * 
 * This class provides several ways to preprocess a List of Revision objects into a PageHistory object with preprocessed revisions.
 * 
 * @author Anna Opaska
 *
 */
public class Preprocession {
	
	private static final String ANONYMOUS = "Anonymous";
	
	private Preprocession(){}
	
	public static List<PreprocessedRevision> preprocessRevisions(List<Revision> revisions){
		return revisions.stream()
				.map(r -> buildPreprocessedRevision(r))
				.collect(Collectors.toList());
	}
	
	public static PreprocessedRevision buildPreprocessedRevision(Revision revision){
		PreprocessedRevision preprocessedRevision = new PreprocessedRevision(revision.getId().intValue(), revision.getTimestamp());
		Contributor contributor = revision.getContributor();
		if(contributor!=null){
			preprocessedRevision.setContributorName(contributor.getUsername());
		} else {
			preprocessedRevision.setContributorName(ANONYMOUS);
		}
		return preprocessedRevision;
	}
	
	
	public static SwebleRevision buildSwebleRevision(Revision revision, PageTitle pageTitle) throws PreprocessionException{
		SwebleRevision preprocessedRevision = new SwebleRevision(buildPreprocessedRevision(revision));
		preprocessedRevision.setPageTitle(pageTitle);
		preprocessedRevision.setPageId(SwebleUtils.buildPageId(pageTitle, preprocessedRevision.getID()));
		/*
		 * There are some cases in which the object EngProcessedPage cannot be build due to a node that misses a 
		 * visit()-method. The issue may be discussed here: https://github.com/OpenCompare/OpenCompare/issues/86
		 * 
		 * In that case edit scripts cannot be build and therefore, edit quality cannot be calculated.
		 * The page is skipped.
		 */
		try {
			preprocessedRevision.setEngProcessedPage(SwebleUtils.buildEngProcessedPage(preprocessedRevision.getPageId(), revision.getText()));
		} catch (SwebleException e) {
			throw new PreprocessionException(e);
		}
		return preprocessedRevision;
	}
	
	public static WhitespaceRevision buildWhitespaceRevision(Revision revision, StringTokenizer tokenizer, int prefixlength){
		WhitespaceRevision preprocessedRevision = new WhitespaceRevision(buildPreprocessedRevision(revision));
		List<StringToken> tokens = tokenizer.tokenize(revision.getText(), preprocessedRevision.getID());
		preprocessedRevision.setWhiteSpaceSegmentedTokens(tokens);
		preprocessedRevision.setWhiteSpaceSegmentedPrefixPositions((HashMap<PrefixTuple, List<Integer>>) PrefixHashMapBuilder.getPrefixHashMap((List)tokens, prefixlength));
		return preprocessedRevision;
	}
	
	public static MarkupSegmentedRevision buildMarkupSegmentedRevision(Revision revision, PageTitle pageTitle, MarkupStringTokenizer tokenizer, int prefixlength) throws PreprocessionException{
		MarkupSegmentedRevision preprocessedRevision = new MarkupSegmentedRevision(buildSwebleRevision(revision, pageTitle));
		List<Token> tokens = tokenizer.tokenize((SwebleRevision)preprocessedRevision);
		
		preprocessedRevision.setMathFormulas(tokenizer.formulas);
		
		preprocessedRevision.setMarkupSegmentedTokens(tokens);
		preprocessedRevision.setMarkupSegmentedPrefixPositions((HashMap<PrefixTuple, List<Integer>>) PrefixHashMapBuilder.getPrefixHashMap((List)tokens, prefixlength));
		return preprocessedRevision;
	}
	
	public static NERevision buildNERevision(Revision revision, PageTitle pageTitle, NETokenizer tokenizer) throws PreprocessionException{
		NERevision preprocessedRevision = new NERevision(buildSwebleRevision(revision, pageTitle));
		List<Token> tokens = (List) tokenizer.tokenize(preprocessedRevision.getEngProcessedPage(), preprocessedRevision.getID());
		preprocessedRevision.setNETokens(tokens);
		preprocessedRevision.setPrefixPositions((HashMap<PrefixTuple, List<Integer>>) PrefixHashMapBuilder.getPrefixHashMap((List)tokens, 3));
		return preprocessedRevision;
	}
	
	public static WTRevision buildWTRevision(Revision revision, PageTitle pageTitle, StringTokenizer tokenizer, int prefixlength) throws PreprocessionException{
		return new WTRevision(
				buildSwebleRevision(revision, pageTitle), 
				buildWhitespaceRevision(revision, tokenizer, prefixlength));
	}
	
	public static WTMarkupSegmentedRevision buildWTMarkupSegmentedRevision(Revision revision, PageTitle pageTitle, MarkupStringTokenizer tokenizer, 
			StringTokenizer stringtokenizer, int prefixlength) throws PreprocessionException{
		return new WTMarkupSegmentedRevision(
				buildMarkupSegmentedRevision(revision, pageTitle, tokenizer, prefixlength), 
				buildWhitespaceRevision(revision, stringtokenizer, prefixlength));
	}


}
