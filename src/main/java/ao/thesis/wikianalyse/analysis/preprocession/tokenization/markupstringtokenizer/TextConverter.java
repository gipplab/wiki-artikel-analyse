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

package ao.thesis.wikianalyse.analysis.preprocession.tokenization.markupstringtokenizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.parser.nodes.WtBold;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtHorizontalRule;
import org.sweble.wikitext.parser.nodes.WtIllegalCodePoint;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtListItem;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtOrderedList;
import org.sweble.wikitext.parser.nodes.WtPageSwitch;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtTable;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTagExtensionBody.WtTagExtensionBodyImpl;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.utils.PrefixHashMapBuilder;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
import org.sweble.wikitext.parser.nodes.WtXmlAttribute;
import org.sweble.wikitext.parser.nodes.WtXmlCharRef;
import org.sweble.wikitext.parser.nodes.WtXmlComment;
import org.sweble.wikitext.parser.nodes.WtXmlElement;
import org.sweble.wikitext.parser.nodes.WtXmlEntityRef;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;
import joptsimple.internal.Strings;

/**
 * A visitor to convert an article AST into a pure text representation. To
 * better understand the visitor pattern as implemented by the Visitor class,
 * please take a look at the following resources:
 * <ul>
 * <li><a
 * href="http://en.wikipedia.org/wiki/Visitor_pattern">http://en.wikipedia
 * .org/wiki/Visitor_pattern</a> (classic pattern)</li>
 * <li><a
 * href="http://www.javaworld.com/javaworld/javatips/jw-javatip98.html">http
 * ://www.javaworld.com/javaworld/javatips/jw-javatip98.html</a> (the version we
 * use here)</li>
 * </ul>
 * 
 * The methods needed to descend into an AST and visit the children of a given
 * node <code>n</code> are
 * <ul>
 * <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node
 * <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code>
 * and gather the return values of the <code>visit()</code> calls in a list,</li>
 * <li><code>mapInPlace(n)</code> - visit the <b>children</b> of node
 * <code>n</code> and replace each child node <code>c</code> with the return
 * value of the call to <code>visit(c)</code>.</li>
 * </ul>
 */
