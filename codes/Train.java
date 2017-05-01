import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.*;
import java.util.ArrayList;

import java.sql.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import net.paoding.analysis.analyzer.PaodingAnalyzer;

public class Train {
	public HashMap<String, TokenInfo> tokens_times_map = new HashMap<String, TokenInfo>();
	public WordsCluster wordClusters;
		
	private String dbName 	  = null;
	
	private String tableName  = null;
	
	private int[] tokens_sum_age 	= new int[7];   //agei 所有词（每条query中去重）的总数  
	private int[] tokens_sum_gender = new int[3];
	private int[] tokens_sum_edu 	= new int[7];

	private int[] clients_limit_age		= {0, 10000, 10000, 10000, 10000, 10000, 10000};   // 最大采样数
	private int[] clients_limit_gender	= {0, 12000, 10000};
	private int[] clients_limit_edu		= {0, 10000, 10000, 10000, 10000, 10000, 10000}; 
	
	private int[] clients_sum_age		= new int[7]; 	// agei 所有用户数
	private int[] clients_sum_gender	= new int[3];
	private int[] clients_sum_edu		= new int[7];
	private int	  all_clients_sum		= 0;
	
	//double[] pre_p_age = {0.01775, 0.395, 0.2665, 0.18015, 0.10705, 0.02945, 0.0041};
	//double[] pre_p_gender = {0.0212, 0.56825, 00.41055};
	//double[] pre_p_edu = {0.0939, 0.00325, 0.00595, 0.1861, 0.27895, 0.37435, 0.0575};
	double[] pre_p_age = {0.0, 0.5, 0.5, 0.5, 0.1, 0.1, 0.05};
	double[] pre_p_gender = {0.0, 0.5, 0.5};
	double[] pre_p_edu = {0.0, 0.09, 0.1, 0.4, 0.4, 0.4, 0.13};
	
	Train(String db, String table, WordsCluster wordC) 
	{
		dbName 		= "jdbc:sqlite:" + db;
		tableName 	= table;
		wordClusters = wordC;
	}
	
	public static void main(String args[])
	{
		Train a = new Train("test.db", "CLIENTS", null);
		a.readTokensFromDB();
		a.InsertTokensToDB("DICT");
		//a.PrintTokensTimesMap();
		//a.ChiSquare();
		
	}
	
	public int GetAllAgeClientsSum()
	{
		int sum = 0;
		for (int i = 0; i < clients_sum_age.length; i ++)
			sum += clients_sum_age[i];
		
		return sum;
	}
	
	public int GetAllGenderClientsSum()
	{
		int sum = 0;
		for (int i = 0; i < clients_sum_gender.length; i ++)
			sum += clients_sum_gender[i];
		
		return sum;
	}
	
	public int GetAllEduClientsSum()
	{
		int sum = 0;
		for (int i = 0; i < clients_sum_edu.length; i ++)
			sum += clients_sum_edu[i];
		
		return sum;		
	}
	
	public int GetAllClientsSum()
	{
		return all_clients_sum;
	}
	
	public static boolean isUseless(String token)
	{
		boolean useless = false;
		
		if (token.length() <= 1)
			useless = true;
		
		String regex = "[0-9]+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(token);
		if (m.matches() == true && token.compareTo("12306") != 0 && token.compareTo("360") != 0)
			useless = true;
		
		return useless;
	}
	
