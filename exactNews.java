/*
package parser.parserdemo.filtertest;
*/
//javac -cp /home/ytl/Code4BiblWords/htmlparser1_6/lib/htmlparser.jar getWord.java
//java -cp /home/ytl/Code4BiblWords/htmlparser1_6/lib/htmlparser.jar:/usr/local/mysql/mysql-connector-java-5.1.35-bin.jar: getWord

import org.htmlparser.Node;  
import org.htmlparser.http.ConnectionManager;  
import org.htmlparser.lexer.Lexer;  
import org.htmlparser.lexer.Page;  
import org.htmlparser.util.ParserException;
import org.htmlparser.Parser;
import org.htmlparser.visitors.TextExtractingVisitor;
import org.htmlparser.util.ParserException;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.Div;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.NotFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Iterator;

import java.util.Vector;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

import java.util.HashMap;
import java.util.Map;

import java.sql.*;

public class exactNews {
	
	public static void nodeFilterTagClass(String url,String encoding,Class tagclass){
        try {
            Parser parser = new Parser();
            parser.setURL(url);
            if(null==encoding){
                parser.setEncoding(parser.getEncoding());
            }else{
                parser.setEncoding(encoding);
            }
            //过滤页面中的链接标签
            NodeFilter filter = new NodeClassFilter(tagclass);
            NodeList list = parser.extractAllNodesThatMatch(filter);
            for(int i=0; i<list.size();i++){
                Node node = (Node)list.elementAt(i);
                System.out.println("link is :" + node.toHtml());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static void nodeFilterTagName(String url,String encoding,String tagName){
        try {
            Parser parser = new Parser();
            parser.setURL(url);
            if(null==encoding){
                parser.setEncoding(parser.getEncoding());
            }else{
                parser.setEncoding(encoding);
            }
            //过滤页面中的链接标签
            NodeFilter filter = new TagNameFilter(tagName);
            NodeList list = parser.extractAllNodesThatMatch(filter);
            for(int i=0; i<list.size();i++){
                Node node = (Node)list.elementAt(i);
                System.out.println("link is :" + node.toHtml());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static void stringFilter(String url,String encoding,String containStr){
        try {
            Parser parser = new Parser();
            parser.setURL(url);
            if(null==encoding){
                parser.setEncoding(parser.getEncoding());
            }else{
                parser.setEncoding(encoding);
            }
            //OrFilter是结合几种过滤条件的‘或’过滤器
            NodeFilter filter = new StringFilter(containStr);
            NodeList list = parser.extractAllNodesThatMatch(filter);
            for(int i=0; i<list.size();i++){
                Node node = (Node)list.elementAt(i);
                System.out.println("link is :" + node.toHtml());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static String replace_bad_str_1(String old_str,String finding,String reps)
	{
		int pos1=0,pos2=-1;
                do
                {
			pos2=-1;
			pos2=old_str.indexOf(finding,pos1);
			if(pos2!=-1)
			{
			      pos1=pos2;
			      String part_1=old_str.substring(0,pos1);
			      pos2=pos1+finding.length();
			      String part_2=old_str.substring(pos2,old_str.length());
			      old_str=part_1+reps+part_2;
			}
			pos1=0;
                }while(pos2>-1);
                
		return old_str;
	}
	
	public static String del_bad_str_1(String old_str,String finding)
	{
		int pos1=0,pos2=-1;
                do
                {
			pos2=-1;
			pos2=old_str.indexOf(finding,pos1);
			if(pos2!=-1)
			{
			      pos1=pos2;
			      String part_1=old_str.substring(0,pos1);
			      pos2=pos1+finding.length();
			      String part_2=old_str.substring(pos2,old_str.length());
			      old_str=part_1+" "+part_2;
			}
			pos1=0;
                }while(pos2>-1);
                
		return old_str;
	}
	
	public static String del_bad_str_2(String old_str,String first,String second)
	{
		int pos1=0,pos2=-1;
                do
                {
			pos2=-1;
			pos2=old_str.indexOf(first,pos1);
			int pos4=pos2;
			int last_p=pos2;
			//System.out.println("^^");
			while(pos4!=-1)
			{
				int pos3=pos4+1;
				pos4=old_str.indexOf(first,pos3);
				if(pos4>-1)
				{
					last_p=pos4;
				}
				//System.out.println("vv"+pos3+"--"+pos4);
			}
			pos2=last_p;
			//System.out.println("++");
			if(pos2!=-1)
			{
			      pos1=pos2;
			      pos2=old_str.indexOf(second,pos1);
			      if(pos2!=-1)
			      {
				      String part_1=old_str.substring(0,pos1);
				      pos2++;
				      String part_2=old_str.substring(pos2,old_str.length());
				      old_str=part_1+part_2;
			      }
			}
			pos1=0;
			//System.out.println("--");
                }while(pos2>-1);
                
                return old_str;
	}
	
	public static void MixedMultiFilter(String url,String encoding,NodeFilter filter){
        try {
            Parser parser = new Parser();
            parser.setURL(url);
            if(null==encoding){
                parser.setEncoding(parser.getEncoding());
            }else{
                parser.setEncoding(encoding);
            }
            //OrFilter是结合几种过滤条件的‘或’过滤器
            int cn=0;
            NodeList list = parser.extractAllNodesThatMatch(filter);
            for(int i=0; i<list.size();i++){
		cn++;
		//System.out.println("==="+cn);
                Node node = (Node)list.elementAt(i);
                String old_str=node.toHtml();
                
                old_str=del_bad_str_2(old_str,"<",">");
                old_str=del_bad_str_2(old_str,"{","}");
                old_str=del_bad_str_1(old_str,"&nbsp;");
                old_str=replace_bad_str_1(old_str,"&ndash;","-");
                old_str=replace_bad_str_1(old_str,"&rsquo;","'");
                old_str=replace_bad_str_1(old_str,"&ldquo;","\"");
                old_str=replace_bad_str_1(old_str,"&rdquo;","\"");
                old_str=replace_bad_str_1(old_str,"&pound;","£");
                old_str=replace_bad_str_1(old_str,"&hellip;","...");
                old_str=replace_bad_str_1(old_str,"&lsquo;","'");
                //old_str=replace_bad_str_1(old_str,"","");
                if(old_str.length()>0)
                {
			System.out.println("[[" + old_str+"]]");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static String getPlainText2(String str)
	{
		try
		{
			Parser parser=new Parser();
			parser.setInputHTML(str);
			
			TextExtractingVisitor visitor=new TextExtractingVisitor();
			parser.visitAllNodesWith(visitor);
			str=visitor.getExtractedText();
		}
		catch(ParserException e)
		{
		     System.out.println(e);
		}
		
		return str;
	}
	
	public static void main (String[] args)  
	{  
		Integer argc=args.length;
		String argc_str=argc.toString();
		argc_str+="\n";
		//System.out.println(argc_str);
		if(argc!=0)
		{
			String errstr="it should be: thispro";
			System.out.println(errstr);
			System.exit(0);
		}
		/*
		Vector<String> meaning_vec=getTransInfo("novel");
		for(int cn=0;cn<meaning_vec.size();cn++)
		{
			System.out.println(meaning_vec.get(cn));
		}*/
		String url="http://www.bbc.com/future/story/20150618-the-strange-expertise-of-burglars";
		//String url="http://www.bbc.com/news/world-us-canada-33214205";
		//String url="http://www.bbc.com/news/world-us-canada-33214197";
		//String url="http://www.bbc.com/news/world-asia-33211935";
		//String url="http://www.bbc.com/news/world-latin-america-33203790";
		//String url="http://www.bbc.com/earth/story/20150619-there-is-alien-dna-inside-you";

		//String content=getPlainText2(url);
		//nodeFilterTagClass(url, "UTF-8", Div.class);
		//nodeFilterTagName(url, "UTF-8", "p");
		
		NodeFilter filter = new TagNameFilter("p");
		//NodeFilter filter = new AndFilter(filters);
		/*
		NodeFilter filterNoHref = new NotFilter( new StringFilter("href") );
		NodeFilter filterP = new TagNameFilter("p");
		NodeFilter filter = new AndFilter(filterNoHref, filterP);
		*/
		MixedMultiFilter(url, "UTF-8", filter);

		//stringFilter(url, "UTF-8", "img.baidu.com");
		//System.out.println(content);
	}
}
