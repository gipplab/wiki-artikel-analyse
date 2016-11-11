package ao.thesis.wikianalyse.diffs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import de.fau.cs.osr.hddiff.utils.ReportItem;
import de.fau.cs.osr.hddiff.utils.WordSubstringJudge;
import de.fau.cs.osr.hddiff.wom.WomNodeEligibilityTester;
import de.fau.cs.osr.hddiff.wom.WomNodeMetrics;
import de.fau.cs.osr.hddiff.wom.WomToDiffNodeConverter;

public class HDDiffEditScriptBuilder {
	
	private PageId pageId;
	
	public HDDiffEditScriptBuilder(PageId pageId){
		this.pageId=pageId;
	}

	public HashMap<Integer, List<List<EditOp>>> buildAllEditScripts(
			List<EngProcessedPage> revisions) throws EngineException {
		
		HashMap<Integer, List<List<EditOp>>> allEditScripts = 
				new HashMap<Integer, List<List<EditOp>>>(revisions.size());
		
		EngProcessedPage target;
		
		/* first revision is empty
		 */
		for(int targetIndex=1; targetIndex<revisions.size(); targetIndex++){
			target = revisions.get(targetIndex);
			List<EngProcessedPage> sources = revisions.subList(0, targetIndex);
			List<List<EditOp>> editScripts = buildEditScripts(target, sources);
			
			allEditScripts.put(targetIndex-1, editScripts);
		}
		
		return allEditScripts;
	}	
	
	public List<List<EditOp>> buildEditScripts(EngProcessedPage target, 
			List<EngProcessedPage> sources) {
		
		List<List<EditOp>> editScripts = 
				new ArrayList<List<EditOp>>(sources.size());
		
		for(int sourceIndex = 0 ; sourceIndex < sources.size() ; sourceIndex++){
			EngProcessedPage source = sources.get(sourceIndex);
			editScripts.add(buildEditScript(source, target));
		}
		return editScripts;
	}
	
	public List<EditOp> buildEditScript(EngProcessedPage source, EngProcessedPage target){
		
		WtWom3Toolbox wtWom3Toolbox = new WtWom3Toolbox();
		
		Wom3Document womTarget = wtWom3Toolbox.astToWom(pageId, target);
		Wom3Document womSource = wtWom3Toolbox.astToWom(pageId, source);
		
		Wom3Node rootTarget = (Wom3Node) womTarget.getDocumentElement();
		Wom3Node rootSource = (Wom3Node) womSource.getDocumentElement();
		
		DiffNode diffNodeTarget = WomToDiffNodeConverter.preprocess(rootTarget);
		DiffNode diffNodeSource = WomToDiffNodeConverter.preprocess(rootSource);
		
		HDDiff hddiff = new HDDiff(diffNodeSource, diffNodeTarget, setupWikiDiff(), 
					new ReportItem());

		return hddiff.editScript();
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
