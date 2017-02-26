import java.io.*;

public class TimesOfToken {
	public int age[] 	= new int[7];
	
	public int gender[] = new int[3];
	
	public int edu[] 	= new int[7];
	
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
