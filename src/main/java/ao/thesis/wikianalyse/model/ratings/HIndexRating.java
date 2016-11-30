package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class HIndexRating implements Rating {
	
	private int hIndex = 0;
	
	private double pRatio = 0;
	
	private int positiveFeedback = 0;
	

	@Override
	public String[] buildOutputLine(){
		return new String[] {
			String.valueOf(hIndex),
			String.valueOf(pRatio),
			String.valueOf(hIndex + pRatio),
			String.valueOf(hIndex * pRatio)
		};
	}

	@Override
	public String[] buildOutputHeadlines() {
		return new String[] {
			"H-Index",
			"P-Ratio",
			"H-Index + P-Ratio",
			"H-Index * P-Ratio"
		};
	}
	
	public void addPositiveFeedback(){
		positiveFeedback++;
	}
	
	public void setHIndex(int pageCount){
		hIndex = Math.min(positiveFeedback, pageCount);
	}
	
	public void setPRatio(int pageCount){
		pRatio = ((double) hIndex / (double) pageCount);
	}

	@Override
	public void setEditorReputation() {
		// TODO Auto-generated method stub
		
	}

}
