import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Messenger {
	
	private DataInputStream readStream;
	private PrintStream writeStream;
	
	Messenger(DataInputStream dis, PrintStream ps)
	{
		this.readStream = dis;
		this.writeStream = ps;
	}
	
	public boolean connectionUser(String usr)
	{
		System.out.println("CONNECT/" + usr);
		writeStream.println("CONNECT/" + usr);
		String answer = new String();
		try
		{
			answer = readStream.readLine();
			System.out.println(answer);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		if (answer.equals("CONNECTED/"+usr))
			return true;
		else
			return false;
	}
	
	public String[] beginRound()
	{
		String[] res = new String[2];
		String line = new String();
		try
		{
			line = readStream.readLine();
			System.out.println(line);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return res;
	}

	public static int getNbMotString(String str)
	{
		int result = 1;
		for(int i = 0; i < str.length(); i++)
		{
			if ( str.charAt(i) == '/' && str.charAt(i - 1) != '\\')
				result = result + 1;
			if ( str.charAt(i) == '/' && i == str.length() - 1)
				result = result - 1;
		}
		return result;
	}

	public static String[] parse(String str)
	{
		int size = getNbMotString(str);
		int i = 0;
		int j = 0;
		String word = new String();
		String[] tab = new String[size];

		word = "";
		while (i < str.length())
		{
			if ( str.charAt(i) == '/' && str.charAt(i - 1) != '\\')
			{
				tab[j] = word;
				word = "";
				j++;
			}
			else if ( i == str.length() - 1)
			{
				word = word + str.charAt(i);
				tab[j] = word;
			}
			else
				word = word + str.charAt(i);
			i++;
		}
		return tab;
	}

	public static String getCommand(String str)
	{
		String[] tab = parse(str);
		return (tab[0]);
	}
}

