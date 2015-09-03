// 脙聝脗楼脙聜脗炉脙聟脗聮脙聝脗楼脙聜脗聟脙聜脗楼脙聝脗楼脙聜脗驴脙聜脗聟脙聝脗漏脙聜脗聹脙聜脗聙脙聝脗搂脙聜脗職脙聜脗聞 java 脙聝脗楼脙聜脗潞脙聜脗聯

import org.htmlparser.Node;  
import org.htmlparser.http.ConnectionManager;  
import org.htmlparser.lexer.Lexer;  
import org.htmlparser.lexer.Page;  
import org.htmlparser.util.ParserException;
import org.htmlparser.Parser;
import org.htmlparser.NodeFilter;
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

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;

// 脙聝脜聽脙聜脗聣脙聜脗漏脙聝脗楼脙聜脗卤脙聜脗聲 HttpServlet 脙聝脗搂脙聜脗卤脙聜脗禄
class WordInfo{
	int pos;
	String word;
}

public class OutputWordsInsideChapter extends HttpServlet {

	static final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	static final String db_address="127.0.0.1";
	static final String db_port="3306";
	static final String db_name="words_and_question";
	static final String encoding="characterEncoding=gbk";
	static final String DB_URL = "jdbc:mysql://"+db_address+":"+db_port+"/"+db_name;
		
	//static final String DB_URL="jdbc:mysql://localhost/words_and_question";

	//  脙聝脜聽脙聜脗聲脙聜脗掳脙聝脜聽脙聜脗聧脙聜脗庐脙聝脗楼脙聜脗潞脙聜脗聯脙聝脗搂脙聜脗職脙聜脗聞脙聝脗楼脙聜脗聡脙聜脗颅脙聝脜聽脙聜脗聧脙聜脗庐
	static final String USER = "root";
	static final String PASS = "";

	//following data should be CLEARED EACH time!!
	static Set<String> words_set=new HashSet<String>();
	//static Map<String,Integer> words2place_map=new HashMap<String,Integer>();
	static Map<Integer,Map<Integer,String> > place2words_map=new HashMap<Integer,Map<Integer,String> >();
	static Map<String,Integer> word2verseno=new HashMap<String,Integer>();
	static Map<Integer,String> verse_info=new TreeMap<Integer,String>();
	static Map<String,String> verseno2tableno=new HashMap<String,String>();
	static Map<String,String> wordno2tableno=new HashMap<String,String>();
	Integer table_cnt=0;
	Integer word_cnt=0;
	Integer verse_cnt=0;
	Integer wQuestion_cnt=0;
	
	String specification_tbname="";
	String phrase_info_tbname="";
	String question_answer_tbname="";
	String verse_info_tbname="";
	String word_place_tbname="";
	String words_meaning_tbname="";
	String words_question_tbname="";
	//above data should be CLEARED EACH time!!
	
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
	
