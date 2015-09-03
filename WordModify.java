// 导入必需的 java 库
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

// 扩展 HttpServlet 类
public class WordModify extends HttpServlet {

	static final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
	static final String db_address="127.0.0.1";
	static final String db_port="3306";
	static final String db_name="words_and_question";
	static final String encoding="characterEncoding=gbk";
	static final String DB_URL = "jdbc:mysql://"+db_address+":"+db_port+"/"+db_name;
		
	//static final String DB_URL="jdbc:mysql://localhost/words_and_question";

	//  数据库的凭据
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
	
	
	public String genFormData(String question,String answer,String mo_meaning,String book,String chapter_no,String verse_no,String word,String verse_tag_no,String word_tag_no,String question_tag_no,String reading_type)
	{
		String ret_v="";
		ret_v+="<form action=\"OutputWordsInsideChapter\" method=\"POST\">";
		ret_v+="<table border=\"1\" width=\"1000\" style=\"word-break:break-word\">";
		
		ret_v+="<tr>";
		ret_v+="<th rowspan=2>";
		ret_v+="<br><b>Meaning</b><br>";
		ret_v+="<textarea name =mo_meaning rows=\"23\" cols=\"90\">"+mo_meaning+"</textarea>";
		ret_v+="</th>";
		ret_v+="<th>";
		ret_v+="<br><b>Question</b><br>";
		ret_v+="<textarea name =question_content rows=\"10\" cols=\"90\">"+question+"</textarea>";
		ret_v+="</th>";
		ret_v+="</tr>";

		ret_v+="<tr>";
		ret_v+="<td>";
		ret_v+="<br>";
		ret_v+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		ret_v+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		ret_v+="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		ret_v+="&nbsp;&nbsp;&nbsp;&nbsp;";
		ret_v+="<b>Answer</b><br>";
		ret_v+="<textarea name =answer_content rows=\"10\" cols=\"90\">"+answer+"</textarea>";
		ret_v+="</td>";
		ret_v+="</tr>";
		
		ret_v+="</table>";
		ret_v+="<input type=\"hidden\" name=\"book\" value=\""+book+"\" />";
		ret_v+="<input type=\"hidden\" name=\"chapter_no\" value=\""+chapter_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"verse_no\" value=\""+verse_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"mo_word\" value=\""+word+"\" />";
		ret_v+="<input type=\"hidden\" name=\"verse_tag\" value=\"verse_no_"+verse_tag_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"word_tag\" value=\""+word_tag_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"wQuestion_tag\" value=\""+question_tag_no+"\" />";
		ret_v+="<input type=\"hidden\" name=\"reading_type\" value=\""+reading_type+"\" />";
		ret_v+="<input type=\"submit\" value=\"UPDATE\" />";
		ret_v+="</form>";
		
		return ret_v;
	}
	
  // 处理 GET 方法请求的方法
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // 设置响应内容类型
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
                "  <li><b>word to be modified</b>:"
                + request.getParameter("word") + "\n" +
                "</ul>\n" +
                "</body>");
	
	String word=request.getParameter("word");
	String book=request.getParameter("book");
	String chapter_no=request.getParameter("chapter_no");
	String word_tag=request.getParameter("word_tag");
	String verse_no=request.getParameter("verse_no");
	String wQuestion_tag=request.getParameter("wQuestion_tag");
	//out.println("word_tag="+word_tag+"<br>");
	//String chapter_no=request.getParameter("chapter_no");
	try{
      //out.println("here in try<br>");
         // 注册 JDBC 驱动器
         Class.forName(JDBC_DRIVER);
//out.println("After Class.forName(JDBC_DRIVER)<br>");
         // 打开一个连接
         conn = DriverManager.getConnection(DB_URL,USER,PASS);
//out.println("After DriverManager.getConnection<br>");
         // 执行 SQL 查询
         stmt = conn.createStatement();
         String sql="SELECT meaning FROM "+words_meaning_tbname+" WHERE word='"+word+"'";
         //out.println(sql);
         ResultSet rs = stmt.executeQuery(sql);

         String meaning=null;
         while(rs.next()){
            // 根据列名称检索
            meaning=rs.getString("meaning");
         }
         
         stmt = conn.createStatement();
         sql="SELECT a.word,a.question_no,b.book,b.chapter_no,b.verse_no,b.question_content,b.answer FROM "+words_question_tbname+" AS a,"+question_answer_tbname+" AS b WHERE a.question_no=b.question_no AND a.word='"+word+"' AND b.book='"+book+"' AND b.chapter_no='"+chapter_no+"' AND b.verse_no='"+verse_no+"'";
         //out.println(sql);
         rs = stmt.executeQuery(sql);

         String question="";
         String answer="";
         while(rs.next()){
            // ???????
            question=rs.getString("b.question_content");
            answer=rs.getString("b.answer");
         }

         /*
        out.println("<form action=\"OutputWordsInsideChapter\" method=\"POST\">");
	out.println("<textarea name =mo_meaning rows=\"10\" cols=\"90\">"+meaning+"</textarea>");
	out.println("<input type=\"hidden\" name=\"question_content\" value=\""+"null"+"\" />");
	out.println("<input type=\"hidden\" name=\"book\" value=\""+book+"\" />");
	out.println("<input type=\"hidden\" name=\"chapter_no\" value=\""+chapter_no+"\" />");
	out.println("<input type=\"hidden\" name=\"mo_word\" value=\""+word+"\" />");
	out.println("<input type=\"hidden\" name=\"answer_content\" value=\""+"null"+"\" />");
	out.println("<input type=\"hidden\" name=\"verse_tag\" value=\""+"null"+"\" />");
	out.println("<input type=\"hidden\" name=\"word_tag\" value=\""+word_tag+"\" />");
	out.println("<input type=\"hidden\" name=\"wQuestion_tag\" value=\""+"null"+"\" />");
	out.println("<input type=\"submit\" value=\"MODIFY\" />");
	out.println("</form>");
	*/
	//genFormData(String question,String answer,String mo_meaning,String book,String chapter_no,String verse_no,String word,String verse_tag_no,String word_tag_no,String question_tag_no)
	String formData=genFormData(question,answer,meaning,book,chapter_no,verse_no,word,"null",word_tag,"null",reading_type);
	out.println(formData);
	out.println("</body></html>");

         // 清理环境
         rs.close();
         stmt.close();
         conn.close();
      }catch(SQLException se){
         // 处理 JDBC 错误
         out.println("SQLException se<br>");
         se.printStackTrace();
      }catch(Exception e){
         // 处理 Class.forName 错误
         out.println("Exception e+["+e+"]<br>");
         e.printStackTrace();
      }finally{
         // 最后是用于关闭资源的块
         try{
            if(stmt!=null)
               stmt.close();
         }catch(SQLException se2){
         }// 我们不能做什么
         try{
            if(conn!=null)
            conn.close();
         }catch(SQLException se){
            se.printStackTrace();
         }//end finally try
      } //end try
      
      out.println("<br><br><td><a href=\"ShowWordsInsideChapter\">Return back to Contents</a></td>");
  }
  // 处理 POST 方法请求的方法
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {
     doGet(request, response);
  }
}
