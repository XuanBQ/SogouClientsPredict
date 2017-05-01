import java.io.*;
import java.sql.*;

public class sqlitetest {
	public static void main(String[] args)
	{
		CreateTable("TEST4");
		InsertFile("test4", "TEST4");
	}
	
	public static void Connect() {
		Connection c = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:test.db");
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Opened database successfully");
	}
	
	public static void CreateTable(String table_name)
	{
	    Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:test.db");
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      String sql = "CREATE TABLE " + table_name + " " +
	                   "(ID TEXT PRIMARY KEY     NOT NULL," +
	                   " AGE            	INT      NOT NULL, " + 
	                   " GENDER            	INT      NOT NULL, " + 
	                   " EDUCATION	        INT 	 NOT NULL," + 
	                   " QUERY_LIST         TEXT)"; 
	                   
	      String sql1 = "CREATE TABLE " + table_name + " " +
	                   "(TOKEN	TEXT	PRIMARY KEY	NOT NULL," +
	                   " AGE0   INT     NOT NULL, " +
	                   " AGE1	INT		NOT NULL, " +
	                   " AGE2	INT		NOT NULL, " +
	                   " AGE3	INT		NOT NULL, " +
	                   " AGE4   INT     NOT NULL, " +
	                   " AGE5   INT     NOT NULL, " +
	                   " AGE6   INT     NOT NULL, " +
	                   " GENDER0 INT    NOT NULL, " +
	                   " GENDER1 INT    NOT NULL, " +
	                   " GENDER2 INT    NOT NULL, " +
	                   " EDU0   INT     NOT NULL, " +
	                   " EDU1	INT		NOT NULL, " +
	                   " EDU2	INT		NOT NULL, " +
	                   " EDU3	INT		NOT NULL, " +
	                   " EDU4   INT     NOT NULL, " +
	                   " EDU5   INT     NOT NULL, " +
	                   " EDU6   INT     NOT NULL, " +
	                   " SUM    INT     NOT NULL)";
	                   
	      stmt.executeUpdate(sql);
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Table created successfully");
	}
	
	public static void Insert()
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:test.db");
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      String sql = "INSERT INTO CLIENTS (ID,AGE,GENDER,EDUCATION,QUERY_LIST) " +
	                   "VALUES ('22DD920316420BE2DF8D6EE651BA174B', 1, 1, 4, '柔和双沟	女生	中财网首页 财经	http://pan.baidu.com/s/1plpjtn9	周公解梦大全查询2345	曹云金再讽郭德纲	总裁大人行行好	中财网第一财经传媒	教师节全文	男子砸毁15墓碑	黄岩岛最新填海图	引起的疲	缘来未迟落跑甜心不好惹	梁朝伟与替身同框	笑傲江湖电视剧任贤齐	小起名字女孩名字	海运行李到堪培拉' );"; 
	      stmt.executeUpdate(sql);

	      stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Records created successfully");
	}
	
	public static void InsertFile(String file_name, String table_name)
	{
		Connection c = null;
	    try {
	    	Class.forName("org.sqlite.JDBC");
	    	c = DriverManager.getConnection("jdbc:sqlite:test.db");
	    	c.setAutoCommit(false);
	    	System.out.println("Opened database successfully");

	    	File file=new File("text/" + file_name);
	    	//File file=new File("text/test2");
	    	InputStreamReader read1=new InputStreamReader(new FileInputStream(file),"UTF-8");
	        BufferedReader br1=new BufferedReader(read1);
	        String text = null;
	        
	        PreparedStatement prep = c.prepareStatement("INSERT INTO " + table_name + " VALUES (?, ?, ?, ?, ?);");
	        while((text=br1.readLine())!=null){
	        	String[] cols = text.split("\\s+", 5);
            
	        	prep.setString(1, cols[0]);
	        	prep.setInt(2, Integer.parseInt(cols[1]));
	        	prep.setInt(3, Integer.parseInt(cols[2]));
	        	prep.setInt(4, Integer.parseInt(cols[3]));
	        	prep.setString(5, cols[4]);
	        	prep.addBatch();
	        }
	        br1.close();
	        prep.executeBatch();

	        c.commit();
	        c.close();
	    } catch ( Exception e ) {
	    	System.err.println(e);
	    	System.exit(0);
	    }
	    System.out.println("Records created successfully");
	}
}
