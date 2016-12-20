package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class TextCountRating implements Rating {
	
	private int textCount = 0;
	
	private double textDecayQuality = 0.0;
	
	@Override
	public double getReputationMeasureResult() {
		return textDecayQuality;
	}
	
	public void setTextCount(int textCount) {
		this.textCount=textCount;
	}
	
	public void setTextDecayQuality(double textDecayQuality) {
		this.textDecayQuality=textDecayQuality;	
	}
	
	public String[] buildOutputLine(){
		return new String[] {String.valueOf(textDecayQuality)};
	}
	
	public static final String[] buildOutputHeadlines(String description) {
		return new String[] {"TextDQ ("+description+")"};
	}
}
