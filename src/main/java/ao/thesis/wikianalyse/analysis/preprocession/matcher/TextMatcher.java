package ao.thesis.wikianalyse.analysis.preprocession.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.analysis.datatypes.MarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTMarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WhitespaceRevision;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.Match;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.utils.MatchComparator;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

/**
 * TextMatcher
 * 
 * This class matches tokens using algorithms from WikiTrust.
 * 
 * @author Anna Opaska
 *
 */
public class TextMatcher {		
	
	private static final Logger LOGGER = Logger.getLogger(TextMatcher.class);
	
	private final int maxComparisations;
	private final int minMatchLength;
	
	private Comparator<Match> comparator = MatchComparator.getDefaultComparator();
	
	private List<Token> revision;
	private List<List<Token>> prevs;
	private List<Map<PrefixTuple, List<Integer>>> prevsPrefixPositions;
	
	
	public TextMatcher(int maxComparisations, int minMatchLength) {
		
		validate(maxComparisations, 1);
		validate(minMatchLength, 1);
		
		this.maxComparisations = maxComparisations;
		this.minMatchLength = minMatchLength;
		
		this.prevs = new ArrayList<>(maxComparisations);
		this.prevsPrefixPositions = new ArrayList<>(maxComparisations);
	}
	
	/**
	 * Validates the given value by checking if it is not null and bigger or equal the second argument.
	 * 
	 * @param value		integer to be validated
	 * @param min		smallest possible value
	 * @throws IllegalArgumentException	if value is null or smaller than min
	 */
	private void validate(int value, int min){
		if(Objects.isNull(value) || value < min){
			throw new IllegalArgumentException();
		}
	}
	
	private void init(WhitespaceRevision revision, List<WhitespaceRevision> prevs){
		this.prevs.clear();
		this.prevsPrefixPositions.clear();
		setTargetRevision((List)revision.getWhiteSpaceSegmentedTokens());
		prevs.stream().forEachOrdered(rev -> setSourceRevision((List)rev.getWhiteSpaceSegmentedTokens(), rev.getWhiteSpaceSegmentedPrefixPositions()));
	}
	
	private void init(MarkupSegmentedRevision revision, List<MarkupSegmentedRevision> prevs){
		this.prevs.clear();
		this.prevsPrefixPositions.clear();
		setTargetRevision((List)revision.getMarkupSegmentedTokens());
		prevs.stream().forEachOrdered(rev -> setSourceRevision((List)rev.getMarkupSegmentedTokens(), rev.getMarkupSegmentedPrefixPositions()));
	}
	
	private void init(NERevision revision, List<NERevision> prevs){
		this.prevs.clear();
		this.prevsPrefixPositions.clear();
		setTargetRevision((List)revision.getNETokens());
		prevs.stream().forEachOrdered(rev -> setSourceRevision((List)rev.getNETokens(), rev.getPrefixPositions()));
	}
	
	
	private void setSourceRevision(List<Token> tokens, Map prefixPositions){
		if(tokens != null && prefixPositions != null){
			prevs.add(tokens);
			((List) this.prevsPrefixPositions).add(prefixPositions);
		}
	}
	
	private void setTargetRevision(List<Token> tokens){
		if(tokens != null){
			this.revision = tokens;
		}
	}
	
	public void matchAndUpdate(NERevision revision, List<NERevision> prevs) {
		
		init(revision, (List)prevs);
		
		if(prevsPrefixPositions != null){
			long timeStart = System.currentTimeMillis();
			TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
			LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
		}
		
	}
	
