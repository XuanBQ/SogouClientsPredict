import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

public class W2VTest {		
		Train train;
		
		private String dbName 	  = null;
		
		private String tableName  = null;
		
		public static void main(String args[])
		{
			W2VTest t = new W2VTest("test.db", "CLIENTS");
			//t.train.readTokensFromDB();
			t.readClients();
		}
		
		public W2VTest(String db, String table)
		{
			dbName = "jdbc:sqlite:" + db;
			tableName = table;
		}
		
		void readClients()
		{
			Connection c = null;
		    Statement stmt = null;
		    try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection(dbName);
		      c.setAutoCommit(false);
		      System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      ResultSet rs = stmt.executeQuery( "SELECT * FROM " + tableName);
		      
		      String path = "text/all_tokens_uniq";
		      File file = new File(path);
			  if(!file.exists()) file.createNewFile();
			  FileOutputStream out 	= new FileOutputStream(file, false); //如果追加方式用true        
			  StringBuffer 	 sb 	= new StringBuffer();
				
		      while ( rs.next() ) {
		    	  //int 	age_index  	= rs.getInt("AGE");
		          //int 	gender_index	= rs.getInt("GENDER");
		          //int 	edu_index		= rs.getInt("EDUCATION");
		          String query_list		= rs.getString("QUERY_LIST");
		          
		          Analyzer analyzer = new PaodingAnalyzer();
		          TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
		          
		          Token t;
		          HashMap<String, Integer> tokens_map = new HashMap<String, Integer>();
		          while((t = tokenStream.next()) != null){
						String token = t.termText();
						
						if (train.isUseless(token) == true)
							continue;
						
						if (tokens_map.containsKey(token) == false)
						{
							tokens_map.put(token, 0);
							continue;
						}
						
						sb.append(token + " ");
		          }
		          sb.append('\n');
		      }
		      out.write(sb.toString().getBytes("utf-8"));
		    } catch(Exception e){
		    	e.printStackTrace();
		    	System.exit(0);
		    }
		    System.out.println("write w2v text successfully");
		}
}
