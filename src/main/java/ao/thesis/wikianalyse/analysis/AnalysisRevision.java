package ao.thesis.wikianalyse.analysis;

import org.joda.time.DateTime;

public interface AnalysisRevision {

	public int getID();
	
	public String getContributorName();
	
	public DateTime getTimestamp();

}
