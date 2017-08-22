package ao.thesis.wikianalyse.analysis.ratingsystems;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.sweble.wikitext.dumpreader.model.Revision;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.Analysis;
import ao.thesis.wikianalyse.analysis.PageHistory;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.preprocession.Preprocession;
import ao.thesis.wikianalyse.analysis.preprocession.PreprocessionException;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.FormulaMatcher;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.Matching;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.TextMatcher;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.StringTokenizer;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.markupstringtokenizer.MarkupStringTokenizer;
import ao.thesis.wikianalyse.analysis.procession.Procession;
import ao.thesis.wikianalyse.analysis.procession.ProcessionException;

/**
 * WikiTrustAnalysis
 * 
 * This class aims to reproduce the analysis behavior of the WikiTrust live system.
 * 
 * 
 * @author anna
 *
 */
public class WikiTrustAnalysis implements Analysis {
	
	private static final int MAX_COMPARISATIONS = 10;
	private static final int PREFIX_LENGTH 		= 3;
	

	private final StringTokenizer whitespaceTokenizer = new StringTokenizer();
	private final MarkupStringTokenizer markupSegTokenizer = new MarkupStringTokenizer();
	
	private TextMatcher matcher = new TextMatcher(MAX_COMPARISATIONS, PREFIX_LENGTH);
	private FormulaMatcher formulamatcher = new FormulaMatcher(MAX_COMPARISATIONS, PREFIX_LENGTH);
	
	public WikiTrustAnalysis(Collection<String> stopWords){
		this.whitespaceTokenizer.setStopWords(stopWords);
		this.markupSegTokenizer.setStopWords(stopWords);
	}
	
	@Override
	public List<PreprocessedRevision> preprocess(List<Revision> revisions, PageTitle title) throws PreprocessionException {
		
		List<PreprocessedRevision> prepRevisions = new LinkedList<>();
		for(Revision revision : revisions){
			prepRevisions.add(Preprocession.buildWTMarkupSegmentedRevision(
					revision, 
					title, 
					markupSegTokenizer, 
					whitespaceTokenizer, 
					PREFIX_LENGTH));
		}

//		List<PreprocessedRevision> prepRevisions = new LinkedList<>();
//		for(Revision revision : revisions){
//			prepRevisions.add(Preprocession.buildSwebleRevision(revision, title));
//		}
		
		PageHistory<PreprocessedRevision> history = new PageHistory<>();
		history.setRevisions(prepRevisions);
		
		for(int i=0; i < revisions.size(); i++){
			PreprocessedRevision target = history.getRevision(i);
			List<PreprocessedRevision> sources = history.getJudgedRevisions(i, MAX_COMPARISATIONS);
			Matching.matchPreprocessedRevision(target, sources, matcher, formulamatcher);
		}
		
		return prepRevisions;
	}
	
	
	@Override
	public List<ProcessedRevision> process(List<PreprocessedRevision> revisions) throws ProcessionException{
		
		List<ProcessedRevision> procRevisions = Procession.processRevisions(revisions);
		
		PageHistory<ProcessedRevision> history = new PageHistory<>();
		history.setRevisions(procRevisions);
		
		for(int i = 0; i < procRevisions.size(); i++){
			ProcessedRevision processedRevision = procRevisions.get(i);
			try{
				processedRevision.setPrev(history.getPrevRevision(i)); //TODO nullpointer?
				Procession.setJudged(processedRevision, history.getJudgedRevisions(i,  MAX_COMPARISATIONS));
				Procession.setJudging(processedRevision, history.getJudgingRevisions(i, MAX_COMPARISATIONS));
				Procession.setEditDistance(processedRevision);
			}catch(Exception e){
				throw new ProcessionException(e);
			}
		}
		return procRevisions;
	}
}
