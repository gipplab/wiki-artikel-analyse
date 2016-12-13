package ao.thesis.wikianalyse.utils.editanalyse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wom3.Wom3Document;
import org.sweble.wom3.Wom3Node;
import org.sweble.wom3.swcadapter.utils.WtWom3Toolbox;

import de.fau.cs.osr.hddiff.HDDiff;
import de.fau.cs.osr.hddiff.HDDiffOptions;
import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.tree.DiffNode;
import de.fau.cs.osr.hddiff.utils.Report;
import de.fau.cs.osr.hddiff.utils.ReportItem;
import de.fau.cs.osr.hddiff.utils.WordSubstringJudge;
import de.fau.cs.osr.hddiff.wom.WomNodeEligibilityTester;
import de.fau.cs.osr.hddiff.wom.WomNodeMetrics;
import de.fau.cs.osr.hddiff.wom.WomToDiffNodeConverter;
import de.fau.cs.osr.utils.visitor.VisitingException;

/**
 * Builds edit scripts using sweble hddiff.
 * 
 * @author anna
 *
 */
public class HDDiffEditScriptBuilder {
	
	private PageId pageId;
	
	private String outputdir;
	
	private Logger logger;
	
	private Report report = new Report();
	
	
	public HDDiffEditScriptBuilder(PageId pageId, Logger logger){
		
		this.pageId=pageId;
		
		this.logger=logger;
	}

	/** Builds all edit scripts for a list of EngProcessedPage objects.
	 * 
	 * Because an edit script for the first revision is needed, the given list has to contain a
	 * first empty EngProcessedPage object.
	 * 
	 * @param revisions				- sorted list of EngProcessedPage objects.
	 * @return
	 * @throws EngineException
	 */
	public List<List<List<EditOp>>> buildAllEditScripts(List<EngProcessedPage> revisions) throws EngineException {
		
		List<List<List<EditOp>>> allEditScripts = new ArrayList<List<List<EditOp>>>(revisions.size());
		
		for(int targetIndex = 1 ; targetIndex < revisions.size() ; targetIndex++){
			
			EngProcessedPage target = revisions.get(targetIndex);
			List<EngProcessedPage> sources = revisions.subList(0, targetIndex);
			
			allEditScripts.add(buildEditScripts(target, sources, targetIndex));
		}
		
		return allEditScripts;
	}
	
	
	private List<List<EditOp>> buildEditScripts(EngProcessedPage target, List<EngProcessedPage> sources, int targetIndex) {
		
		List<List<EditOp>> editScripts = new ArrayList<List<EditOp>>(sources.size());
		
		for(int sourceIndex = 0 ; sourceIndex < sources.size() ; sourceIndex++){
			
			ReportItem reportItem = new ReportItem();
			HDDiff hddiff;
			
			if((hddiff = buildEditScript(sources.get(sourceIndex), target, reportItem)) != null){
				editScripts.add(hddiff.editScript());
				report.add(reportItem);
			} else {
				editScripts.add(null);
			}
		}
		//writeEditScriptInfo(targetIndex);
		
		return editScripts;
	}
	
	
	private HDDiff buildEditScript(EngProcessedPage source, EngProcessedPage target, ReportItem reportItem){
		
		WtWom3Toolbox wtWom3Toolbox = new WtWom3Toolbox();
		
		Wom3Document womTarget;
		Wom3Document womSource;
		
		try{
			
			womTarget = wtWom3Toolbox.astToWom(pageId, target);
			womSource = wtWom3Toolbox.astToWom(pageId, source);
			
		} catch (VisitingException | IllegalArgumentException e){
			/*
			 * Gets thrown when pageId has an invalid title that contains /
			 * TODO check earlier
			 */
			logger.error(e.getMessage());
			return null;
		}
		
		Wom3Node rootTarget = (Wom3Node) womTarget.getDocumentElement();
		Wom3Node rootSource = (Wom3Node) womSource.getDocumentElement();
		
		DiffNode diffNodeTarget = WomToDiffNodeConverter.preprocess(rootTarget);
		DiffNode diffNodeSource = WomToDiffNodeConverter.preprocess(rootSource);
		
		return new HDDiff(diffNodeSource, diffNodeTarget, setupWikiDiff(), reportItem);
	}
	
	
	private void writeEditScriptInfo(int targetIndex){
		//TODO handle output in rating system class
		
		try {
			
			logger.info("Write Edit Script Report.");
			
			report.writeCsv(new File(outputdir+"/EditScriptReport_"+targetIndex+".csv"), Locale.ENGLISH, "UTF-8");
			
		} catch (IOException e) {
			
			logger.error("Edit Script Report could not be written.");
		}
	}
	
	
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
	
	/** SOURCE: de.fau.cs.osr.hddiff.perfsuite.TaskProcessor
	 */
	private HDDiffOptions setupWikiDiff(){
		
		HDDiffOptions options = new HDDiffOptions();
		
		options.setNodeMetrics(new WomNodeMetrics());
		
		options.setMinSubtreeWeight(12);
		
		options.setEnableTnsm(true);
		
		options.setTnsmEligibilityTester(new WomNodeEligibilityTester());
		
		options.setTnsmSubstringJudge(new WordSubstringJudge(8, 3));
		
		return options;
	}
}
