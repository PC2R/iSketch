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

	private MainWindow mWindow = new MainWindow(this);
	
	private String userPseudo;
	private String drawerPseudo;

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
		if (answer.equals("WELCOME/"+ usr + "/"))
		{
			//this.addPlayer(usr, "0");
			this.userPseudo = usr;
			return true;
		}
		else
			return false;
	}

	public String getPlayers()
	{
		String[] tab;
		String line = new String();

		System.out.println("Attente de la liste des joueurs");
		while (true)
		{
			try
			{
				line = readStream.readLine();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			System.out.println("S->C : " + line);
			tab = parse(line);
			if (tab[0].equals("CONNECTED"))
			{
				this.addPlayer(tab[1], "0");
			}
			if (tab[0].equals("NEW_ROUND"))
				break;
		}
		return line;
	}

	public String beginRound(String line, String name)
	{
		String res;
		String[] tab = parse(line);
		
		System.out.println("Debut du round");
		if (tab[0].equals("NEW_ROUND"))
		{
			this.drawerPseudo = tab[2];
			if (tab[1].equals(name))
			{
				res = new String(tab[3]);
				System.out.println("Vous êtes dessinateur. Vous devez dessiner le mot " + res);
			}
			else
			{
				res = new String();
				System.out.println("Vous êtes joueur.");
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

	public void stopThread()
	{
		System.out.println("Arret de threads");
		tListener.interrupt();
		tSender.interrupt();
	}

	
	/* COMMAND */

	public synchronized void interpretCommand()
	{
		String tab[] = parse(msgFromServer.getMsg());
		if (tab[0].equals("GUESSED"))
			this.mWindow.guessed(tab);
		else if (tab[0].equals("WORD_FOUND"))
		{
			this.mWindow.wordFound(tab);
			if (tab[1].equals(this.userPseudo))
				this.mWindow.setDisableButton();
		}
		else if (tab[0].equals("WORD_FOUND_TIMEOUT"))
			this.mWindow.wordFoundTimeOut(tab);
		else if (tab[0].equals("SCORE_ROUND"))
			this.mWindow.scoreOut(tab);
		else if (tab[0].equals("END_ROUND"))
			this.mWindow.endRound(tab);
		else if (tab[0].equals("LINE"))
			this.mWindow.line(tab);
		else if (tab[0].equals("BROADCAST"))
			this.mWindow.broadcast(tab);
		else if (tab[0].equals("EXITED"))
		{
			if (tab[1].equals(this.drawerPseudo))
				mWindow.exitDrawer(tab);
			else
				mWindow.exitFinder(tab);
		}
		else if (tab[0].equals("NEW_ROUND"))
		{
			if (tab[1].equals(this.userPseudo))
				this.mWindow.setActifMode(tab[3]);
			else
				this.setPassiveMode();
			this.mWindow.setAvailableButton();
		}
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

	public void sendCommandSetColor(int r, int g, int b)
	{
		synchronized (msgToServer)
		{
			System.out.println("Changement de couleur");
			msgToServer.setMsg("SET_COLOR/" + Integer.toString(r) + "/" + 
					Integer.toString(g) + "/" + 
					Integer.toString(b) + "/");
			msgToServer.notifyAll();
		}
	}

	public void sendCommandSetSize(int size)
	{
		synchronized (msgToServer)
		{
			System.out.println("Changement de taille");
			msgToServer.setMsg("SET_SIZE/" + Integer.toString(size) + "/");
			msgToServer.notifyAll();
		}
	}

	public void sendCommandSetLine(int x1, int y1, int x2, int y2)
	{
		synchronized (msgToServer) 
		{
			System.out.println("On trace un trait");
			msgToServer.setMsg("SET_LINE/" + Integer.toString(x1) + "/" + 
					Integer.toString(y1) + "/" +
					Integer.toString(x2) + "/" +
					Integer.toString(y2) + "/");
			msgToServer.notifyAll();
		}
	}

	public void sendCommandPass()
	{
		synchronized (msgToServer)
		{
			System.out.println("Le dessinateur veut passer son tour");
			msgToServer.setMsg("PASS/");
			msgToServer.notifyAll();
		}
	}
	
	public void sendCommandCheat()
	{
		synchronized (msgToServer)
		{
			System.out.println( this.userPseudo + " pense que le dessinateur triche");
			msgToServer.setMsg("CHEAT/" + this.userPseudo + "/");
			msgToServer.notifyAll();
		}
	}
	
	public void sendCommandExit()
	{
		synchronized (msgToServer)
		{
			System.out.println( this.userPseudo + " a quitté le jeu");
			msgToServer.setMsg("EXIT/" + this.userPseudo + "/");
			msgToServer.notifyAll();
		}
	}
	
	
	/* GRAPHIC WINDOW ACTIONS */

	public void addPlayer(String name, String score)
	{
		mWindow.addPlayer(name, score);
	}

	public void setPassiveMode() 
	{ 
		System.out.println("La zone de dessin n'est pas active");
		this.mWindow.setPassiveMode();
	}

	public void setActifMode(String word) 
	{ 
		System.out.println("La zone de dessin est active");
		this.mWindow.setActifMode(word);
	}

	public void closeWindow()
	{
		mWindow.dispose();
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

