package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;

import org.joda.time.DateTime;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

public class ProcessedRevision extends SwebleRevision {

	// Edit Ouantity
	
	private double editDistance = 0;
	
	// Text Ouantity
	
	private int insertedWhiteSpaceSegWords = 0;
	
	private int insertedMarkupSegWords = 0;
	
	public int insertedMath = 0;
	
//	private int insertedMarkupElements = 0;
	
	private int insertedNEs = 0;
	
	private HashMap<Integer, Integer> whiteSpaceSegWords = null; 	// Results from Whitespace Segmentation and Matching
	
	private HashMap<Integer, Integer> markupSegWords = null;		// Results from Markup Supported Word Segmentation and Matching
	
	public HashMap<Integer, Integer> math = null;		// Results from Markup Supported Word Segmentation and Matching
	
	private HashMap<Integer, Integer> markupElements = null;		// Results from Markup Segmentation and Matching
	
	private HashMap<Integer, Integer> NEs = null;					// Results from NE Segmentation and Matching
	
	// References
	
	private ProcessedRevision prev = null;

	/**
	 * Revisions that follow this revisions and have a different editor thus can function as judges for this
	 * revision.
	 * WikiTrust TextDecayQuality: 	ten following revisions 
	 * WikiTrust EditLongevity:		three following revisions
	 * Efficiency:					three revisions after two weeks (in this program)
	 */
	private ProcessedRevision[] judging = null;
	
	/**
	 * Revisions that go before this revisions and have a different editor thus can be judges by this
	 * revision.
	 * WikiTrust TextDecayQuality: 	ten previous revisions 
	 * WikiTrust EditLongevity:		three previous revisions
	 * Efficiency:					three revisions before two weeks (in this program)
	 */
	private ProcessedRevision[] judged = null;
	
	public ProcessedRevision(int id, String contributorName, DateTime timestamp, PageId pageId, PageTitle pageTitle, EngProcessedPage procPage) {
		super(id, timestamp);
		super.setContributorName(contributorName);
		super.setPageId(pageId);
		super.setPageTitle(pageTitle);
		super.setEngProcessedPage(procPage);
	}

	public ProcessedRevision[] getJudging() {
		return judging;
	}

	public void setJudging(ProcessedRevision[] judging) {
		this.judging = judging;
	}

	public ProcessedRevision[] getJudged() {
		return judged;
	}

	public void setJudged(ProcessedRevision[] judged) {
		this.judged = judged;
	}

	public ProcessedRevision getPrev() {
		return prev;
	}

	public void setPrev(ProcessedRevision prev) {
		this.prev = prev;
	}

	public double getEditDistance() {
		return editDistance;
	}

	public void setEditDistance(double editDistance) {
		this.editDistance = editDistance;
	}

	public int getInsertedWhiteSpaceSegWords() {
		return insertedWhiteSpaceSegWords;
	}

	public void setInsertedWhiteSpaceSegWords(int insertedWhiteSpaceSegWords) {
		this.insertedWhiteSpaceSegWords = insertedWhiteSpaceSegWords;
	}

	public int getInsertedMarkupSegWords() {
		return insertedMarkupSegWords;
	}

	public void setInsertedMarkupSegWords(int insertedMarkupSegWords) {
		this.insertedMarkupSegWords = insertedMarkupSegWords;
	}

//	public int getInsertedMarkupElements() {
//		return insertedMarkupElements;
//	}

//	public void setInsertedMarkupElements(int insertedMarkupElements) {
//		this.insertedMarkupElements = insertedMarkupElements;
//	}

	public HashMap<Integer, Integer> getWhiteSpaceSegWords() {
		return whiteSpaceSegWords;
	}

	public void setWhiteSpaceSegWords(HashMap<Integer, Integer> whiteSpaceSegWords) {
		this.whiteSpaceSegWords = whiteSpaceSegWords;
	}

	public HashMap<Integer, Integer> getMarkupSegWords() {
		return markupSegWords;
	}

	public void setMarkupSegWords(HashMap<Integer, Integer> markupSegWords) {
		this.markupSegWords = markupSegWords;
	}

	public HashMap<Integer, Integer> getNEs() {
		return NEs;
	}

	public void setNEs(HashMap<Integer, Integer> nEs) {
		NEs = nEs;
	}

	public int getInsertedNEs() {
		return insertedNEs;
	}

	public void setInsertedNEs(int insertedNEs) {
		this.insertedNEs = insertedNEs;
	}

//	public HashMap<Integer, Integer> getMarkupElements() {
//		return markupElements;
//	}

//	public void setMarkupElements(HashMap<Integer, Integer> markupElements) {
//		this.markupElements = markupElements;
//	}

}
