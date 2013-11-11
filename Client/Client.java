import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.lang.*;

@SuppressWarnings("deprecation")
public class Client
{
    protected int			PORT = 2013;
    protected String		user = "pc2r";
    protected InetAddress	address;

	public int getNbMotString(String str)
	{
		int	result = 1;

		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == '/' && str.charAt(i - 1) != '\\')
				result = result + 1;
			if (str.charAt(i) == '/' && i == str.length() - 1)
				result = result - 1;
		}
		return (result);
	}

	public String[] parse(String str)
	{
		int size = getNbMotString(str);
		int i = 0;
		int j = 0;
		String word = new String();
		String[] tab = new String[size];

		word = "";
		while (i < str.length())
		{
			if (str.charAt(i) == '/' && str.charAt(i - 1) != '\\')
			{
				tab[j] = word;
				word = "";
				j++;
			}
			else if (i == str.length() - 1)
			{
				word = word + str.charAt(i);
				tab[j] = word;
			}
			else
				word = word + str.charAt(i);
			i++;
		}
		return (tab);
	}

	public boolean setOptions(String[] args)
	{
		if (args.length != 0 && args.length != 2 && args.length != 4 )
		{
			System.out.println("Bad options, wanted :\n\t-port : set the port\n\t-user : set the user name\n");
			return (false);
		}
		for (int i = 0; i < args.length - 1; i++)
		{
			if (args[i] == "-port")
				PORT = Integer.decode(args[i + 1]);
			if (args[i] == "-user")
				user = args[i + 1];
		}
		return (true);
	}
	public static void main(String[] args)
	{
		Client	client = new Client();
		Socket	s = null;

		try
		{
			client.address = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		if (!client.setOptions(args))
			System.exit(1);
		try
		{
			char	c;
			String	line = new String();

			s = new Socket (client.address, client.PORT);
			System.out.println("Socket successfuly created");
			DataInputStream canalLecture = new DataInputStream(s.getInputStream());
			PrintStream canalEcriture = new PrintStream(s.getOutputStream());
			System.out.println("Connexion found : " + s.getInetAddress() + " port : " + s.getPort());
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
			catch(IOException e2)
			{

			}
		}
	}
}