	public void matchAndUpdate(PreprocessedRevision revision, List<PreprocessedRevision> prevs){
		if(prevs != null && revision != null && !prevs.isEmpty()){
			
			if(revision instanceof WTMarkupSegmentedRevision){
				
				init(((WTMarkupSegmentedRevision) revision).getWhitespaceRevision(), prevs.stream()
											.map(r -> ((WTMarkupSegmentedRevision) r).getWhitespaceRevision())
											.collect(Collectors.toList()));
				if(prevsPrefixPositions != null){
					long timeStart = System.currentTimeMillis();
					TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
					((WTMarkupSegmentedRevision) revision).getWhitespaceRevision().setWhiteSpaceSegmentedTokens((List)this.revision);
					LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
				}
				
				init(((MarkupSegmentedRevision) revision), (List)prevs);
				if(prevsPrefixPositions != null){
					long timeStart = System.currentTimeMillis();
					TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
					((WTMarkupSegmentedRevision) revision).setMarkupSegmentedTokens((List)this.revision);
					LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
				}
				
			} else if(revision instanceof WTRevision){
				init(((WTRevision) revision).getWhitespaceRevision(), (List)prevs);
				
				if(prevsPrefixPositions != null){
					long timeStart = System.currentTimeMillis();
					TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
					LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
				}
			} else if(revision instanceof WhitespaceRevision){
				init((WhitespaceRevision)revision, (List)prevs);
				
				if(prevsPrefixPositions != null){
					long timeStart = System.currentTimeMillis();
					TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
					LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
				}
				
			} else if(revision instanceof MarkupSegmentedRevision){
				init((MarkupSegmentedRevision)revision, (List)prevs);
				
				if(prevsPrefixPositions != null){
					long timeStart = System.currentTimeMillis();
					TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
					LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
				}
				
			}
		}
	}
	
	private List<Match> findBestMatches(){
		List<Match> bestMatches = new LinkedList<>();
		List<Match> allMatches = new LinkedList<>();

		for(int sourceIndex = 0 ; sourceIndex < prevs.size() ; sourceIndex++){
			List<Match> currMatches = findMatches(sourceIndex);
			allMatches.addAll(currMatches);
			if(sourceIndex == prevs.size() - maxComparisations - 1){
				break;
			}
		}
		if(!allMatches.isEmpty()){
			Object[] objects = revision.toArray();
			Token[] tokens = Arrays.copyOf(objects, objects.length, Token[].class);
			PriorityQueue<Match> queue = new PriorityQueue<>(allMatches.size(), comparator);
			queue.addAll(allMatches);
	
			filterBestMatches(queue, tokens, bestMatches);
		}
		return bestMatches;
	}
	
	
	private void filterBestMatches(PriorityQueue<Match> queue, Token[] targetElements, List<Match> bestMatches){

		while (!queue.isEmpty()){
			Match currentMatch = queue.peek();
			
			boolean matchRelevant = false;
			boolean completeMatch = true;
			
			int subMatchLength = 0;
					
			for(int matchlength = 0 ; matchlength < currentMatch.getLength() ; matchlength++){	
				
				if(targetElements[currentMatch.getTargetPos() + matchlength] != null){
					subMatchLength++;
					matchRelevant = true;
				}
				if(targetElements[currentMatch.getTargetPos() + matchlength] == null){					
					completeMatch = false;
					if(matchRelevant){
						Match subMatch = new Match(
								subMatchLength,
								currentMatch.getSourcePos() + (matchlength + 1) - subMatchLength,
								currentMatch.getTargetPos() + (matchlength + 1) - subMatchLength,
								currentMatch.getTotalSourceLength(),
								currentMatch.getTotalTargetLength(),
								currentMatch.getChunk());
						queue.add(subMatch);
						subMatchLength = 0;
						matchRelevant = false;
					}
				}
			}
			if(completeMatch){
				bestMatches.add(currentMatch);			
				for(int matchlength = 0 ; matchlength < currentMatch.getLength() ; matchlength++){
					targetElements[currentMatch.getTargetPos() + matchlength] = null;
				}
				
				boolean everythingMatched = true;
				for(int i = 0 ; i < targetElements.length ; i++){	
					if(targetElements[i] != null){
						everythingMatched = false;
						break;
					}
				} 
				if (everythingMatched){
					return;
				}
				
			} else if(matchRelevant){
				Match subMatch = new Match(
						subMatchLength,
						currentMatch.getSourcePos() + currentMatch.getLength() - subMatchLength,
						currentMatch.getTargetPos() + currentMatch.getLength() - subMatchLength,
						currentMatch.getTotalSourceLength(),
						currentMatch.getTotalTargetLength(),
						currentMatch.getChunk());
				queue.add(subMatch);
			}
			queue.poll();
		}
	}
	

