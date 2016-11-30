package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class MarkupCountRating implements Rating {
	
	private int insertedMathTokens = 0;
	
	private int insertedNETokens = 0;
	
	private int insertedLinks = 0;
	
	private int insertedCategories = 0;
	
	private int insertedFiles = 0;
	
	private int insertedHeaderWords = 0;
	
	private int wordsSetItalic = 0;
	
	private int wordsSetBold = 0;

	
	public void setInsertedMathTokens(int insertedMathTokens){
		this.insertedMathTokens=insertedMathTokens;
	}
	
	public void setInsertedNETokens(int insertedNETokens) {
		this.insertedNETokens=insertedNETokens;	
	}
	
	public void setInsertedLinks(int insertedLinks){
		this.insertedLinks=insertedLinks;
	}
	
	public void setInsertedCategories(int insertedCategories) {
		this.insertedCategories=insertedCategories;	
	}
	
	public void setInsertedFiles(int insertedFiles){
		this.insertedFiles=insertedFiles;
	}
	
	public void setInsertedHeaderWords(int insertedHeaderWords) {
		this.insertedHeaderWords=insertedHeaderWords;	
	}
	
	@Override
	public String[] buildOutputLine(){
		return new String[] {
			String.valueOf(insertedMathTokens),
			String.valueOf(insertedNETokens),
			String.valueOf(insertedLinks),
			String.valueOf(insertedCategories),
			String.valueOf(insertedFiles),
			String.valueOf(insertedHeaderWords)
		};
	}

	@Override
	public String[] buildOutputHeadlines() {
		return new String[] {
			"Inserted MathTokens",
			"Inserted NETokens",
			"Inserted Links",
			"Inserted Categories",
			"Inserted Files",
			"Inserted HeaderWords"
		};
	}

	@Override
	public void setEditorReputation() {
		// TODO Auto-generated method stub
		
	}
	
}
