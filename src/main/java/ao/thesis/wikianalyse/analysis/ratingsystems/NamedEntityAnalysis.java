package ao.thesis.wikianalyse.analysis.ratingsystems;

import java.util.LinkedList;
import java.util.List;

import org.sweble.wikitext.dumpreader.model.Revision;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.Analysis;
import ao.thesis.wikianalyse.analysis.PageHistory;
import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.preprocession.Preprocession;
import ao.thesis.wikianalyse.analysis.preprocession.PreprocessionException;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.Matching;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.TextMatcher;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.entitytokenizer.NETokenizer;
import ao.thesis.wikianalyse.analysis.procession.Procession;
import ao.thesis.wikianalyse.analysis.procession.ProcessionException;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NamedEntityAnalysis implements Analysis{
	
	private NETokenizer tokenizer = new NETokenizer();
	private TextMatcher matcher = new TextMatcher(10, 1);
	
	public NamedEntityAnalysis(CRFClassifier<CoreLabel> classifier){
		this.tokenizer.setClassifier(classifier);
	}
	
	public List<PreprocessedRevision> preprocess(List<Revision> revisions, PageTitle title) throws PreprocessionException {

		List<PreprocessedRevision> prepRevisions = new LinkedList<>();
		for(Revision revision : revisions){
			prepRevisions.add(Preprocession.buildNERevision(revision, title, tokenizer));
		}
		
		PageHistory<PreprocessedRevision> history = new PageHistory<>();
		history.setRevisions(prepRevisions);
		
		for(int i=0; i < revisions.size(); i++){
			PreprocessedRevision target = history.getRevision(i);
			List<PreprocessedRevision> sources = history.getJudgedRevisions(i, 10);
			
//			Matching.matchNEBagOfWords((NERevision)target, (List)sources);
			Matching.matchNEsWithGreedy((NERevision)target, (List)sources, matcher);
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
			processedRevision.setPrev(history.getPrevRevision(i));
			
			Procession.setJudged(processedRevision, history.getJudgedRevisions(i, 10));
			Procession.setJudging(processedRevision, history.getJudgingRevisions(i, 10));
			Procession.setEditDistance(processedRevision);
		}
		return procRevisions;
	}
}
