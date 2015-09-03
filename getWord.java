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

public class getWord {

    static Set mark_set=new HashSet();
    static Map<String,Map<String,Map<Integer,Integer> > > word_place=new HashMap();
    static String ret_content="";
    
    public static void getTransInfo(String word) {
    	ConnectionManager manager;  
		Lexer lexer,back_lexer;  
		Node node;
	ret_content="";
	
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
						ret_content+="[-[-[-基本解释-]-]-][开始]\n";
						record_container=0;
					}
					if( record_container==0 && tofile.indexOf("/div")!=-1 )
					{
						record_container=1;
						ret_content+="\n[-[-[-基本解释-]-]-][结束]";
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
							ret_content+="[-[-[-21世纪大英汉词典-]-]-][结束]\n";
							break;
						}
						record_21centery=0;
						ret_content+="\n[-[-[-21世纪大英汉词典-]-]-][开始]\n";
						continue;
					}
					if( record_21centery==0 && tag_write_1_21cen==1 )
					{
						//appendMethodB(mywriter, "21世纪字典数据"+tofile+"\n");
						String originalHtml=node.toHtml();
						if(tag_omit_21cen_sn==0)
						{
							ret_content+=meaning_sn.toString()+"."+originalHtml+"\n";
						}
						else if(tag_omit_21cen_sn==1)
						{
							ret_content+=originalHtml+"\n";
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
						ret_content+="\n[-[-[-英英释义-]-]-][开始]\n";
						continue;
					}
					if( record_engeng==0 && tag_write_1_engeng==1 )
					{
						//appendMethodB(mywriter, "21世纪字典数据"+tofile+"\n");
						String originalHtml=node.toHtml();
						if(tag_omit_engeng_sn==0)
						{
							ret_content+=meaning_sn.toString()+"."+originalHtml+"\n";
						}
						else if(tag_omit_engeng_sn==1)
						{
							ret_content+=originalHtml+"\n";
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
						ret_content+="\n[-[-[-英英释义-]-]-][开始]\n";
						continue;
					}
					if(tofile.indexOf("英英翻译结束")!=-1)
					{
						record_engeng=1;
						ret_content+="[-[-[-英英释义-]-]-][结束]\n";
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
    }
  
	public static String myGetLine()
	{
		String line="";
		if(ret_content.length()==0)
		{
			line=null;
			return line;
		}
		
		int pos=ret_content.indexOf("\n",0);
		//System.out.println("pos="+pos+"\tMYBEGIN"+ret_content+"\nMYEND");
		
		if(pos==-1)
		{
			line=ret_content;
			ret_content="";
		}
		else
		{
			line=ret_content.substring(0,pos);
			pos++;
			ret_content=ret_content.substring(pos,ret_content.length());
		}
		//System.out.println("|||"+line+"|||");
		return line;
	}
	
	public static Vector readFileline2vector_v2(){
	 	Vector fileline_vec=new Vector();
		{
		       String line=null;
		       String[] arrs=null;
			while ( (line=myGetLine())!=null )
			{
		    	   arrs=line.split(",");
		    	   //System.out.println(line);
		    	   String bad_info="&nbsp";
		    	   if( line.indexOf(bad_info)!=0 && line.length()==bad_info.length() )
		    	   {
			    break;
		    	   }
		    	   fileline_vec.addElement(line);
		          //System.out.println(arrs[0] + " : " + arrs[1] + " : " + arrs[2]);
		        }
		}
		return fileline_vec;
    }
	
	public static Vector readFileline2vector(){
	  String temp_file_name="bad.temp";
		Vector fileline_vec=new Vector();
		try{
			
			FileWriter mywriter;
			mywriter= new FileWriter(temp_file_name, false);
			appendMethodB(mywriter,ret_content);
			mywriter.close();
			
			FileInputStream fis=new FileInputStream(temp_file_name);
		       InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
		       BufferedReader br = new BufferedReader(isr);
		        //简写如下
		       //BufferedReader br = new BufferedReader(new InputStreamReader(
		       //        new FileInputStream("E:/phsftp/evdokey/evdokey_201103221556.txt"), "UTF-8"));
		       String line="";
		       String[] arrs=null;
		       while ((line=br.readLine())!=null) {
		    	   arrs=line.split(",");
		    	   //System.out.println(line);
		    	   String bad_info="&nbsp";
		    	   if( line.indexOf(bad_info)!=0 && line.length()==bad_info.length() )
		    	   {
			    break;
		    	   }
		    	   fileline_vec.addElement(line);
		          System.out.println(":::"+line+":::");
		        }
		       br.close();
		       isr.close();
		       fis.close();
		       
		}catch (IOException e) {  
            e.printStackTrace();
		}
		return fileline_vec;
    }
	
    public static void appendMethodB(FileWriter mywriter, String content) {  
        try {  
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        	mywriter.write(content);  
        	//mywriter.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
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
	
    public static void getBibleInfo(String book_charpter2tag,Statement stmt)throws SQLException,java.lang.ClassNotFoundException {
    	ConnectionManager manager;  
	Lexer lexer,back_lexer;  
	Node node;
	String book_name="";
	String chaprpter_no="";
	String verse_no="";
	String verse="";
	String file_content="";
    	
    		try
		{
			manager = Page.getConnectionManager();

			String bible_html_page="http://bible.kyhs.me/kjv/";
			bible_html_page+=book_charpter2tag;
			bible_html_page+=".htm";
//System.out.println("{{"+bible_html_page+"}}");
			//获得有道字典单词界面上的基本解释信息
			//lexer=new Lexer(manager.openConnection("http://astro.sina.com.cn/sagittarius.html"));
			lexer=new Lexer(manager.openConnection(bible_html_page));
			//System.out.println("--A--");
			int record_container=-1;
			boolean tag_find_verse_no=false;
			int cnt_td_accumulate=0;
			int cnt_after_find=0;
			
			boolean tag_find_title=false;
			
			while(null!=(node=lexer.nextNode(false)))
			{
				Integer beg=node.getStartPosition();
				Integer end=node.getEndPosition();
				Integer len=end-beg;
				String tofile=node.toString();
				
				if( -1!=tofile.indexOf("title") && -1==tofile.indexOf("/title") && book_name=="" )
				{
					tag_find_title=true;
					continue;
				}
				if(true==tag_find_title)
				{
					//System.out.println("<|"+node.toPlainTextString()+"|>");
					book_name=node.toPlainTextString();
					String finding="The Book of ";
					int pos1=book_name.indexOf(finding);
					if(pos1!=-1)
					{
						pos1+=finding.length();
						book_name=book_name.substring(pos1,book_name.length());
						System.out.println("BOOK ["+book_name+"]");
					}
					tag_find_title=false;
				}
				int posf=tofile.indexOf("td class=\"v\"");
				//System.out.println("--TOFILE--"+tofile+":"+posf);
				if(-1==posf)
				{
					if( true==tag_find_verse_no && 1==cnt_td_accumulate )
					{
						cnt_after_find++;
						if(cnt_after_find==1)
						{
							//System.out.println("--SEE--"+tofile);
							//System.out.println(node.toPlainTextString());
							String place_info_str=node.toPlainTextString();
							int pos1=place_info_str.indexOf(":");
							if(pos1!=-1)
							{
								chaprpter_no=place_info_str.substring(0,pos1);
								verse_no=place_info_str.substring(pos1+1,place_info_str.length());
							}
							else
							{
								verse_no=place_info_str.substring(0,place_info_str.length());
							}
						}
						
						if( tofile.indexOf("td")!=-1 && tofile.indexOf("/td")==-1 )
						{
							cnt_td_accumulate=2;
						}
					}
					else
					{
						if(cnt_td_accumulate==2)
						{
							//System.out.println("--SEE 2--"+tofile+" LEN "+tofile.length());
							//System.out.println(node.toPlainTextString());
							verse=deal_escape_character_4_sql(node.toPlainTextString());
							
							String insert_sql="INSERT INTO verse_info (verse,book,chapter_no,verse_no) VALUES('"+verse+"','"+book_name+"',"+chaprpter_no+","+verse_no+")";
							//System.out.println("===>"+insert_sql+"|");
							stmt.executeUpdate(insert_sql);
							//System.out.println(chaprpter_no+":"+verse_no+" "+verse);
							file_content+=chaprpter_no+":"+verse_no+"\t"+verse+"\n";
							
							cnt_td_accumulate=0;
							tag_find_verse_no=false;
							cnt_after_find=0;
						}
					}
				}
				else
				{
					//System.out.println("--MET--"+tofile);
					tag_find_verse_no=true;
					cnt_td_accumulate=1;
					if(record_container==0)
					{
						String originalHtml=node.toHtml();
						//originalHtml+="\n";
						//appendMethodB(mywriter, originalHtml);
						//System.out.println("{{"+originalHtml+"}}");
					}
				}
			}
		}
		catch (ParserException e)  
		{  
			e.printStackTrace();  
		}
	
	String real_outfile_name="";
    	FileWriter mywriter=null;
    	/*
    	try
    	{
		if(chaprpter_no.length()==1)
		{
			real_outfile_name=verse_dir+"/"+book_name+"-0"+chaprpter_no+".txt";
		}
		else
		{
			real_outfile_name=verse_dir+"/"+book_name+"-"+chaprpter_no+".txt";
		}
		
		//System.out.println(real_outfile_name);
		CreateFileIfNotExist(real_outfile_name);
		mywriter= new FileWriter(real_outfile_name, false);
		appendMethodB(mywriter,file_content);
		mywriter.close();

		String later_name=book_name+".";
		if(chaprpter_no.length()==1)
		{
			later_name+="0";
		}
		later_name+=chaprpter_no;
		System.out.println(later_name);
	}
	catch (IOException e) 
	{
		e.printStackTrace();  
	}*/
    }

    public static Set understood_word(String filename)
    {
    	Set<String> understood_wordlist=new HashSet<String>();
    	try{
    		FileInputStream fis=new FileInputStream(filename);
    		InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
    		BufferedReader br = new BufferedReader(isr);
    		
    		String line="";
    		String[] arrs=null;
    		while ((line=br.readLine())!=null)
    		{
    			arrs=line.split(",");
    			//System.out.println(line);
    			if(line.length()==0)
    			{
    				continue;
    			}
    			String word=line.toLowerCase();
    			understood_wordlist.add(word);
    			//System.out.println(arrs[0] + " : " + arrs[1] + " : " + arrs[2]);
    		}
    		br.close();
    		isr.close();
    		fis.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return understood_wordlist;
    }
    
    public static void CreateFileIfNotExist(String fname) 
    {
    	File file=new File(fname);  
		if(!file.exists())  
		{  
			try
			{  
				file.createNewFile();  
			} 
			catch (IOException e) 
			{  
				// TODO Auto-generated catch block  
				e.printStackTrace();  
			}  
		}
		file=null;
    }
    
    public static int DoesFileExist(String fname) 
    {
    	int retv=1;
    	File file=new File(fname);  
		if(!file.exists())  
		{  
			System.out.println("不存在文件"+fname);
			retv=0;
		}
		file=null;
		
		return retv;
    }
    
    public static String write2realoutfile(Vector tempfileline_vec)
    {
    	//CreateFileIfNotExist(real_outfile_name);
    	//boolean tag_studied=false;
    	String word_line="";
    	String real_content="";
    	//System.out.println("LEN:"+ret_content.length());
    	
    	{
    		//mywriter= new FileWriter(real_outfile_name, false);
		//mywriter_2= new FileWriter(new_wordlist_file_name, false);
        	
        	Integer sum=tempfileline_vec.size();
    		for(Integer index=0;index<sum;index++)
    		{
    			Integer show_index=index+1;
    			Integer show_left=sum-show_index;
    			//System.out.print("第"+show_index.toString()+"个，还剩下"+show_left.toString()+"个\r");
    			String str=(String)tempfileline_vec.elementAt(index);
    			
    			if( str.indexOf("-]-]-][开始]")!=-1 || str.indexOf("-]-]-][结束]")!=-1 ||
    					str.indexOf("[-[-[-单词结束-]-]-]")!=-1 )
    			{
    				continue;
    			}
    			
    			int pos1=0,pos2=str.length()-1;
    			for(;pos1<str.length();pos1++)
    			{
    				char single_ch=str.charAt(pos1);
    				if( single_ch!=' ' && single_ch!='\t' )
    				{
    					break;
    				}
    			}
    			for(;pos2>=0;pos2--)
    			{
    				char single_ch=str.charAt(pos2);
    				if( single_ch!=' ' && single_ch!='\t' )
    				{
    					break;
    				}
    			}
    			
    			if( pos1==str.length() || pos2==-1)
    			{
    				continue;
    			}
    			String real_str=str.substring(pos1,pos2+1);
    			if(real_str.length()==0)
    			{
    				continue;
    			}
    			if(real_str.indexOf("节]-]-]")!=-1)
    			{
    				real_content+="\n";
    				
    				String finding="[-[-[";
    				int posf=real_str.indexOf(finding);
    				if(posf==-1)
    				{
    					System.out.println("\t\t\t解析错误1"+real_str);
    					System.exit(0);
    				}
    				posf+=finding.length();
    				String temp=real_str.substring(posf);//x章x节-]-]-]xxxx
    				finding="]-]-]";
    				posf=temp.indexOf(finding);
    				if(posf==-1)
    				{
    					System.out.println("\t\t\t解析错误2");
    					System.exit(0);
    				}
    				String zj=temp.substring(0,posf);
    				String later=temp.substring(posf);//-]-]-]xxxx
    				String word=later.substring(finding.length());
    				real_str="()("+zj+")"+word;
    				//System.out.println("单词:"+word+" 章节信息:"+zj);
    				//tag_studied=alreadystudied_wordslist.containsKey(word);
    				word_line=real_str;
    				/*
    				if(true==tag_studied)
    				{
				  String place_info=alreadystudied_wordslist.get(word).toString();
				  word_line+="|-->"+place_info;
				  //System.out.println("B单词:"+word+" 章节信息:"+zj);
				  //System.out.println("输出信息为【"+word+place_info+"】"+"\n\n");
    				}
    				else
    				{
				  appendMethodB(mywriter_2, word+"\n");//写入单词列表文件中
    				}*/
    				real_content+=word_line+"\n";//写入完整单词信息文件中
    			}
    			else
    			{
			  //if(false==tag_studied)
			  {
			    real_content+=real_str+"\n";
			    //System.out.println("输出信息为【"+real_str+"】"+"\n\n");
			    //System.in.read();
			  }
    			}
    			
    			//System.out.println("输出信息为【"+real_str+"】");
    		}
    	}
    	
    	return real_content;
    }
    
    public static Map GetStudiedWordsList(String alreadystudied_wordslist_file_name)
    {
      Map word_list = new HashMap();
      
      int tag_file_exist=DoesFileExist(alreadystudied_wordslist_file_name);
      if(tag_file_exist==0)
      {
	 System.exit(0);
      }
      
      try{
	FileInputStream fis=new FileInputStream(alreadystudied_wordslist_file_name);
	InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
	BufferedReader br = new BufferedReader(isr);
	
	String line="";
	String[] arrs=null;
	while ((line=br.readLine())!=null) 
	{
	  arrs=line.split(",");
	  //System.out.println(line);
	  int pos1=line.indexOf("[",0);
	  String word=line.substring(0,pos1);
	  pos1++;
	  int pos2=line.indexOf("]",pos1);
	  
	  String chap_no=line.substring(pos1,pos2);
	  
	  pos2++;	  
	  pos1=line.indexOf("<",pos2);
	  pos1++;
	  pos2=line.indexOf(">",pos1);
	  String verse_no=line.substring(pos1,pos2);
	  
	  String word_place="{";
	  word_place+=chap_no;
	  word_place+="章";
	  word_place+=verse_no;
	  word_place+="节}";
	  
	  word_list.put(word,word_place);
	  
	 //System.out.println(word+":"+word_place);
	  //fileline_vec.addElement(line);
	  //System.out.println(arrs[0] + " : " + arrs[1] + " : " + arrs[2]);
	}
	br.close();
	isr.close();
	fis.close();
      }catch (IOException e) 
      { 
	e.printStackTrace();
      }
      //System.exit(0);
      return word_list;
    }
    
	public static Connection getConnection() throws SQLException,java.lang.ClassNotFoundException 
	{
		String driver = "com.mysql.jdbc.Driver";
		String db_address="127.0.0.1";
		String db_port="3306";
		String db_name="words_and_question";
		String encoding="characterEncoding=gbk";
		String url = "jdbc:mysql://"+db_address+":"+db_port+"/"+db_name;
		String db_user = "root";
		String db_password = "";
		
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, db_user, db_password);        
		return conn;        
	}
	
public static void set_mark()
{
  Set set=new HashSet();
  mark_set.add(",");
  mark_set.add(";");
  mark_set.add(".");
  mark_set.add("!");
  mark_set.add("?");
  mark_set.add("'");
  mark_set.add("\"");
  mark_set.add("`");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
  //mark_set.insert("");
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

	public static boolean tag_exist_word(String word,Statement stmt)throws SQLException,java.lang.ClassNotFoundException
	{
		boolean ret_v=false;
		
		String find_word_sql="SELECT * FROM words_meaning WHERE word='"+word+"' AND meaning!=''";
		ResultSet rs = stmt.executeQuery(find_word_sql);
		if(rs.next())
		{
			ret_v=true;
		}
		
		return ret_v;
	}
	
	public static void show_word_set(Statement stmt)throws SQLException,java.lang.ClassNotFoundException
	{
	  int word_set_size=word_place.size();
	  
	  Set<String> empty_word_list=new HashSet<String>();
		Set<String> have_set=new HashSet<String>();
		System.out.println("words cnt="+word_place.size());
		int cn=1;
		for (Map.Entry<String,Map<String,Map<Integer,Integer> > > entry_1 : word_place.entrySet())
		{
			if(cn%1000==0)
			{
				System.out.print("checking exist words,cn="+cn+"------------------------------\r");
			}
			
			String word=entry_1.getKey();
			boolean already_have=tag_exist_word(word,stmt);
			if(already_have==false)
			{
				empty_word_list.add(word);
			}
			else
			{
				have_set.add(word);
			}
			cn++;
		}
		System.out.print("checking exist words,cn="+cn+"------------------------------\r");
		System.out.println("have_set.size()="+have_set.size());
		System.out.println("Insert Begin"+empty_word_list.size());
		cn=1;
		Iterator<String> it = empty_word_list.iterator();
		while (it.hasNext())
		{
			String word = it.next();
			getTransInfo(word);
			Vector tempfileline_vec=readFileline2vector_v2();
			String word_detail_info=write2realoutfile(tempfileline_vec);
			word_detail_info=deal_escape_character_4_sql(word_detail_info);
			ret_content="";
			//System.out.println("word:"+entry_1.getKey()+"\n"+word_detail_info);
			if(cn%100==0)
			{
				System.out.print("size="+word_set_size+",cn="+cn+",word:"+word+"	------------------------------\r");
			}
			//String ins_sql="INSERT IGNORE INTO words_meaning (word,meaning,question_entry) VALUES ('"+word+"','"+word_detail_info+"',NULL)";
			if(word_detail_info.length()>0)
			{
				//String ins_sql="UPDATE words_meaning SET meaning='"+word_detail_info+"',question_entry=NULL WHERE word='"+word+"'";
				System.out.println(ins_sql);
				stmt.executeUpdate(ins_sql);
			}
			
			cn++;
		}
		System.out.println("");
	  /*
	  for (Map.Entry<String,Map<String,Map<Integer,Integer> > > entry_1 : word_place.entrySet())
	  {
	    String word=entry_1.getKey();
	    
	    for (Map.Entry<String,Map<Integer,Integer> > entry_2 : entry_1.getValue().entrySet())
	    {
	      for (Map.Entry<Integer,Integer> entry_3 : entry_2.getValue().entrySet())
	      {
		System.out.println("word:"+entry_1.getKey()+" book:"+entry_2.getKey()+" chapter:"+entry_3.getKey()+" verse:"+entry_3.getValue());
	      }
	    }
	  }*/
	}
	
	public static void add_word_place_info(String word,String book,int chapter_no,int verse_no)
	{
	  //Map<String,Map<String,Map<Integer,Integer> > > word_place
	  if(word.length()>0)
	  {
	    boolean tag_studied=word_place.containsKey(word);
	    if(true==tag_studied)
	    {
	      Map<String,Map<Integer,Integer> > item_1=word_place.get(word);
	      tag_studied=item_1.containsKey(book);
	      if(true==tag_studied)
	      {
		Map<Integer,Integer> item_2=item_1.get(book);
		tag_studied=item_2.containsKey(chapter_no);
		if(true==tag_studied)
		{
		  ;
		}
		else
		{
		  item_2.put(chapter_no,verse_no);
		}
	      }
	      else
	      {
		Map<Integer,Integer> place_chapter_verse=new HashMap();
		place_chapter_verse.put(chapter_no,verse_no);
		
		item_1.put(book,place_chapter_verse);
	      }
	    }
	    else
	    {
	      Map<Integer,Integer> place_chapter_verse=new HashMap();
	      place_chapter_verse.put(chapter_no,verse_no);
	      
	      Map<String,Map<Integer,Integer> > place_book_chapter_verse=new HashMap();
	      place_book_chapter_verse.put(book,place_chapter_verse);
	      
	      word_place.put(word,place_book_chapter_verse);
	    }
	  }
	}
	
	public static void AllBookVerse(Statement stmt)throws SQLException,java.lang.ClassNotFoundException
	{
	  //String sql="SELECT * FROM verse_info WHERE book='Genesis' AND chapter_no=1 AND verse_no=1";
	  String sql="SELECT * FROM verse_info";
	  ResultSet rs = stmt.executeQuery(sql);
	  int cnt=1;
	  while(rs.next()){
            // 根据列名称检索
            //int question_no  = rs.getInt("question_no");
            String book = rs.getString("book");
            int chapter_no=rs.getInt("chapter_no");
            int verse_no=rs.getInt("verse_no");
            String verse = rs.getString("verse");

            // 显示值
            //System.out.println(book+","+chapter_no+","+verse_no+"["+verse+"]");
            if(cnt%1000==0)
            {
	      System.out.println(book+","+chapter_no+","+verse_no);
            }
            cnt++;
            
            {
	      
		int pos1=0;
		String line=verse;
		String finding=" ";
		int pos2=line.indexOf(finding,pos1);
		String possible_word,word;
		while(pos2!=-1)
		{
		    possible_word=line.substring(pos1,pos2);
		    //parseword(chapter_no,verse_no,possible_word,word,mark_set,word_1st_appearance,verse_words_list);
		    word=parseword(possible_word);
		    add_word_place_info(word,book,chapter_no,verse_no);
		    pos1=pos2+1;
		    if(pos1>=line.length())
		    {
		      break;
		    }
		    pos2=line.indexOf(finding,pos1);
		}
		possible_word=line.substring(pos1,line.length());
		//parseword(chap_dig,no_dig,possible_word,word,mark_set,word_1st_appearance,verse_words_list);
		word=parseword(possible_word);
		add_word_place_info(word,book,chapter_no,verse_no);
            }
         }
         
	}
	
	public static void ReadBibleVerses()throws SQLException,java.lang.ClassNotFoundException
	{
	  Statement stmt=null;
		Connection conn=null;
		
		try
		{
			
			conn=getConnection();
			if(!conn.isClosed())
			{
				System.out.println("Succeeded connecting to the Database!");
			}
			
			stmt = conn.createStatement();
			//for()
			{
			  AllBookVerse(stmt);
			}
		}
		catch(java.lang.ClassNotFoundException e)
		{
			System.out.println("SQLException ["+e+"]<br>");
		}
		
		show_word_set(stmt);
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
		String verse_dir=args[0];
		String book_charpter2tag=args[1];
		*/
		FileWriter mywriter;
		try
		{
			//System.out.println("目前还没有啥问题哦");
			set_mark();
			ReadBibleVerses();
			
			//System.out.println("");
		}
		catch (Exception e) 
		{
			e.printStackTrace();  
		}
	}
}
