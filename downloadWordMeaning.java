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

public class downloadWordMeaning {

    static Set mark_set=new HashSet();
    static Map<String,Map<String,Map<Integer,Integer> > > word_place=new HashMap();
    
    public static Vector<String> getTransInfo(String word) {
    	ConnectionManager manager;  
		Lexer lexer,back_lexer;  
		Node node;
	String ret_content="";
	Vector<String> line_vec=new Vector<String>();
	
		try
		{
			manager = Page.getConnectionManager();

			String dic_html_page="http://dict.youdao.com/search?q=";
			dic_html_page+=word;
			dic_html_page+="&keyfrom=dict.index";

			//获得有道字典单词界面上的基本解释信息
			//lexer=new Lexer(manager.openConnection("http://astro.sina.com.cn/sagittarius.html"));
			lexer=new Lexer(manager.openConnection(dic_html_page));
			
			int record_container=-1;
			while(null!=(node=lexer.nextNode(false)))
			{
				Integer beg=node.getStartPosition();
				Integer end=node.getEndPosition();
				Integer len=end-beg;
				String tofile=node.toString();
				//tofile+="\n";
				int posf=tofile.indexOf("Txt (");
				if(-1==posf)
				{
					//appendMethodB(mywriter, tofile);
					if(tofile.indexOf("trans-container")!=-1)
					{
						//ret_content+="[-[-[-基本解释-]-]-][开始]\n";
						record_container=0;
					}
					if( record_container==0 && tofile.indexOf("/div")!=-1 )
					{
						record_container=1;
						//ret_content+="\n[-[-[-基本解释-]-]-][结束]";
						break;
					}
				}
				else
				{
					if(record_container==0)
					{
						String originalHtml=node.toHtml();
						//originalHtml+="\n";
						ret_content+=originalHtml;
						line_vec.addElement(originalHtml);
					}
				}
			}
			
			//获得有道字典单词界面上的21世纪答应汉词典中的信息
			lexer=new Lexer(manager.openConnection(dic_html_page));
			
			int record_21centery=-1;
			int tag_write_1_21cen=0;
			int tag_omit_21cen_sn=1;
			Integer meaning_sn=0;
			while(null!=(node=lexer.nextNode(false)))
			{
				String tofile=node.toString();
				tofile+="\n";
				int posf=tofile.indexOf("Txt (");
				
				if(-1!=posf)//是一个文本TXT
				{
					if(tofile.indexOf("21世纪大英汉词典")!=-1)
					{
						if(tofile.indexOf("以上来源于：《21世纪大英汉词典》")!=-1)
						{
							record_21centery=1;
							//ret_content+="[-[-[-21世纪大英汉词典-]-]-][结束]\n";
							break;
						}
						record_21centery=0;
						//ret_content+="\n[-[-[-21世纪大英汉词典-]-]-][开始]\n";
						continue;
					}
					if( record_21centery==0 && tag_write_1_21cen==1 )
					{
						//appendMethodB(mywriter, "21世纪字典数据"+tofile+"\n");
						String originalHtml=node.toHtml();
						if(tag_omit_21cen_sn==0)
						{
							ret_content+=meaning_sn.toString()+"."+originalHtml+"\n";
							line_vec.addElement(meaning_sn.toString()+"."+originalHtml);
						}
						else if(tag_omit_21cen_sn==1)
						{
							ret_content+=originalHtml+"\n";
							line_vec.addElement(originalHtml);
						}
						
						
						tag_write_1_21cen=0;
					}
				}
				else if(-1!=tofile.indexOf("Tag ("))//不是一个文本TXT
				{
					if(record_21centery==0)
					{
						//appendMethodB(mywriter, "测试数据"+tofile+"\n");
						if( tofile.indexOf("span class=\"def\"")!=-1 || 
								tofile.indexOf("span class=\"def wordGroup\"")!=-1 || 
								tofile.indexOf("span class=\"def wordGroup collapse\"")!=-1 || 
								tofile.indexOf("span class=\"pos wordGroup collapse\"")!=-1 ||
								tofile.indexOf("span class=\"pos wordGroup\"")!=-1)
						{
							tag_write_1_21cen=1;
							//appendMethodB(mywriter, "下一条数据应该是解释的数据");
							if( tofile.indexOf("span class=\"pos wordGroup collapse\"")!=-1 ||
									tofile.indexOf("span class=\"pos wordGroup\"")!=-1 )
							{
								meaning_sn=0;
								tag_omit_21cen_sn=1;
							}
							if( tofile.indexOf("span class=\"def\"")!=-1 || 
									tofile.indexOf("span class=\"def wordGroup\"")!=-1 || 
									tofile.indexOf("span class=\"def wordGroup collapse\"")!=-1 )
							{
								meaning_sn++;
								tag_omit_21cen_sn=0;
							}
						}
					}
				}
			}

			//获得有道字典单词界面上的英英释义的信息
			lexer=new Lexer(manager.openConnection(dic_html_page));
			
			int record_engeng=-1;
			int tag_write_1_engeng=0;
			int tag_omit_engeng_sn=1;
			meaning_sn=0;
			while(null!=(node=lexer.nextNode(false)))
			{
				String tofile=node.toString();
				tofile+="\n";
				int posf=tofile.indexOf("Txt (");
				/*
				if(record_engeng==0)
				{
					appendMethodB(mywriter, "正在寻找英英释义"+tofile+"\n");
				}
				*/
				//appendMethodB(mywriter, "正在寻找EngEng释义"+tofile+"\n");
				if(-1!=posf)//是一个文本TXT
				{
					if( tofile.indexOf("英英释义")!=-1 && record_engeng!=0 )
					{
						record_engeng=0;
						//appendMethodB(mywriter, "开始英英释义\n");
						//ret_content+="\n[-[-[-英英释义-]-]-][开始]\n";
						continue;
					}
					if( record_engeng==0 && tag_write_1_engeng==1 )
					{
						//appendMethodB(mywriter, "21世纪字典数据"+tofile+"\n");
						String originalHtml=node.toHtml();
						if(tag_omit_engeng_sn==0)
						{
							ret_content+=meaning_sn.toString()+"."+originalHtml+"\n";
							line_vec.addElement(meaning_sn.toString()+"."+originalHtml);
						}
						else if(tag_omit_engeng_sn==1)
						{
							ret_content+=originalHtml+"\n";
							line_vec.addElement(originalHtml);
						}
						
						
						tag_write_1_engeng=0;
					}
				}
				else if(-1!=tofile.indexOf("Rem ("))//不是一个文本TXT
				{
					if( tofile.indexOf("eng eng result start")!=-1 && record_engeng!=0 )
					{
						record_engeng=0;
						//appendMethodB(mywriter, "开始英英释义\n");
						//ret_content+="\n[-[-[-英英释义-]-]-][开始]\n";
						continue;
					}
					if(tofile.indexOf("英英翻译结束")!=-1)
					{
						record_engeng=1;
						//ret_content+="[-[-[-英英释义-]-]-][结束]\n";
						break;
					}
				}
				else if(-1!=tofile.indexOf("Tag ("))//不是一个文本TXT
				{
					if(record_engeng==0)
					{
						//appendMethodB(mywriter, "测试数据"+tofile+"\n");
						if( tofile.indexOf("span class=\"def\"")!=-1 || 
								tofile.indexOf("span class=\"def wordGroup collapse\"")!=-1 || 
								tofile.indexOf("span class=\"pos\"")!=-1)
						{
							tag_write_1_engeng=1;
							//appendMethodB(mywriter, "下一条数据应该是解释的数据");
							if( tofile.indexOf("span class=\"pos\"")!=-1 )
							{
								meaning_sn=0;
								tag_omit_engeng_sn=1;
							}
							if( tofile.indexOf("span class=\"def\"")!=-1 || 
									tofile.indexOf("span class=\"def wordGroup collapse\"")!=-1 )
							{
								meaning_sn++;
								tag_omit_engeng_sn=0;
							}
						}
					}
				}
			}
		}
		catch (ParserException e)  
		{  
			e.printStackTrace();  
		}
		
		for(int cn=0;cn<line_vec.size();)
		{
			String line=line_vec.get(cn);
			
			int bad_cnt=0;
			for(int sn=0;sn<line.length();sn++)
			{
				if( (int)(line.charAt(sn))==10 || (int)(line.charAt(sn))==32 )
				{
					bad_cnt++;
				}
			}
			
			if(bad_cnt==line.length())
			{
				line_vec.remove(cn);
			}
			else
			{
				cn++;
			}
		}
		/*
		for(int cn=0;cn<line_vec.size();cn++)
		{
			System.out.println("====>"+line_vec.get(cn)+"^^^");
			String cs=line_vec.get(cn);
			for(int sn=0;sn<cs.length();sn++)
			{
				char ch=cs.charAt(sn);
				int chas=(int)ch;
				System.out.print(chas+" ");
			}
			System.out.println("");
		}
		*/
		return line_vec;
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
		Vector<String> meaning_vec=getTransInfo("novel");
		for(int cn=0;cn<meaning_vec.size();cn++)
		{
			System.out.println(meaning_vec.get(cn));
		}
	}
}