public class TextConverter
		extends
			AstVisitor<WtNode>
{
	/*
	 * Pattern
	 */
	
	private static String whitespace =  "\\s+|\\p{Z}+";
	
	private static String whitespacePunct =  "\\s+|\\p{Z}+|(?=(?U)\\p{Punct})|(?<=(?U)\\p{Punct})|(?=\\p{Punct})|(?<=\\p{Punct})";
	
	private Pattern pattern;
	
	private static Pattern patternWSP = Pattern.compile(whitespacePunct);
	
	private static Pattern patternWS = Pattern.compile(whitespace);

	
	public StringBuilder sb;
	
	public StringBuilder line;
	
	/**
	 * Becomes true if we are no long at the Beginning Of the whole Document.
	 */
	private boolean pastBod;
	
	private int needNewlines;
	
	private boolean needSpace;
	
	private boolean noWrap;
	
	// =========================================================================
	
	private List<Token> tokens = new ArrayList<>();
	
	public List<MathFormulaToken> mathtokens = new ArrayList<>();
	
	int position = 0;
	
	TextConverter(){}
	
	@Override
	protected WtNode before(WtNode node)
	{
		// This method is called by go() before visitation starts
		sb = new StringBuilder();
		line = new StringBuilder();
		pastBod = false;
		needNewlines = 0;
		needSpace = false;
		noWrap = false;
		
		pattern = patternWSP;
		
		return super.before(node);
	}
	
	@Override
	protected Object after(WtNode node, Object result)
	{
		finishLine();
		// This method is called by go() after visitation has finished
		// The return value will be passed to go() which passes it to the caller
		return tokens;
	}
	
	// =========================================================================
	
	public void visit(WtNode n)
	{
		iterate(n);
	}
	
	public void visit(WtXmlAttribute e)
	{
		/*
		 * Not needed as significant text information and severely slows down the matching process.
		 */
	}
	
	public void visit(WtTable n)
	{
		iterate(n);
	}
	
	public void visit(WtNodeList n)
	{
		iterate(n);
	}
	
	public void visit(WtUnorderedList e)
	{
		iterate(e);
	}
	
	public void visit(WtOrderedList e)
	{
		iterate(e);
	}
	
	public void visit(WtListItem item)
	{
		iterate(item);
	}
	
	public void visit(EngPage p)
	{
		iterate(p);
	}
	
	public void visit(WtText text)
	{
		write(text.getContent());
	}
	
	public void visit(WtWhitespace w)
	{
		write(" ");
	}
	
	public void visit(WtBold b)
	{
		iterate(b);
	}
	
	public void visit(WtItalics i)
	{
		iterate(i);
	}
	
	public void visit(WtXmlCharRef cr)
	{
		write(Character.toChars(cr.getCodePoint()));
	}
	
	public void visit(WtXmlEntityRef er)
	{
		Pattern save = pattern;
		pattern = patternWS;
		String ch = er.getResolved();
		if (ch == null)
		{
			write('&');
			write(er.getName());
			write(';');
		}
		else
		{
			write(ch);
		}
		pattern = save;
	}
	
	public void visit(WtUrl wtUrl)
	{
		Pattern save = pattern;
		pattern = patternWS;
		if (!wtUrl.getProtocol().isEmpty())
		{
			write(wtUrl.getProtocol());
			write(':');
		}
		write(wtUrl.getPath());
		pattern = save;
	}
	
	/*
	 * Examples from Help:Wiki_markup
	 * 
	 * [http://www.wikipedia.org Wikipedia] -> Wikipedia
	 * 
	 * [http://www.wikipedia.org] 			-> [1]
	 * 
	 * http://www.wikipedia.org				-> http://www.wikipedia.org
	 * 
	 */
	public void visit(WtExternalLink link)
	{
		Pattern save = pattern;
		pattern = patternWS;
		iterate(link);
		pattern = save;
	}
	
	public void visit(WtImageLink link)
	{
		/*
		 * Ignore image link
		 */
	}

	/*
	 * Not included: automatically renamed links, blend links, language codes
	 */
	public void visit(WtInternalLink link)
	{
		finishLine();
		
		Pattern save = pattern;
		pattern = patternWS;

		List saveElements = tokens;
		StringBuilder saveSB = sb;
		
		tokens = new LinkedList<>();
		
		sb = new StringBuilder();
		iterate(link.getTarget());
		finishLine();
		String target = sb.toString();
		
		if(target.toLowerCase().startsWith("category") || target.toLowerCase().startsWith("categoria")){
			/*
			 * Category is not displayed
			 */
			return;
		}
		
		if(target.toLowerCase().startsWith("file") || target.toLowerCase().startsWith("immagine")){
			/*
			 * Displayed image is ignored
			 */
			return;
		}
		
		String displayed = null;
		if (link.hasTitle()){
			/*
			 * Renamed link
			 */
			sb = new StringBuilder();
			iterate(link.getTitle());
			finishLine();
			displayed = sb.toString();
		}
		
		String[] referenceSegments = target.toLowerCase().split(":");

		if((Objects.nonNull(referenceSegments) && referenceSegments.length > 1)){
			
			/*
			 * [[MediaWiki:Example]]			-> MediaWiki:Example
			 * [[:Category:Character sets|]]	-> Character sets
			 */
			String ref;
			
			if(Strings.isNullOrEmpty(referenceSegments[0]) && referenceSegments.length > 2){
				
				/*
				 * [[:Category:Character sets|]]	-> Character sets
				 */
				
				ref = referenceSegments[1];
				
				/*
				 * Suffix "|" displays no name space
				 */
				if(target.endsWith("|")){
					displayed = referenceSegments[2];
				} else {
					displayed = ref+":"+referenceSegments[2];
				}
				
			} else {
				ref = referenceSegments[0];
				
				/*
				 * Suffix "|" displays no name space
				 */
				if(target.endsWith("|")){
					displayed = referenceSegments[1];
				} else {
					displayed = ref+":"+referenceSegments[1];
				}
			}
			
		} else {
			/*
			 * No renamed link
			 */
			if(displayed == null){
				displayed = target;
			}
		}
		
		tokens = saveElements;
		sb = saveSB;
		
		write(displayed.trim());
		pattern = save;
		
	}
	
	public void visit(WtSection s)
	{
		iterate(s);
	}
	
	public void visit(WtParagraph p)
	{
		iterate(p);
	}
	
	public void visit(WtHorizontalRule hr)
	{

	}
	
	public void visit(WtXmlElement e)
	{
		if ("amp".equalsIgnoreCase(e.getName()))
		{
			write("&");
		}
		else if ("lt".equalsIgnoreCase(e.getName()))
		{
			write("<");
		}
		else if ("gt".equalsIgnoreCase(e.getName()))
		{
			write(">");
		}
		else if ("nbsp".equalsIgnoreCase(e.getName()))
		{
			write("\u00A0");
		}
		else if ("middot".equalsIgnoreCase(e.getName()))
		{
			write("\u00B7");
		}
		else if ("mdash".equalsIgnoreCase(e.getName()))
		{
			write("\u2014");
		}
		else if ("ndash".equalsIgnoreCase(e.getName()))
		{
			write("\u2013");
		}
		else if ("equiv".equalsIgnoreCase(e.getName()))
		{
			write("\u2261");
		}
		else
			if ("br".equalsIgnoreCase(e.getName()))
		{
//			newline(1);
		}
		else
		{
			iterate(e.getBody());
		}
	}
	
	public void visit(WtIllegalCodePoint n)
	{
		
	}
	
	public void visit(WtXmlComment n)
	{
		/*
		 * Invisible text
		 */
	}
	
	public void visit(WtTemplate n)
	{
		/*
		 * Templates are used to replicate content
		 */
	}
	
	public void visit(WtTemplateArgument n)
	{
		/*
		 * Templates are used to replicate content
		 */
	}
	
	public void visit(WtTagExtensionBodyImpl n)
	{
		write(n.getContent());
	}
	
	public void visit(WtTagExtension n)
	{
		if(("math").equalsIgnoreCase(n.getName())){
			
			finishLine();
			
			List saveElements = tokens;
			StringBuilder saveSB = sb;
			
			tokens = new LinkedList<>();
			sb = new StringBuilder();
			pattern = patternWSP;
			
			iterate(n);
			finishLine();
			
			MathFormulaToken mathformula = new MathFormulaToken(tokens, sb.toString(), position);
			mathformula.setPrevsPrefixPositions(PrefixHashMapBuilder.getPrefixHashMap(tokens, 5));
			
			sb = saveSB;
			tokens = saveElements;
			
			mathtokens.add(mathformula);
			tokens.add(mathformula);
			position++;
		}
	}
	
	public void visit(WtPageSwitch n)
	{
		/*
		 * Uncommon in articles
		 */
	}
	
	// =========================================================================
	
//	private void newline(int num)
//	{
//		if (pastBod)
//		{
//			if (num > needNewlines)
//				needNewlines = num;
//		}
//	}
	
	private void wantSpace()
	{
		if (pastBod)
			needSpace = true;
	}
	
	private void finishLine()
	{
		sb.append(line.toString());
		line.setLength(0);
	}
	
	private void writeNewlines(int num)
	{
		finishLine();
		sb.append(StringUtils.strrep('\n', num));
		needNewlines = 0;
		needSpace = false;
	}
	
	private void writeWord(String s)
	{
		int length = s.length();
		if (length == 0){
			return;
		}
			
		if (!noWrap && needNewlines <= 0)
		{
			if (needSpace)
				length += 1;
			
			if (line.length() + length >= 60 && line.length() > 0)
				writeNewlines(1);
		}
		
		if (needSpace && needNewlines <= 0)
			line.append(' ');
		
		if (needNewlines > 0)
			writeNewlines(needNewlines);
		
		needSpace = false;
		pastBod = true;
		line.append(s);
		
		tokens.add(new StringToken(s));
		position++;
	}
	
	private void write(String s)
	{
		if (s.isEmpty())
			return;
		
		if (Character.isSpaceChar(s.charAt(0)))
			wantSpace();

		String[] words = pattern.split(s.replaceAll("\\s+(?=\\p{Punct})", "").trim());
		wantSpace();

		
		for (int i = 0; i < words.length;)
		{
			writeWord(words[i]);
			if (++i < words.length)
				wantSpace();
		}
		
		if (Character.isSpaceChar(s.charAt(s.length() - 1)))
			wantSpace();
	}
	
	private void write(char[] cs)
	{
		write(String.valueOf(cs));
	}
	
	private void write(char ch)
	{
		writeWord(String.valueOf(ch));
	}
	
//	private void write(int num)
//	{
//		writeWord(String.valueOf(num));
//	}
	
}