	public static Vector<String> MixedMultiFilter(String url,String encoding,PrintWriter out){
		Vector<String> lines_vec=new Vector<String>();
        try {
		NodeFilter filter = new TagNameFilter("p");
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
			//out.println("[[" + old_str+"]]");
			lines_vec.addElement(old_str);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lines_vec;
    }
    
	public static Vector<String> getTransInfo(String word) {
    	
	Vector<String> line_vec=new Vector<String>();
	
	
	ConnectionManager manager;  
		Lexer lexer,back_lexer;  
		Node node;
	String ret_content="";
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
		
		return line_vec;
	}
	
	public void SetTableInfo(String reading_type,PrintWriter out)
	{
		if(reading_type.equals("bible")==true)
		{
			specification_tbname="my_specification";
			phrase_info_tbname="phrase_info";
			question_answer_tbname="question_answer";
			verse_info_tbname="verse_info";
			word_place_tbname="word_place";
			words_meaning_tbname="words_meaning";
			words_question_tbname="words_question";
		}
		else if(reading_type.equals("literature")==true)
		{
			specification_tbname="literature_my_specification";
			phrase_info_tbname="literature_phrase_info";
			question_answer_tbname="literature_question_answer";
			verse_info_tbname="literature_verse_info";
			word_place_tbname="literature_word_place";
			words_meaning_tbname="literature_words_meaning";
			words_question_tbname="literature_words_question";
		}
		else if(reading_type.equals("news")==true)
		{
			specification_tbname="news_my_specification";
			phrase_info_tbname="news_phrase_info";
			question_answer_tbname="news_question_answer";
			verse_info_tbname="news_verse_info";
			word_place_tbname="news_word_place";
			words_meaning_tbname="news_words_meaning";
			words_question_tbname="news_words_question";
		}
	}
	
	public String FormInfo4OutputWordsInsideChapter(String book,String chapter_no,String mo_word,String mo_meaning,String question_content,String answer_content,String verse_tag,String word_tag,String wQuestion_tag,String reading_type)
	{
		String form_data="OutputWordsInsideChapter?";
		form_data+="reading_type="+reading_type;
		form_data+="&book="+book;
		form_data+="&chapter_no="+chapter_no;
		form_data+="&mo_word="+mo_word;
		form_data+="&mo_meaning="+mo_meaning;
		form_data+="&question_content="+question_content;
		form_data+="&answer_content="+answer_content;
		form_data+="&verse_tag="+verse_tag;
		form_data+="&word_tag="+word_tag;
		form_data+="&wQuestion_tag="+wQuestion_tag;
	    
		return form_data;
	}
	
	public static String deal_escape_character_4_sql(String old_str)
	{
		String new_str="";
		
		int i=0;
		while(i<old_str.length())
		{
			String temp_str="";
			if( old_str.charAt(i)=='\'' || old_str.charAt(i)=='\"' || old_str.charAt(i)=='\\' || old_str.charAt(i)=='\n' || 
				old_str.charAt(i)=='-' )
			{
				temp_str="\\";
				temp_str+=old_str.charAt(i);
			}
			else
			{
				temp_str+=old_str.charAt(i);
			}
			new_str+=temp_str;
			
			i++;
			
			if(i%100000==0)
			{
				System.out.println("deal_escape_character length"+old_str.length()+" now "+i);
			}
		}
		
		return new_str;
	}
	
public static int mark_find(String possible_word)
{
  int pos=-1;
  for(pos=0;pos<possible_word.length();pos++)
  {
    //System.out.println("pos at "+pos+" ["+possible_word.charAt(pos)+"]");
    if ( ! ( ( possible_word.charAt(pos)>='a' && possible_word.charAt(pos)<='z' ) || ( possible_word.charAt(pos)>='A' && possible_word.charAt(pos)<='Z' ) ) )
    {
      break;
    }
  }
  //System.out.println("pos at "+pos+" len="+possible_word.length());
  if(pos>possible_word.length())
  {
    pos=-1;
  }
  //System.out.println("pos="+pos);
  return pos;
}

	static WordInfo parseword(String possible_word,PrintWriter out/*int chap_no,int verse_no,String possible_word,set<string> &mark_set,
	  map<string,pair<int,int> > &word_1st_appearance,map<pair<int,int>,vector<string> > &verse_words_list*/
	)
	{
	  WordInfo word_detail=new WordInfo();
	  int pos1=0;
	  while(pos1<possible_word.length())
	  {
		  if( ( possible_word.charAt(pos1)>='a' && possible_word.charAt(pos1)<='z' ) || ( possible_word.charAt(pos1)>='A' && possible_word.charAt(pos1)<='Z' ) )
		  {
			  break;
		  }
		  pos1++;
	  }
	  if(pos1>=possible_word.length())
	  {
		  word_detail.word="";
		  word_detail.pos=-1;
		  return word_detail;
	  }
	  word_detail.pos=pos1;
	  possible_word=possible_word.substring(pos1,possible_word.length());
	  pos1=0;
	  int pos3=mark_find(possible_word);
	  word_detail.word=possible_word;
	  //out.println("A possible_word:["+word_detail.word+"]&nbsp;");
	  if(pos3!=-1)
	  {
		  //out.println("XX "+pos1+"->"+pos3+",");
	    word_detail.word=possible_word.substring(pos1,pos3);
	    //System.out.println("["+word+"]:"+word.length());
	  }
	//out.println("B possible_word:["+word_detail.word+"]&nbsp;<"+pos3+">&nbsp;");
	  //set_word_appearance_info(word_1st_appearance,verse_words_list,word,chap_dig,no_dig);
	  return word_detail;
	}
	
	static Map<Integer,String> allWordsInsideVerse(String verse,int verse_no,PrintWriter out)
	{
		Map<Integer,String> place_allwords=new TreeMap<Integer,String>();
		//out.println("<br><br>VERSE "+verse_no+":"+verse+"<br><br>");
		int pos1=0;
		String line=verse;
		String finding=" ";
		int pos2=line.indexOf(finding,pos1);
		String possible_word,word;
		while(pos2!=-1)
		{
			possible_word=verse.substring(pos1,pos2);
			//parseword(chapter_no,verse_no,possible_word,word,mark_set,word_1st_appearance,verse_words_list);
			//word=parseword(possible_word,out);
			WordInfo word_info=parseword(possible_word,out);
			word=word_info.word;
			pos1+=word_info.pos;
			
			place_allwords.put(pos1,word);
			//out.println("--"+pos1+":"+"["+word+"]"+" ");
			pos1=pos2+1;
			if(pos1>=verse.length())
			{
				 break;
			}
			pos2=line.indexOf(finding,pos1);
		}
		possible_word=verse.substring(pos1,line.length());
		//parseword(chap_dig,no_dig,possible_word,word,mark_set,word_1st_appearance,verse_words_list);
		//word=parseword(possible_word,out);
		WordInfo word_info=parseword(possible_word,out);
		word=word_info.word;
		pos1+=word_info.pos;
		
		place_allwords.put(pos1,word);
		//out.println("++"+pos1+":"+"["+word+"]"+"<br><br>");
		return place_allwords;
	}
	
	static void addVerseWordPlace(String verse,int verse_no,PrintWriter out)
	{
		verse_info.put(verse_no,verse);
		int pos1=0;
		String line=verse;
		String finding=" ";
		int pos2=line.indexOf(finding,pos1);
		String possible_word,word;
		while(pos2!=-1)
		{
			possible_word=verse.substring(pos1,pos2);
			//parseword(chapter_no,verse_no,possible_word,word,mark_set,word_1st_appearance,verse_words_list);
			//word=parseword(possible_word,out);
			WordInfo word_info=parseword(possible_word,out);
			word=word_info.word;
			pos1+=word_info.pos;
			
			if(words_set.contains(word)==false)
			{
				word2verseno.put(word,verse_no);
			}
			if( false==words_set.contains(word) )
			{
				words_set.add(word);
				if( false==place2words_map.containsKey(verse_no) )
				{
					Map<Integer,String> p2w_item=new TreeMap<Integer,String>();
					p2w_item.put(pos1,word);
					place2words_map.put(verse_no,p2w_item);
					//out.println(verse_no+":"+word+"<br>");
				}
				else
				{
					place2words_map.get(verse_no).put(pos1,word);
					//out.println(verse_no+":"+word+"<br>");
				}
			}
			
			pos1=pos2+1;
			if(pos1>=verse.length())
			{
				 break;
			}
			pos2=line.indexOf(finding,pos1);
		}
		possible_word=verse.substring(pos1,line.length());
		//parseword(chap_dig,no_dig,possible_word,word,mark_set,word_1st_appearance,verse_words_list);
		//word=parseword(possible_word,out);
		WordInfo word_info=parseword(possible_word,out);
		word=word_info.word;
		pos1+=word_info.pos;
			
		if(words_set.contains(word)==false)
		{
			word2verseno.put(word,verse_no);
		}
		if( false==words_set.contains(word) )
		{
			words_set.add(word);
			if( false==place2words_map.containsKey(verse_no) )
			{
				Map<Integer,String> p2w_item=new TreeMap<Integer,String>();
				p2w_item.put(pos1,word);
				place2words_map.put(verse_no,p2w_item);
				//out.println(verse_no+":"+word+"<br>");
			}
			else
			{
				place2words_map.get(verse_no).put(pos1,word);
				//out.println(verse_no+":"+word+"<br>");
			}
		}
	}
	
	public Vector<Integer> getChapterList4thisBook(PrintWriter out,Connection conn,Statement stmt,String book)
	{
		Vector<Integer> chapters_list=new Vector<Integer>();
		
		try
		{
			String query_sql="SELECT DISTINCT chapter_no FROM "+verse_info_tbname+" WHERE book='"+book+"'";
			//out.println(query_sql);
			stmt = conn.createStatement();
			ResultSet result=stmt.executeQuery(query_sql);
			
			int chapter_no=0;
			while(result.next())
			{
				chapter_no=result.getInt("chapter_no");
				chapters_list.addElement(chapter_no);
				//out.println("<br>FOUND<br>");
			}
		}
		catch(SQLException e)
		{
			out.println("WRONG SQL-->"+e+"<br>");
		}
		
		return chapters_list;
	}
	
	public Vector<String> AddBooksInBible()
	{
		Vector<String> book_list=new Vector<String>();
		book_list.addElement("Genesis");
book_list.addElement("Exodus");
book_list.addElement("Leviticus");
book_list.addElement("Numbers");
book_list.addElement("Deuteronomy");
book_list.addElement("Joshua");
book_list.addElement("Judges");
book_list.addElement("Ruth");
book_list.addElement("1st Samuel");
book_list.addElement("2nd Samuel");
book_list.addElement("1st Kings");
book_list.addElement("2nd Kings");
book_list.addElement("1st Chronicles");
book_list.addElement("2nd Chronicles");
book_list.addElement("Ezra");
book_list.addElement("Nehemiah");
book_list.addElement("Esther");
book_list.addElement("Job");
book_list.addElement("Psalms");
book_list.addElement("Proverbs");
book_list.addElement("Ecclesiastes");
book_list.addElement("Song of Solomon");
book_list.addElement("Isaiah");
book_list.addElement("Jeremiah");
book_list.addElement("Lamentations");
book_list.addElement("Ezekiel");
book_list.addElement("Daniel");
book_list.addElement("Hosea");
book_list.addElement("Joel");
book_list.addElement("Amos");
book_list.addElement("Obadiah");
book_list.addElement("Jonah");
book_list.addElement("Micah");
book_list.addElement("Nahum");
book_list.addElement("Habakkuk");
book_list.addElement("Zephaniah");
book_list.addElement("Haggai");
book_list.addElement("Zechariah");
book_list.addElement("Malachi");
book_list.addElement("Matthew");
book_list.addElement("Mark");
book_list.addElement("Luke");
book_list.addElement("John");
book_list.addElement("Acts");
book_list.addElement("Romans");
book_list.addElement("1st Corinthians");
book_list.addElement("2nd Corinthians");
book_list.addElement("Galatians");
book_list.addElement("Ephesians");
book_list.addElement("Philippians");
book_list.addElement("Colossians");
book_list.addElement("1st Thessalonians");
book_list.addElement("2nd Thessalonians");
book_list.addElement("1st Timothy");
book_list.addElement("2nd Timothy");
book_list.addElement("Titus");
book_list.addElement("Philemon");
book_list.addElement("Hebrews");
book_list.addElement("James");
book_list.addElement("1st Peter");
book_list.addElement("2nd Peter");
book_list.addElement("1st John");
book_list.addElement("2nd John");
book_list.addElement("3rd John");
book_list.addElement("Jude");
book_list.addElement("Revelation");
		
		return book_list;
	}
	
	public void showBooksInBible(PrintWriter out,Vector<String> books_list,String book)
	{
		int sn=1;
		int mod=2;
		out.println("<table border=\"1\" width=\"1000px\">");
		//out.println("chapters_list.size()="+chapters_list.size()+"<br>");
		int line_cnt=1+books_list.size()/mod;
		
		for (int cnt=0; cnt<books_list.size(); cnt++)
		{
			if(sn%mod==1)
			{
				out.println("<tr>");
				if(sn==1)
				{
					out.println("<td align=\"center\" width=\"300px\"; rowspan=\""+line_cnt+"\">Contents of the Bible&nbsp;&nbsp;&nbsp;</td>");
				}
			}
			String bname = (String)books_list.get(cnt);
			
			if(bname.equals(book)==true)
			{
				out.println("<cb_s>"+bname+"</cb_s>");
			}
			else
			{
				out.println("<ob_s>"+bname+"</ob_s>");
			}
			out.println("</a></td>");
			
			sn++;
			if(sn%mod==1)
			{
				out.println("</tr>");
			}
		}
		
		out.println("</table>");
	}
	
	public void //showChaptersInBook(PrintWriter out,String current_chapter_no_str,Vector<Integer> chapters_list,String book,String mo_word,String mo_meaning,String question_content,String answer_content)
	showChaptersInBook(PrintWriter out,String current_chapter_no_str,Vector<Integer> chapters_list,String book,String reading_type)
	{
		Integer current_chapter=Integer.parseInt(current_chapter_no_str);
		
		//out.println("Directly jump to the chapters in this book<br>");
		int sn=1;
		int mod=25;
		out.println("<table border=\"0\" width=\"1000px\" align=\"left\">");
		//out.println("chapters_list.size()="+chapters_list.size()+"<br>");
		int line_cnt=1+chapters_list.size()/mod;
		
		for (int cnt=0; cnt<chapters_list.size(); cnt++)
		{
			if(sn%mod==1)
			{
				out.println("<tr>");
				if(sn==1)
				{
					out.println("<td align=\"center\" width=\"100px\"; rowspan=\""+line_cnt+"\">Directly To Chapters in this Book</td>");
				}
			}
			Integer chapter_no = (Integer)chapters_list.get(cnt);
			String form_data=FormInfo4OutputWordsInsideChapter(book,chapter_no.toString(),"null","null","null","null","null","null","null",reading_type);
			
			int current_chapter_i=current_chapter;
			int chapter_no_i=chapter_no;
			//out.println("<td><a href=\"OutputWordsInsideChapter?book="+book+"&chapter_no="+chapter_no+"&mo_word="+mo_word+"&mo_meaning="+mo_meaning+"&question_content="+question_content+"&answer_content="+answer_content+"\">"+chapter_no+"</a></td>");
			out.println("<td width=\"70\"><a href=\""+form_data+"\">");
			if(current_chapter_i==chapter_no_i)
			{
				out.println("<cc_s>"+chapter_no+"</cc_s>");
			}
			else
			{
				out.println("<oc_s>"+chapter_no+"</oc_s>");
			}
			out.println("</a></td>");
			
			sn++;
			if(sn%mod==1)
			{
				out.println("</tr>");
			}
		}
		
		out.println("</table>");
	}
	
	public Map<Integer,Integer> which_set_choose_word(Map<Integer,String> time1st_words_inside_this_verse,Map<Integer,String> place_allwords,PrintWriter out)
	{
		Map<Integer,Integer> all0_1st1_place_info=new TreeMap<Integer,Integer>();
		
		for (Map.Entry<Integer,String> entry_1 : place_allwords.entrySet())
		{
			Integer word_pos=entry_1.getKey();
			all0_1st1_place_info.put(word_pos,0);
		}
		
		if(null!=time1st_words_inside_this_verse)
		{
			for(Map.Entry<Integer,String> entry_1 : time1st_words_inside_this_verse.entrySet())
			{
				Integer word_pos=entry_1.getKey();
				if( false==all0_1st1_place_info.containsKey(word_pos) )
				{
					out.println("<br>------------XXX---------<br>");
				}
				else
				{
					all0_1st1_place_info.put(word_pos,1);
				}
			}
		}
		
		return all0_1st1_place_info;
	}
	
	public String produceTableTagName(String tag_head)
	{
		table_cnt=table_cnt+1;
		
		String new_tag=tag_head+"_"+table_cnt.toString();
		
		return new_tag;
	}
	
	public String produceWordTagName(String tag_head)
	{
		word_cnt=word_cnt+1;
		
		String new_tag=tag_head+"_"+word_cnt.toString();
		
		return new_tag;
	}
	
	public String produceWordQuestionTagName(String tag_head)
	{
		wQuestion_cnt=wQuestion_cnt+1;
		
		String new_tag=tag_head+"_"+wQuestion_cnt.toString();
		
		return new_tag;
	}
	
	public String produceVerseTagName(String tag_head)
	{
		verse_cnt=verse_cnt+1;
		
		String new_tag=tag_head+"_"+verse_cnt.toString();
		
		return new_tag;
	}
	
	public void printWhere2find(String word,Map<String,String> word_tag_set,Map<String,String> wordno2tableno,PrintWriter out)
	{
		if( false==word_tag_set.containsKey(word) )
		{
			out.println("<br>!!!!!!!!!!!!!! should not be like this way !!!!!<br>");
		}
		else
		{
			String word_tag=word_tag_set.get(word);
			String table_tag=wordno2tableno.get(word_tag);
			String clk_action="open_last('"+table_tag+"','"+word_tag+"')";
			//String place2find="<a href=\"#"+word_tag+"\">old word from verse&nbsp;&nbsp;"+word2verseno.get(word)+"</a>";
			//String place2find="<a href=\""+clk_action+"\">old word from verse&nbsp;&nbsp;"+word2verseno.get(word)+"</a>";
			String place2find="<button onclick=\""+clk_action+"\">old word from verse&nbsp;&nbsp;"+word2verseno.get(word)+"</button>";
			out.println(place2find);
		}
	}
	
	public void clearEmptyQuestion(Connection conn,PrintWriter out)throws SQLException
	{
		Statement stmt=null;
		
		String clear_empty_question="DELETE FROM "+question_answer_tbname+" WHERE question_content='' OR question_content=NULL";
		stmt=conn.createStatement();
		stmt.execute(clear_empty_question);
		
		String clear_useless_wowrd_question_sql="DELETE FROM "+words_question_tbname+" WHERE question_no NOT IN (SELECT question_no FROM "+question_answer_tbname+")";
		stmt=conn.createStatement();
		stmt.execute(clear_useless_wowrd_question_sql);
	}
	
	public void addOrUpdateQuestion(String book,String chapter_no,String verse_no,String question_content,String answer_content,int max_question_no,Connection conn,PrintWriter out)throws SQLException
	{
		clearEmptyQuestion(conn,out);
		
		String query_sql="SELECT * FROM "+question_answer_tbname+" WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no+" AND question_no NOT IN (SELECT question_no FROM words_question)";
		//out.println("<br> SQL::"+query_sql+"<br>");
		Statement stmt = conn.createStatement();
		ResultSet result=stmt.executeQuery(query_sql);
		boolean old_q=false;
		while(result.next())
		{
			old_q=true;
			//out.println("<br>FOUND<br>");
		}
		if(old_q==false)
		{
			String max_quesiton_no_sql="SELECT MAX(question_no) FROM "+question_answer_tbname;
			stmt = conn.createStatement();
			result=stmt.executeQuery(max_quesiton_no_sql);
			while(result.next())
			{
				max_question_no=result.getInt("MAX(question_no)");
				max_question_no++;
				//out.println("question_no=["+max_question_no+"]<br>");
			}
			
			String sql="INSERT INTO "+question_answer_tbname+" (question_no,book,chapter_no,verse_no,question_content,answer) VALUES("+max_question_no+",'"+book+"',"+chapter_no+","+verse_no+",'"+question_content+"','"+answer_content+"')";
			//out.println(sql);
			stmt = conn.createStatement();
			stmt.execute(sql);
		}
		else
		{
			String u_sql="UPDATE "+question_answer_tbname+" SET question_content='"+question_content+"',answer='"+answer_content+"' WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no+" AND question_no NOT IN (SELECT question_no FROM words_question)";
			//out.println(u_sql);
			stmt = conn.createStatement();
			stmt.execute(u_sql);
		}
	}
	
	public void addOrUpdateWordQuestion(String book,String chapter_no,String verse_no,String word,String question,String answer,Connection conn,PrintWriter out)throws SQLException
	{
		//Clear useless question_no inside words_question
		clearEmptyQuestion(conn,out);
		
		boolean tag_omit_question_insert=false;
		
		Statement stmt=conn.createStatement();
		String query_sql="SELECT a.word,a.question_no,b.book,b.chapter_no,b.verse_no,b.question_content,b.answer FROM "+words_question_tbname+" AS a,"+question_answer_tbname+" AS b WHERE a.question_no=b.question_no AND a.word='"+word+"' AND b.book='"+book+"' AND b.chapter_no="+chapter_no+" AND b.verse_no="+verse_no;
		//out.println("<br>^^^^^^^^^"+query_sql+"<br>");
		ResultSet result = stmt.executeQuery(query_sql);
		
		if(false==result.next())
		{
			String max_quesiton_no_sql="SELECT MAX(question_no) FROM "+question_answer_tbname;
			stmt = conn.createStatement();
			result=stmt.executeQuery(max_quesiton_no_sql);
			int max_q_no=0;
			while(result.next())
			{
				max_q_no=result.getInt("MAX(question_no)");
				//out.println("question_no=["+max_question_no+"]<br>");
			}
			max_q_no++;
			//out.println("<br>^^BBBB111<br>");
			String sql_1="INSERT IGNORE INTO "+question_answer_tbname+" (question_no,book,chapter_no,verse_no,question_content,answer) VALUES(";
			sql_1+=max_q_no+",'"+book+"',"+chapter_no+","+verse_no+",'"+question+"','"+answer+"')";
			//out.println(sql_1+"<br>");
			String sql_2="INSERT IGNORE INTO "+words_question_tbname+" (word,question_no) VALUES(";
			sql_2+="'"+word+"',"+max_q_no+")";
			//out.println(sql_2+"<br>");
			
			stmt = conn.createStatement();
			stmt.execute(sql_1);
			stmt = conn.createStatement();
			stmt.execute(sql_2);
		}
		else
		{
			int q_no=result.getInt("a.question_no");
			String u_sql="UPDATE "+question_answer_tbname+" SET question_content='"+question+"',answer='"+answer+"' WHERE question_no="+q_no;
			//out.println(u_sql);
			stmt = conn.createStatement();
			stmt.execute(u_sql);
		}
	}
	
	public void addOrUpdatePhrase(String phrase,String phrase_meaning,Connection conn,PrintWriter out)throws SQLException
	{// for phrase duplicated phrases are permited, so just use 'insert into'
		//out.println("addOrUpdatePhrase"+"<br>");
		{
			String sql_1="INSERT INTO "+phrase_info_tbname+" (phrase,meaning) VALUES('";
			sql_1+=phrase+"','"+phrase_meaning+"')";
			//out.println(sql_1+"<br>");
			
			Statement stmt = conn.createStatement();
			stmt.execute(sql_1);
		}
	}
	
	public void addOrUpdateSpecification(String book,String chapter_no,String verse_no,String spe_content,Connection conn,PrintWriter out)throws SQLException
	{
		String sql_q="SELECT * FROM "+specification_tbname+" WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no;
		Statement stmt = conn.createStatement();
		ResultSet res=stmt.executeQuery(sql_q);
		
		String sql_1="";
		if(res.next())
		{
			sql_1="UPDATE "+specification_tbname+" SET content='"+spe_content+"' WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no;
		}
		else
		{
			sql_1="INSERT INTO "+specification_tbname+" (content,book,chapter_no,verse_no) VALUES('";
			sql_1+=spe_content+"','"+book+"',"+chapter_no+","+verse_no+")";
		}
		//out.println(sql_1+"<br>");
		
		stmt = conn.createStatement();
		stmt.execute(sql_1);
	}
	
	public String addBreaklineInfo(String line,PrintWriter out)
	{
		String ret_line="";
		int showme_tag=0;
		/*
			if(line.indexOf("等候；逗留；耽搁vt. 等待n. 逗留adj. ")!=-1)
			{
			  out.println("<br><br>MEANING 1:["+line+"]<br><br>");
			  showme_tag=1;
			}*/
		int pos1=0,pos2=0;
		pos2=line.indexOf("\n",pos1);
		if(pos2==-1)
		{
			ret_line+=line+"<br>";
		}
		String single_line="";
		while( ( pos2 )!=-1 )
		{
			if(pos1!=0)
			{
				ret_line+="<br>";
			}
			single_line=line.substring(pos1,pos2);
			if(showme_tag==1)
			{
			  out.println(">>>"+single_line+"|||");
			}
			pos1=pos2+1;
			
			ret_line+=single_line;
			
			pos2=line.indexOf("\n",pos1);
		}
		single_line=line.substring(pos1,line.length());
		if(showme_tag==1)
			{
			  out.println("<<<>>>"+single_line+"|||");
			}
		if(single_line.length()>0 && pos1>0)
		{
			ret_line+="<br>"+single_line;
		}
		
		return ret_line;
	}
	
	public int JumpBack2LastLocation(String word_tag_inside_form,String wQuestion_tag_inside_form,String verse_tag_inside_form,PrintWriter out)
	{
		int ret_v=0;
		String tag_place="";
		if( null!=word_tag_inside_form && word_tag_inside_form.equals("null")!=true && word_tag_inside_form.length()!=0 )
		{
			tag_place=word_tag_inside_form;
			ret_v=1;
		}
		else
		{
			if( null!=verse_tag_inside_form && verse_tag_inside_form.equals("null")!=true && verse_tag_inside_form.length()!=0 )
			{
				tag_place=verse_tag_inside_form;
				ret_v=2;
			}
		}
		
		//out.println("<br><a href=\"#"+tag_place+"\">Back jump to the last location where you leave</a><br><br>");
		/*
		if( word_tag_inside_form.equals("null")!=true && word_tag_inside_form.length()!=0 )
		{
			out.println("<br><a href=\"#"+word_tag_inside_form+"\">Back jump to the last location where you leave</a><br><br>");
		}
		if( wQuestion_tag_inside_form.equals("null")!=true && wQuestion_tag_inside_form.length()!=0 )
		{
			out.println("<br><a href=\"#"+wQuestion_tag_inside_form+"\">Back jump to the last location where you leave</a><br><br>");
		}
		else
		{
			if( verse_tag_inside_form.equals("null")!=true && verse_tag_inside_form.length()!=0 )
			{
				out.println("<br><a href=\"#"+verse_tag_inside_form+"\">Back jump to the last location where you leave</a><br><br>");
			}
		}
		*/
		
		return ret_v;
	}
	
	public Vector<String> GetNewsLines(String news_content,Connection conn,PrintWriter out)
	{
		Vector<String> verse_list=new Vector<String>();
		
		int pos1=0,pos2=0;
		String finding="\r";
		pos2=news_content.indexOf(finding,pos1);
		while(pos2!=-1)
		{
			String s_line=news_content.substring(pos1,pos2);
			pos1=pos2+finding.length();
			if(s_line.length()>2)
			{
				verse_list.addElement(s_line);
			}
			//out.println("<br>LEN:"+s_line.length()+"<br>");
			news_content=news_content.substring(pos1,news_content.length());
			pos1=pos2=0;
			pos2=news_content.indexOf(finding,pos1);
		}
		if(news_content.length()>0)
		{
			String s_line=news_content.substring(pos1,news_content.length());
			if(s_line.length()>2)
			{
				verse_list.addElement(s_line);
			}
			//out.println("<br>2===="+s_line+"===<br>");
		}
		
		return verse_list;
	}
	
	public String addNews(String book,String news_content,Connection conn,PrintWriter out)throws SQLException
	{
		Vector<String> verse_list=GetNewsLines(news_content,conn,out);
		
		String sql="SELECT MAX(chapter_no) FROM "+verse_info_tbname;
		Statement stmt = conn.createStatement();
		//out.println(sql);
		ResultSet rs = stmt.executeQuery(sql);

		String sech="";
		boolean tag_have=false;
		while(rs.next())
		{
			tag_have=true;
			sech=rs.getString("Max(chapter_no)");
		}
		//out.println("<br>=====<br>"+"<br>["+sech+"]<br>");
		Integer chapter_no=0;
		if(true==tag_have)
		{
			if(sech!=null)
			{
				chapter_no=Integer.parseInt(sech);
			}
		}
		chapter_no++;
		
		int verse_no=1;
		for(int cn=0;cn<verse_list.size();cn++)
		{
			String verse=verse_list.get(cn);
			String sql_1="INSERT INTO "+verse_info_tbname+" (verse,book,chapter_no,verse_no) VALUES('";
			sql_1+=verse+"','"+book+"',"+chapter_no+","+verse_no+")";
			//out.println("<br>SQL:"+sql_1+"<br>");
			
			stmt = conn.createStatement();
			stmt.execute(sql_1);
			
			verse_no++;
		}
		
		return chapter_no.toString();
	}
	
	public boolean invisibleStr(String line)
	{
		boolean ret_v=false;
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
			ret_v=true;
		}
		
		return ret_v;
	}
	
