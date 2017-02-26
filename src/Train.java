import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

import java.sql.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import net.paoding.analysis.analyzer.PaodingAnalyzer;

public class Train {
	public HashMap<String, TimesOfToken> tokens_times_map = new HashMap<String, TimesOfToken>();
	
	private int	   all_tokens_sum = 0;
	
	private String dbName 	  = null;
	
	private String tableName  = null;
	
	Train(String db, String table) 
	{
		dbName 		= "jdbc:sqlite:" + db;
		tableName 	= table;
	}
	
	public static void main(String args[])
	{
		Train a = new Train("test.db", "TRAIN0");
		a.readTokensFromDB();
		//a.PrintTokensTimesMap();
		System.out.println("tokens_sum: " + a.GetTokensSum());
	}
	
	public  void readTokensFromDB()
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
	    	  HashMap<String, Integer> line_tokens_map = new HashMap<String, Integer>();
	          int 	age_index  		= rs.getInt("AGE");
	          int 	gender_index	= rs.getInt("GENDER");
	          int 	edu_index		= rs.getInt("EDUCATION");
	          String query_list		= rs.getString("QUERY_LIST");
	        
	          /*System.out.println( "age = " + age_index );
	          System.out.println( "gender = " + gender_index );
	          System.out.println( "edu = " + edu_index );
	          System.out.println( "query_list = " + query_list );
	          System.out.println();
	          */
	          
	          Analyzer analyzer = new PaodingAnalyzer();
	          TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
	          
	          Token t;
	          while((t=tokenStream.next())!=null){
	              //System.out.println(t);
	              String token = t.termText();
	     	 		
	   	          if (line_tokens_map.containsKey(token) == false) 
	   	        	 line_tokens_map.put(token, 0);
	   	          else
	   	        	 continue;
	   	          
	   	          
	   	          if (tokens_times_map.containsKey(token) == false)
	   	 			  tokens_times_map.put(token, new TimesOfToken());
	   	 		
	   	 		  TimesOfToken times = tokens_times_map.get(token);
	   	 		  times.age[age_index] ++;
	   	 		  times.gender[gender_index] ++;
	   	 		  times.edu[edu_index] ++;
	   	 		  
	   	 		  all_tokens_sum ++;
	          }
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      e.printStackTrace();
	      System.exit(0);
	    }
	    System.out.println("train Operation done successfully");
			
	}
	
	public int GetTokensSum()
	{
		return all_tokens_sum;
	}
	
	public double[] GetFrequencyOfAToken(String token, int a_token_sum, int  tokens_times[])
	{
		int arr_len = tokens_times.length;
		double[] p = new double[arr_len];
		
		for (int i = 0; i < arr_len; i ++)
		{
			int  times = tokens_times[i];
			if (times == 0)
			{
				if (a_token_sum >= 10)
					times = 1;
				else
				{
					p[i] = 0.0;
					continue;
				}
			}
			
			p[i] = (double)times / (double)all_tokens_sum;
		}
		
		return p;
	}
	public double[] GetFrequencyOfATokenInAge(String token)
	{
		TimesOfToken times_of_token = tokens_times_map.get(token);
		
		double[] p = new double[7];
		if (times_of_token != null)
			 p = GetFrequencyOfAToken(token, times_of_token.GetATokenTimesSum(), times_of_token.age);
		
		return p;
	}
	
	public double[] GetFrequencyOfATokenInGender(String token)
	{
		TimesOfToken times_of_token = tokens_times_map.get(token);
		
		double[] p = new double[3];
		if (times_of_token != null)
			p = GetFrequencyOfAToken(token, times_of_token.GetATokenTimesSum(), times_of_token.gender);
		
		return p;
	}
	
	public double[] GetFrequencyOfATokenInEdu(String token)
	{
		TimesOfToken times_of_token = tokens_times_map.get(token);
		
		double[] p = new double[7];
		if (times_of_token != null)
			p = GetFrequencyOfAToken(token, times_of_token.GetATokenTimesSum(), times_of_token.edu);
		
		return p;
	}
	
	public void PrintTokensTimesMap()
	{
		Iterator it = tokens_times_map.keySet().iterator();  
        while(it.hasNext()) {  
            String key = (String)it.next();  
            System.out.println("key:" + key);  
            tokens_times_map.get(key).print();
            System.out.println();
        }  
	}
}
