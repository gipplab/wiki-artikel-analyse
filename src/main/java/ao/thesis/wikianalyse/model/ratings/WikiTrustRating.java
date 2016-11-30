package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class WikiTrustRating implements Rating {

	private double editLongevity = 0.0;
	
	private double textDecayQuality = 0.0;
	
	
	public void setEditLongevity(double editLongevity){
		this.editLongevity=editLongevity;
	}
	
	public void setTextDecayQuality(double textDecayQuality) {
		this.textDecayQuality=textDecayQuality;	
	}
	
	public String[] buildOutputLine(){
		return new String[] {
			String.valueOf(editLongevity), 
			String.valueOf(textDecayQuality)
		};
	}

	@Override
	public String[] buildOutputHeadlines() {
		return new String[] {
			"Edit Longevity",
			"Text Decay Quality"
		};
	}

	@Override
	public void setEditorReputation() {
		// TODO Auto-generated method stub
		
	}


}