	public Vector<String> parseNewsPage(String news_url,PrintWriter out)
	{
		Vector<String> line_vec=MixedMultiFilter(news_url, "UTF-8",out);
		//out.println("<br>SIZE="+line_vec.size()+"<br>");
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
		
		return line_vec;
	}
	
	public String addNewsV2(String book,String chapter_no,String news_url,Connection conn,PrintWriter out)
	{
		Vector<String> news_line=parseNewsPage(news_url,out);
		Statement stmt;
		
		int verse_no=1;
		for(int cn=0;cn<news_line.size();cn++)
		{
			String verse=news_line.get(cn);
			verse=deal_escape_character_4_sql(verse);
			String sql_1="INSERT IGNORE INTO "+verse_info_tbname+" (verse,book,chapter_no,verse_no) VALUES('";
			sql_1+=verse+"','"+book+"',"+chapter_no+","+verse_no+")";
			try
			{
				stmt = conn.createStatement();
				stmt.execute(sql_1);
			}
			catch(SQLException e)
			{
				out.println("<br> SQLException:"+e);
				out.println("<br>SQL:"+sql_1+"<br>");
			}
			
			verse_no++;
		}
		
		String ins_sql="INSERT INTO news_url (book,chapter_no,url) VALUES('"+book+"',"+chapter_no+",'"+news_url+"')";
		try
		{
			stmt = conn.createStatement();
			stmt.execute(ins_sql);
		}
		catch(SQLException e)
		{
			out.println("<br> SQLException:"+e);
			out.println("<br>SQL:"+ins_sql+"<br>");
		}
		
		return chapter_no.toString();
	}
	
