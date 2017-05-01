import java.io.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import java.sql.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
import java.util.Random;

public class Predict {
	private String dbName;
	private String tableName;
	private String trainTableName;
	
	private Train train;
	private WordsCluster wordCluster;

	public static void main(String args[])
	{
		Predict test = new Predict("test.db", "TEST3", "TRAIN3");
		test.train.readTokensFromDB();
		test.train.CalPOfTokens();
		//test.train.ChiSquare();
		//test.CalPrePs();
		test.readQuerys();
	}
	
	Predict(String db, String predict_table, String train_table)
	{
		dbName = "jdbc:sqlite:" + db;
		tableName = predict_table;
		
		trainTableName = train_table;
		
		//wordCluster = new WordsCluster("/Users/bqxuan/eclipse/workspace/SogouClientsAnalysis/word2vec/classes_60000.txt");
		
		train = new Train(db, train_table, wordCluster);
	}
	
	public void readQuerys()
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection(dbName);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      int age_right = 0, gender_right = 0, edu_right = 0, all_right = 0;
	      int all_num = 0, age_num = 0, gender_num = 0, edu_num = 0;
	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM " + tableName);
	      while ( rs.next() ) {
	          int 	age_answer  	= rs.getInt("AGE");
	          int 	gender_answer	= rs.getInt("GENDER");
	          int 	edu_answer		= rs.getInt("EDUCATION");
	          String query_list		= rs.getString("QUERY_LIST");
	        
	          // adding words'p to arr
	          Analyzer analyzer = new PaodingAnalyzer();
	          TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
	          Token t;
	          ArrayList<POfToken> tokens_p_arr = new ArrayList<POfToken>();
	          while((t = tokenStream.next()) != null){
	          	String token = t.termText();
	  			
	          	//token = wordCluster.GetCluster(token);
	          	if (tokens_p_arr.contains(token) == true)
	          		continue;
	          	
	          	TokenInfo token_info = train.tokens_times_map.get(token);
	          	if (token_info != null)
	          		tokens_p_arr.add(token_info.p_of_token);
	          }
	  		
	          boolean a_right = true;
	          boolean g_right = true;
	          boolean e_right = true;
	          int age_predict = 0, gender_predict = 0, edu_predict = 0;
	          // predict
	          System.out.println("-------------------start--------------------");
	          if (age_answer != 0)
	          {
	        	  age_predict		= PredictAge(tokens_p_arr);
	        	  if (age_answer == age_predict) age_right ++;
	        	  else { a_right = false; System.out.println("age_wrong: " + age_predict + "  answer: " + age_answer + "\n" + query_list); }
	        	  age_num ++;
	          }
	          
	          if (gender_answer != 0)
	          {
	        	  gender_predict 	= PredictGender(tokens_p_arr);
		          if (gender_answer == gender_predict) 	gender_right ++;
		          else { g_right = false; }
		          gender_num ++;		          
	          }
	          
	          if (edu_answer != 0)
	          {
	        	  edu_predict		= PredictEdu(tokens_p_arr);
	        	  if (edu_answer == edu_predict)	 edu_right ++;
	        	  else { e_right = false;  System.out.println("edu_wrong: " + edu_predict + "  answer: " + edu_answer + "\n" + query_list); }
	        	  edu_num ++;
	          }
	     	 
	          all_num ++;
	          if (a_right && g_right && e_right) all_right ++;
	          System.out.println("age: " + age_predict + " gender: " + gender_predict + " edu: " + edu_predict);
	          System.out.println("----------------------end-------------------");
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	      
	      System.out.println("AGE_PRECISE = " + (double)age_right / (double)age_num);
	      System.out.println("GENDER_PRECISE = " + (double)gender_right / (double)gender_num);
	      System.out.println("EDU_PRECISE = " + (double)edu_right / (double)edu_num);
	      System.out.println("ALL_PRECISE = " + (double)all_right / (double)all_num);
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      e.printStackTrace();
	      System.exit(0);
	    }
	    System.out.println("Operation done successfully");
			
	}
	
	public void PredictAClient(String query_list)
	{
		Analyzer analyzer = new PaodingAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
        
        ArrayList<POfToken> p_of_token_arr = new ArrayList<POfToken>();
        Token t;
        try
        {
        	while((t = tokenStream.next()) != null){
        		String token = t.termText();
			
        		TokenInfo token_info = train.tokens_times_map.get(token);
        		if (token_info != null)
        			p_of_token_arr.add(token_info.p_of_token);
				
        	}
        } catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

        
        int age_predict		= PredictAge(p_of_token_arr);
        int gender_predict 	= PredictGender(p_of_token_arr);
        int edu_predict		= PredictEdu(p_of_token_arr);
        
        System.out.println("age = " + age_predict + " gender = " + gender_predict + " edu = " + edu_predict);
	}
	
	public int PredictAge(ArrayList<POfToken> p_of_token_arr)
	{
		//System.out.println("tokens: " + p_of_token_arr.size());
		double[] p_age_of_query = new double[7];
		Iterator it = p_of_token_arr.iterator();
		
		while (it.hasNext())
		{
			POfToken p_of_token = (POfToken)it.next();
			for (int i = 1; i < 7; i ++)
			{
				if (p_of_token.age_p[i] <= 0.0)  // ignore this token
					continue;
				
				p_age_of_query[i] += (Math.log(1 - p_of_token.age_p[i]) - Math.log(p_of_token.age_p[i]));
			}
		}
		
		double max 	  = p_age_of_query[1];
		int max_index = 1;
		System.out.print("1: " + p_age_of_query[1] + " ");
		for (int i = 2; i < 7; i ++)
		{
			System.out.print(i + ": "+ p_age_of_query[i] + " " );
			//p_age_of_query[i] = 1.0 / (1.0 + Math.exp(p_age_of_query[i]));
			//System.out.println("after: " + i + ": "+ p_age_of_query[i] + " ");
			if (p_age_of_query[i] < max)
			{
				max 		= p_age_of_query[i];
				max_index 	= i;
			}
		}
		System.out.println("\n age_max_index : " + max_index);
		
		return max_index;
	}
	
	public int PredictGender(ArrayList<POfToken> p_of_token_arr)
	{
		double[] p_gender_of_query = new double[3];
		Iterator it = p_of_token_arr.iterator();
		
		while (it.hasNext())
		{
			POfToken p_of_token = (POfToken)it.next();
			for (int i = 1; i < 3; i ++)
			{
				if (p_of_token.gender_p[i] <= 0.0)  // ignore this token
					continue;

				p_gender_of_query[i] += (Math.log(1 - p_of_token.gender_p[i]) - Math.log(p_of_token.gender_p[i]));
				//System.out.println(i +": " + p_of_token.gender_p[i] + "   " + p_gender_of_query[i]);
			}
		}
		
		double max 	  = p_gender_of_query[1];
		int max_index = 1;
		for (int i = 2; i < 3; i ++)
		{
			//p_gender_of_query[i] = 1.0 / (1.0 + Math.exp(p_gender_of_query[i]));
			//System.out.println(i + ": " + p_gender_of_query[i]);
			if (p_gender_of_query[i] < max)
			{
				max 		= p_gender_of_query[i];
				max_index 	= i;
			}
			//System.out.println("max_index: " + max_index);
		}

		return max_index;
	}
	
	public int PredictEdu(ArrayList<POfToken> p_of_token_arr)
	{
		double[] p_edu_of_query = new double[7];
		Iterator it = p_of_token_arr.iterator();
		
		while (it.hasNext())
		{
			POfToken p_of_token = (POfToken)it.next();
			for (int i = 1; i < 7; i ++)
			{
				if (p_of_token.edu_p[i] <= 0.0)  // ignore this token
					continue;
				
				p_edu_of_query[i] += Math.log(1 - p_of_token.edu_p[i]) - Math.log(p_of_token.edu_p[i]);
			}
		}
		
		double max 	  = p_edu_of_query[1];
		int max_index = 1;
		System.out.print("1: " + p_edu_of_query[1] + " ");
		for (int i = 2; i < 7; i ++)
		{
			//p_edu_of_query[i] = 1.0 / (1.0 + Math.exp(p_edu_of_query[i]));
			System.out.print(i + ": "+ p_edu_of_query[i] + " " );
			if (p_edu_of_query[i] < max)
			{
				max 		= p_edu_of_query[i];
				max_index 	= i;
			}
		}
		System.out.println("\n edu_max_index : " + max_index);

		return max_index;
		
	}
}
