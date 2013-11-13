import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.lang.*;

public class Client {

	protected static int PORT = 2013;
	protected static InetAddress address;
	private static String user = new String();
	private String role;
	private String word;

	private static boolean setOptions(String[] args)
	{
		if (args.length != 0 && args.length != 2 && args.length != 4 )
		{
			System.out.println("Bad options, wanted :\n\t-port : set the port\n\t-user : set the user name\n");
			return false;
		}
		for (int i = 0; i < args.length - 1; i++)
		{
			if (args[i] == "-port")
				PORT = Integer.decode(args[i + 1]);
			if (args[i].equals("-user"))
				user = args[i + 1];	
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args)
	{
		try
		{
			address = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		}
		Socket s = null;
		if (!setOptions(args))
			System.exit(1);
		if ( user.isEmpty())
			user = "pc2r";
		try
		{
			/* Cannot use this constructor but it should be
			 ** s = new Socket (address, PORT);
			 */
			s = new Socket("localhost", PORT);

			System.out.println("Socket successfuly created");
			DataInputStream dis = new DataInputStream(s.getInputStream());
			PrintStream ps = new PrintStream(s.getOutputStream());
			System.out.println("Connexion found : " + s.getInetAddress() + "\nport : " + s.getPort());
			Messenger msg = new Messenger(dis, ps);
			while (true)
			{
				if(msg.connectionUser(user))
				{
					msg.beginRound();
				}
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