	public int badAsciiCodePos(String old_str,Vector<Integer> bad_ascii_list,PrintWriter out)
	{
		int pos1=0,pos2=0;
		int pos3=0;
		boolean tag_found_bad=false;
		out.println("<br><br><br>XXXXXXXXX<br><br><br>");
		while(pos1<old_str.length())
		{
			out.println("<br><br><br>["+old_str.charAt(pos1)+"]:["+bad_ascii_list.get(pos3)+"]<br>");
			out.println("["+(int)( old_str.charAt(pos1) )+"]:["+(int)( bad_ascii_list.get(pos3) )+"]<br>");
			if( (int)( old_str.charAt(pos1) ) == (int)( bad_ascii_list.get(pos3) ) )
			{
				pos2=pos1+1;
				pos3++;
				out.println("<br>"+"AAAAA"+"<br>");
				if( (int)( old_str.charAt(pos2) ) == (int)( bad_ascii_list.get(pos3) ) )
				{
					pos2++;
					pos3++;
					if( (int)( old_str.charAt(pos2) ) == (int)( bad_ascii_list.get(pos3) ) )
					{
						pos2++;
						pos3++;
						if( (int)( old_str.charAt(pos2) ) == (int)( bad_ascii_list.get(pos3) ) )
						{
							tag_found_bad=true;
							break;
						}
						else
						{
							pos3=0;
						}
					}
					else
					{
						pos3=0;
					}
				}
				else
				{
					pos3=0;
				}
			}
			pos1++;
		}
		
		if(true==tag_found_bad)
		{
			pos2-=3;
		}
		else
		{
			pos2=-1;
		}
		
		return pos2;
	}
	
	public String delBadAsciiCode_4_1_str(String word,String old_str,PrintWriter out,Vector<Integer> bad_ascii_list)
	{
		String ret_v=old_str;
		boolean tag_bad_str=false;
		
		int BAD_LEN=4;
		int pos1=0,pos2=0;
		pos2=badAsciiCodePos(old_str,bad_ascii_list,out);
		while(pos2!=-1)
		{
			tag_bad_str=true;
			String part1=old_str.substring(0,pos2);
			pos2+=BAD_LEN;
			String part2=old_str.substring(pos2,old_str.length());
			old_str=part1+part2;
			pos1=pos2=0;
			pos2=badAsciiCodePos(old_str,bad_ascii_list,out);
		}
		ret_v=old_str;
		
		if(tag_bad_str==true)
		{
			out.println("<br>bad ascii inside the meaning of word "+word+"<br>");
		}
    
		return ret_v;
	}
	
	public String delBadAsciiCode(String word,String old_str,PrintWriter out)
	{
		String ret_v=old_str;
		
		Vector<Integer> bad_ascii_list=new Vector<Integer>();
		bad_ascii_list.clear();
		bad_ascii_list.addElement(0xF0);
		bad_ascii_list.addElement(0xA0);
		bad_ascii_list.addElement(0xB3);
		bad_ascii_list.addElement(0xA8);
		old_str=delBadAsciiCode_4_1_str(word,old_str,out,bad_ascii_list);
		
		bad_ascii_list.clear();
		bad_ascii_list.addElement(0xF0);
		bad_ascii_list.addElement(0xAA);
		bad_ascii_list.addElement(0xA9);
		bad_ascii_list.addElement(0x95);
		bad_ascii_list.addElement(0xEF);
		bad_ascii_list.addElement(0xBC);
		old_str=delBadAsciiCode_4_1_str(word,old_str,out,bad_ascii_list);
		
		bad_ascii_list.clear();
		bad_ascii_list.addElement(0xF0);
		bad_ascii_list.addElement(0xA0);
		bad_ascii_list.addElement(0xB3);
		bad_ascii_list.addElement(0xA8);
		bad_ascii_list.addElement(0x0A);
		old_str=delBadAsciiCode_4_1_str(word,old_str,out,bad_ascii_list);
		out.println("<br><br><br>DDDDDDD<br><br><br>");
		/*
		bad_ascii_list.clear();
		bad_ascii_list.addElement();
		bad_ascii_list.addElement();
		bad_ascii_list.addElement();
		bad_ascii_list.addElement();
		bad_ascii_list.addElement();
		bad_ascii_list.addElement();
		old_str=delBadAsciiCode_4_1_str(word,old_str,out,bad_ascii_list);
		*/
		
		ret_v=old_str;
		
		return ret_v;
	}
	
