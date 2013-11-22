import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

public class Client {

	protected static int PORT;
	protected static InetAddress address;
	private static String user = new String();

	private static boolean setOptions(String[] args)
	{
		if (args.length != 0 && args.length != 2 && args.length != 4 )
		{
		    System.out.println("Bad options, wanted :\n\t-port : set the port\n\t-user : set the user name\n");
		    return false;
		}
		if (args.length == 0)
		{
		    PORT = 2013;
		    user = "pc2r";
		}
		else if (args.length == 2)
		{
		    if (args[0].equals("-port"))
			{
			    PORT = Integer.decode(args[1]);
			    user = "pc2r";
			}
			if (args[0].equals("-user"))
			{
			    user = args[1];
			    PORT = 2013;
			}
		}
		else
		{
			 for (int i = 0; i < args.length - 1; i++)
			{
				 if (args[i].equals("-port"))
					PORT = Integer.decode(args[i + 1]);
				if (args[i].equals("-user"))
					user = args[i + 1];
			}
		}
	return true;
	}
    
	public static void main(String[] args)
	{
		String round = new String();
		String word;
		int role; // 0 drawer - 1 finder
		
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
		try
		{
			//Cannot use this constructor but it should be
			s = new Socket (address, PORT);
			//s = new Socket("localhost", PORT);

			System.out.println("Socket successfuly created");
			//DataInputStream dis = new DataInputStream(s.getInputStream());
			BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintStream ps = new PrintStream(s.getOutputStream());
			System.out.println("Connexion found : " + s.getInetAddress() + "\nport : " + s.getPort());
			Messenger msg = new Messenger(dis, ps);
			if(msg.connectionUser(user))
			{
				while (true)
				{
					round = msg.beginRound();
					if ( !round.isEmpty())
					{
						role = 0; // drawer 
						word = round;
						msg.waitEndRound();
					}
					else
					{
						role = 1; // finder
						msg.startThread();
						msg.wordProposition("MOT");
					}
				}
			}
			else
			{
				System.out.println("Connection au jeu refusée");
				if (s != null)
					s.close();
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
