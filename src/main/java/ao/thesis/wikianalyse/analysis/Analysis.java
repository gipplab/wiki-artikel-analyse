package ao.thesis.wikianalyse.analysis;

import java.util.List;

import org.sweble.wikitext.dumpreader.model.Revision;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.preprocession.PreprocessionException;
import ao.thesis.wikianalyse.analysis.procession.ProcessionException;

public interface Analysis {
	
	public List<ProcessedRevision> process(List<PreprocessedRevision> revisions) throws ProcessionException;

	public List<PreprocessedRevision> preprocess(List<Revision> revisions, PageTitle title) throws PreprocessionException;

}