	public void AddNewWord(String word,Connection conn,PrintWriter out)
	{
		//out.println("<br>A:<br>");
		String meaning="";
		String ins_sql="";
		try
		{
			String search_sql="SELECT word FROM "+words_meaning_tbname+" WHERE word='"+word+"'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(search_sql);
			boolean found=false;
			while(rs.next())
			{
				found=true;
			}
			if(found==false)
			{
				Vector<String> meaning_lines=getTransInfo(word);
				//out.println("<br>hi 2 xxx 0001:"+word+"<br>");
				if(meaning_lines.size()<1)
				{
					return;
				}
				meaning="";
				int cn=0;
				//out.println("<br>hi 2 xxx 0002:"+word+"<br>");
				meaning+=meaning_lines.get(cn);
				//out.println("<br>B:<br>");
				cn++;
				for(;cn<meaning_lines.size();cn++)
				{
					meaning+="\n";
					meaning+=meaning_lines.get(cn);
				}
				//meaning=delBadAsciiCode(word,meaning,out);
				meaning=deal_escape_character_4_sql(meaning);
				ins_sql="INSERT INTO "+words_meaning_tbname+" (word,meaning,question_entry) VALUES('"+word+"','"+meaning+"',NULL)";
				//out.println("<br>meaing:"+ins_sql+"<br>");
				stmt = conn.createStatement();
				stmt.executeUpdate(ins_sql);
			}
		}
		catch(SQLException se)
		{
			out.println("<br>SQLException se<br>"+se+"<br>"+word+"<br>"+ins_sql+"<br>");
			se.printStackTrace();
		}
	}
	
	public String js_call_java_test()
	{
		return "<br><br><br>HI js_call_java_test<br><br><br>";
	}
	
	public void print_style(PrintWriter out)
	{
		out.println("<script language=\"javascript\">");

out.println("function tablecollapse3(table_id)");  
out.println("{");
out.println("  var t=document.getElementsByTagName('table');  ");
out.println("  var checktest= new RegExp(\"(^|\\s)\" + collapseClass + \"(\\s|$)\");  ");
out.println("  for (var i=0;i<t.length;i++)  ");
out.println("  {");
out.println("    if(!checktest.test(t[i].className)){continue;}    ");
out.println("    var tb_id=t[i]['id'];");
out.println("    if(tb_id!=table_id)");
out.println("    {");
out.println("      var tb=t[i].getElementsByTagName('tbody');  ");
out.println("      for(var j=0;j<tb.length;j++)  ");
out.println("      {  ");
out.println("	tb[j].style.display='none';  ");
out.println("      }");
out.println("    }");
out.println("    else");
out.println("    {");
out.println("      var tb=t[i].getElementsByTagName('tbody');  ");
out.println("      for(var j=0;j<tb.length;j++)  ");
out.println("      {");
out.println("	tb[j].style.display='';  ");
out.println("      }    ");
out.println("    }");
out.println("  }");  
out.println("} ");

out.println("function tablecollapse2(tag_open_true)");
out.println("{");
out.println("	var collapseClass='footcollapse'; ");
out.println("	var t=document.getElementsByTagName('table');");
	
out.println("	var checktest= new RegExp(\"(^|\\s)\" + collapseClass + \"(\\s|$)\");");
	
out.println("	console.log('一共 '+t.length);");
out.println("	button_obj=document.getElementById(\"button_1\");");
out.println("	for (var i=0;i<t.length;i++)  ");
out.println("	{  ");
out.println("		if(!checktest.test(t[i].className)){continue;}   "); 
out.println("		var tb_id=t[i]['id'];");
out.println("		console.log(' ');");
out.println("		console.log('正在运行 '+i);");
		 
out.println("		var tb=t[i].getElementsByTagName('tbody');");
out.println("		for(var j=0;j<tb.length;j++)  ");
out.println("		{  ");
out.println("			if(button_obj.value==\"collapse\")");
out.println("			{");
out.println("				if(tb[j].style.display!='none')");
out.println("				{");
out.println("					tb[j].style.display='none';");
out.println("					console.log(\"原来是 \"+tb[j].style.display);");
out.println("				}");
				
out.println("				console.log(\"设置成 \"+tb[j].style.display);");
out.println("			}");
out.println("			else if(button_obj.value==\"spread\")");
out.println("			{");
out.println("				if(tb[j].style.display=='none')");
out.println("				{");
out.println("					tb[j].style.display='';");
out.println("					console.log(\"原来是 \"+tb[j].style.display);");
out.println("				}");
				
out.println("				console.log(\"设置成 \"+tb[j].style.display);");
out.println("			}");
out.println("			console.log(\"OK\");");
out.println("		}");
out.println("	}");
out.println("	if(button_obj.value==\"collapse\")");
out.println("	{");
out.println("		button_obj.value=\"spread\";");
out.println("	}");
out.println("	else if(button_obj.value==\"spread\")");
out.println("	{");
out.println("		button_obj.value=\"collapse\"");
out.println("	}");
out.println("}");

out.println("function tablecollapse(table_id,tag_operate_all,tag_open_true)  ");
out.println("{   "); 
out.println("var collapseClass='footcollapse';  ");
out.println("var collapsePic='http://webdesign.chinaitlab.com/UploadFiles_8014/200706/20070620173213376.gif';  ");
out.println("var expandPic='http://webdesign.chinaitlab.com/UploadFiles_8014/200706/20070620173213394.gif';  ");
out.println("var initialCollapse=true; ");
out.println("var t=document.getElementsByTagName('table');  ");
out.println("var checktest= new RegExp(\"(^|\\s)\" + collapseClass + \"(\\s|$)\");  ");
out.println("for (var i=0;i<t.length;i++)  ");
out.println("{  ");
out.println("   if(!checktest.test(t[i].className)){continue;}   "); 
out.println("   var tb_id=t[i]['id'];");
out.println("   t[i].getElementsByTagName('tfoot')[0].onclick=function()  ");
out.println("   {  ");
out.println("    var tb=this.parentNode.getElementsByTagName('tbody'); "); 
out.println("    for(var i=0;i<tb.length;i++)  ");
out.println("    {  ");
out.println("     tb[i].style.display=tb[i].style.display=='none'?'':'none';  ");
out.println("    }");
out.println("   }  ");
out.println("   if(tag_operate_all==1)");
out.println("   {");
out.println("		if(tag_open_true==1)");
out.println("		{");
out.println("			var tb=t[i].getElementsByTagName('tbody');  ");
out.println("			for(var j=0;j<tb.length;j++)  ");
out.println("			{  ");
out.println("				tb[j].style.display='run-in'; ");
out.println("var newa=document.createElement('a');  ");
out.println("   newa.href='#';  ");
out.println("   newa.onclick=function(){return false;}	");
out.println("			}  ");  
out.println("		}");
out.println("		else if(tag_open_true==0)");
out.println("		{");
out.println("			var tb=t[i].getElementsByTagName('tbody');  ");
out.println("			for(var j=0;j<tb.length;j++)  ");
out.println("			{  ");
out.println("				tb[j].style.display='none';  ");
out.println("			}     ");
out.println("		}");
out.println("   }");
out.println("   else");
out.println("   {");
out.println("		if(tb_id!=table_id)");
out.println("		{");
			
out.println("				var tb=t[i].getElementsByTagName('tbody'); "); 
out.println("				for(var j=0;j<tb.length;j++)  ");
out.println("				{  ");
out.println("					tb[j].style.display='none';  ");
out.println("				}  ");
out.println("		}");
out.println("   }");
out.println("   var newa=document.createElement('a');  ");
out.println("   newa.href='#';  ");
out.println("   newa.onclick=function(){return false;}  ");
out.println("   var newimg=document.createElement('img');  ");
out.println("   newimg.src=initialCollapse?expandPic:collapsePic;  ");
out.println("   var tf=t[i].getElementsByTagName('tfoot')[0];  ");
out.println("   var lt=tf.getElementsByTagName('td')[tf.getElementsByTagName('td').length-1];  ");
out.println("   newa.appendChild(newimg);  ");
out.println("   lt.insertBefore(newa,lt.firstChild);  ");
out.println("}  ");  
out.println("}  ");

out.println("function goto_vserse()");
out.println("{");
out.println("  var text_ojb1=document.getElementById('goto_verse_no');");
out.println("  var verse_no=text_ojb1.value;");
out.println("  var table_tag='table_no_'+verse_no;");
out.println("  open_last(table_tag,'');");
out.println("}");

out.println("function open_last(table_id,anchor)");
out.println("{  ");
out.println("  location.hash='';//!!!this is very important, if omitted, 2nd location.hash will be useless");
out.println("  var collapseClass='footcollapse';");
out.println("  var t=document.getElementsByTagName('table');  ");
out.println("var checktest= new RegExp(\"(^|\\s)\" + collapseClass + \"(\\s|$)\");  ");
out.println("  for (var i=0;i<t.length;i++)  ");
out.println("  {  ");
out.println("    if(!checktest.test(t[i].className)){continue;}    ");
out.println("    var tb_id=t[i]['id'];");
out.println("    if(tb_id==table_id)");
out.println("    {");
out.println("      var tb=t[i].getElementsByTagName('tbody');  ");
out.println("      for(var j=0;j<tb.length;j++)  ");
out.println("      {");
out.println("	tb[j].style.display='';  ");
out.println("      }");
out.println("      location.hash='#'+table_id;");
out.println("     if(anchor!='')");
out.println("           {");
out.println("     	location.hash='#'+anchor;");
out.println("           }");
out.println("      break;");
out.println("    }");
out.println("  }");
//out.println("alert('hi '+table_id+'--'+anchor+' end')");
out.println("} ");

out.println("function js_call_java_test_1()");
out.println("{ ");
out.println("var b='apple';");
out.println("alert(b);");
out.println("var a = \"<%=js_call_java_test()%>\";");
out.println("alert(a);");
out.println("} ");

out.println("function callback(str)");
out.println("  {");
out.println("   alert(str);");
out.println("  }");
out.println("  function test_A()");
out.println("  {");
out.println("   Hello.sayHelloTo(\"zhangsan\",callback);");
out.println("  }");
  
out.println("</script>");
out.println("<style type=\"text/css\">");
out.println("body{");
out.println("font-family:Arial,Sans-Serif;");
out.println("font-size:90%;");
out.println("background:#cc9;");
out.println("}");
out.println("#boundary{");
out.println("background:#f8f8f8;");
out.println("padding:2em;");
//out.println("width:40em;");
out.println("}");
out.println("h1{");
out.println("font-family:\"Trebuchet MS\",Sans-serif;");
out.println("text-transform:uppercase;");
out.println("color:#696;");
out.println("font-size:120%;");
out.println("}");
out.println("table.footcollapse{");
//out.println("width:30em;");
out.println("}");

out.println("table.footcollapse th{");
out.println("text-align:left;");
out.println("}");
out.println("table.footcollapse,table.footcollapse th,table.footcollapse th");
out.println("{");
out.println("border:none;");
out.println("border-collapse:collapse; ");
out.println("}");
out.println("table.footcollapse thead th");
out.println("{");
//out.println("width:10em;");
out.println("border-style:solid;");
out.println("border-width:1px;");
out.println("border-color:#cff #69c #69c #cff;");
out.println("background:#9cf;");
out.println("padding:2px 10px;");
out.println("}");
out.println("table.footcollapse tfoot th,");
out.println("table.footcollapse tfoot td");
out.println("{");
out.println("border-style:solid;");
out.println("border-width:1px;");
out.println("border-color:#9cf #369 #369 #9cf;");
out.println("background:#69c;");
out.println("padding:2px 10px;");
out.println("}");
out.println("table.footcollapse tbody{");
out.println("background:#ddd;");
out.println("}");
out.println("table.footcollapse tbody td{");
out.println("padding:5px 10px;");
out.println("border:1px solid #999;");
out.println("}");
out.println("table.footcollapse tbody th{");
out.println("padding:2px 10px;");
out.println("border:1px solid #999;");
out.println("border-left:none;");
out.println("}");
out.println("table.footcollapse tbody tr.odd{");
out.println("background:#ccc;");
out.println("}");
out.println("table.footcollapse tfoot td img{");
out.println("border:none;");
out.println("vertical-align:bottom;");
out.println("padding-left:10px;");
out.println("float:right;");
out.println("}");

out.println("*{margin:0; padding:0}");
out.println("a{color:#528036; text-decoration:none}");
out.println("a:hover{color:#000; text-decoration:none}");
out.println("ul{list-style:none}");
out.println("h1{font-size:16px}");
out.println(".clear{clear:both}");
out.println("#site_nav{position:fixed; width:160px; padding:6px 10px; height:100%; background:#ffc; overflow-y:auto; OVERFLOW: auto;}");
out.println("#site_nav ul{margin:2px}");
out.println("#site_nav ul li{line-height:22px}");
out.println("#site_nav ul li ul{padding-left:12px}");
out.println("#content{padding: 10px 10px 10px 190px;}");
out.println("#content ul{margin:10px auto}");
/*
		out.println("body {background-color: #3399FF}");
		out.println("ve_s {color: black}");
		out.println("mean_s {color: #00FF33}");
		out.println("mean_s {font-weight: bold}");
		out.println("word_s {color: yellow}");*/
		out.println("cc_s {font-weight: bold}");
		out.println("cc_s {color: red}");
		//out.println("cc_s {font-size: 20px}");
		out.println("oc_s {color: white}");
		out.println("cb_s {color: red}");
		out.println("ob_s {color: black}");
		
out.println("</style>");
	}
	
