package ao.thesis.wikianalyse.utils;

import java.util.List;

import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wom3.Wom3Document;
import org.sweble.wom3.Wom3Node;
import org.sweble.wom3.swcadapter.utils.WtWom3Toolbox;

import ao.thesis.wikianalyse.analysis.datatypes.SwebleRevision;
import de.fau.cs.osr.hddiff.HDDiff;
import de.fau.cs.osr.hddiff.HDDiffOptions;
import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.tree.DiffNode;
import de.fau.cs.osr.hddiff.utils.ReportItem;
import de.fau.cs.osr.hddiff.utils.WordSubstringJudge;
import de.fau.cs.osr.hddiff.wom.WomNodeEligibilityTester;
import de.fau.cs.osr.hddiff.wom.WomNodeMetrics;
import de.fau.cs.osr.hddiff.wom.WomToDiffNodeConverter;
import de.fau.cs.osr.utils.visitor.VisitingException;

public class HDDiffUtils {
	
	private HDDiffUtils(){}
	
	public static List<EditOp> buildEditScript(SwebleRevision prev, SwebleRevision revision, PageTitle pageTitle) 
			throws VisitingException, SwebleException {
		if(prev != null){
			return buildEditScript(prev.getPageId(), revision.getPageId(), prev.getEngProcessedPage(), revision.getEngProcessedPage());
		} 
		return buildEditScript(SwebleUtils.buildPageId(pageTitle, -1), revision.getPageId(), SwebleUtils.buildEmptyEngProcessedPage(pageTitle), revision.getEngProcessedPage());
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
	
	/**
	 * 
	 * @param sId
	 * @param tId
	 * @param source
	 * @param target
	 * @return	
	 * @throws 			VisitingException thrown if pageId has an invalid title that contains "/" and if there is a method missing. 
	 * 					Issue: https://github.com/OpenCompare/OpenCompare/issues/86
	 */
	public static List<EditOp> buildEditScript(PageId sId, PageId tId, EngProcessedPage source, EngProcessedPage target) 
			throws VisitingException // thrown if pageId has an invalid title that contains "/" 
	{ 
		WtWom3Toolbox wtWom3Toolbox = new WtWom3Toolbox();
		Wom3Document womTarget = wtWom3Toolbox.astToWom(tId, target);
		Wom3Document womSource = wtWom3Toolbox.astToWom(sId, source);
		Wom3Node rootTarget = womTarget.getDocumentElement();
		Wom3Node rootSource = womSource.getDocumentElement();
		DiffNode diffNodeTarget = WomToDiffNodeConverter.preprocess(rootTarget);
		DiffNode diffNodeSource = WomToDiffNodeConverter.preprocess(rootSource);
		HDDiff hddiff = new HDDiff(diffNodeSource, diffNodeTarget, setupWikiDiff(), new ReportItem());
		return hddiff.editScript();
	}
	
	private static HDDiffOptions setupWikiDiff(){
		HDDiffOptions options = new HDDiffOptions();
		options.setNodeMetrics(new WomNodeMetrics());
		options.setMinSubtreeWeight(12);
		options.setEnableTnsm(true);
		options.setTnsmEligibilityTester(new WomNodeEligibilityTester());
		options.setTnsmSubstringJudge(new WordSubstringJudge(8, 3));
		return options;
	}

}
