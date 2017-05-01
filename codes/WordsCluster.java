import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Iterator;

public class WordsCluster {
	public static void main(String args[])
	{
		WordsCluster w = new WordsCluster("/Users/bqxuan/eclipse/workspace/SogouClientsAnalysis/word2vec/classes.txt");
		w.print();
	}
	
	private HashMap<String, String> word_cluster_map = new HashMap<String, String>();
	private String dict_path;
	
	public WordsCluster(String path)
	{
		dict_path = path;
		LoadClusters(path);
	}
	
	private void LoadClusters(String path)
	{ 
		 try 
		 {
			 Scanner input = new Scanner(new File(path));
		 
			 while (input.hasNext())
			 {
				 String word    = input.next();
				 String cluster = input.next();
				 
				 word_cluster_map.put(word, cluster);
			 }
		 }
		 catch (Exception e)
		 {
			 e.printStackTrace();
			 System.exit(0);
		 }
	}
	
	public String GetCluster(String word)
	{
		return word_cluster_map.get(word);
	}
	
	public void print()
	{
		Iterator it = word_cluster_map.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			String cluster = word_cluster_map.get(key);
			System.out.println(key + " " + cluster);
		}
	}
}
