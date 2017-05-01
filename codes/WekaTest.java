import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class WekaTest {
	//Train train;
	
	
	private String dbName 	  = null;
	
	private String tableName  = null;
	
	public static void main(String args[])
	{
		WekaTest t = new WekaTest("test.db", "CLIENTS");
		//t.train.readTokensFromDB();
		t.readClients();
	}
	
	public WekaTest(String db, String table)
	{
		dbName = "jdbc:sqlite:" + db;
		tableName = table;
		//train = new Train(db, table);
	}
	
	void readClients()
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	    	String path_age    = "text/weka_age.arff";
			String path_gender = "text/weka_gender.arff";
			String path_edu	   = "text/weka_edu.arff";
			
			File file_age = new File(path_age);
			if(!file_age.exists()) file_age.createNewFile();
			
			File file_gender = new File(path_gender);
			if(!file_gender.exists()) file_gender.createNewFile();
			
			File file_edu = new File(path_edu);
			if(!file_edu.exists()) file_edu.createNewFile();
			
			FileOutputStream out_age 	= new FileOutputStream(file_age, true); //如果追加方式用true        
			StringBuffer 	 sb_age 	= new StringBuffer();
			sb_age.append("@relation age\n@attribute text string\n@attribute class {0,1,2,3,4,5,6}\n@data\n");
			
			FileOutputStream out_gender = new FileOutputStream(file_gender, true); //如果追加方式用true        
			StringBuffer 	 sb_gender 	= new StringBuffer();
			sb_gender.append("@relation gender\n@attribute text string\n@attribute class {0,1,2}\n@data\n");
			
			FileOutputStream out_edu 	= new FileOutputStream(file_edu, true); //如果追加方式用true        
			StringBuffer 	 sb_edu 	= new StringBuffer();
			sb_edu.append("@relation edu\n@attribute text string\n@attribute class {0,1,2,3,4,5,6}\n@data\n");
	      
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(dbName);
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");
	      
	      
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM " + tableName);
	      
			while ( rs.next() ) {
				int 	age_index  	= rs.getInt("AGE");
				int 	gender_index	= rs.getInt("GENDER");
				int 	edu_index		= rs.getInt("EDUCATION");
				String query_list		= rs.getString("QUERY_LIST");
	  		  
				String s = "'" + query_list + "',";
				sb_age.append(s + age_index + "\n");
				sb_gender.append(s + gender_index + "\n");
				sb_edu.append(s + query_list + "\n");
			}
			out_age.write(sb_age.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			out_gender.write(sb_gender.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			out_edu.write(sb_edu.toString().getBytes("utf-8"));//注意需要转换对应的字符集
			
			out_age.close();
			out_gender.close();
			out_edu.close();
	      
			rs.close();
			stmt.close();
			c.close();
	    } catch ( Exception e ) {
	    	System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    	e.printStackTrace();
	    	System.exit(0);
	    }
	    System.out.println("WekaData Operation done successfully");
	}
}