	public List<Match> findMatches(int sourceIndex){

		List<Token> target = revision;
		List<Token> source = prevs.get(sourceIndex);
		int targetSize = target.size();
		int sourceSize = source.size();
		Map<PrefixTuple, List<Integer>> sourcePrefixPositions = prevsPrefixPositions.get(sourceIndex);
		
		List<Match> matches = new LinkedList<>();
		
		/*
		 * optimization header and trailer
		 */
		int matchStart = checkHeader(matches, target, source, targetSize, sourceSize, sourceIndex);
		int matchEnd = checkTrailer(matches, target, source, targetSize, sourceSize, sourceIndex);

		// check matches for all elements in target
		for(int posT = matchStart; (posT + minMatchLength - 1) < matchEnd ; posT++){
		
			int longestlength = 1;
			
			PrefixTuple currPrefix = buildPrefixTuple(target, posT, minMatchLength);
			if(sourcePrefixPositions.containsKey(currPrefix)){
				
				/*
				 * optimization max matches
				 */
				if(sourcePrefixPositions.get(currPrefix).size() > 50){
					currPrefix.setNoMatching(); // sets source id to -1 which resembles stop word handling
					continue;
				}
				
				// check matches for all prefix positions in source
				for(int posS : sourcePrefixPositions.get(currPrefix)){
					
					int length = minMatchLength;
					while(posS + length < sourceSize 
							&& posT + length < matchEnd // matchEnd < targetSize
							&& source.get(posS + length).equals(target.get(posT + length))){
						length++;
					}
					/*
					 * optimization longest match
					 */
//					while(length >= minMatchLength){
						Match match = new Match(
								length,
								posS,
								posT,
								sourceSize, 
								targetSize, 
								sourceIndex);
						matches.add(match);
						
						if(length > longestlength){
							longestlength = length;
						}
//					length--;
//					}
				}
			}
			posT+=(longestlength-1);
		}
		return matches;
	}
	
	
	private PrefixTuple buildPrefixTuple(List<Token> target, int pos, int length){
		Token[] prefix = new Token[length];
		for(int i=0; i < length; i++){
			prefix[i] = target.get(pos+i);
		}
		return new PrefixTuple(prefix);
	}
	
	
	/*
	 * optimization
	 */
	private int checkHeader(List<Match> matches, List<Token> target, List<Token> source, int targetSize, int sourceSize, int sourceIndex){
		int matchStart = 0;

		int counter = 0;
		while(counter < sourceSize 
				&& counter < targetSize 
				&& target.get(counter).equals(source.get(counter))){
			matchStart++;
			counter++;
		}
		if(counter >= minMatchLength){
			Match header = new Match(counter, 0, 0, sourceSize, targetSize, sourceIndex);
			matches.add(header);
		}
		return matchStart;
	}
	
	
	/*
	 * optimization
	 */
	private int checkTrailer(List<Match> matches, List<Token> target, List<Token> source, int targetSize, int sourceSize, int sourceIndex){
		int matchEnd = targetSize;
		int counter = 0;
		while(targetSize-counter > 1 
				&& sourceSize-counter > 1 
				&& target.get(targetSize-1-counter).equals(source.get(sourceSize-1-counter))){
			counter++;
			matchEnd--;
		}
		if(counter >= minMatchLength){
			Match trailer = new Match(counter, sourceSize-counter, targetSize-counter, sourceSize, targetSize, sourceIndex);
			matches.add(trailer);
		}
		return matchEnd;
	}
	
}