	public  void readTokensFromDB()
	{
		Connection c = null;
	    Statement stmt = null;
	    int id = 1;
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
	          
   	 		  clients_sum_age[age_index] ++;
   	 		  clients_sum_gender[gender_index] ++;
   	 		  clients_sum_edu[edu_index] ++;
   	 		  all_clients_sum ++;
	          
	          Analyzer analyzer = new PaodingAnalyzer();
	          TokenStream tokenStream = analyzer.tokenStream(query_list, new StringReader(query_list));
	          
	          Token t;
	          while((t=tokenStream.next())!=null){
	              //System.out.println(t);
	              String token = t.termText();
	     	 	  if (isUseless(token) == true)
	     	 		  continue;
	     	 	  
	     	 	  //token = wordClusters.GetCluster(token);
	     	 	  
	   	          if (line_tokens_map.containsKey(token) == false) 
	   	        	 line_tokens_map.put(token, 0);
	   	          else
	   	        	 continue;
	   	          
	   	          
	   	          if (tokens_times_map.containsKey(token) == false)
	   	 			  tokens_times_map.put(token, new TokenInfo(token, id++));
	   	 		
	   	 		  TokenInfo times = tokens_times_map.get(token);
	   	 		  if (age_index != 0 && clients_sum_age[age_index] <= clients_limit_age[age_index])
	   	 		  {
	   	 			tokens_sum_age[age_index] ++;
	   	 			times.age[age_index] ++;
	   	 		  }
	   	 		  if (gender_index != 0 && clients_sum_gender[gender_index] <= clients_limit_gender[gender_index])
	   	 		  {
	   	 			  times.gender[gender_index] ++;
	   	 			  tokens_sum_gender[gender_index] ++;
	   	 		  }
	   	 		  if (edu_index != 0 && clients_sum_edu[edu_index] <= clients_limit_edu[edu_index] )
	   	 		  {
	   	 			  times.edu[edu_index] ++;
	   	 			  tokens_sum_edu[edu_index] ++;
	   	 		  }
	   	 		  
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
	
	public void ChiSquare()
	{
		ArrayList<TokenInfo> token_info_arrlist 	= new ArrayList<TokenInfo>();
		
		Iterator it = tokens_times_map.keySet().iterator();  
        while(it.hasNext()) {  
            String token = (String)it.next();   
            TokenInfo token_info = tokens_times_map.get(token);
            
            if (token_info.GetATokenTimesSum() <= 9000) 
            	continue;
            
            // age
            for (int i = 0; i < 7; i ++)
            {
            	//if (token_info.age[i] <= 36 && token_info.GetATokenTimesSum() <= 80) 
            		//continue;
            
            	int age_A = token_info.age[i];
            	int age_B = token_info.GetATokenTimesSum() - token_info.age[i];
            	int age_C = clients_sum_age[i] - token_info.age[i];
            	int age_D = GetAllAgeClientsSum() - token_info.GetATokenTimesSum() - age_C;
            	int age_AD_BC = age_A*age_D - age_B*age_C; 
            
            	token_info.age_chi[i] = (double)(age_AD_BC * age_AD_BC) / (double)((age_A+age_B) * (age_C+age_D));
            }
            // gender
            for (int i = 0; i < 3; i ++)
            {
            	//if (token_info.gender[i] <= 36 && token_info.GetATokenTimesSum() <= 80) 
            		//continue;
            
            	int gender_A = token_info.gender[i];
            	int gender_B = token_info.GetATokenTimesSum() - token_info.gender[i];
            	int gender_C = clients_sum_gender[i] - token_info.gender[i];
            	int gender_D = GetAllGenderClientsSum() - token_info.GetATokenTimesSum() - gender_C;
            	int gender_AD_BC = gender_A*gender_D - gender_B*gender_C; 
            
            	token_info.gender_chi[i] = (double)(gender_AD_BC * gender_AD_BC) / (double)((gender_A+gender_B) * (gender_C+gender_D));
            }
            // edu
            for (int i = 0; i < 7; i ++)
            {
            	//if (token_info.edu[i] <= 36 && token_info.GetATokenTimesSum() <= 80) 
            		//continue;
            
            	int edu_A = token_info.edu[i];
            	int edu_B = token_info.GetATokenTimesSum() - token_info.edu[i];
            	int edu_C = clients_sum_edu[i] - token_info.edu[i];
            	int edu_D = GetAllEduClientsSum() - token_info.GetATokenTimesSum() - edu_C;
            	int edu_AD_BC = edu_A*edu_D - edu_B*edu_C; 
            
            	token_info.edu_chi[i] = (double)(edu_AD_BC * edu_AD_BC) / (double)((edu_A+edu_B) * (edu_C+edu_D));
            	token_info_arrlist.add(token_info); 
            }
            
            token_info_arrlist.add(token_info);
        }
        
        final int n = token_info_arrlist.size();
        //int n = 0;
        System.out.println("arrlist_size = " + token_info_arrlist.size());
        
        // age0
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[0].compareTo(e1.age_chi[0]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[0] = true;
        }
        // age1
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[1].compareTo(e1.age_chi[1]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[1] = true;
        }
        // age2
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[2].compareTo(e1.age_chi[2]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[2] = true;
        }
        // age3
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[3].compareTo(e1.age_chi[3]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[3] = true;
        }
        // age4
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[4].compareTo(e1.age_chi[4]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[4] = true;
        }
        // age5
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[5].compareTo(e1.age_chi[5]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[5] = true;
        }
        // age6
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[6].compareTo(e1.age_chi[6]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[6] = true;
        }
        
        // age6
        token_info_arrlist.sort((e1, e2) -> e2.age_chi[6].compareTo(e1.age_chi[6]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_age_chi[6] = true;
        }
        
        // gender0
        token_info_arrlist.sort((e1, e2) -> e2.gender_chi[0].compareTo(e2.gender_chi[0]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_gender_chi[0] = true;
        }
        // gender1
        token_info_arrlist.sort((e1, e2) -> e2.gender_chi[1].compareTo(e2.gender_chi[1]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_gender_chi[1] = true;
        }
        // gender2
        token_info_arrlist.sort((e1, e2) -> e2.gender_chi[2].compareTo(e2.gender_chi[2]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_gender_chi[0] = true;
        }
        
        // edu 0
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[0].compareTo(e1.edu_chi[0]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[0] = true;
        }
        // edu 1
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[1].compareTo(e1.edu_chi[1]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[1] = true;
        }
        // edu 2
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[2].compareTo(e1.edu_chi[2]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[2] = true;
        }
        // edu 3
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[3].compareTo(e1.edu_chi[3]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[3] = true;
        }
        // edu4
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[4].compareTo(e1.edu_chi[4]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[4] = true;
        }
        // edu 5
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[5].compareTo(e1.edu_chi[5]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[5] = true;
        }
        // edu 6
        token_info_arrlist.sort((e1, e2) -> e2.edu_chi[6].compareTo(e1.edu_chi[6]));
        for (int i = 0; i < n; i ++)
        {
        	TokenInfo t_in_arr = token_info_arrlist.get(i);
        	
        	TokenInfo token_info = tokens_times_map.get(t_in_arr.token);
        	token_info.is_edu_chi[6] = true;
        }
       
      //System.out.println(tokens_times_map.size() + " " + token_info_arrlist.size());
       //for (int i = 0; i < 1000; i ++)
    	   //System.out.println(token_info_arrlist.get(i).token + ": " + token_info_arrlist.get(i).age_chi[1] + ": " + token_info_arrlist.get(i).age[1]);
	}
	
	public boolean FilterAWord(TokenInfo token_info)
	{
		if (token_info.GetATokenTimesSum() < 80)
			return true;
		else
			return false;
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
		
		for (int i = 1; i < arr_len; i ++)
		{
			if (frequency_train[i] > 0.0)
			{
				double p1 = frequency_train[i] * pre_p[i];
				double p2 = frequency_train[1] * pre_p[1];
				for (int j = 2; j < arr_len; j ++)
					p2 += frequency_train[j] * pre_p[j];
				
				// p
				result[i] = p1 / p2;
				if (result[i] == 1.0)
				{
					//System.out.println("p: 1.0 " + "p1: " + p1 + " p2: " + p2);
					result[i] = 0.999;
				}
			}
			else
				result[i] = 0.0;
			
			
		}
		
	}
	
	/*
	 * Calculate p of all tokens about all properties
	 */
	public void CalPOfTokens()
	{
		try {			
			Iterator it = tokens_times_map.keySet().iterator();  
	        while(it.hasNext()) {  
	            String token = (String)it.next();  
	            
	            TokenInfo token_info = tokens_times_map.get(token);	
				
				// p in age
				CalPOfAToken(GetFrequencyOfATokenInAge(token), pre_p_age, token_info.p_of_token.age_p);
				
				// p in gender
				CalPOfAToken(GetFrequencyOfATokenInGender(token), pre_p_gender, token_info.p_of_token.gender_p);
				
				// p in education
				CalPOfAToken(GetFrequencyOfATokenInEdu(token), pre_p_edu, token_info.p_of_token.edu_p);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public double[] GetFrequencyOfAToken(String token, int a_token_sum, int  tokens_times[], int tokens_sum[], int field)
	{
		int arr_len = tokens_times.length;
		double[] p = new double[arr_len];
		
		for (int i = 1; i < arr_len; i ++)
		{
			int  times = tokens_times[i];
			
			
			if (field == 0) // age
			{
				if ((times <= 37 && a_token_sum < 81) || (times >= 2000 && a_token_sum >= 5000))
				{
					p[i] = 0.0;
					continue;
				}
			}
			else if (field == 1) // gender
			{
				if (times <= 35 && a_token_sum < 75)
				{
					p[i] = 0.0;
					continue;
				}
			}
			else if (field == 2) // edu
			{
				if ((times <= 110 && a_token_sum < 230) || (times >= 4000 && a_token_sum >= 7000))
				{
					p[i] = 0.0;
					continue;
				}
			}
			
			p[i] = ((double)times + 0.01) / (double)(tokens_sum[i]);
		}
		
		return p;
	}
	
	public double[] GetFrequencyOfATokenInAge(String token)
	{
		TokenInfo times_of_token = tokens_times_map.get(token);
		
		double[] p = new double[7];
		if (times_of_token != null)
			 p = GetFrequencyOfAToken(token, times_of_token.GetATokenTimesSum(), times_of_token.age, tokens_sum_age, 0);
		
		return p;
	}
	
	public double[] GetFrequencyOfATokenInGender(String token)
	{
		TokenInfo times_of_token = tokens_times_map.get(token);
		
		double[] p = new double[3];
		if (times_of_token != null)
			p = GetFrequencyOfAToken(token, times_of_token.GetATokenTimesSum(), times_of_token.gender, tokens_sum_gender, 1);
		
		return p;
	}
	
	public double[] GetFrequencyOfATokenInEdu(String token)
	{
		TokenInfo times_of_token = tokens_times_map.get(token);
		
		double[] p = new double[7];
		if (times_of_token != null)
			p = GetFrequencyOfAToken(token, times_of_token.GetATokenTimesSum(), times_of_token.edu, tokens_sum_edu, 2);
		
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
	
	private void InsertTokensToDB(String table_name)
	{
		Iterator it = tokens_times_map.keySet().iterator();
		Connection c = null;
	    try {
	    	Class.forName("org.sqlite.JDBC");
	    	c = DriverManager.getConnection("jdbc:sqlite:test.db");
	    	c.setAutoCommit(false);
	    	System.out.println("Opened database successfully");
	        
	        PreparedStatement prep = c.prepareStatement("INSERT INTO " + table_name + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	        while(it.hasNext()){
	        	String token = (String)it.next();
				TokenInfo times = tokens_times_map.get(token);
            
	        	prep.setString(1, token);
	        	for (int i = 0; i < 7; i ++)
	        		prep.setInt(2 + i, times.age[i]);
	        	for (int i = 0; i < 3; i ++)
	        		prep.setInt(9 + i, times.gender[i]);
	        	for (int i = 0; i < 7; i ++)
	        		prep.setInt(12 + i, times.edu[i]);
	        	prep.setInt(19, times.GetATokenTimesSum());
	        	prep.addBatch();
	        }
	        prep.executeBatch();

	        c.commit();
	        c.close();
	    } catch ( Exception e ) {
	    	System.err.println(e);
	    	System.exit(0);
	    }
	    System.out.println(" Dict Records created successfully");
	}
	
	public int CalUsingWordsSum()
	{
		int sum = 0;
		Iterator it = tokens_times_map.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			TokenInfo token_info = tokens_times_map.get(key);
			//if (FilterAWord(token_info) != true)
				sum ++;
		}
		return sum;
	}
}
