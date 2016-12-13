package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class EditOpRating implements Rating {
	
	/* Revision Rating:	quantity measures
	 */
	private int insertedWords = 0;	
	private int deletedWords = 0;	
	private int movedWords = 0;
	private int updatedWords = 0;
	
	/* Editor Rating: 	quality measure, depends on the judging distance 
	 * 					between the current and a later revision. Editors 
	 * 					can only be judged properly if there already are enough
	 * 					judging revisions.
	 */
	private double editLongevity = 0.0;
	
	@Override
	public double getReputationMeasureResult() {
		return editLongevity;
	}
	
	public void setEditLongevity(double editLongevity) {
		this.editLongevity = editLongevity;
	}
	
	public void setInserted(int inserts){
		this.insertedWords=inserts;
	}
	
	public void setDeleted(int deletes) {
		this.deletedWords = deletes;	
	}
	
	public void setMoved(int moves){
		this.movedWords = moves;
	}
	
	public void setUpdated(int updates){
		this.updatedWords = updates;
	}
	
	public static final String[] revRatingHeadlines() {
		return new String[] {
				"Insert Activities",
				"Delete Activities",
				"Move Activities",
				"Update Activities",
				"Edit Longevity"
				};
	}
	
	@Override
	public String[] buildOutputLine() {
		return new String[] {
			String.valueOf(insertedWords),
			String.valueOf(deletedWords),
			String.valueOf(movedWords),
			String.valueOf(updatedWords),
			String.valueOf(editLongevity)
		};
	}
	
//	/*--------------------------------------
//	 * calculation methods for an edit significance
//	 */
//	public double calculateWeightedEditSignificance(double inWeight, double delWeight, double moWeight, double upWeight) {
//		return inWeight * insertedWords + delWeight * deletedWords + moWeight * movedWords + upWeight * updatedWords;
//	}
//	
//	public static final String[] weightedHeadlines(double inWeight, double delWeight, double moWeight, double upWeight) {
//		return new String[] {
//				"WES (IN:"+inWeight+",DEL:"+delWeight+",MO:"+moWeight+",UP:"+upWeight+")"
//				};
//	}
//	
//	public String[] buildWESLine(double inWeight, double delWeight, double moWeight, double upWeight) {
//		return new String[] {
//			String.valueOf(calculateWeightedEditSignificance(inWeight,delWeight,moWeight,upWeight)),
//		};
//	}
}
