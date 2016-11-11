package ao.thesis.wikianalyse;

import java.math.BigInteger;
import java.util.ArrayList;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.sweble.wikitext.dumpreader.export_0_10.PageType;
import org.sweble.wikitext.dumpreader.export_0_10.RevisionType;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

public class PageOrga {
	
	private static Logger logger = Logger.getLogger(PageOrga.class);
	
	private List<Object> revisions;
	private WtEngineImpl engine;
	
	private TextAnalysis textAnalysis;
	private EditAnalysis editAnalysis;
	
	public PageOrga(Object page, WtEngineImpl engine, 
			TextAnalysis textAnalysis, EditAnalysis editAnalysis){
		
		this.revisions = ((PageType) page).getRevisionOrUpload();
		this.engine = engine;
		
		this.textAnalysis = textAnalysis;
		this.editAnalysis = editAnalysis;
	}
	
	public void analyse(PageId pageId){

		logger.info("Start analysis.");

		for(Object revision : revisions){
			
			String wikitext = "";
			BigInteger id = BigInteger.ZERO;
			
			if(revision instanceof RevisionType){
				wikitext = ((RevisionType) revision).getText().getValue();
				id = ((RevisionType) revision).getId();
			}
			
			EngProcessedPage epp = null;
			try {
				epp = engine.postprocess(pageId, wikitext, null);
			} catch (EngineException e) {
				logger.error("EngProcessedPage could not be generated.", e);
				break;
			}
			textAnalysis.tokenize(epp, id);
			editAnalysis.addProcessedPage(epp);
		}
		
		textAnalysis.updateSources();
		editAnalysis.setEditScripts();
		
		logger.info("Analysis ready.");
	}
	
	/**Judges edits of the given editor by calculating the average edit longevity
	 * for all edits.
	 * @param editorId	- id of judged editor
	 * @param distance	- distance between judged and judging revision
	 * @return average edit longevity for edits by the given editor
	 */
	public double judgeEdits(BigInteger editorId, int distance){
		
		List<Object> editorRevisions = getAllRevisionsByEditorId(editorId);
		return editAnalysis.calculateEditQuality(editorRevisions, distance);
	}
	
	/**Judges texts of the given editor by calculating the average decay quality for all
	 * inserted texts.
	 * @param editorId	- id of judged editor
	 * @param max		- max number of following revisions that are used for judging
	 * @return average decay quality for inserted text by the given editor
	 */
	public double judgeText(BigInteger editorId, int max){
		
		double sumQuality = 0.0;
		
		List<Object> editorRevisions = getAllRevisionsByEditorId(editorId);
		List<Object> judgingRevisions;
		
		for(Object revision : editorRevisions){
			
			if(revision instanceof Integer){
				int revisionindex = (Integer) revision;
				
				judgingRevisions = getJudgingRevisions(revisionindex, max);
				BigInteger id = ((RevisionType)revisions.get(revisionindex)).getId();
				
				sumQuality += textAnalysis.calculateDecayQuality(revisionindex, id, judgingRevisions);
			}
		}
		return sumQuality/(editorRevisions.size()); 
	}
	
	//-----------------------------------------------

	/**Getter for revision indices by one editor.
	 * @param editorId
	 * @return list of revision indices (!) that were created by the editor with the given id
	 */
	public List<Object> getAllRevisionsByEditorId(BigInteger editorId){
		List<Object> editorRevisions = new ArrayList<Object>();
		if(editorId != null){
			for(int index = 0 ; index < revisions.size() ; index++){
				Object item = revisions.get(index);
				
				if (item instanceof RevisionType){
					BigInteger currId = ((RevisionType) item).getContributor().getId();
					
					if(currId != null && currId.equals(editorId)){
						editorRevisions.add(index);
					}
				}
			}
		}
		return editorRevisions;
	}
	
	public int getNamedEntityCount(BigInteger editorId){
		int count = 0;
		List<Object> editorRevisions = getAllRevisionsByEditorId(editorId);
		for(Object revision : editorRevisions){
			if(revision instanceof Integer){
				int revisionindex = (Integer) revision;
				BigInteger id = ((RevisionType)revisions.get(revisionindex)).getId();
				count += textAnalysis.countNamedEntities(revisionindex, id);
			}
		}
		return count;
	}
	
