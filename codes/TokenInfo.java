import java.io.*;
import java.util.Comparator;

public class TokenInfo {
	String token;
	int		id;
	
	public int age[] 	= new int[7];			 		// 卡方校验前， 记录这个词在age[i]中出现过多少次（每条query内部会去重，即即使它在同一query出现了多次也只算一次）	
	public int gender[] = new int[3];
	public int edu[] 	= new int[7];
	
	public Double age_chi[] 	= new Double[7];		// 卡方校验的开方值
	public Double gender_chi[] 	= new Double[3];
	public Double edu_chi[]		= new Double[7];
	
	public boolean is_age_chi[] = new boolean[7]; 		// 经过卡方校验后，true则它是age[i]的特征，false则不是
	public boolean is_gender_chi[] = new boolean[3];
	public boolean is_edu_chi[] = new boolean[7];

	public POfToken p_of_token = new POfToken();
	
	public TokenInfo(String t, int d)
	{
		token = t;
		id = d;
	}
	
	public int GetATokenTimesSum()
	{
		return gender[0] + gender[1] + gender[2];
	}
	
	public void print()
	{
		for (int i = 0; i < age.length; i ++)
			System.out.print("age" + i + ": " + age[i] + "  ");
		
		for (int i = 0; i < gender.length; i ++)
			System.out.print("gender" + i + ": " + gender[i] + "  ");
		
		for (int i = 0; i < edu.length; i ++)
			System.out.print("edu" + i + ": " + edu[i] + "  ");
	}
}


