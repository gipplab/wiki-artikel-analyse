package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class TextPersistenceRating implements Rating {

	private boolean persistenceAfterOneDay = false;
	
	private boolean persistenceAfterOneWeek = false;
	
	private boolean persistenceAfterTwoWeeks = false;
	
	private boolean persistenceAfterFourWeeks = false;
	

	public void setTextPersistenceAfterTwoWeeks(boolean persistence) {
		this.persistenceAfterTwoWeeks=persistence;
	}
	
	public void setTextPersistenceAfterFourWeeks(boolean persistence) {
		this.persistenceAfterFourWeeks=persistence;
	}
	
	
	public String[] buildOutputLine(){
		return new String[] {
			String.valueOf(persistenceAfterTwoWeeks), 
			String.valueOf(persistenceAfterFourWeeks)
		};
	}

	@Override
	public String[] buildOutputHeadlines() {
		return new String[] {
				"Persistence 2 W", 
				"Persistence 4 W"
			};
	}

	@Override
	public void setEditorReputation() {
		// TODO Auto-generated method stub
		
	}


}
