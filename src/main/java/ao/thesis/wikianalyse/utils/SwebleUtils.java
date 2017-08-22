package ao.thesis.wikianalyse.utils;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public class SwebleUtils {
	
	private static WikiConfig config = DefaultConfigEnWp.generate();
	private static WtEngineImpl engine = new WtEngineImpl(config);
	
	private SwebleUtils(){}
	
	public static PageTitle buildPageTitle(String title) throws SwebleException {
		try {
			return PageTitle.make(config, title.replace("/","_")); // throws VisitingException else 
		} catch (LinkTargetException e) {
			throw new SwebleException(e);
		}
	}
	
	public static PageId buildPageId(PageTitle title, int revision){
		return new PageId(title, revision);
	}
	
	public static EngProcessedPage buildEngProcessedPage(PageId id, String text) throws SwebleException {
		try {
			return engine.postprocess(id, text, null);
		} catch (EngineException e) {
			throw new SwebleException(e);
		}
	}

	public static EngProcessedPage buildEmptyEngProcessedPage(PageTitle title) throws SwebleException {
		PageId id = buildPageId(title, -1);
		try {
			return engine.postprocess(id, "", null);
		} catch (EngineException e) {
			throw new SwebleException(e);
		}
	}
	
//	public static EngProcessedPage buildEngProcessedPage(PageTitle title, int revision, String text) throws SwebleException {
//		try {
//			return engine.postprocess(buildPageId(title, revision), text, null);
//		} catch (EngineException e) {
//			throw new SwebleException();
//		}
//	}

}
