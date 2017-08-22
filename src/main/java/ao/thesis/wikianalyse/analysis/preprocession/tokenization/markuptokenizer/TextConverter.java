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

package ao.thesis.wikianalyse.analysis.preprocession.tokenization.markuptokenizer;

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
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTagExtensionBody.WtTagExtensionBodyImpl;

import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Markup;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MarkupToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.StringToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
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
	private static String wsPattern = "\\s+|\\p{Z}+";
	
	private static String wspunctPattern =  wsPattern +"|(?=(?U)\\p{Punct})|(?<=(?U)\\p{Punct})|(?=\\p{Punct})|(?<=\\p{Punct})";

	private static final Pattern ws = Pattern.compile(wsPattern);
	
	private static final Pattern wspunct = Pattern.compile(wspunctPattern);
	
//	private final int wrapCol = 80;
	
	public StringBuilder sb;
	
	public StringBuilder line;
	
	/**
	 * Becomes true if we are no long at the Beginning Of the whole Document.
	 */
	private boolean pastBod;
	
	private int needNewlines;
	
	private boolean needSpace;
	
	private boolean noWrap;
	
	private LinkedList<Integer> sections;
	
	// =========================================================================
	
	private List<Token> tokens = new LinkedList<>();
	
	public List<StringToken> stringtokens = new LinkedList<>();
	
	private Pattern usedPattern = wspunct;
	
	TextConverter(){}
	
	@Override
	protected WtNode before(WtNode node)
	{
		// This method is called by go() before visitation starts
		sb = new StringBuilder();
		line = new StringBuilder();
		
//		extLinkNum = 1;
		pastBod = false;
		needNewlines = 0;
		needSpace = false;
		noWrap = false;
		sections = new LinkedList<>();
		
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
		finishLine();
		
		List saveElements = tokens;
		StringBuilder saveSB = sb;
		
		tokens = new LinkedList<>();
		sb = new StringBuilder();
		usedPattern = wspunct;
		
		iterate(b);
		finishLine();
		MarkupToken bold = new MarkupToken(tokens, sb.toString().trim(), Markup.BOLD, sb.toString().trim());
		
		sb = saveSB;
		tokens = saveElements;
		
		tokens.add(bold);
	}
	
	public void visit(WtItalics i)
	{
		finishLine();
		
		List saveElements = tokens;
		StringBuilder saveSB = sb;
		
		tokens = new LinkedList<>();
		sb = new StringBuilder();
		usedPattern = wspunct;
		
		iterate(i);
		finishLine();
		MarkupToken italics = new MarkupToken(tokens, sb.toString().trim(), Markup.ITALIC, sb.toString().trim());
		
		sb = saveSB;
		tokens = saveElements;
		
		tokens.add(italics);
	}
	
	public void visit(WtXmlCharRef cr)
	{
		write(Character.toChars(cr.getCodePoint()));
	}
	
	public void visit(WtXmlEntityRef er)
	{
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
	}
	
	public void visit(WtUrl wtUrl)
	{
		if (!wtUrl.getProtocol().isEmpty())
		{
			write(wtUrl.getProtocol());
			write(':');
		}
		write(wtUrl.getPath());
	}
	
	public void visit(WtExternalLink link)
	{
		finishLine();
		
		List saveElements = tokens;
		StringBuilder saveSB = sb;
		
		String title;
		String reference;
		List titletokens;
		
		if (link.hasTitle()){
			tokens = new LinkedList<>();
			sb = new StringBuilder();
			usedPattern = wspunct;
			iterate(link.getTitle());
			finishLine();
			titletokens = tokens;
			title = sb.toString();
			
			List saveWords = stringtokens;
			sb = new StringBuilder();
			usedPattern = ws;
			iterate(link.getTarget());			
			reference = sb.toString();
			stringtokens = saveWords;
			
		} else {
			tokens = new LinkedList<>();
			sb = new StringBuilder();
			usedPattern = ws;
			iterate(link.getTarget());
			finishLine();
			title = sb.toString();
			reference = sb.toString();
			titletokens = tokens;
		}
		MarkupToken externlink = new MarkupToken(titletokens, title.trim(), Markup.EXTERNLINK, reference.trim());
		
		sb = saveSB;
		tokens = saveElements;
		
		tokens.add(externlink);
		usedPattern = wspunct;
	}
	
	public void visit(WtImageLink link)
	{
		finishLine();
		
		List saveElements = tokens;
		StringBuilder saveSB = sb;
		
		String title;
		String reference;
		List titletokens;
		
		if (link.hasTitle()){
			tokens = new LinkedList<>();
			sb = new StringBuilder();
			iterate(link.getTitle());
			usedPattern = wspunct;
			finishLine();
			titletokens = tokens;
			title = sb.toString();
			
			List saveWords = stringtokens;
			sb = new StringBuilder();
			usedPattern = ws;
			iterate(link.getTarget());			
			reference = sb.toString();
			stringtokens = saveWords;
			
		} else {
			tokens = new LinkedList<>();
			sb = new StringBuilder();
			usedPattern = ws;
			iterate(link.getTarget());
			finishLine();
			title = sb.toString();
			reference = sb.toString();
			titletokens = tokens;
		}
		MarkupToken imagelink = new MarkupToken(titletokens, title.trim(), Markup.IMAGELINK, reference.trim());
		
		sb = saveSB;
		tokens = saveElements;
		
		tokens.add(imagelink);
		usedPattern = wspunct;
	}
	
	public void visit(WtInternalLink link)
	{
		finishLine();
		
		List saveElements = tokens;
		StringBuilder saveSB = sb;
		
		String title;
		MarkupToken internallink;
		
		tokens = new LinkedList<>();
		sb = new StringBuilder();
		usedPattern = ws;
		iterate(link.getTarget());
		finishLine();
		
		String reference = sb.toString();
		String[] referenceSegments = reference.toLowerCase().split(":");
		
		if(Objects.nonNull(referenceSegments) && referenceSegments.length > 1){
			title = referenceSegments[1];	
			if(Strings.isNullOrEmpty(reference = referenceSegments[0]) && referenceSegments.length > 2){
				reference = referenceSegments[1];
				title = referenceSegments[2];
			}
		} else {
			title = reference;
		}
		if(reference.toLowerCase().contains("immagine")){
			internallink = new MarkupToken(tokens, title.trim(), Markup.IMAGELINK, reference.trim());
		}else if(reference.toLowerCase().contains("file")){
			internallink = new MarkupToken(tokens, title.trim(), Markup.FILE, reference.trim());
		} else if (reference.toLowerCase().contains("categoria")){
			internallink = new MarkupToken(tokens, title.trim(), Markup.CATEGORY, reference.trim());
		} else {
			internallink = new MarkupToken(tokens, title.trim(), Markup.LINK, reference.trim());
		}
		
		sb = saveSB;
		tokens = saveElements;
		
		tokens.add(internallink);
		usedPattern = wspunct;
	}
	
	public void visit(WtSection s)
	{
		finishLine();
		
		List saveElements = tokens;
		List saveWords = stringtokens;
		
		StringBuilder saveSB = sb;
		boolean saveNoWrap = noWrap;
		
		tokens = new LinkedList<>();
		sb = new StringBuilder();
		noWrap = true;

		usedPattern = ws;
		iterate(s.getHeading());
		finishLine();
		
		String title = sb.toString().trim();
		
		if (s.getLevel() >= 1)
		{
			while (sections.size() > s.getLevel())
				sections.removeLast();
			while (sections.size() < s.getLevel())
				sections.add(1);
			
			StringBuilder sb2 = new StringBuilder();
			for (int i = 0; i < sections.size(); ++i)
			{
				if (i < 1)
					continue;
				
				sb2.append(sections.get(i));
				sb2.append('.');
			}
			
			if (sb2.length() > 0)
				sb2.append(' ');
			sb2.append(title);
			title = sb2.toString();
		}
		
		tokens = new LinkedList<>();
		stringtokens = saveWords;
		sb = new StringBuilder();
		
		write(title);
		finishLine();
		MarkupToken header = new MarkupToken(tokens, title.trim(), Markup.HEADER, title.trim());
		
		noWrap = saveNoWrap;
		sb = saveSB;
		tokens = saveElements;
		
		tokens.add(header);
		usedPattern = wspunct;
		
		iterate(s.getBody());
		
		while (sections.size() > s.getLevel())
			sections.removeLast();
		sections.add(sections.removeLast() + 1);
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
	
	// =========================================================================
	// Stuff we want to hide
	
	public void visit(WtIllegalCodePoint n)
	{
	}
	
	public void visit(WtXmlComment n)
	{
	}
	
	public void visit(WtTemplate n)
	{
	}
	
	public void visit(WtTemplateArgument n)
	{
	}
	
	public void visit(WtTagExtensionBodyImpl n)
	{
//		write(n.getContent());
	}
	
	public void visit(WtTagExtension n)
	{
		if(n.getNodeTypeName().equals("<math>")){
			
			finishLine();
			
			List saveElements = tokens;
			StringBuilder saveSB = sb;
			
			tokens = new LinkedList<>();
			sb = new StringBuilder();
			usedPattern = wspunct;
			
			iterate(n);
			finishLine();
			
			MarkupToken mathformula = new MarkupToken(tokens, sb.toString().trim(), Markup.MATH, sb.toString().trim());
			
			sb = saveSB;
			tokens = saveElements;
			
			tokens.add(mathformula);
		}
	}
	
	public void visit(WtPageSwitch n)
	{
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
		stringtokens.add(new StringToken(s));
	}
	
	private void write(String s)
	{
		if (s.isEmpty())
			return;
		
		if (Character.isSpaceChar(s.charAt(0)))
			wantSpace();

		String text = s;
		if(usedPattern.equals(wspunct)){
			text = s.replaceAll("\\s+(?=\\p{Punct})", "");
		}
		String[] words = usedPattern.split(text);
		wantSpace();
		
		for (int i = 0; i < words.length;)
		{
			writeWord(words[i]);
			if (++i < words.length)
				wantSpace();
		}
		
		if (Character.isSpaceChar(text.charAt(text.length() - 1)))
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
