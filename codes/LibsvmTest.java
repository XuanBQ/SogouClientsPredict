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

public class LibsvmTest {
	// TokenTFIDF 是记录某个词单条query的情况，age gender edu是确定的
	public class TokenTFIDF {
		public Integer id;			// 编号
		
		public int times;		// 记录这个词在这条query中出现过多少次（每条query内部不去重）
		
		public double tf;
		public double idf;
		
		public double tf_idf;	// 	tf*idf 值
	}
	
	Train train;
	
	public HashMap<String, TokenTFIDF> tokens_times_map = new HashMap<String, TokenTFIDF>();
	
	private String dbName 	  = null;
	
	private String tableName  = null;
	
	private WordsCluster wordCluster;
	
	public static void main(String args[])
	{
		LibsvmTest t = new LibsvmTest("test.db", "CLIENTS");
		
		//t.train.readTokensFromDB();
		t.readClients();
	}
	
	public LibsvmTest(String db, String table)
	{
		dbName = "jdbc:sqlite:" + db;
		tableName = table;
		
		wordCluster = new WordsCluster("/Users/bqxuan/eclipse/workspace/SogouClientsAnalysis/word2vec/classes_20000.txt");
		
		train = new Train(db, table, wordCluster);
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
	      
	      while ( rs.next() ) {
	    	  int 	age_index  	    = rs.getInt("AGE");
	          int 	gender_index	= rs.getInt("GENDER");
	          int 	edu_index		= rs.getInt("EDUCATION");
	          String query_list		= rs.getString("QUERY_LIST");
	  		  
	          PrintToFile(age_index, gender_index, edu_index, query_list);
	          
	          /*int tokens_sum_a_query = 0;
	          
	          Analyzer analyzer = new PaodingAnalyzer();
	          TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
	          
	          HashMap<String, TokenTFIDF> tokens_tfidf_map = new HashMap<String, TokenTFIDF>();
	  		  Token t;
	  		  
	  		  int max_times = 0;
	  		  while((t = tokenStream.next()) != null){
				String token = t.termText();
				
				if (train.isUseless(token) == true)
	     	 		  continue;
				
				token = wordCluster.GetCluster(token);
				
				if (tokens_tfidf_map.containsKey(token) == false)
					tokens_tfidf_map.put(token, new TokenTFIDF());
				
				TokenTFIDF token_tfidf = tokens_tfidf_map.get(token);
				token_tfidf.times ++;
				tokens_sum_a_query++;
				
				max_times = token_tfidf.times > max_times ? token_tfidf.times : max_times;
	  		  }
	  		  
	  		  GenAClientTFIDFData(tokens_tfidf_map, max_times, age_index, gender_index, edu_index);*/
	      }
	      
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      e.printStackTrace();
	      System.exit(0);
	    }
	    System.out.println("LibSvmData Operation done successfully");
	}
	
	public void PrintToFile(int age, int gender, int edu, String text)
	{
		try
        {	
			String path_age    = "text/age_no_0";
			String path_gender = "text/gender_no_0";
			String path_edu	   = "text/edu_no_0";
			
			File file_age = new File(path_age);
			if(!file_age.exists()) file_age.createNewFile();
			
			File file_gender = new File(path_gender);
			if(!file_gender.exists()) file_gender.createNewFile();
			
			File file_edu = new File(path_edu);
			if(!file_edu.exists()) file_edu.createNewFile();
			
			FileOutputStream out_age 	= new FileOutputStream(file_age, true); //如果追加方式用true        
			StringBuffer 	 sb_age 	= new StringBuffer();
			
			FileOutputStream out_gender = new FileOutputStream(file_gender, true); //如果追加方式用true        
			StringBuffer 	 sb_gender 	= new StringBuffer();
			
			FileOutputStream out_edu 	= new FileOutputStream(file_edu, true); //如果追加方式用true        
			StringBuffer 	 sb_edu 	= new StringBuffer();
			
			if (age != 0)
			{
				sb_age.append(age + " " + text + "\n");
				out_age.write(sb_age.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			}
			if (gender != 0)
			{
				sb_gender.append(gender + " " + text + "\n");
				out_gender.write(sb_gender.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			}
			if (edu != 0)
			{
				sb_edu.append(edu + " " + text + "\n");
				out_edu.write(sb_edu.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			}
			
			out_age.close();
			out_gender.close();
			out_edu.close();

        }catch(IOException ex){
            System.out.println(ex.getStackTrace());
            System.exit(0);
        }
	}
	
	public void GenAClientTFIDFData(HashMap<String, TokenTFIDF> tokens_tfidf_map, int tokens_sum_a_query, int age, int gender, int edu)
	{
		try
        {
			String path_age    = "text/age";
			String path_gender = "text/gender";
			String path_edu	   = "text/edu";
			
			File file_age = new File(path_age);
			if(!file_age.exists()) file_age.createNewFile();
			
			File file_gender = new File(path_gender);
			if(!file_gender.exists()) file_gender.createNewFile();
			
			File file_edu = new File(path_edu);
			if(!file_edu.exists()) file_edu.createNewFile();
			
			FileOutputStream out_age 	= new FileOutputStream(file_age, true); //如果追加方式用true        
			StringBuffer 	 sb_age 	= new StringBuffer();
			sb_age.append(age);
			
			FileOutputStream out_gender = new FileOutputStream(file_gender, true); //如果追加方式用true        
			StringBuffer 	 sb_gender 	= new StringBuffer();
			sb_gender.append(gender);
			
			FileOutputStream out_edu 	= new FileOutputStream(file_edu, true); //如果追加方式用true        
			StringBuffer 	 sb_edu 	= new StringBuffer();
			sb_edu.append(edu);

			ArrayList<TokenTFIDF> tokens_tfidf_arrlist = new ArrayList<TokenTFIDF>();
			
			Iterator it = tokens_tfidf_map.keySet().iterator();
			while (it.hasNext())
			{
				String token = (String)it.next();
				TokenTFIDF token_tfidf 	= tokens_tfidf_map.get(token);
				
				TokenInfo  token_info 	= train.tokens_times_map.get(token);
				if (token_info.GetATokenTimesSum() <= 100) // filter
					continue;
			
				// get id
				token_tfidf.id = token_info.id;
			
				// get tf*idf
				double tf  = (double)token_tfidf.times / (double)tokens_sum_a_query;
				double idf = Math.log( (double)train.GetAllClientsSum() / (double)token_info.GetATokenTimesSum() );
				token_tfidf.tf = tf;
				token_tfidf.idf = idf;
				token_tfidf.tf_idf = tf * idf;	
				
				tokens_tfidf_arrlist.add(token_tfidf);
			}
			tokens_tfidf_arrlist.sort((e1, e2) -> e1.id.compareTo(e2.id));
			
			Iterator arr_it = tokens_tfidf_arrlist.iterator();
			String s =" ";
			while(arr_it.hasNext())
			{
				TokenTFIDF t = (TokenTFIDF)arr_it.next();
				s = s + t.id + ":" + t.tf_idf + " ";
				//System.out.println(t.id);
			}
			sb_age.append(s + "\n");
			sb_gender.append(s + "\n");
			sb_edu.append(s + "\n");
			
			out_age.write(sb_age.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			out_gender.write(sb_gender.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			out_edu.write(sb_edu.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			
			out_age.close();
			out_gender.close();
			out_edu.close();

        }catch(IOException ex){
            System.out.println(ex.getStackTrace());
            System.exit(0);
        }
	}
	
}