  // 脙聝脗楼脙垄脗聜脗卢脙聜脗聞脙聝脗搂脙聜脗聬脙聜脗聠 GET 脙聝脜聽脙聜脗聳脙聜脗鹿脙聝脜聽脙聜脗鲁脙聜脗聲脙聝脜隆脙聜脗炉脙聜脗路脙聝脜聽脙聜脗卤脙聜脗聜脙聝脗搂脙聜脗職脙聜脗聞脙聝脜聽脙聜脗聳脙聜脗鹿脙聝脜聽脙聜脗鲁脙聜脗聲
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // 脙聝脜隆脙聜脗庐脙聟脜鸥脙聝脗搂脙聟脗聯脙聜脗庐脙聝脗楼脙聜脗聯脙聜脗聧脙聝脗楼脙聜脗潞脙聜脗聰脙聝脗楼脙聜脗聠脙聜脗聟脙聝脗楼脙聜脗庐脙聜脗鹿脙聝脗搂脙聜脗卤脙聜脗禄脙聝脗楼脙聜脗聻脙聜脗聥
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	
	Statement stmt=null;
	Connection conn=null;
	
	String reading_type=request.getParameter("reading_type");
	SetTableInfo(reading_type,out);
	
	try{
	    Class.forName(JDBC_DRIVER);
	    conn = DriverManager.getConnection(DB_URL,USER,PASS);
        }
        catch(SQLException se){
	    out.println("SQLException se<br>");
	    se.printStackTrace();
	}
	catch(Exception e){
	    out.println("Exception e+["+e+"]<br>");
	    e.printStackTrace();
	}
	  String title = "Servlet Experiment Page";
      String docType =
      "<!doctype html public \"-//w3c//dtd html 4.0 " +
      "transitional//en\">\n";
      out.println(docType +
                "<html>\n" +
                "<head><title>" + title + "</title>");
                print_style(out);
		out.println("</head>\n");
			
	out.println("<div id=\"site_nav\">");
out.println("    <h1>Function 1</h1>");
out.println("    <ul>");
out.println("      <li>Position Function");
out.println("          <ul>");
out.println("            <li><a href=\"#\"><input name=\"ok\" type=\"button\" id=\"button_1\" value=\"collapse\" onClick=\"tablecollapse2(0);\"></a></li>");
out.println("            <li><input name=\"ok\" type=\"button\" id=\"button_2\" value=\"Back to last position\"></li>");
out.println("            <li>To verse<input name='jump_verse' size='3' type='text' id='goto_verse_no' value='1'>&nbsp;<input name='jump_v' type='button' id='button_3' value='Go'></li>");
out.println("         </ul>");
out.println("      </li>");
out.println("      <li>Books Info</li>");
//out.println("          <ul>");
out.println("	      <li>Old Testment</a></li>");
//out.println("		<ul>");
	  
	  Vector<String> books_name=new Vector<String>();
	  try{
	      stmt = conn.createStatement();
	      
	      //sql = "SELECT question_no,question_content,answer FROM "+question_answer_tbname;
	      String sql_book="SELECT DISTINCT book FROM "+verse_info_tbname;
	      ResultSet rs_book = stmt.executeQuery(sql_book);
	      while(rs_book.next()){
		  String book = rs_book.getString("book");
		  books_name.addElement(book);
	      }
	  }
	  catch(SQLException se){
	      out.println("SQLException se<br>");
	      se.printStackTrace();
	  }
	  int book_cnt=0;
	  for(;book_cnt<books_name.size();book_cnt++)
	  {
	      String book=(String)books_name.get(book_cnt);
	      String chapter_no = "1";
	      String form_data=FormInfo4OutputWordsInsideChapter(book,chapter_no,"null","null","null","null","null","null","null",reading_type);
	      out.println("<li><a href=\""+form_data+"\">"+book+"</a></li>");
	      if(book.equals("Malachi")==true)
	      {
		  break;
	      }
	  }
            /*
out.println("		  <li><a href='#'>1</a>");
out.println("		  <li><a href='#'>1</a>");
out.println("		  <li><a href='#'>1</a>");
out.println("		  <li><a href='#'>1</a>");*/
//out.println("		</ul>");
//out.println("	      </li>");
out.println("	      <li>New Testment</li>");
//out.println("		<ul>");
	  for(;book_cnt<books_name.size();book_cnt++)
	  {
	      String book=(String)books_name.get(book_cnt);
	      String chapter_no = "1";
	      String form_data=FormInfo4OutputWordsInsideChapter(book,chapter_no,"null","null","null","null","null","null","null",reading_type);
	      out.println("<li><a href=\""+form_data+"\">"+book+"</a></li>");
	  }
/*
out.println("		  <li><a href='#'>1</a>");
out.println("		  <li><a href='#'>1</a>");
out.println("		  <li><a href='#'>1</a>");
out.println("		  <li><a href='#'>1</a>");*/
//out.println("		</ul>");
//out.println("	      </li>");
//out.println("          </ul>");
//out.println("     </li>");
out.println("    </ul>");
out.println("</div>");

out.println("<div id=\"content\">");

                out.println("<body bgcolor=\"#f0f0f0\">\n" +
                "<h1 align=\"center\">" + title + "</h1>\n" +
                "<br><td><a href=\"ShowWordsInsideChapter\">Return back to Contents</a></td>"+
                "<ul>\n" +
                "  <li><b>book name</b>:"
                + request.getParameter("book") + "\n" +
                "  <li><b>chapter number</b>:"
                + request.getParameter("chapter_no") + "\n" +
                "</ul>\n" +
                "</body>");
	//out.println("==A<br>");
	String book=request.getParameter("book");
	String chapter_no=request.getParameter("chapter_no");
	String verse_no=request.getParameter("verse_no");
	String mo_word=request.getParameter("mo_word");
	String mo_meaning=request.getParameter("mo_meaning");
	String spe_content=request.getParameter("spe_content");
	String phrase=request.getParameter("phrase");
	String phrase_meaning=request.getParameter("phrase_meaning");
	
