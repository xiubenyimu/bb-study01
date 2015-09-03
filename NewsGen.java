// ????? java ?
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

// ?? HttpServlet ?
public class NewsGen extends HttpServlet {

	static final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	static final String db_address="127.0.0.1";
	static final String db_port="3306";
	static final String db_name="words_and_question";
	static final String encoding="characterEncoding=gbk";
	static final String DB_URL = "jdbc:mysql://"+db_address+":"+db_port+"/"+db_name;
		
	//static final String DB_URL="jdbc:mysql://localhost/words_and_question";

	//  ??????
	static final String USER = "root";
	static final String PASS = "";

	static Set<String> words_set=new HashSet<String>();
	//static Map<String,Integer> words2place_map=new HashMap<String,Integer>();
	static Map<Integer,Vector<String> > place2words_map=new HashMap<Integer,Vector<String> >();
	static Map<Integer,String> verse_info=new HashMap<Integer,String>();
	
	String specification_tbname="";
	String phrase_info_tbname="";
	String question_answer_tbname="";
	String verse_info_tbname="";
	String word_place_tbname="";
	String words_meaning_tbname="";
	String words_question_tbname="";
	
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
	/*
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

	static String parseword(String possible_word
	)
	{
	  String word;
	  //System.out.println("possible_word:["+possible_word+"]");
	  int pos3=mark_find(possible_word);
	  word=possible_word;
	  if(pos3!=-1)
	  {
	    word=possible_word.substring(0,pos3);
	    //System.out.println("["+word+"]:"+word.length());
	  }
	
	  //set_word_appearance_info(word_1st_appearance,verse_words_list,word,chap_dig,no_dig);
	  return word;
	}
	
	static void addVerseWordPlace(String verse,int verse_no)
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
			word=parseword(possible_word);
			if( false==words_set.contains(word) )
			{
				words_set.add(word);
				if( false==place2words_map.containsKey(verse_no) )
				{
					Vector<String> words_list=new Vector<String>();
					words_list.addElement(word);
					place2words_map.put(verse_no,words_list);
				}
				else
				{
					place2words_map.get(verse_no).addElement(word);
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
		word=parseword(possible_word);
		if( false==words_set.contains(word) )
		{
			words_set.add(word);
			if( false==place2words_map.containsKey(verse_no) )
			{
				Vector<String> words_list=new Vector<String>();
				words_list.addElement(word);
				place2words_map.put(verse_no,words_list);
			}
			else
			{
				place2words_map.get(verse_no).addElement(word);
			}
		}
	}
	*/
	
	public String genFormData(String reading_type,String book,String chapter_no)
	{
		String ret_v="";
		ret_v+="<form action=\"OutputWordsInsideChapter\" method=\"POST\">";
		
		ret_v+="<br><b>News Info</b><br>";
		ret_v+="<textarea name =news_url rows=\"5\" cols=\"90\">"+""+"</textarea>";
		ret_v+="<input type=\"hidden\" name=\"reading_type\" value=\""+reading_type+"\" />";
		ret_v+="<input type=\"hidden\" name=\"book\" value=\""+book+"\" />";
		ret_v+="<input type=\"hidden\" name=\"chapter_no\" value=\""+chapter_no+"\" />";
		ret_v+="<input type=\"submit\" value=\"UPDATE\" />";
		ret_v+="</form>";
		
		return ret_v;
	}
	
	
	public String getChapterNo(PrintWriter out)
	{
		Connection conn=null;
	
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
		}
		catch(SQLException se)
		{
			out.println("SQLException se<br>");
			se.printStackTrace();
		}
		catch(Exception e)
		{
			out.println("Exception e+["+e+"]<br>");
			e.printStackTrace();
		}
	
		String sql="SELECT MAX(chapter_no) FROM "+verse_info_tbname;
		Statement stmt;
		Integer chapter_no=0;
		
		try
		{
			stmt= conn.createStatement();
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
			
			if(true==tag_have)
			{
				if(sech!=null)
				{
					chapter_no=Integer.parseInt(sech);
				}
			}
			chapter_no++;
		}
		catch(SQLException e)
		{
			out.println("<br> SQLException:"+e);
			out.println("<br>SQL:"+sql+"<br>");
		}
		
		return chapter_no.toString();
	}
	
  // ?? GET ???????
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // ????????
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	
	String reading_type=request.getParameter("reading_type");
	SetTableInfo(reading_type,out);
	
	  String title = "Servlet Experiment Page";
      String docType =
      "<!doctype html public \"-//w3c//dtd html 4.0 " +
      "transitional//en\">\n";
      out.println(docType +
                "<html>\n" +
                "<head><title>" + title + "</title></head>\n" +
                "<body bgcolor=\"#f0f0f0\">\n" +
                "<h1 align=\"center\">" + title + "</h1>\n" +
                "<br>"+
                "<ul>\n" +
                "  <li><b>book name</b>:"
                + request.getParameter("book") + "\n" +
                "<br><br>Add you news below:<br>"+
                //+ request.getParameter("word") + "\n" +
                "</ul>\n" +
                "</body>");
	
	//String word=request.getParameter("word");
	String book=request.getParameter("book");
	//String chapter_no=request.getParameter("chapter_no");
	//String verse_no=request.getParameter("verse_no");
	
	try{
		String chapter_no=getChapterNo(out);
	String formData=genFormData(reading_type,book,chapter_no);
	out.println(formData);
	out.println("</body></html>");
         // ????
         /*
         rs.close();
         stmt.close();
         conn.close();*/
      
      }catch(Exception e){
         // ?? Class.forName ??
         out.println("Exception e+["+e+"]<br>");
         e.printStackTrace();
      }
      
      out.println("<br><br><td><a href=\"ShowWordsInsideChapter\">Return back to Contents</a></td>");
  }
  // ?? POST ???????
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {
     doGet(request, response);
  }
}

