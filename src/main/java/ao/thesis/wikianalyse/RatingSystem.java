package ao.thesis.wikianalyse;

import ao.thesis.wikianalyse.io.InputReader;
import ao.thesis.wikianalyse.io.OutputWriter;

import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.WikiOrga;

/**
 * System to rate Wikipedia editors.
 * 
 * @author anna
 *
 */
public abstract class RatingSystem {
	
	
	protected WikiOrga orga = null;
	
	protected RatingBuilder rb = null;
	
	protected OutputWriter ow = null;
	
	/**Reads pages from input direction.
	 * @param dir			- input direction
	 * @throws Exception is thrown if the input can not be read.
	 */
	public void readPages(String dir) throws Exception{
		
		setWikiOrga((new InputReader(dir)).getWikiOrga());
	};

	public void setWikiOrga(WikiOrga orga){
		
		this.orga = orga;
		
		rb = new RatingBuilder(this.orga.getRevisionIds());
	}
	
	/** Processes input revisions to something that can be rated.
	 * @param outputDir
	 */
	public abstract void setEditVolume(String outputDir);
	
	/** Associates editors to edits and text if needed.
	 * @param outputDir
	 */
	public abstract void associateEditors(String outputDir);
	
	/** Rates revisions.
	 * @param outputDir
	 */
	public abstract void rateEdits(String outputDir);
	
	/** Rates editors.
	 * @param outputDir
	 */
	public abstract void rateEditors(String outputDir);
	
	
//	public void printOutput(String dir){
//		
//		OutputWriter writer = new OutputWriter(dir, orga.getTitles());
//		writer.writeTimelineOutput();
//		rb.output(orga.getChronologicalRevisions());
//	};
	

	public void run(String inputdir, String outputdir) throws Exception{
		
		readPages(inputdir);
		
		setEditVolume(outputdir);
		
		associateEditors(outputdir);
		
		rateEdits(outputdir);
		
		rateEditors(outputdir);
	}
	
}