	String news_url=request.getParameter("news_url");
	
	Vector<String> books_list=null;
	
	if(null!=mo_meaning)
	{
		mo_meaning=deal_escape_character_4_sql(mo_meaning);
		mo_meaning=new String(mo_meaning.getBytes("ISO-8859-1"),"UTF-8");
	}
	String question_content=request.getParameter("question_content");
	if(null!=question_content)
	{
		question_content=deal_escape_character_4_sql(question_content);
		question_content=new String(question_content.getBytes("ISO-8859-1"),"UTF-8");
	}
	String answer_content=request.getParameter("answer_content");
	if(null!=answer_content)
	{
		answer_content=deal_escape_character_4_sql(answer_content);
		answer_content=new String(answer_content.getBytes("ISO-8859-1"),"UTF-8");
	}
	if(null!=spe_content)
	{
		spe_content=deal_escape_character_4_sql(spe_content);
		spe_content=new String(spe_content.getBytes("ISO-8859-1"),"UTF-8");
	}
	
	if(null!=phrase)
	{
		phrase=deal_escape_character_4_sql(phrase);
		phrase=new String(phrase.getBytes("ISO-8859-1"),"UTF-8");
	}
	
	if(null!=phrase_meaning)
	{
		phrase_meaning=deal_escape_character_4_sql(phrase_meaning);
		phrase_meaning=new String(phrase_meaning.getBytes("ISO-8859-1"),"UTF-8");
	}
	
	if(null!=news_url)
	{
		//out.println("<br>APP 1:["+news_content+"]<br>");
		//news_content=deal_escape_character_4_sql(news_content);
		//out.println("<br>APP 2:["+news_content+"]<br>");
		news_url=new String(news_url.getBytes("ISO-8859-1"),"UTF-8");
		//out.println("<br>APP 3:["+news_content+"]<br>");
	}
	
	String verse_tag_inside_form=request.getParameter("verse_tag");
	String word_tag_inside_form=request.getParameter("word_tag");
	String wQuestion_tag_inside_form=request.getParameter("wQuestion_tag");
	
	//out.println("==BwQuestion_tag_inside_form"+wQuestion_tag_inside_form+"<br>");
	Vector<Integer> chapters_list=null;
	Map<String,String> word_just1sttime_tag_set=new HashMap<String,String>();
	
	words_set.clear();
	place2words_map.clear();
	word2verseno.clear();
	verse_info.clear();
	verseno2tableno.clear();
	wordno2tableno.clear();
	table_cnt=0;
	word_cnt=0;
	verse_cnt=0;
	wQuestion_cnt=0;
	int word_verse_bkinfo=0;
	
	//out.println("==B<br>");
	String bkplace_table_tag="";

	//out.println("<br>table 1:"+verse_info_tbname+"<br>");
	
	try{
      //out.println("here in try<br>");
         // 脙聝脜聽脙聜脗鲁脙聟脗隆脙聝脗楼脙聜脗聠脙聜脗聦 JDBC 脙聝脗漏脙聜脗漏脙聜脗卤脙聝脗楼脙聜脗聤脙聟脗隆脙聝脗楼脙聜脗聶脙聟脗隆
         //Class.forName(JDBC_DRIVER);
//out.println("After Class.forName(JDBC_DRIVER)<br>");
         // 脙聝脜聽脙聜脗聣脙聜脗聯脙聝脗楼脙聟脗聮脙聜脗聙脙聝芒聜卢脙聟脜啪脙聜脗聙脙聝芒聜卢脙聟脜啪脙聜脗陋脙聝脜隆脙聜脗驴脙聜脗聻脙聝脜聽脙聜脗聨脙聜脗楼
         //conn = DriverManager.getConnection(DB_URL,USER,PASS);
//out.println("After DriverManager.getConnection<br>");
         // 脙聝脜聽脙聜脗聣脙聜脗搂脙聝脜隆脙聜脗隆脙聜脗聦 SQL 脙聝脜聽脙聜脗聼脙聜脗楼脙聝脜隆脙聜脗炉脙聜脗垄
         
	//out.println("mo_word=["+mo_word+"]");
	//out.println("mo_meaning=["+mo_meaning+"]");
	
	boolean tag_omit_question_insert=false;
	if( null!=mo_word && null!=mo_meaning && mo_word.equals("null")!=true && mo_meaning.equals("null")!=true )
        {
		stmt = conn.createStatement();
		String sql="UPDATE "+words_meaning_tbname+" SET meaning='"+mo_meaning+"' WHERE word='"+mo_word+"'";
		//out.println(sql);
		stmt.execute(sql);
		//out.println("==after UPDATE WORD MEANING<br>");
		
		if( question_content.equals("null")!=true && question_content.length()>0 )
		{
			addOrUpdateWordQuestion(book,chapter_no,verse_no,mo_word,question_content,answer_content,conn,out);
			tag_omit_question_insert=true;
			//out.println("==after UPDATE WORD QUESTION<br>");
		}
	}
	
	if( null!=phrase && null!=phrase_meaning && phrase.equals("null")!=true && phrase_meaning.equals("null")!=true )
	{
		//out.println("<br>Phrase="+phrase+"<br>Phrase meaning="+phrase_meaning+"<br>");
		addOrUpdatePhrase(phrase,phrase_meaning,conn,out);
	}
	
	//out.println("A ["+question_content+"]<br>");
	//out.println("==C<br>");
	int max_question_no=0;
        if( null!=question_content && question_content.equals("null")!=true && question_content.length()>0 )
        {
		//out.println("SQL["+max_quesiton_no_sql+"]<br>");
		if(false==tag_omit_question_insert)
		{
			addOrUpdateQuestion(book,chapter_no,verse_no,question_content,answer_content,max_question_no,conn,out);
			//out.println("==after UPDATE Question<br>");
		}
	}
	if(null!=spe_content && spe_content.equals("null")!=true && spe_content.length()>0 )
	{
		addOrUpdateSpecification(book,chapter_no,verse_no,spe_content,conn,out);
	}
	
	if(null!=news_url && news_url.equals("null")!=true && news_url.length()>0 )
	{
		//do sth here! e.g. format news into database;
		chapter_no=addNewsV2(book,chapter_no,news_url,conn,out);
	}
	
	chapters_list=getChapterList4thisBook(out,conn,stmt,book);
	
        //out.println("==D<br>");
         
         String sql="SELECT chapter_no,verse_no,verse FROM "+verse_info_tbname+" WHERE book='"+book+"' AND chapter_no="+chapter_no;
        //out.println("<br>~~~~~"+sql+"<br>");
        stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql);

         // 脙聝芒聜卢脙聜脗禄脙聜脗聨脙聝脗搂脙聜脗禄脙聜脗聯脙聝脜聽脙聜脗聻脙聜脗聹脙聝脗漏脙聜脗聸脙聜脗聠脙聝芒聜卢脙聟脜啪脙聜脗颅脙聝脜聽脙聜脗聫脙聜脗聬脙聝脗楼脙聜脗聫脙聜脗聳脙聝脜聽脙聜脗聲脙聜脗掳脙聝脜聽脙聜脗聧脙聜脗庐
         while(rs.next()){
            // 脙聝脜聽脙聜脗聽脙聜脗鹿脙聝脜聽脙聜脗聧脙聜脗庐脙聝脗楼脙聜脗聢脙聜脗聴脙聝脗楼脙聜脗聬脙聜脗聧脙聝脗搂脙聜脗搂脙聜脗掳脙聝脜聽脙聜脗拢脙聜脗聙脙聝脗搂脙聟脜聯脙聜脗垄
            String verse  = rs.getString("verse");
            int verse_no_i=rs.getInt("verse_no");
            int chapter_no_i=rs.getInt("chapter_no");
            //out.println(verse_no_i+" ");
            addVerseWordPlace(verse,verse_no_i,out);
         }
         //out.println("<br>");

         books_list=AddBooksInBible();
         //showBooksInBible(out,books_list,book);
	//showChaptersInBook(out,chapter_no,chapters_list,book,mo_word,mo_meaning,question_content,answer_content);
	showChaptersInBook(out,chapter_no,chapters_list,book,reading_type);
	
	word_verse_bkinfo=JumpBack2LastLocation(word_tag_inside_form,wQuestion_tag_inside_form,verse_tag_inside_form,out);
	
	//out.println("<br><table border=\"1\" width=\"1000\" style=\"word-break:break-word\">");
	//out.println("<br>AAAAAAAAAAAAAA<br>");
	/*
	Set<String> printedWordSet=new HashSet<String>();
	printedWordSet.clear();
	*/

