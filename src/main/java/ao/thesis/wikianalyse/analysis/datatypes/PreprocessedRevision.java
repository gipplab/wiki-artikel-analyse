package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.List;

import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.AnalysisRevision;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;


public class PreprocessedRevision implements AnalysisRevision {

	private final int id;
	
	private final DateTime timestamp;
	
	private String contributorName = "";
	
	List<MathFormulaToken> math;
	
	
	public PreprocessedRevision(int id, DateTime timestamp){
		this.id=id;
		this.timestamp=timestamp;
	}

	public String getContributorName() {
		return contributorName;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public int getID() {
		return id;
	}

	public void setContributorName(String username) {
		this.contributorName=username;
	}

	public List<MathFormulaToken> getMathFormulas() {
		return math;
	}
	
	public void setMathFormulas(List<MathFormulaToken> math) {
		this.math = math;
	}

}
