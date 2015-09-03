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
public class Question extends HttpServlet {

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

	static String parseword(String possible_word/*int chap_no,int verse_no,String possible_word,set<string> &mark_set,
	  map<string,pair<int,int> > &word_1st_appearance,map<pair<int,int>,vector<string> > &verse_words_list*/
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
	
	
	public String genFormData(String question,String answer,String spe_content,String book,String chapter_no,String verse_no,String reading_type)
	{
		String ret_v="";
		ret_v+="<form action=\"OutputWordsInsideChapter\" method=\"POST\">";
		ret_v+="<br><b>Question</b><br>";
		ret_v+="<textarea name =question_content rows=\"5\" cols=\"90\">"+question+"</textarea>";
		ret_v+="<br><b>Answer</b><br>";
		ret_v+="<textarea name =answer_content rows=\"5\" cols=\"90\">"+answer+"</textarea>";
		ret_v+="<br><b>Specification</b><br>";
		ret_v+="<textarea name =spe_content rows=\"5\" cols=\"90\">"+spe_content+"</textarea>";
		ret_v+="<input type=\"hidden\" name=\"book\" value=\""+book+"\" />";
		ret_v+="<input type=\"hidden\" name=\"chapter_no\" value=\""+chapter_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"verse_no\" value=\""+verse_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"verse_tag\" value=\"verse_no_"+verse_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"reading_type\" value=\""+reading_type+"\" />";
		ret_v+="<input type=\"submit\" value=\"UPDATE\" />";
		ret_v+="</form>";
		
		return ret_v;
	}
	
	
  // ?? GET ???????
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // ????????
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	
	Statement stmt=null;
	Connection conn=null;
	
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
                "<br><td><a href=\"ShowWordsInsideChapter\">Return back to Contents</a></td>"+
                "<ul>\n" +
                "  <li><b>book name</b>:"
                + request.getParameter("book") + "\n" +
                "  <li><b>chapter number</b>:"+
                request.getParameter("chapter_no") + "\n" +
                "  <li><b>verse number</b>:"+
                request.getParameter("verse_no") + "\n" +
                "<br><br>  Update or insert your question/specification blow for verse:<br>"+
                //+ request.getParameter("word") + "\n" +
                "</ul>\n" +
                "</body>");
	
	String word=request.getParameter("word");
	String book=request.getParameter("book");
	String chapter_no=request.getParameter("chapter_no");
	String verse_no=request.getParameter("verse_no");
	
	try{
      //out.println("here in try<br>");
         // ?? JDBC ???
         Class.forName(JDBC_DRIVER);
//out.println("After Class.forName(JDBC_DRIVER)<br>");
         // ??????
         conn = DriverManager.getConnection(DB_URL,USER,PASS);
//out.println("After DriverManager.getConnection<br>");
         // ?? SQL ??
         stmt = conn.createStatement();
         String sql="SELECT question_content,answer FROM "+question_answer_tbname+" WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no+"  AND question_no NOT IN (SELECT question_no FROM words_question)";
         //out.println(sql);
         ResultSet rs = stmt.executeQuery(sql);

         String question="";
         String answer="";
         String meaning=null;
         while(rs.next()){
            // ???????
            question=rs.getString("question_content");
            answer=rs.getString("answer");
         }
         
	sql="SELECT content FROM "+specification_tbname+" WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no;
	rs = stmt.executeQuery(sql);
	String spe_content="";
	while(rs.next()){
            // ???????
            spe_content=rs.getString("content");
         }
         
         String verse="";
         String sql_v="SELECT * FROM "+verse_info_tbname+" WHERE book='"+book+"' AND chapter_no="+chapter_no+" AND verse_no="+verse_no;
         stmt = conn.createStatement();
         rs = stmt.executeQuery(sql_v);
         while(rs.next()){
            // ???????
            verse=rs.getString("verse");
         }
         out.println("<b>"+verse+"</b><br>");
	
//out.println("verse_no="+verse_no+"<br>");
/*
        out.println("<form action=\"OutputWordsInsideChapter\" method=\"POST\">");
	out.println("<br><b>Question</b><br>");
	out.println("<textarea name =question_content rows=\"10\" cols=\"90\">"+question+"</textarea>");
	out.println("<br><b>Answer</b><br>");
	out.println("<textarea name =answer_content rows=\"10\" cols=\"90\">"+answer+"</textarea>");
	out.println("<input type=\"hidden\" name=\"mo_meaning\" value=\""+"null"+"\" />");
	out.println("<input type=\"hidden\" name=\"book\" value=\""+book+"\" />");
	out.println("<input type=\"hidden\" name=\"chapter_no\" value=\""+chapter_no+"\" />");
	out.println("<input type=\"hidden\" name=\"verse_no\" value=\""+verse_no+"\" />");
	out.println("<input type=\"hidden\" name=\"mo_word\" value=\""+"null"+"\" />");
	out.println("<input type=\"hidden\" name=\"verse_tag\" value=\"verse_no_"+verse_no+"\" />");
	out.println("<input type=\"hidden\" name=\"word_tag\" value=\""+"null"+"\" />");
	out.println("<input type=\"hidden\" name=\"wQuestion_tag\" value=\""+"null"+"\" />");
	out.println("<input type=\"submit\" value=\"UPDATE\" />");
	out.println("</form>");
	*/
	String formData=genFormData(question,answer,spe_content,book,chapter_no,verse_no,reading_type);
	out.println(formData);
	out.println("</body></html>");

         // ????
         rs.close();
         stmt.close();
         conn.close();
      }catch(SQLException se){
         // ?? JDBC ??
         out.println("SQLException se<br>");
         se.printStackTrace();
      }catch(Exception e){
         // ?? Class.forName ??
         out.println("Exception e+["+e+"]<br>");
         e.printStackTrace();
      }finally{
         // ???????????
         try{
            if(stmt!=null)
               stmt.close();
         }catch(SQLException se2){
         }// ???????
         try{
            if(conn!=null)
            conn.close();
         }catch(SQLException se){
            se.printStackTrace();
         }//end finally try
      } //end try
      
      out.println("<br><br><td><a href=\"ShowWordsInsideChapter\">Return back to Contents</a></td>");
  }
  // ?? POST ???????
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {
     doGet(request, response);
  }
}

