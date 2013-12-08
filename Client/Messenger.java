import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class Messenger {

	private PrintStream writeStream;
	private BufferedReader readStream;

	private ThreadSender tSender;
	private ThreadListener tListener;
	private Message msgToServer = new Message();
	private Message msgFromServer = new Message();

	private MainWindow mWindow;

	private String userPseudo;
	private String drawerPseudo;

	private ArrayList<Player> listPlayers = new ArrayList<Player>();

	Messenger(BufferedReader dis, PrintStream ps)
	{
		this.readStream = dis;
		this.writeStream = ps;
		tSender = new ThreadSender(writeStream, msgToServer);
		tListener = new ThreadListener(readStream, msgFromServer, this);
	}

	public int connexionChoice()
	{
		int res = -1;
		System.out.println("Choisissez parmis les différents choix de connections possibles :");
		System.out.println("1 - S'enregistrer");
		System.out.println("2 - Se connecter avec identifiant/mot de passe");
		System.out.println("3 - Jouer en anonyme");
		System.out.print("Entrez votre choix : ");
		BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));
		try 
		{
			String chaine = entree.readLine();
			res = Integer.parseInt(chaine);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -1;
		}
		return res;
	}
	
	public void connectUserWithChoice(String user)
	{
		int choice = this.connexionChoice();
		while (choice == -1)
			choice = this.connexionChoice();
		BufferedReader entree = new BufferedReader(new InputStreamReader(System.in));
		if(choice == 1)
		{
			System.out.print("Entrez un mot de passe : ");
			try 
			{
				String chaine = entree.readLine();
				System.out.println("C->S : REGISTER/" + protectString(user) + "/" + chaine + "/");
				writeStream.println("REGISTER/" + protectString(user) + "/" + chaine + "/");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if(choice == 2)
		{
			System.out.print("Entrez votre mot de passe : ");
			try 
			{
				String chaine = entree.readLine();
				System.out.println("C->S : LOGIN/" + protectString(user) + "/" + chaine + "/");
				writeStream.println("LOGIN/" + protectString(user) + "/" + chaine + "/");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("C->S : CONNECT/" + protectString(user) + "/");
			writeStream.println("CONNECT/" + protectString(user) + "/");
		}
	}
	
	public boolean connectionUser(String usr)
	{
		this.connectUserWithChoice(usr);
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
		if (answer.equals("WELCOME/"+ protectString(usr) + "/"))
		{
			//this.addPlayer(usr, "0");
			this.mWindow = new MainWindow(this);
			this.userPseudo = protectString(usr);
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
		this.tSender.start();
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
				this.listPlayers.add(new Player(tab[1]));
			}
			if (tab[0].equals("LISTEN"))
				this.mWindow.listen(tab);
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
				this.mWindow.setDisableButton();
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
		//tSender.start();
	}

	public void stopThread()
	{
		System.out.println("Arret de threads");
		tListener.interrupt();
		tSender.interrupt();
	}

	public void removePlayer(String name)
	{
		int	i;
		if (!this.listPlayers.isEmpty())
		{
			for (i = 0; i < this.listPlayers.size(); i++)
			{
				if (name.equals(listPlayers.get(i).getPseudo()))
				{
					this.listPlayers.remove(i);
					break;
				}
			}
		}
	}
	
	public void updateScore(String name, int to_add)
	{
		int	i;
		if (!this.listPlayers.isEmpty())
		{
			for (i = 0; i < this.listPlayers.size(); i++)
			{
				System.out.println(listPlayers.get(i).getPseudo());
				if (name.equals(listPlayers.get(i).getPseudo()))
				{
					this.listPlayers.get(i).updateScore(to_add);
					break;
				}
			}
		}
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
		{
			for (int i = 1; i < tab.length - 1; i = i + 2)
				this.updateScore(tab[i], Integer.parseInt(tab[i + 1]));
			this.mWindow.scoreOut(this.listPlayers);
		}
		else if (tab[0].equals("END_ROUND"))
		{
			this.mWindow.setDisableButton();
			this.mWindow.setPassiveModeSilence();
			this.mWindow.endRound(tab);
		}
		else if (tab[0].equals("LINE"))
			this.mWindow.line(tab);
		else if (tab[0].equals("BROADCAST"))
			this.mWindow.broadcast(tab);
		else if (tab[0].equals("EXITED"))
		{
			this.removePlayer(tab[1]);
			this.mWindow.scoreOut(this.listPlayers);
			if (tab[1].equals(this.drawerPseudo))
				mWindow.exitDrawer(tab);
			else
				mWindow.exitFinder(tab);
		}
		else if (tab[0].equals("NEW_ROUND"))
		{
			this.mWindow.cleanBoard();
			if (tab[1].equals(this.userPseudo))
			{
				this.mWindow.setActifMode(tab[3]);
				this.mWindow.setDisableButton();
			}
			else
			{
				this.setPassiveMode();
				this.mWindow.setAvailableButton();
			}
		}
		else if (tab[0].equals("LISTEN"))
			this.mWindow.listen(tab);
		else
			System.out.println("Commande inconnue : " + tab[0]);
	}

	public void wordProposition(String word)
	{
		synchronized (msgToServer)
		{
			System.out.println("Proposition du mot : " + word);
			msgToServer.setMsg("GUESS/" + protectString(word) + "/");
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
	
	public void sendCommandTalk(String chat)
	{
		synchronized (msgToServer)
		{
			msgToServer.setMsg("TALK/" + protectString(chat)  + "/");
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
			//if ( str.charAt(i) == '/' && i == str.length() - 1)
				//result = result - 1;
		}
		//System.out.println("Nombre de mot :" + result);
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
				//System.out.println(str.charAt(i) + " word1 = " + word);
				tab[j] = word;
				word = "";
				j++;
			}
			else if ( i == str.length() - 1)
			{
				//System.out.println(str.charAt(i) + " word2 = " + word);
				word = word + str.charAt(i);
				tab[j] = word;
			}
			else
			{
				//System.out.println(str.charAt(i) + " word3 = " + word);
				word = word + str.charAt(i);
			}
			i++;
		}
		return tab;
	}

	public static String getCommand(String str)
	{
		String[] tab = parse(str);
		return (tab[0]);
	}

	public static String protectString(String str)
	{
		String res = new String();
		int i;
		for (i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == '/' || str.charAt(i) == '\\')
				res = res + '\\';
			res = res + str.charAt(i);
		}
		//System.out.println("chaine protégée :" + res);
		return res;
	}

}

