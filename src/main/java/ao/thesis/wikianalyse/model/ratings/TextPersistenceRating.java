package ao.thesis.wikianalyse.model.ratings;

import org.joda.time.Duration;

import ao.thesis.wikianalyse.model.Rating;


public class TextPersistenceRating implements Rating {
	
	public final static Duration oneHour = new Duration(60L*60L*1000L);
	public final static Duration oneDay = new Duration(24L*60L*60L*1000L);
	public final static Duration oneWeek = new Duration(7L*24L*60L*60L*1000L);
	public final static Duration twoWeeks = new Duration(14L*24L*60L*60L*1000L);
	public final static Duration fourWeeks = new Duration(28L*24L*60L*60L*1000L);

	private boolean textOneDay = false;
	private boolean textOneWeek = false;
	private boolean textTwoWeeks = false;
	private boolean textFourWeeks = false;
	private boolean editOneDay = false;
	private boolean editOneWeek = false;
	private boolean editTwoWeeks = false;
	private boolean editFourWeeks = false;
	
	/* Editor Rating: currently, only the two week persistence is used to judge editors
	 * 
	 * (non-Javadoc)
	 * @see ao.thesis.wikianalyse.model.Rating#getReputationMeasureResult()
	 */
	@Override
	public double getReputationMeasureResult() {
		if(textTwoWeeks==true && editTwoWeeks == true){
			return 1;
		} else if(textTwoWeeks==true || editTwoWeeks == true){
			return 0;
		} else 
			return -1;
	}

	public void setOneDayTextPers(boolean textOneDay) {
		this.textOneDay=textOneDay;
	}
	public void setOneWeekTextPers(boolean textOneWeek) {
		this.textOneWeek=textOneWeek;
	}
	public void setTwoWeeksTextPers(boolean textTwoWeeks) {
		this.textTwoWeeks=textTwoWeeks;
	}
	public void setFourWeeksTextPers(boolean textFourWeeks) {
		this.textFourWeeks=textFourWeeks;
	}
	
	public void setOneDayEditPers(boolean editOneDay) {
		this.editOneDay=editOneDay;
	}
	public void setOneWeekEditPers(boolean editOneWeek) {
		this.editOneWeek=editOneWeek;
	}
	public void setTwoWeeksEditPers(boolean editTwoWeeks) {
		this.editTwoWeeks=editTwoWeeks;
	}
	public void setFourWeeksEditPers(boolean editFourWeeks) {
		this.editFourWeeks=editFourWeeks;
	}
	
	public String[] buildOutputLine(){
		return new String[] {
			String.valueOf(textOneDay),
			String.valueOf(textOneWeek),
			String.valueOf(textTwoWeeks),
			String.valueOf(textFourWeeks),
			String.valueOf(editOneDay),
			String.valueOf(editOneWeek),
			String.valueOf(editTwoWeeks),
			String.valueOf(editFourWeeks)
		};
	}
	
	public static final String[] buildOutputHeadlines(double factor) {
		return new String[] {
				"Text D ("+String.valueOf(factor)+")",
				"Text W ("+String.valueOf(factor)+")",
				"Text 2W ("+String.valueOf(factor)+")",
				"Text 4W ("+String.valueOf(factor)+")",
				"Edit D ("+String.valueOf(factor)+")",
				"Edit W ("+String.valueOf(factor)+")",
				"Edit 2W ("+String.valueOf(factor)+")",
				"Edit 4W ("+String.valueOf(factor)+")"
			};
	}
}
