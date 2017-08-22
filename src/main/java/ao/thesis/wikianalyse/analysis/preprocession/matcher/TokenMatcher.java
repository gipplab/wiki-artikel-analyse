package ao.thesis.wikianalyse.analysis.preprocession.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.Match;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.PrefixTuple;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.utils.MatchComparator;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.ComplexToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

public class TokenMatcher {
//	
//	private static final Logger LOGGER = Logger.getLogger(TextMatcher.class);
//	
//	private final int maxComparisations;
//	private final int minMatchLength;
//	
//	private Comparator<Match> comparator = MatchComparator.getDefaultComparator();
//	
//	private List<Token> revision;
//	private List<List<StringToken>> prevs;
//	
//	/*
//	 * May contain Complex Token Objects
//	 */
//	private List<Map<PrefixTuple, List<Integer>>> prevsPrefixPositions;
//	
//	
//	public TokenMatcher(int maxComparisations, int minMatchLength) {
//		
//		validate(maxComparisations, 1);
//		validate(minMatchLength, 1);
//		
//		this.maxComparisations = maxComparisations;
//		this.minMatchLength = minMatchLength;
//		
//		this.prevs = new ArrayList<>(maxComparisations);
//		this.prevsPrefixPositions = new ArrayList<>(maxComparisations);
//	}
//	
//	/**
//	 * Validates the given value by checking if it is not null and bigger or equal the second argument.
//	 * 
//	 * @param value		integer to be validated
//	 * @param min		smallest possible value
//	 * @throws IllegalArgumentException	if value is null or smaller than min
//	 */
//	private void validate(int value, int min){
//		if(Objects.isNull(value) || value < min){
//			throw new IllegalArgumentException();
//		}
//	}
//	
//	private void init(PreprocessedRevision revision, List<PreprocessedRevision> prevs){
//		
//		this.prevs.clear();
//		this.prevsPrefixPositions.clear();
//		
//		setTargetRevision(revision.getComplexTokenizedText());
//		prevs.stream().forEachOrdered(rev -> setSourceRevision(rev.getTokenizedText(), rev.getTextPrefixPositions()));
//	}
//	
//	private void setSourceRevision(List<StringToken> list, Map prefixPositions){
//		if(list != null && prefixPositions != null){
//			prevs.add(list);
//			((List) this.prevsPrefixPositions).add(prefixPositions);
//		}
//	}
//	
//	private void setTargetRevision(List<Token> tokens){
//		if(tokens != null){
//			this.revision = tokens;
//		}
//	}
//	
//	public void matchAndUpdate(PreprocessedRevision revision, List<PreprocessedRevision> prevs){
//		if(prevs != null && revision != null && !prevs.isEmpty()){
//			
//			init(revision, prevs);
//			
//			if(prevsPrefixPositions != null){
//				
//				long timeStart = System.currentTimeMillis();
//				TokenSourceUpdater.update(findBestMatches(), this.revision, this.prevs);
//				LOGGER.info("Matching time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
//			}
//		}
//	}
//	
//	private List<Match> findBestMatches(){
//		List<Match> bestMatches = new LinkedList<>();
//		List<Match> allMatches = new LinkedList<>();
//
//		for(int sourceIndex = 0 ; sourceIndex < prevs.size() ; sourceIndex++){
//			List<Match> currMatches = findMatches(sourceIndex);
//			allMatches.addAll(currMatches);
//			if(sourceIndex == prevs.size() - maxComparisations - 1){
//				break;
//			}
//		}
//		if(!allMatches.isEmpty()){
//			Object[] objects = revision.toArray();
//			Token[] tokens = Arrays.copyOf(objects, objects.length, Token[].class);
//			PriorityQueue<Match> queue = new PriorityQueue<>(allMatches.size(), comparator);
//			queue.addAll(allMatches);
//	
//			filterBestMatches(queue, tokens, bestMatches);
//		}
//		return bestMatches;
//	}
//	
//	
//	private void filterBestMatches(PriorityQueue<Match> queue, Token[] targetElements, List<Match> bestMatches){
//
//		while (!queue.isEmpty()){
//			Match currentMatch = queue.peek();
//			
//			boolean matchRelevant = false;
//			boolean completeMatch = true;
//			
//			int subMatchLength = 0;
//					
//			for(int matchlength = 0 ; matchlength < currentMatch.getLength() ; matchlength++){	
//				
//				if(targetElements[currentMatch.getTargetPos() + matchlength] == null){
//					completeMatch = false;
//					if(matchRelevant){
//						Match subMatch = new Match(
//								subMatchLength,
//								currentMatch.getSourcePos() + matchlength - subMatchLength,
//								currentMatch.getTargetPos() + matchlength - subMatchLength,
//								currentMatch.getTotalSourceLength(),
//								currentMatch.getTotalTargetLength(),
//								currentMatch.getChunk());
//						queue.add(subMatch);
//						subMatchLength = 0;
//						matchRelevant = false;
//					}
//				}
//				if(!completeMatch && targetElements[currentMatch.getTargetPos() + matchlength] != null){
//					subMatchLength++;
//					matchRelevant = true;
//				}
//			}
//			if(matchRelevant){
//				Match subMatch = new Match(
//						subMatchLength,
//						currentMatch.getSourcePos() + (currentMatch.getLength() - 1) - subMatchLength,
//						currentMatch.getTargetPos() + (currentMatch.getLength() - 1) - subMatchLength,
//						currentMatch.getTotalSourceLength(),
//						currentMatch.getTotalTargetLength(),
//						currentMatch.getChunk());
//				queue.add(subMatch);
////				subMatchLength = 0;
////				matchRelevant = false;
//			}
//			if(completeMatch){
//				bestMatches.add(currentMatch);			
//				for(int matchlength = 0 ; matchlength < currentMatch.getLength() ; matchlength++){
//					targetElements[currentMatch.getTargetPos() + matchlength] = null;
//				}
//				
//				boolean everythingMatched = true;
//				
//				for(int i = 0 ; i < targetElements.length ; i++){	
//					if(targetElements[i] != null){
//						everythingMatched = false;
//						break;
//					}
//				} 
//				if (everythingMatched){
//					return;
//				}
//				
//			}
//			queue.poll();
//		}
//	}
//	
//
//	public List<Match> findMatches(int sourceIndex){
//
//		List<Token> target = revision;
//		List<StringToken> source = prevs.get(sourceIndex);
//		int targetSize = target.size();
//		int sourceSize = source.size();
//		Map<PrefixTuple, List<Integer>> sourcePrefixPositions = prevsPrefixPositions.get(sourceIndex);
//		
//		List<Match> matches = new LinkedList<>();
//		
//		/*
//		 * optimization header and trailer
//		 */
//		int matchStart = checkHeader(matches, target, source, targetSize, sourceSize, sourceIndex);
//		int matchEnd = checkTrailer(matches, target, source, targetSize, sourceSize, sourceIndex);
//
//		// check matches for all elements in target
//		for(int posT = matchStart; (posT + minMatchLength - 1) < matchEnd ; posT++){
//		
//			PrefixTuple currPrefix = buildPrefixTuple(target, posT, minMatchLength);
//			
//			int longestlength = matchPrefix(
//					currPrefix, 
//					matches, 
//					sourcePrefixPositions, 
//					posT, 
//					matchEnd, 
//					target, 
//					source, 
//					sourceSize, 
//					targetSize, 
//					sourceIndex);
//			
//			posT+=(longestlength-1);
//		}
//		return matches;
//	}
//	
//	private int matchPrefix(
//			PrefixTuple prefix, 
//			List<Match> matches, 
//			Map sourcePrefixPositions, 
//			int posT, 
//			int matchEnd, 
//			List<Token> target, 
//			List<StringToken> source, 
//			int sourceSize, 
//			int targetSize, 
//			int sourceIndex){
//		
//		Token token = null;
//		if(sourcePrefixPositions.containsKey(prefix) 
//				&& prefix.tokens.length == 1 
//				&& (token = prefix.tokens[0]) instanceof ComplexToken){
//			
//			for(int posS : (List<Integer>) sourcePrefixPositions.get(prefix)){
//				Match match = new Match(
//						((ComplexToken)token).getLength(),
//						posS,
//						posT,
//						sourceSize, 
//						targetSize, 
//						sourceIndex);
//				matches.add(match);
//			}
//			return 1;
//			
//		} else if(sourcePrefixPositions.containsKey(prefix)){
//			
//			List<Integer> positions = (List<Integer>) sourcePrefixPositions.get(prefix);
//		
//			int longestlength = prefix.tokens.length;
//			Match match = null;
//			
//			for(int posS : positions){
//				int length = prefix.tokens.length;
//				while(posS + length < sourceSize 
//						&& posT + length < matchEnd // matchEnd < targetSize
//						&& source.get(posS + length).equals(target.get(posT + length))){
//					length++;
//				}
//				if(length >= longestlength){
//					match = new Match(
//							length,
//							posS,
//							posT,
//							sourceSize, 
//							targetSize, 
//							sourceIndex);
//					longestlength = length;
//				}
//			}
//			if(match != null){
//				matches.add(match);
//			}
//			return longestlength;
//		} else {
//			
//			if(!sourcePrefixPositions.containsKey(prefix) 
//				&& prefix.tokens.length == 1 
//				&& token instanceof ComplexToken){
//			
//				List<Token> tokens = ((ComplexToken)token).getElements();
//			
//				int newPos = posT;
//				for(PrefixTuple innerPrefix : buildInnerTokenPrefixList(tokens, posT)){
//					matchPrefix(
//							innerPrefix,
//							matches, 
//							sourcePrefixPositions, 
//							newPos, 
//							matchEnd, 
//							target, 
//							source, 
//							sourceSize, 
//							targetSize, 
//							sourceIndex);
//					newPos++;
//				}
//			}
//			
//			return 1;
//		}
//	}
//	
//	private List<PrefixTuple> buildInnerTokenPrefixList(List<Token> tokens, int pos){
//
//		List<PrefixTuple> result = new LinkedList<>();
//		for(int index = 0; index < tokens.size() ; index++){
//			result.add(buildPrefixTuple(tokens, pos+index, 1));
//		}
//		return result;
//	}
//	
//	private PrefixTuple buildPrefixTuple(List<Token> target, int pos, int length){
//		Token[] prefix = new Token[length];
//		for(int i=0; i < length; i++){
//			
//			Token token;
//			if((token = target.get(pos+i)) instanceof ComplexToken && i == 0){
//				
//				// Set ComplexToken as PrefixTuple
//				prefix[i] = token;
//				return new PrefixTuple(Arrays.copyOf(prefix, 1));
//				
//			} else if(token instanceof ComplexToken && i != 0){
//				
//				//Set previous Tokens as PrefixTuple
//				return new PrefixTuple(Arrays.copyOf(prefix, i));
//			} else {
//				
//				prefix[i] = token;
//			}
//		}
//		return new PrefixTuple(prefix);
//	}
//	
//	
//	/*
//	 * optimization
//	 */
//	private int checkHeader(List<Match> matches, List<Token> target, List<StringToken> source, int targetSize, int sourceSize, int sourceIndex){
//		int matchStart = 0;
//
//		int counter = 0;
//		while(counter < sourceSize 
//				&& counter < targetSize 
//				&& target.get(counter).equals(source.get(counter))){
//			matchStart++;
//			counter++;
//		}
//		if(counter >= minMatchLength){
//			Match header = new Match(counter, 0, 0, sourceSize, targetSize, sourceIndex);
//			matches.add(header);
//		}
//		return matchStart;
//	}
//	
//	
//	/*
//	 * optimization
//	 */
//	private int checkTrailer(List<Match> matches, List<Token> target, List<StringToken> source, int targetSize, int sourceSize, int sourceIndex){
//		int matchEnd = targetSize;
//		int counter = 0;
//		while(targetSize-counter > 1 
//				&& sourceSize-counter > 1 
//				&& target.get(targetSize-1-counter).equals(source.get(sourceSize-1-counter))){
//			counter++;
//			matchEnd--;
//		}
//		if(counter >= minMatchLength){
//			Match trailer = new Match(counter, sourceSize-counter, targetSize-counter, sourceSize, targetSize, sourceIndex);
//			matches.add(trailer);
//		}
//		return matchEnd;
//	}
//	

}
