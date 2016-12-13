package ao.thesis.wikianalyse.model;

/**
 * Interface for the rating of one revision.
 * 
 * @author anna
 *
 */
public interface Rating {
	
	/** Values for the csv-output.
	 * @return array of values for all ratings that should be printed.
	 */
	public String[] buildOutputLine();
	
//	/** Stores the editor reputation for the rated revision 
//	 *  to allow chronological reputation development.
//	 */
//	public void setOrUpdateEditorReputation(double reputation);
	
	public double getReputationMeasureResult();
}
