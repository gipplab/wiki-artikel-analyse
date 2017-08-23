package ao.thesis.wikianalyse.io.output;

import java.util.List;

import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;

public interface OutputWriter {
	
	public void writeRevisions(List<ProcessedRevision> revisions, PageTitle title);

}