	public int getWordCount(BigInteger editorId){
		int count = 0;
		List<Object> editorRevisions = getAllRevisionsByEditorId(editorId);
		for(Object revision : editorRevisions){
			if(revision instanceof Integer){
				int revisionindex = (Integer) revision;
				BigInteger id = ((RevisionType)revisions.get(revisionindex)).getId();
				count += textAnalysis.countWords(revisionindex, id);
			}
		}
		return count;
	}
	
	public int getMathTokenCount(BigInteger editorId){
		int count = 0;
		List<Object> editorRevisions = getAllRevisionsByEditorId(editorId);
		for(Object revision : editorRevisions){
			if(revision instanceof Integer){
				int revisionindex = (Integer) revision;
				BigInteger id = ((RevisionType)revisions.get(revisionindex)).getId();
				count += textAnalysis.countMathTokens(revisionindex, id);
			}
		}
		return count;
	}
	
	public int countPersistentText(BigInteger editorId){
		int count = 0;
		List<Object> editorRevisions = getAllRevisionsByEditorId(editorId);
		
		for(Object item : editorRevisions){
			if(item instanceof Integer){
				int revisionindex = (Integer) item;
				BigInteger id = ((RevisionType)revisions.get(revisionindex)).getId();

				try {
					Duration fourWeeks = DatatypeFactory.newInstance()
							.newDurationDayTime(true, 28, 0, 0, 0);
					if(isPersistentAfterDuration(revisionindex, id, 0.9, fourWeeks)){
						count++;
					}
				} catch (DatatypeConfigurationException e) {
					logger.error("Could create duration.",e);
				}
			}
		}
		return count;
	}
	
	private boolean isPersistentAfterDuration(int revisionindex, BigInteger id, 
			double factor, Duration duration){
		
		int wordInserts;
		if((wordInserts = textAnalysis.countWords(revisionindex, id))==0){
			//TODO in this case should not be counted in total revision count
			return false;
		}
		
		double count = 0.0;
		
		try {
			XMLGregorianCalendar thisTimestamp = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar();
			thisTimestamp = (XMLGregorianCalendar) 
					((RevisionType) revisions.get(revisionindex)).getTimestamp().clone();
			
			thisTimestamp.add(duration);
			
			for(int index = 0 ; index < revisions.size() ; index++){
				Object revision = revisions.get(index);
				
				if(revision instanceof RevisionType){
					XMLGregorianCalendar judgingTimestamp = ((RevisionType) revision).getTimestamp();
					
					if(judgingTimestamp.compare(thisTimestamp) >= 0){
						count = (double) textAnalysis.countWords(index, id);
						break;
					}
				}
			}
		} catch (DatatypeConfigurationException e) {
			logger.error("Could not add duration to timestamp.",e);
		}
		return (count >= (factor * ((double) wordInserts)));
	}

	/**Returns indices of revisions that follow the given revision and do not have the same editor
	 * @param index		- index of revision to be judged
	 * @param max		- max number of judging revisions to search for
	 * @return indices of the judging revisions
	 */
	private List<Object> getJudgingRevisions(int index, int max){
		List<Object> judgingRevisions = new ArrayList<Object>();
		
		try{
			if(index < revisions.size()){
				BigInteger judgedContribId;
				
				if(revisions.get(index) instanceof RevisionType){
					
					judgedContribId = ((RevisionType) revisions.get(index)).getContributor().getId();
					
					index++;
					while (index < revisions.size() && 0 < max){
						RevisionType nextRevision = ((RevisionType) revisions.get(index));
						
						if(nextRevision.getContributor().getId() != null //allows only registered editors to judge
								&& !nextRevision.getContributor().getId().equals(judgedContribId)){
							judgingRevisions.add((Object) index);
							max--;
						}
						index++;
					}
				}
			} else {
				throw new IllegalArgumentException("Index not part of revisions.");
			}
		} catch (IllegalArgumentException e) {
			
		}
		return judgingRevisions;
	}

}