	out.println("<script>window.onload=tablecollapse('table_1',0,1);</script>");
	//out.println("<br>hi 1: "+verse_info.size()+"<br>");
	for (Map.Entry<Integer,String> entry_1 : verse_info.entrySet())
	{
		String table_tag=produceTableTagName("table_no");
		out.println("<table border=\"1\" width=\"1000\" style=\"word-break:break-word\" class=\"footcollapse\" id=\""+table_tag+"\">");
		int v_no=entry_1.getKey();
		String verse=verse_info.get(v_no);
	//out.println("<br>hi 2-->:"+v_no+"<br>");	
		String verse_tag=produceVerseTagName("verse_no");
		verseno2tableno.put(verse_tag,table_tag);
		out.println("<thead><tr><th width=\"50\">");
		//out.println("<a name=\""+verse_tag+"\">"+chapter_no+":"+v_no+"</a>&nbsp;<a href=\"Question?book="+book+"&chapter_no="+chapter_no+"&verse_no="+v_no+"&verse_tag="+verse_tag+"\">Question</a>");
		out.println("<a name=\""+verse_tag+"\" href=\"Question?book="+book+"&reading_type="+reading_type+"&chapter_no="+chapter_no+"&verse_no="+v_no+"&verse_tag="+verse_tag+"\">"+chapter_no+":"+v_no+"</a>");
		//out.println("KKKK1111<button ononclick=\"js_call_java_test_1()\">Click Me!</button>PPPP1111");
		///
		String js_sample="<script type=\"text/javascript\" src=\"dwr/engine.js\"></script>";
		js_sample+="<script type=\"text/javascript\" src=\"dwr/util.js\"></script>";
		js_sample+="<script type=\"text/javascript\" src=\"dwr/interface/Hello.js\"></script>";
		//String clk_action="js_call_java_test()";
		String clk_action="test_A()";
		String place2find="<button onclick=\""+clk_action+"\">"+"word"+"</button>";
		out.println(place2find);
		////
		out.println("</th><th colspan=4 align=\"justify\">");
		out.println("<ve_s>"+verse+"</ve_s>");
		out.println("</th></tr></thead>");
		out.println("<tfoot><tr><th></th><td colspan=\"4\" align='center'>click here to collapse or spread</td></tr></tfoot>");
		out.println("<tbody>");
		
		Map<Integer,String> place_allwords=allWordsInsideVerse(verse,v_no,out);
		
		Map<Integer,String> time1st_words_inside_this_verse=null;
		if( true==place2words_map.containsKey(v_no) )
		{
			time1st_words_inside_this_verse=place2words_map.get(v_no);
		}
		//out.println("<br>hi 2 xxx 1:"+v_no+"<br>");
		//////////////////
		Map<Integer,Integer> all0_1st1_place_info=which_set_choose_word(time1st_words_inside_this_verse,place_allwords,out);
			
		for (Map.Entry<Integer,Integer> entry_word_1st : all0_1st1_place_info.entrySet())
		{
			Integer word_pos=entry_word_1st.getKey();
			Integer zero_one=all0_1st1_place_info.get(word_pos);
			
			String word=null;
			String meaning="";
			
			String word_tag=produceWordTagName("word_no");
			wordno2tableno.put(word_tag,table_tag);
			
			if( null!=time1st_words_inside_this_verse )
			{
				if( 1==zero_one )
				{
					word=time1st_words_inside_this_verse.get(word_pos);
					word_just1sttime_tag_set.put(word,word_tag);
				}
				else if( 0==zero_one )
				{
					word=place_allwords.get(word_pos);
				}
			}
			else
			{
				word=place_allwords.get(word_pos);
			}
			
			if( reading_type.equals("literature")==true || reading_type.equals("news")==true )
			{
				AddNewWord(word,conn,out);
			}
			
			out.println("<tr><td></td><td width=\"40\">");
			out.println(word_cnt);
			out.println("</td><td width=\"120\">");
			//out.println("<a name=\""+word_tag+"\" href=\"WordModify?book="+book+"&chapter_no="+chapter_no+"&word="+word+"&book="+book+"&chapter_no="+chapter_no+"&word_tag="+word_tag+"\">"+word+"</a>");
			out.println("<a name=\""+word_tag+"\" href=\"WordModify?book="+book+"&reading_type="+reading_type+"&chapter_no="+chapter_no+"&verse_no="+v_no+"&word="+word+"&word_tag="+word_tag+"\"><word_s>"+word+"</word_s></a>");
			//<a href=\"WordModify?book="+book+"&chapter_no="+chapter_no+"&word="+word+"&book="+book+"&chapter_no="+chapter_no+"\">modify</a>
			out.println("</td><td>");
			
			if( null!=time1st_words_inside_this_verse )
			{
				if( 1==zero_one )
				{
					stmt = conn.createStatement();
					sql="SELECT meaning FROM "+words_meaning_tbname+" WHERE word='"+word+"'";
					//out.println("<br><br>"+sql+"<br><br>");
					rs = stmt.executeQuery(sql);
					meaning="";
					while(rs.next())
					{
						meaning+=rs.getString("meaning");
					}
					//out.println(meaning);this should not been output
				}
				else
				{
					printWhere2find(word,word_just1sttime_tag_set,wordno2tableno,out);
				}
			}
			else
			{
				printWhere2find(word,word_just1sttime_tag_set,wordno2tableno,out);
			}
			//out.println("<br><br>MEANING 1:["+meaning+"]<br><br>");
			
			meaning=addBreaklineInfo(meaning,out);
			
			//out.println("<br><br>MEANING 2:["+meaning+"]<br><br>");
			out.println("<mean_s>"+meaning+"</mean_s>");
			//out.println("</td><td><a href=\"WordModify?book="+book+"&chapter_no="+chapter_no+"&word="+word+"&book="+book+"&chapter_no="+chapter_no+"\">modify</a></td></tr>");
			String wQuestion_tag=produceWordQuestionTagName("wQuestion");
			out.println("</td><td width=\"30\"><a name=\""+wQuestion_tag+"\" href=\"Phrase?book="+book+"&reading_type="+reading_type+"&chapter_no="+chapter_no+"&verse_no="+v_no+"&word="+word+"&word_tag="+word_tag+"\">PH</a></td></tr>");
		}
		out.println("</tbody></table><br>");
		//out.println("<br>hi 2<<==:"+v_no+"<br>");
	}
	
         // 脙聝脜聽脙聟脜啪脙聜脗聟脙聝脗搂脙聜脗聬脙聜脗聠脙聝脗搂脙聜脗聨脙聜脗炉脙聝脗楼脙聜脗垄脙聜脗聝
         rs.close();
         stmt.close();
         conn.close();
      }catch(SQLException se){
         // 脙聝脗楼脙垄脗聜脗卢脙聜脗聞脙聝脗搂脙聜脗聬脙聜脗聠 JDBC 脙聝脗漏脙聜脗聰脙聜脗聶脙聝脜隆脙聜脗炉脙聜脗炉
         out.println("SQLException se<br>");
         se.printStackTrace();
      }catch(Exception e){
         // 脙聝脗楼脙垄脗聜脗卢脙聜脗聞脙聝脗搂脙聜脗聬脙聜脗聠 Class.forName 脙聝脗漏脙聜脗聰脙聜脗聶脙聝脜隆脙聜脗炉脙聜脗炉
         out.println("Exception e+["+e+"]<br>");
         e.printStackTrace();
      }finally{
         // 脙聝脜聽脙聜脗聹脙聜脗聙脙聝脗楼脙聜脗聬脙聜脗聨脙聝脜聽脙聜脗聵脙聜脗炉脙聝脗搂脙聜脗聰脙聟脗隆脙聝芒聜卢脙聜脗潞脙聜脗聨脙聝脗楼脙聜脗聟脙聜脗鲁脙聝脗漏脙聜脗聴脙聜脗颅脙聝脜隆脙聜脗碌脙聜脗聞脙聝脜聽脙聜脗潞脙聜脗聬脙聝脗搂脙聜脗職脙聜脗聞脙聝脗楼脙聜脗聺脙聜脗聴
         try{
            if(stmt!=null)
               stmt.close();
         }catch(SQLException se2){
         }// 脙聝脜聽脙聜脗聢脙聜脗聭脙聝芒聜卢脙聜脗禄脙聜脗卢脙聝芒聜卢脙聟脜啪脙聜脗聧脙聝脜隆脙聜脗聝脙聟脗聯脙聝脗楼脙聜脗聛脙聜脗職脙聝芒聜卢脙聜脗禄脙聜脗聙脙聝芒聜卢脙聜脗鹿脙聜脗聢
         try{
            if(conn!=null)
            conn.close();
         }catch(SQLException se){
            se.printStackTrace();
         }//end finally try
      } //end try
      
      //showBooksInBible(out,books_list,book);
      //showChaptersInBook(out,chapter_no,chapters_list,book,mo_word,mo_meaning,question_content,answer_content);
      showChaptersInBook(out,chapter_no,chapters_list,book,reading_type);
      
      //JumpBack2LastLocation(word_tag_inside_form,wQuestion_tag_inside_form,verse_tag_inside_form,out);
      
      out.println("<br><br><table><tr><td><a href=\"ShowWordsInsideChapter\">Return back to Contents</a></table></td></tr>");
      out.println("</div>");
      
      out.println("<script>window.onload=tablecollapse('table_1',0,1);");
	
	String anchor="";
	if(word_verse_bkinfo==1)
	{
		bkplace_table_tag=wordno2tableno.get(word_tag_inside_form);
		anchor=word_tag_inside_form;
	}
	else if(word_verse_bkinfo==2)
	{
		bkplace_table_tag=verseno2tableno.get(verse_tag_inside_form);
		anchor=verse_tag_inside_form;
	}
	
	out.println("var button_Obj=document.getElementById(\"button_2\"); ");
	out.println("button_Obj.onclick=function(){");
	String func_str="open_last('"+bkplace_table_tag+"','"+anchor+"');";
	out.println(func_str);
	out.println("}; ");
	
	out.println("button_Obj=document.getElementById('button_3');");
	out.println("  button_Obj.onclick=function(){");
	out.println("  goto_vserse();");
	out.println("};");

	out.println("</script>");
	
      out.println("</body></html>");
  }
  // 脙聝脗楼脙垄脗聜脗卢脙聜脗聞脙聝脗搂脙聜脗聬脙聜脗聠 POST 脙聝脜聽脙聜脗聳脙聜脗鹿脙聝脜聽脙聜脗鲁脙聜脗聲脙聝脜隆脙聜脗炉脙聜脗路脙聝脜聽脙聜脗卤脙聜脗聜脙聝脗搂脙聜脗職脙聜脗聞脙聝脜聽脙聜脗聳脙聜脗鹿脙聝脜聽脙聜脗鲁脙聜脗聲
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {
     doGet(request, response);
  }
}
