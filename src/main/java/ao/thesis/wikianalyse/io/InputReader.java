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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sweble.wikitext.dumpreader.DumpReader;
import org.sweble.wikitext.dumpreader.export_0_10.PageType;
import org.sweble.wikitext.dumpreader.export_0_10.RevisionType;

public class InputReader {
	
	private static InputReader inputReader = new InputReader();
	
	private static Logger logger = Logger.getLogger(InputReader.class);
	
	private List<Object> pages = new ArrayList<Object>();
	
	public static InputReader getInputReader(){
		return inputReader;
		}

	private InputReader(){}
	
	
	public List<Object> read(String inputDir) 
			throws Exception
	{
		File[] inputFiles = new File(inputDir).listFiles();
		DumpReader reader = null;
		FileInputStream stream = null;

		if(inputFiles.length!=0){
			try 
			{
				for(File file : inputFiles){
					logger.info("Read file: "+file.getName()+".");
					
					stream = new FileInputStream(file);
					reader = new DumpReaderExtension(stream, logger, inputDir);
					reader.unmarshal();
				}
			}
			finally 
			{
				try 
				{
					if (reader != null)
						reader.close();
				}
				finally 
				{
					if (stream != null)
						stream.close();
				}
			}
		}
		return pages;
	}

	private class DumpReaderExtension extends DumpReader
	{
		private Logger logger;
		
		private DumpReaderExtension(InputStream stream, 
				Logger logger, String input_dir) throws Exception
		{
			super(stream, StandardCharsets.UTF_8, input_dir, logger, true);
			this.logger=logger;
		}
		
		/** 
		 * WikiTrust ignores revisions that are followed by a revision by the same author
		 */
		private void filterRevisions(PageType p)
		{
			RevisionType currentRevision;
			RevisionType nextRevision;
			
			int size= p.getRevisionOrUpload().size();
			
			for(int currentPosition=0; currentPosition<p.getRevisionOrUpload().size()-1; currentPosition++)
			{
				currentRevision = (RevisionType) p.getRevisionOrUpload().get(currentPosition);
				nextRevision = (RevisionType) p.getRevisionOrUpload().get(currentPosition+1);
				
				if(currentRevision.getContributor().getId()!=null 
					&& nextRevision.getContributor().getId()!=null
					&& currentRevision.getContributor().getId().equals(nextRevision.getContributor().getId()))
				{
					p.getRevisionOrUpload().remove(currentPosition);
					currentPosition--;
				}
			}
			logger.info("Filtered "+(size-p.getRevisionOrUpload().size())+" revisions.");
		}
	
		@Override
		protected void processPage(Object mediaWiki, Object page) throws Exception 
		{
			filterRevisions((PageType) page);
			pages.add(page);
		}
		
		@Override
		protected boolean processRevision(Object mediaWiki, Object revision) throws Exception 
		{
			return true;
		}
	}
}

