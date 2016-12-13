package ao.thesis.wikianalyse;

import java.io.IOException;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.io.InputReader;
import ao.thesis.wikianalyse.io.OutputWriter;

import ao.thesis.wikianalyse.model.RatingBuilder;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.ratingsystems.MathRatingSystem;
import ao.thesis.wikianalyse.ratingsystems.SwebleEditRatingSystem;
import ao.thesis.wikianalyse.ratingsystems.TokenCountRatingSystem;

/**System that rates Wikipedia editors.
 * 
 * @author anna
 */
public abstract class RatingSystem {
	
	protected static Logger logger;
	private String inputdir;
	private String outputdir;
	
	protected WikiOrga orga = null;
	protected RatingBuilder rb = null;
	protected OutputWriter ow = null;
	
	private boolean writePreOutput = false;
	private boolean writeProcOutput = true;
	private boolean writePostOutput = false; //TODO editor reputation output

	
	public void run(String inputdir, String outputdir) throws Exception{
		
		this.inputdir=inputdir;
		this.outputdir=outputdir;
		logger = Logger.getLogger(RatingSystem.class);
		
		logger.info("Start init.");
		init();
		
		logger.info("Start preprocession.");
		preprocess();
		logger.info("Preprocession ready.");
		if(writePreOutput){
			writePreOutcome();
			logger.info("Wrote Output.");
		}
		
		logger.info("Start procession.");
		process();
		logger.info("Procession ready.");
		if(writeProcOutput){
			writeProcOutcome();
			logger.info("Wrote Output.");
		}
		
		logger.info("Start postprocession.");
		postprocess();
		logger.info("Postprocession ready.");
		if(writePostOutput){
			writePostOutcome();
			logger.info("Wrote Output.");
		}
	}
	
	private void init(){
		try {
			readPages(inputdir);
		} catch (Exception e) {
			return;
		}
		setOutputWriter(new OutputWriter(outputdir, orga.getTitles()));
	};
	
	private void readPages(String dir) throws Exception{
		setWikiOrga((new InputReader(dir)).getWikiOrga());
	};
	
	
	public abstract void preprocess();
	
	public abstract void process();

	public abstract void postprocess();
	
	
	public void setWikiOrga(WikiOrga orga){
		this.orga = orga;
		setRatingBuilder(new RatingBuilder(this.orga.getRevisionIds()));
	}
	
	public void setRatingBuilder(RatingBuilder rb){
		this.rb = rb;
	}
	
	public RatingBuilder getRatingBuilder(){
		return rb;
	}
	
	public void setOutputWriter(OutputWriter ow){
		this.ow = ow;
	}
	
	
	public void writePreOutcome() throws IOException{
		if(this instanceof SwebleEditRatingSystem){
//			writeEditScript();
		} else if (this instanceof TokenCountRatingSystem 
				|| this instanceof MathRatingSystem){
//			writeTokens();
		}
	}
	
	public void writeProcOutcome() throws IOException{
		writeRating();
	}
	
	public void writePostOutcome() throws IOException{
//		writeTimeline();
//		writeEditors();
	}
	
	private void writeRating() throws IOException{
		for(String title : orga.getTitles()){
			ow.writeRatingsOutput(rb.getOutput(orga.getSortedHistory(title)), rb.getHeadLines(), title, "Rating");
		}
	};
	
//	TODO
//	private void writeTimeline() throws IOException{
//		ow.writeTimelineOutput(rb.getOutput(orga.getChronologicalRevisions()), rb.getHeadLines(), "Timeline");
//	};
//	
//	private void writeEditors() throws IOException{
//		ow.writeEditorOutput(rb.getOutput(orga.getChronologicalRevisions()), rb.getHeadLines(), "Editors");
//	};
	
}
