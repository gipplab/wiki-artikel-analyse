package ao.thesis.wikianalyse.io.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.sweble.wikitext.dumpreader.DumpReader;
import org.sweble.wikitext.dumpreader.model.DumpConverter;
import org.sweble.wikitext.dumpreader.model.Page;
import org.sweble.wikitext.dumpreader.model.Revision;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.analysis.Analysis;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.preprocession.PreprocessionException;
import ao.thesis.wikianalyse.analysis.procession.ProcessionException;
import ao.thesis.wikianalyse.analysis.ratingsystems.NamedEntityAnalysis;
import ao.thesis.wikianalyse.io.output.NEDataOutputWriter;
import ao.thesis.wikianalyse.io.output.TokenOutputWriter;
import ao.thesis.wikianalyse.io.output.WikiTrustDataOutputWriter;
import ao.thesis.wikianalyse.utils.SwebleException;
import ao.thesis.wikianalyse.utils.SwebleUtils;
import edu.stanford.nlp.ie.crf.CRFClassifier;


public class InputReader {
	
	private final static Logger LOGGER = Logger.getLogger(InputReader.class);
	
	private final String dir;
	
	private final Analysis analysis;
	
	private final DateTime limitReadingDate;
	private final int limitRevisions;
	private final int limitArticles;
	
