
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

	private MainWindow mWindow = new MainWindow(); 

	Messenger(BufferedReader dis, PrintStream ps)
	{
		this.readStream = dis;
		this.writeStream = ps;
		tSender = new ThreadSender(writeStream, msgToServer);
		tListener = new ThreadListener(readStream, msgFromServer, this);
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

	public void getPlayers()
	{
		String[] tab;
		String line = new String();
		int i;
		
		try
		{
			System.out.println("Attente de la liste des joueurs");
			line = readStream.readLine();
			System.out.println("S->C : " + line);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		tab = parse(line);
		if (tab[0].equals("SCORE_ROUND"))
		{
			for (i = 1; i < tab.length - 1; i = i + 2)
			{
				this.addPlayer(tab[i], tab[i + 1]);
			}
		}
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
	}

	public void startThread()
	{
		System.out.println("Lancement de threads");
		tListener.start();
		tSender.start();
	}

	public void closeWindow()
	{
		mWindow.dispose();
	}

	/* COMMAND */
	
	public synchronized void interpretCommand()
	{
		String tab[] = parse(msgFromServer.getMsg());
		if (tab[0].equals("GUESSED"))
			mWindow.guessed(tab);
		else if (tab[0].equals("WORD_FOUND"))
			mWindow.wordFound(tab);
		else if (tab[0].equals("WORD_FOUND_TIME_OUT"))
			mWindow.wordFoundTimeOut(tab);
		else if (tab[0].equals("SCORE_OUT"))
			mWindow.scoreOut(tab);
		else if (tab[0].equals("END_ROUND"))
			mWindow.endRound(tab);
		else
			System.out.println("Commande inconnue : " + tab[0]);
	}
	
	public void wordProposition(String word)
	{
		synchronized (msgToServer)
		{
			System.out.println("Proposition du mot : " + word);
			msgToServer.setMsg("GUESS/" + word + "/");
			msgToServer.notifyAll();
		}
	}

	public void addPlayer(String name, String score)
	{
		mWindow.addPlayer(name, score);
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

