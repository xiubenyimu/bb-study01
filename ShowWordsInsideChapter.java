import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
 
public class ShowWordsInsideChapter extends HttpServlet{
    // JDBC 脙聝脗漏脙聜脗漏脙聜脗卤脙聝脗楼脙聜脗聤脙聟脗隆脙聝脗楼脙聜脗聶脙聟脗隆脙聝脗楼脙聜脗聬脙聜脗聧脙聝脗搂脙聜脗搂脙聜脗掳脙聝脗楼脙聜脗聮脙聜脗聦脙聝脜聽脙聜脗聲脙聜脗掳脙聝脜聽脙聜脗聧脙聜脗庐脙聝脗楼脙聜脗潞脙聜脗聯脙聝脗搂脙聜脗職脙聜脗聞 URL
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
      
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      
      // 脙聝脜隆脙聜脗庐脙聟脜鸥脙聝脗搂脙聟脗聯脙聜脗庐脙聝脗楼脙聜脗聯脙聜脗聧脙聝脗楼脙聜脗潞脙聜脗聰脙聝脗楼脙聜脗聠脙聜脗聟脙聝脗楼脙聜脗庐脙聜脗鹿脙聝脗搂脙聜脗卤脙聜脗禄脙聝脗楼脙聜脗聻脙聜脗聥
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();
      //response.setCharacterEncoding("UTF-8");
      
      
      //response.setCharacterEncoding("GB2312");
      //response.setHeader("content-type","text/html;charset=GBK");
      
      Statement stmt=null;
      Connection conn=null;
      String title = "Database results数据库测试";
      String docType =
        "<!doctype html public \"-//w3c//dtd html 4.0 " +
         "transitional//en\">\n";
         out.println(docType +
         "<html>\n" +
         "<head><title>" + title + "</title></head>\n" +
         "<body bgcolor=\"#f0f0f0\">\n" +
         "<h1 align=\"center\">" + title + "</h1>\n");
         
         //out.println("before try JDBC_DRIVER is ["+JDBC_DRIVER+"] URL is ["+DB_URL+"]<br>");
      try{
      //out.println("here in try<br>");
         // 脙聝脜聽脙聜脗鲁脙聟脗隆脙聝脗楼脙聜脗聠脙聜脗聦 JDBC 脙聝脗漏脙聜脗漏脙聜脗卤脙聝脗楼脙聜脗聤脙聟脗隆脙聝脗楼脙聜脗聶脙聟脗隆
         Class.forName(JDBC_DRIVER);
//out.println("After Class.forName(JDBC_DRIVER)<br>");
         // 脙聝脜聽脙聜脗聣脙聜脗聯脙聝脗楼脙聟脗聮脙聜脗聙脙聝芒聜卢脙聟脜啪脙聜脗聙脙聝芒聜卢脙聟脜啪脙聜脗陋脙聝脜隆脙聜脗驴脙聜脗聻脙聝脜聽脙聜脗聨脙聜脗楼
         conn = DriverManager.getConnection(DB_URL,USER,PASS);
//out.println("After DriverManager.getConnection<br>");
         // 脙聝脜聽脙聜脗聣脙聜脗搂脙聝脜隆脙聜脗隆脙聜脗聦 SQL 脙聝脜聽脙聜脗聼脙聜脗楼脙聝脜隆脙聜脗炉脙聜脗垄
         stmt = conn.createStatement();
         String sql;
         //sql = "SELECT question_no,question_content,answer FROM question_answer";
         sql="SELECT DISTINCT book FROM verse_info";
         ResultSet rs = stmt.executeQuery(sql);
         /*
if(rs!=null)
{
out.println("rs!=null<br>");
}
else{
out.println("rs==null<br>");
}*/
         // 脙聝芒聜卢脙聜脗禄脙聜脗聨脙聝脗搂脙聜脗禄脙聜脗聯脙聝脜聽脙聜脗聻脙聜脗聹脙聝脗漏脙聜脗聸脙聜脗聠脙聝芒聜卢脙聟脜啪脙聜脗颅脙聝脜聽脙聜脗聫脙聜脗聬脙聝脗楼脙聜脗聫脙聜脗聳脙聝脜聽脙聜脗聲脙聜脗掳脙聝脜聽脙聜脗聧脙聜脗庐
         while(rs.next()){
            // 脙聝脜聽脙聜脗聽脙聜脗鹿脙聝脜聽脙聜脗聧脙聜脗庐脙聝脗楼脙聜脗聢脙聜脗聴脙聝脗楼脙聜脗聬脙聜脗聧脙聝脗搂脙聜脗搂脙聜脗掳脙聝脜聽脙聜脗拢脙聜脗聙脙聝脗搂脙聟脜聯脙聜脗垄
            //int question_no  = rs.getInt("question_no");
            String book = rs.getString("book");

            // 脙聝脜聽脙聜脗聵脙聟脜鸥脙聝脗搂脙垄脗聜脗卢脙聜脗潞脙聝脗楼脙聜脗聙脙聟脗聮
            //out.println("question_no: " + question_no + "<br>");
            out.println("The book of " + book);
            sql="SELECT DISTINCT chapter_no FROM verse_info WHERE book='"+book+"'";
            stmt = conn.createStatement();
            ResultSet rs_1 = stmt.executeQuery(sql);
            int sn=1;
            int mod=40;
            out.println("<table border=\"1\">");
            while(rs_1.next())
            {
		if(sn%mod==1)
		{
			out.println("<tr>");
		}
		String chapter_no = rs_1.getString("chapter_no");
		
		String form_data=FormInfo4OutputWordsInsideChapter(book,chapter_no,"null","null","null","null","null","null","null","bible");
		
		out.println("<td><a href=\""+form_data+"\">"+chapter_no+"</a></td>");
		sn++;
		if(sn%mod==1)
		{
			out.println("</tr>");
		}
            }
            out.println("</table><br>");
         }
         out.println("</body></html>");

         // 脙聝脜聽脙聟脜啪脙聜脗聟脙聝脗搂脙聜脗聬脙聜脗聠脙聝脗搂脙聜脗聨脙聜脗炉脙聝脗楼脙聜脗垄脙聜脗聝
         rs.close();
         stmt.close();
         conn.close();
      }catch(SQLException se){
         // 脙聝脗楼脙垄脗聜脗卢脙聜脗聞脙聝脗搂脙聜脗聬脙聜脗聠 JDBC 脙聝脗漏脙聜脗聰脙聜脗聶脙聝脜隆脙聜脗炉脙聜脗炉
         out.println("SQLException se "+se+"<br>");
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
   }
} 