	private UserGroupReader usergroupreader;

	
	public InputReader(String dir, 
			Analysis analysis, 
			DateTime limitReadingDate,
			int limitRevisions,  
			int limitArticles) {
		
		Validate.notNull(dir);
		Validate.notNull(limitReadingDate);
		Validate.notNull(limitRevisions);
		Validate.notNull(limitArticles);
		Validate.notNull(analysis);
		
		this.dir=dir;
		this.analysis=analysis;
		this.limitReadingDate=limitReadingDate;
		this.limitRevisions=limitRevisions;
		this.limitArticles=limitArticles;
		
		usergroupreader = null;
		try {
			usergroupreader = new UserGroupReader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	
	public void read() throws Exception {
		File[] inputFiles = new File(dir).listFiles();
		
		if(inputFiles.length != 0){
			// Unable totalEntitySizeLimit to enable the full reading of the XML-Dump
			System.setProperty("jdk.xml.totalEntitySizeLimit", "0"); 
			
			DumpReader reader = null;
			FileInputStream stream = null;
			
			try {
				for(File file : inputFiles){
					stream = new FileInputStream(file);
					reader = new WikipediaXMLDumpReader(stream, LOGGER, dir+"/"+file.getName());
					reader.unmarshal();
				}
			} finally {
				// Undo totalEntitySizeLimit
				System.clearProperty("jdk.xml.totalEntitySizeLimit"); 
				try {
					if (stream != null){
						stream.close();
					}
					if (usergroupreader != null){
						usergroupreader.close();
					}
				} finally {
					if (stream != null){
						stream.close();
					}
					if (usergroupreader != null){
						usergroupreader.close();
					}
				}
			}
		}
	}

	private class WikipediaXMLDumpReader extends DumpReader{
		
		private final DumpConverter converter = new DumpConverter();
		
		private final Filter filter = new Filter(limitReadingDate);
		
		private final WikiTrustDataOutputWriter writer = new WikiTrustDataOutputWriter("Timeline");
		
		private final NEDataOutputWriter newriter = new NEDataOutputWriter("NETimeline");
		
		private final BarnstarReader barnstarReader = new BarnstarReader("Barnstars");
		
		private int pageCounter = 0;
		
		private int revisionCounter = 0;
	
		private WikipediaXMLDumpReader(InputStream stream, Logger logger, String dir) 
				throws Exception{
			super(stream, StandardCharsets.UTF_8, dir, logger, true);
			

		}
		
		@Override
		protected void processPage(Object mediaWiki, Object item) throws RevisionLimitException {
			
			Page page = converter.convertPage(item);
			BigInteger namespace = page.getNamespace();
			
			/*
			 * Article Pages
			 */
			if(namespace.equals(BigInteger.valueOf(0)) && !page.isRedirect()){ //Pages from NS0 and no redirect page
				
				PageTitle title = null;
				
				try {
					title = SwebleUtils.buildPageTitle(page.getTitle());
				} catch (SwebleException e) {
					LOGGER.warn("PageTitle could not be build: "+page.getTitle(), e);
				}
				
				List<Revision> revisions = page.getRevisions();
				
				if(Objects.nonNull(title) && revisions.get(0).getTimestamp().isBefore(filter.getLimitDate())){
					
					/*
					 * There are some cases it which the revisions are not sorted in the correct way in the dump.
					 * It is necessary to sort the revisions.
					 */
					revisions.sort(new Comparator<Revision>(){
						@Override
						public int compare(Revision r1, Revision r2) {
							return r1.getTimestamp().compareTo(r2.getTimestamp());
						}
					});
					
					revisions = filter.filter(revisions);
//					
					LOGGER.info("NS0: "+page.getTitle()+" "+getParsedCount()+" "+revisionCounter+"/"+limitRevisions
							+" "+pageCounter+"/"+limitArticles);
					
					pageCounter++;
					revisionCounter+=revisions.size();
					
					if(usergroupreader != null){
						for(Revision revision : revisions){
							if(revision.getContributor() != null && revision.getContributor().getId() != null && revision.getContributor().getUsername() != null){
								BigInteger id = revision.getContributor().getId();
								String name = revision.getContributor().getUsername();
								usergroupreader.print(id , name);
							} else {
								usergroupreader.print(BigInteger.valueOf(-1), "Anonym");
							}
						}
					}

					analysePage(revisions, title);
				}
			
			/*
			 * User Pages
			 */
			} if(namespace.equals(BigInteger.valueOf(2))){
//				LOGGER.info("NS2: "+page.getTitle()+" "+getParsedCount());
//				
//				/*
//				 * Barnstar prefixes
//				 */
//				String originalBarnstar = "[[Immagine:Original Barnstar Hires.png";
//				String editBarnstar = "[[Immagine:Editors Barnstar Hires.png";
//				String workingBarnstar = "[[Immagine:Working Man's Barnstar Hires.png";
//				String graphicBarnstar = "[[Immagine:Graphic Designer Barnstar Hires.png";
//				String defenderBarnstar = "[[Immagine:WikiDefender Barnstar Hires.png";
//				String qualityBarnstar = "[[Immagine:Quality Barnstar v2.0.png";
//				String eraseBarnstar = "[[Immagine:Barnstar erase recentism.svg";
//				
//				String[] segments = page.getTitle().split(":");
//				String username;
//				if(segments.length == 2){
//					username = segments[1];
//				} else {
//					return;
//				}
//				String latestText = page.getRevisions().get(page.getRevisions().size()-1).getText();
//				
//				int count = 0;
//				if(latestText.contains(originalBarnstar)){
//					count++;
//				}
//				if(latestText.contains(editBarnstar)){
//					count++;
//				}
//				if(latestText.contains(workingBarnstar)){
//					count++;
//				}
//				if(latestText.contains(graphicBarnstar)){
//					count++;
//				}
//				if(latestText.contains(defenderBarnstar)){
//					count++;
//				}
//				if(latestText.contains(qualityBarnstar)){
//					count++;
//				}
//				if(latestText.contains(eraseBarnstar)){
//					count++;
//				}
				
//				barnstarReader.writeBarnstarOutput(username, String.valueOf(count));
			}
		}
		
		
		private void analysePage(List<Revision> revisions, PageTitle title) throws RevisionLimitException{

			List<ProcessedRevision> procRevisions = null;		
			List<ProcessedRevision> neProcRevisions = null;
			try {
				if(pageCounter > limitArticles || revisionCounter > limitRevisions){
					throw new RevisionLimitException();
				}
				
				List<PreprocessedRevision> preprocessedRevisions = analysis.preprocess(revisions, title);
//				List<PreprocessedRevision> nePreprocessedRevisions = neanalysis.preprocess(revisions, title);
				
//				TokenOutputWriter.writeTokens(preprocessedRevisions, title);
				
				procRevisions = analysis.process(preprocessedRevisions);
//				neProcRevisions = neanalysis.process(nePreprocessedRevisions);
				
			} catch (PreprocessionException e) {
				LOGGER.warn("Page could not be preprocessed: "+title, e);
				procRevisions=null;
				neProcRevisions=null;
				
			} catch (ProcessionException e) {
				LOGGER.warn("Page could not be processed: "+title, e);
				procRevisions=null;
				neProcRevisions=null;
				
			} finally {
				if(Objects.nonNull(procRevisions) 
//						&& Objects.nonNull(neProcRevisions)
						){
					
					writer.writeRevisions(procRevisions, title);
//					newriter.writeRevisions(neProcRevisions, title);
				}
			}
		}
	}
}