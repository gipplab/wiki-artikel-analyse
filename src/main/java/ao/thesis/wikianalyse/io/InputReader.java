package ao.thesis.wikianalyse.io;

/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sweble.wikitext.dumpreader.DumpReader;
import org.sweble.wikitext.dumpreader.model.DumpConverter;
import org.sweble.wikitext.dumpreader.model.Page;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.WikiOrgaImpl;

import org.sweble.wikitext.dumpreader.model.Contributor;


public class InputReader {
	
	private static Logger logger = Logger.getLogger(InputReader.class);
	
	private static WikiConfig config = DefaultConfigEnWp.generate();
	
	private static WtEngineImpl engine = new WtEngineImpl(config);
	
	
	private Map<RevisionID, EngProcessedPage> engProcessedPages = new HashMap<RevisionID, EngProcessedPage>();
	
//	private Map<Contributor, List<RevisionID>> editors = new HashMap<Contributor, List<RevisionID>>();
	
	private Map<BigInteger, List<RevisionID>> registredEditors = new HashMap<BigInteger, List<RevisionID>>();
	
	private Map<String, PageId> pageInfos = new HashMap<String, PageId>();
	
	//TODO language usage
	private Map<String, String> languages = new HashMap<String, String>();
	
	
	public InputReader(String dir) throws Exception {
		read(dir);
	}
	
	public WikiOrga getWikiOrga(){
		
		WikiOrgaImpl orga = new WikiOrgaImpl();
		
		orga.setRevisionIDs(engProcessedPages.keySet());
		
		orga.setPageTitleAndId(pageInfos);
		
//		orga.setEditors(registredEditors);
		
		orga.setEngProcessedPages(engProcessedPages);
		
		return orga;
	}
	
	private void read(String dir) throws Exception {
		
		File[] inputFiles = new File(dir).listFiles();
		DumpReader reader = null;
		FileInputStream stream = null;

		if(inputFiles.length != 0){
			try {
				for(File file : inputFiles){
					logger.info("Read file: " + file.getName()+".");
					stream = new FileInputStream(file);
					reader = new DumpReaderExtension(stream, logger, dir);
					reader.unmarshal();
				}
			} finally {
				try {
					if (reader != null)
						reader.close();
				} finally {
					if (stream != null)
						stream.close();
				}
			}
		}
	}
	
	
	private class DumpReaderExtension extends DumpReader
	{
		private Logger logger;
		
		private DumpConverter converter = new DumpConverter();
		
		private DumpReaderExtension(InputStream stream, Logger logger, String dir) throws Exception{
			super(stream, StandardCharsets.UTF_8, dir, logger, true);
			this.logger=logger;
		}
		
		@Override
		protected void processPage(Object mediaWiki, Object item) throws Exception {
			
			Page page = converter.convertPage(item);
			
			String title = page.getTitle();
			
			PageId id = new PageId(PageTitle.make(config, title), -1);
			
			filterAndProcessRevisions(page, id);
			
			pageInfos.put(title, id);
			
			languages.put(title, getLanguage(mediaWiki));
		}
		
		private String getLanguage(Object mediaWiki){
			
			if (mediaWiki instanceof org.sweble.wikitext.dumpreader.export_0_5.MediaWikiType)
				return ((org.sweble.wikitext.dumpreader.export_0_5.MediaWikiType) mediaWiki).getLang();

			else if (mediaWiki instanceof org.sweble.wikitext.dumpreader.export_0_6.MediaWikiType)
				return ((org.sweble.wikitext.dumpreader.export_0_6.MediaWikiType) mediaWiki).getLang();

			else if (mediaWiki instanceof org.sweble.wikitext.dumpreader.export_0_7.MediaWikiType)
				return ((org.sweble.wikitext.dumpreader.export_0_7.MediaWikiType) mediaWiki).getLang();

			else if (mediaWiki instanceof org.sweble.wikitext.dumpreader.export_0_8.MediaWikiType)
				return ((org.sweble.wikitext.dumpreader.export_0_8.MediaWikiType) mediaWiki).getLang();

			else if (mediaWiki instanceof org.sweble.wikitext.dumpreader.export_0_9.MediaWikiType)
				return ((org.sweble.wikitext.dumpreader.export_0_9.MediaWikiType) mediaWiki).getLang();

			else if (mediaWiki instanceof org.sweble.wikitext.dumpreader.export_0_10.MediaWikiType)
				return ((org.sweble.wikitext.dumpreader.export_0_10.MediaWikiType) mediaWiki).getLang();

			else return "";
	}
		
		/** 
		 * WikiTrust ignores revisions that are followed by a revision by the same author
		 */
		private void filterAndProcessRevisions(Page page, PageId pageId){
			
			int originalSize = page.getRevisions().size();
			Contributor curr, next;
			
			int newIndex = 0;
			
			for(int currPosition = 0 ; currPosition < (page.getRevisions().size() - 1) ; currPosition++){
				
				curr = page.getRevisions().get(currPosition).getContributor();
				next = page.getRevisions().get(currPosition + 1).getContributor();
				
				if((curr != null) && (next != null) && (curr.getId() != null) && (next.getId() != null)
						&& (curr.getId().equals(next.getId()))){
					
					page.getRevisions().remove(currPosition);
					
					currPosition--;
					
				} else if ((curr != null) && (curr.getId() != null)) {
					
					RevisionID id = new RevisionID(page.getRevisions().get(currPosition), newIndex, page.getTitle());
					
					if(!registredEditors.containsKey(curr.getId())){
						registredEditors.put(curr.getId(), new ArrayList<RevisionID>());
					}
					registredEditors.get(curr.getId()).add(id);
					
					//TODO fix missing last revision!
					
					try {
						engProcessedPages.put(id, engine.postprocess(pageId, page.getRevisions().get(currPosition).getText(), null));
						
					} catch (EngineException e) {
						logger.error("EngProcessedPage could not be generated.", e);
						engProcessedPages.put(id, null);
					}
					newIndex++;
				}
			}
			logger.info("Filtered "+(originalSize-page.getRevisions().size())+" revisions.");
		}
	}

}

