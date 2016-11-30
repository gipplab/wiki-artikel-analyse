package ao.thesis.wikianalyse.model;

/**
 * Interface for the rating of one revision.
 * 
 * @author anna
 *
 */
public interface Rating {
	
	/** Headlines for the csv-output.
	 * @return array of headlines for all ratings that should be printed.
	 */
	public String[] buildOutputHeadlines();

	/** Values for the csv-output.
	 * @return array of values for all ratings that should be printed.
	 */
	public String[] buildOutputLine();
	
	/** Stores the editor reputation for that revision 
	 *  to allow chronological reputation development.
	 */
	public void setEditorReputation();
}
