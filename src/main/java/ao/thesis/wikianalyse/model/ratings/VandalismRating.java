package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;

public class VandalismRating implements Rating {

	boolean vandalism = false;
	
	public void setVandalism(boolean vandalism) {
		this.vandalism=vandalism;
	}
	
	@Override
	public String[] buildOutputLine() {
		if(vandalism){
			return new String[] {"1"};
		} else {
			return new String[] {"0"};
		}
	}
	
	public static final String[] buildOutputHeadlines() {
		return new String[] {"Vandalism?"};
	}

	@Override
	public double getReputationMeasureResult() {
		if(vandalism){
			return 0.0;
		} else {
			return 1.0;
		}
	}
}
