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
import java.util.HashMap;
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

import ao.thesis.wikianalyse.model.Editor;
import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.model.WikiOrga;
import ao.thesis.wikianalyse.model.WikiOrgaImpl;

import org.sweble.wikitext.dumpreader.model.Contributor;


public class InputReader {
	
	private static Logger logger = Logger.getLogger(InputReader.class);
	
	private static WikiConfig config = DefaultConfigEnWp.generate();
	private static WtEngineImpl engine = new WtEngineImpl(config);
	
	private UserGroupReader userGroupReader = null;

	private Map<RevisionID, EngProcessedPage> engProcessedPages = new HashMap<RevisionID, EngProcessedPage>();
	private Map<String, Editor> editors = new HashMap<String, Editor>();
	private Map<String, PageId> pageIds = new HashMap<String, PageId>();
	private Map<String, String> languages = new HashMap<String, String>();
	
	private final static int MAX_PAGES = 30;
	private final static int MAX_REVISIONS = 20;
	
	public InputReader(String dir) throws Exception {
		read(dir);
	}
	
	
	public WikiOrga getWikiOrga(){
		
		WikiOrgaImpl orga = new WikiOrgaImpl();
		
		orga.setRevisionIDs(engProcessedPages.keySet());
		orga.setPageTitleAndId(pageIds);
		if(userGroupReader != null){
			orga.setUsergroupReader(userGroupReader);
		}
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
					
					if(file.getName().endsWith(".sql")){
						userGroupReader = new UserGroupReader(file, logger);
						
					} else {

						logger.info("Read file: " + file.getName()+".");
						stream = new FileInputStream(file);
						reader = new DumpReaderExtension(stream, logger, dir+"/"+file.getName());
						reader.unmarshal();
					}
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
			
			if(page.getNamespace().equals(BigInteger.valueOf(0)) && pageIds.keySet().size() <= MAX_PAGES){

				PageId id = new PageId(PageTitle.make(config, page.getTitle()), -1);

				preprocessRevisions(page, id, true);
				
				pageIds.put(page.getTitle(), id);
				
				languages.put(page.getTitle(), getLanguage(mediaWiki));
				
			} else if (page.getNamespace().equals(BigInteger.valueOf(2))){
				
				BarnstarReader barnstarReader = new BarnstarReader();
				
				String username = page.getTitle().split("\\:")[1];
				
				//editors.get(username).setBarnstarInformation(barnstarReader.parseUserPage(page));
			}
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
		private void preprocessRevisions(Page page, PageId pageId, boolean filter){
			
			boolean addPage;
			
			int originalSize = page.getRevisions().size();
			
			/*
			 * Add empty first revision.
			 */
			try {
				engProcessedPages.put(RevisionID.getNullRevision(page.getTitle()), engine.postprocess(pageId,"", null));
				
			} catch (EngineException e) {
				logger.error("EngProcessedPage could not be generated.", e);
				engProcessedPages.put(RevisionID.getNullRevision(page.getTitle()), null);
			}
			
			Contributor curr, next;
			
			int newIndex = 0;
			
			for(int currPosition = 0 ; currPosition < page.getRevisions().size() ; currPosition++){
				
				curr = page.getRevisions().get(currPosition).getContributor();
				next = null;
				
				Editor currEditor = null;
				
				/* TODO filter double anonymous editors?
				 */
				if(filter){
					
					if(currPosition < page.getRevisions().size() - 1){
	
						addPage = false;
						next = page.getRevisions().get(currPosition + 1).getContributor();
					}
					
					if(curr == null){
						
						//anonymous
						String ip = page.getRevisions().get(currPosition).getContributorIp();
						currEditor = new Editor(ip , BigInteger.ZERO);
						editors.put(ip, currEditor);
						addPage = true;
						
					} else if((next != null) && 
							(curr != null) && 
							(curr.getId() != null) &&
							(next.getId() != null) &&
							(curr.getId().equals(next.getId()))){
						
						//filtered
						page.getRevisions().remove(currPosition);
						currPosition--;
						addPage = false;
						
					} else {
						
						//registered
						if(editors.containsKey(curr.getUsername())){
							currEditor = editors.get(curr.getUsername());
						} else {
							currEditor = new Editor(curr.getUsername(), curr.getId());
							editors.put(curr.getUsername(), currEditor);
						}
						
						addPage = true;
					}
					
				} else {
					
					addPage = true;
				}
					
				if(addPage){
					
					RevisionID id = new RevisionID(page.getRevisions().get(currPosition), currEditor, newIndex, page.getTitle());

					try {
						engProcessedPages.put(id, engine.postprocess(pageId, page.getRevisions().get(currPosition).getText(), null));
					
					} catch (EngineException e) {
						logger.error("EngProcessedPage could not be generated.", e);
						engProcessedPages.put(id, null);
					}
					
					newIndex++;
				}
				
				if(MAX_REVISIONS+1 <= engProcessedPages.keySet().stream().filter(id -> id.getPageTitle().equals(page.getTitle())).count()){
					break;
				}
			}
			logger.info("Filtered "+(originalSize - page.getRevisions().size())+" revisions in \""+page.getTitle()+"\".");
		}
	}

}

