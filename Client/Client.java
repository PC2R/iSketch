import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

	protected static final int PORT = 2013;

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

	@SuppressWarnings("deprecation")
	public static void main(String[] args)
	{
		Socket s = null;
		if (args.length != 1)
		{
			System.err.println("Usage : ./java Client 127.0.0.1");
			System.exit(1);
		}
		try
		{
			s = new Socket(args[0], PORT);
			System.out.println("Socket successfuly created");
			DataInputStream canalLecture = new DataInputStream(s.getInputStream());
			PrintStream canalEcriture = new PrintStream(s.getOutputStream());
			System.out.println("Connexion found : " + s.getInetAddress() + " port : " + s.getPort());
			String line = new String();
			char c;
			while (true)
			{
				System.out.flush();
				line = "";
				c = (char) System.in.read();
				while (c != '\n')
				{
				    line = line + c;
				    c = (char) System.in.read();
				 }
				canalEcriture.println(line); // sending command to the server
				canalEcriture.flush();
				line = canalLecture.readLine(); // receiving answer from the server
				System.out.println(line);
			}
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
		finally
		{
			try 
			{
				if (s != null)
					s.close();
			}
			catch(IOException e2) {}
		}
	}
}
