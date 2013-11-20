import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Messenger {
	
	private PrintStream writeStream;
	private BufferedReader readStream;
	
	private ThreadSender tSender;
	private ThreadListener tListener;
	private Message msgToServer = new Message();
	private Message msgFromServer = new Message();
	
	Messenger(BufferedReader dis, PrintStream ps)
	{
		this.readStream = dis;
		this.writeStream = ps;
		tSender = new ThreadSender(writeStream, msgToServer);
		tListener = new ThreadListener(readStream, msgFromServer);
	}
	
	public boolean connectionUser(String usr)
	{
		System.out.println("C->S : CONNECT/" + usr + "/");
		writeStream.println("CONNECT/" + usr + "/");
		String answer = new String();
		try
		{
			answer = readStream.readLine();
			System.out.println("S->C : " + answer);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		if (answer.equals("CONNECTED/"+ usr + "/"))
			return true;
		else
			return false;
	}
	
	public String beginRound()
	{
		String res;
		String line = new String();
		String[] tab;
		try
		{
			System.out.println("Attente du debut du round");
			line = readStream.readLine();
			System.out.println("S->C : " + line);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		tab = parse(line);
		if (tab[0].equals("NEW_ROUND"))
		{
			if (tab[1].equals("drawer"))
			{
				res = new String(tab[2]);
				System.out.println("Vous êtes dessinateur. Vous devez dessiner le mot " + res);
			}
			else
			{
				res = new String();
				System.out.println("Vous êtes joueur. Devinez le mot pour gagner");
			}
		}
		else
			res = new String();
		return res;
	}

	public void waitEndRound()
	{
		String res;
		String line = new String();
		String[] tab;
		try
		{
			System.out.println("Le dessinateur dessine jusqu'a la fin du round");
			line = readStream.readLine();
			System.out.println("S->C : " + line);
			tab = parse(line);
			while (!tab[0].equals("END_ROUND"))
			{
				line = readStream.readLine();
				System.out.println("S->C : " + line);
				tab = parse(line);
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void startThread()
	{
		System.out.println("Lancement de threads");
		Thread tl = new Thread(tListener);
		Thread ts = new Thread(tSender);
		tl.start();
		ts.start();
	}

	public String wordProposition(String word)
	{
		System.out.println("Proposition du mot : " + word);
		msgToServer.setMsg("GUESS/" + word + "/");
		return msgFromServer.getMsg();
	}
	
	/* STATIC METHODES */
	
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

