package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class TokenCountRating implements Rating {

	/* Revision Rating:	quantity measures
	 */
//	private int tokens = 0;		// does not cover tables, templates,..
	private int mathFormulas = 0;
//	private int mathTokens = 0;	// rated separately
	private int nETokens = 0;
	private int links = 0;
	private int categories = 0;
	private int files = 0;
	private int headerWords = 0;
	private int italic = 0;		// not used
	private int bold = 0;		// not used
	
	/* Editor Rating: 	quality measure, depends on the judging distance 
	 * 					between the current and several later revisions. Editors 
	 * 					can only be judged properly if there already are enough
	 * 					judging revisions.
	 */
	private double textdecayQuality = 0.0;
	
	@Override
	public double getReputationMeasureResult() {
		return textdecayQuality;
	}
	
//	public void setInsertedTokens(int insertedTokens){
//		this.tokens=insertedTokens;
//	}
	
	public void setInsertedMathFormulas(int insertedMathFormulas) {
		this.mathFormulas=insertedMathFormulas;
	}
	
//	public void setInsertedMathTokens(int insertedMathTokens){
//		this.mathTokens=insertedMathTokens;
//	}
	
	public void setInsertedNETokens(int insertedNETokens) {
		this.nETokens=insertedNETokens;	
	}
	
	public void setInsertedLinks(int insertedLinks){
		this.links=insertedLinks;
	}
	
	public void setInsertedCategories(int insertedCategories) {
		this.categories=insertedCategories;	
	}
	
	public void setInsertedFiles(int insertedFiles){
		this.files=insertedFiles;
	}
	
	public void setInsertedHeaderWords(int insertedHeaderWords) {
		this.headerWords=insertedHeaderWords;	
	}
	
	@Override
	public String[] buildOutputLine(){
		return new String[] {
//			String.valueOf(tokens),
			String.valueOf(mathFormulas),
//			String.valueOf(mathTokens),
			String.valueOf(nETokens),
			String.valueOf(links),
			String.valueOf(categories),
			String.valueOf(files),
			String.valueOf(headerWords)
		};
	}
	
	public static final String[] buildOutputHeadlines(String description) {
		return new String[] {
//			"Tokens ("+description+")",
			"MathForm ("+description+")",
//			"MathTokens ("+description+")",
			"NEs ("+description+")",
			"Links ("+description+")",
			"Categs ("+description+")",
			"Files ("+description+")",
			"Heads ("+description+")"
		};
	}
	
//	/*--------------------------------------
//	 * calculation methods for an edit significance
//	 */
//	public static final String[] postProcHL(double linkW, double catW, double fileW) {
//		return new String[] {
//			"MRS ("+linkW+", "+catW+", "+fileW+")"
//		};
//	}
//	
//	public double calculateWeightedRefSignificance(double linkW, double catW, double fileW) {
//		return linkW * links + catW * categories + fileW * files;
//	}
//	
//	public String[] buildPostProcLine(double linkW, double catW, double fileW) {
//		return new String[] {
//			String.valueOf(calculateWeightedRefSignificance(linkW, catW, fileW))
//		};
//	}
}
