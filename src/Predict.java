import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

import java.sql.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import net.paoding.analysis.analyzer.PaodingAnalyzer;

public class Predict {
	private String dbName;
	private String tableName;
	private String trainTableName;
	
	private Train train;
	
	double[] pre_p_age = new double[7];
	double[] pre_p_gender = new double[3];
	double[] pre_p_edu = new double[7];
	
	public static void main(String args[])
	{
		Predict test = new Predict("test.db", "TEST3", "TRAIN3");
		test.train.readTokensFromDB();
		test.CalPrePs();
		test.readQuerys();
	}
	
	Predict(String db, String predict_table, String train_table)
	{
		dbName = "jdbc:sqlite:" + db;
		tableName = predict_table;
		
		trainTableName = train_table;
		
		train = new Train(db, train_table);
	}
	
	public void CalPrePs()
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection(dbName);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      ResultSet rs;
	      // query sum
	      rs = stmt.executeQuery( "SELECT count(*) FROM " + trainTableName);
	      int query_sum = rs.getInt(1);
	      System.out.println("query_sum = " + query_sum);
	      // age0-age6
	      for (int i = 0; i < pre_p_age.length; i ++)
	      {
	    	  rs = stmt.executeQuery("SELECT count(*) FROM " + trainTableName + " WHERE AGE = " + i + ";");
	    	  int agei_sum = rs.getInt(1);
	    	  pre_p_age[i] = (double)agei_sum / (double)query_sum;
	    	  //System.out.println("pre_p_age" + i + " = " + pre_p_age[i]);
	      }
	      // gender0-3
	      for (int i = 0; i < pre_p_gender.length; i ++)
	      {
	    	  rs = stmt.executeQuery("SELECT count(*) FROM " + trainTableName + " WHERE GENDER = " + i + ";");
	    	  int genderi_sum = rs.getInt(1);
	    	  pre_p_gender[i] = (double)genderi_sum / (double)query_sum;
	    	  //System.out.println("pre_p_gender" + i + " = " + pre_p_gender[i]);
	      }
	      // edu0-6
	      for (int i = 0; i < pre_p_age.length; i ++)
	      {
	    	  rs = stmt.executeQuery("SELECT count(*) FROM " + trainTableName + " WHERE EDUCATION = " + i + ";");
	    	  int edui_sum = rs.getInt(1);
	    	  pre_p_edu[i] = (double)edui_sum / (double)query_sum;
	    	  //System.out.println("pre_p_edu" + i + " = " + pre_p_edu[i]);
	      }
	      
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      e.printStackTrace();
	      System.exit(0);
	    }
	    System.out.println("Operation done successfully");
	    
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
	      int all_num = 0;
	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM " + tableName);
	      while ( rs.next() ) {
	          int 	age_answer  	= rs.getInt("AGE");
	          int 	gender_answer	= rs.getInt("GENDER");
	          int 	edu_answer		= rs.getInt("EDUCATION");
	          String query_list		= rs.getString("QUERY_LIST");
	        
	          /*System.out.println("Answer: ");
	          System.out.print( " age = " + age_answer );
	          System.out.print( " gender = " + gender_answer );
	          System.out.print( " edu = " + edu_answer );
	          System.out.println();
	          */
	          Analyzer analyzer = new PaodingAnalyzer();
	          TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
	          System.out.println(query_list);
	          
	          HashMap<String, POfToken> tokens_p_map = CalPOfTokens(tokenStream);
	          
	          int age_predict		= PredictAge(tokens_p_map);
	          int gender_predict 	= PredictGender(tokens_p_map);
	          int edu_predict		= PredictEdu(tokens_p_map);
	     	 
	          /*System.out.println("Predict: ");
	          System.out.print( " age = " + age_predict );
	          System.out.print( " gender = " + gender_predict );
	          System.out.print( " edu = " + edu_predict );
	          System.out.println("\n");
	          */
	          all_num ++;
	          if (age_answer == age_predict) 		age_right ++;
	          if (gender_answer == gender_predict) 	gender_right ++;
	          if (edu_answer == edu_predict)		edu_right ++;
	          if (age_answer == age_predict && gender_answer == gender_predict && edu_answer == edu_predict) all_right ++;
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	      
	      System.out.println("AGE_PRECISE = " + (double)age_right / (double)all_num);
	      System.out.println("GENDER_PRECISE = " + (double)gender_right / (double)all_num);
	      System.out.println("EDU_PRECISE = " + (double)edu_right / (double)all_num);
	      System.out.println("ALL_PRECISE = " + (double)all_right / (double)all_num);
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      e.printStackTrace();
	      System.exit(0);
	    }
	    System.out.println("Operation done successfully");
			
	}
	
	/*
	 * Calculte p of a token about a property
	 */
	public void CalPOfAToken(double[] frequency_train, double[] pre_p, double[] result)
	{
		int arr_len = frequency_train.length;
		if (arr_len != pre_p.length || arr_len != result.length)
		{
			System.err.println("In CalPOfAToken: length error!");
			System.exit(0);
		}
		
		for (int i = 0; i < arr_len; i ++)
		{
			if (frequency_train[i] > 0.0)
			{
				double p1 = frequency_train[i] * pre_p[i];
				double p2 = frequency_train[0] * pre_p[0];
				for (int j = 1; j < arr_len; j ++)
					p2 += frequency_train[j] * pre_p[j];
				
				// p
				result[i] = p1 / p2;
			}
			else
				result[i] = 0.0;
		}
	}
	
	/*
	 * Calculate p of all tokens about all properties
	 */
	public HashMap<String, POfToken> CalPOfTokens(TokenStream tokenStream)
	{
		HashMap<String, POfToken> tokens_p_map = new HashMap<String, POfToken>();
		try {
			
			Token t;
			while((t = tokenStream.next()) != null){
				String token = t.termText();
				
				System.out.println(token);
				
				// if the token's p has been calculated
				if (tokens_p_map.containsKey(token) == true)
					continue;
				
				POfToken p_of_token = new POfToken();
				tokens_p_map.put(token, p_of_token);
				
				// p in age
				CalPOfAToken(train.GetFrequencyOfATokenInAge(token), pre_p_age, p_of_token.age);
				
				// p in gender
				CalPOfAToken(train.GetFrequencyOfATokenInGender(token), pre_p_gender, p_of_token.gender);
				
				// p in education
				CalPOfAToken(train.GetFrequencyOfATokenInEdu(token), pre_p_edu, p_of_token.edu);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return tokens_p_map;
	}
	
	public int PredictAge(HashMap<String, POfToken> tokens_p_map)
	{
		double[] p_age_of_query = new double[7];
		Iterator it = tokens_p_map.keySet().iterator();
		
		while (it.hasNext())
		{
			String token = (String)it.next();
			POfToken p_of_token = tokens_p_map.get(token);
			for (int i = 0; i < 7; i ++)
			{
				if (p_of_token.age[i] <= 0.0)  // ignore this token
					continue;
				
				p_age_of_query[i] += Math.log(1 - p_of_token.age[i]) - Math.log(p_of_token.age[i]);
			}
		}
		
		double max 	  = 0.0;
		int max_index = 0;
		for (int i = 0; i < 7; i ++)
		{
			p_age_of_query[i] = 1.0 / (1.0 + Math.exp(p_age_of_query[i]));
			if (p_age_of_query[i] > max)
			{
				max 		= p_age_of_query[i];
				max_index 	= i;
			}
		}

		return max_index;
	}
	
	public int PredictGender(HashMap<String, POfToken> tokens_p_map)
	{
		double[] p_gender_of_query = new double[3];
		Iterator it = tokens_p_map.keySet().iterator();
		
		while (it.hasNext())
		{
			String token = (String)it.next();
			POfToken p_of_token = tokens_p_map.get(token);
			for (int i = 0; i < 3; i ++)
			{
				if (p_of_token.gender[i] <= 0.0)  // ignore this token
					continue;
				
				p_gender_of_query[i] += Math.log(1 - p_of_token.gender[i]) - Math.log(p_of_token.gender[i]);
			}
		}
		
		double max 	  = 0.0;
		int max_index = 0;
		for (int i = 0; i < 3; i ++)
		{
			p_gender_of_query[i] = 1.0 / (1.0 + Math.exp(p_gender_of_query[i]));
			if (p_gender_of_query[i] > max)
			{
				max 		= p_gender_of_query[i];
				max_index 	= i;
			}
		}

		return max_index;
	}
	
	public int PredictEdu(HashMap<String, POfToken> tokens_p_map)
	{
		double[] p_edu_of_query = new double[7];
		Iterator it = tokens_p_map.keySet().iterator();
		
		while (it.hasNext())
		{
			String token = (String)it.next();
			POfToken p_of_token = tokens_p_map.get(token);
			for (int i = 0; i < 7; i ++)
			{
				if (p_of_token.edu[i] <= 0.0)  // ignore this token
					continue;
				
				p_edu_of_query[i] += Math.log(1 - p_of_token.edu[i]) - Math.log(p_of_token.edu[i]);
			}
		}
		
		double max 	  = 0.0;
		int max_index = 0;
		for (int i = 0; i < 7; i ++)
		{
			p_edu_of_query[i] = 1.0 / (1.0 + Math.exp(p_edu_of_query[i]));
			if (p_edu_of_query[i] > max)
			{
				max 		= p_edu_of_query[i];
				max_index 	= i;
			}
		}

		return max_index;
	}
}
