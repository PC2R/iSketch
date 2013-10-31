import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
	protected static final int PORT = 2013;

	@SuppressWarnings("deprecation")
	public static void main(String[] args)
	{
		Socket s = null;
		if (args.length != 1)
		{
			System.err.println("Erreur");
			System.exit(1);
		}
		try
		{
			s = new Socket(args[0], PORT);
			System.out.println("La sockette est créee");
			DataInputStream canalLecture = new DataInputStream(s.getInputStream());
			PrintStream canalEcriture = new PrintStream(s.getOutputStream());
			System.out.println("Connexion établie : " + s.getInetAddress() + " port : " + s.getPort());
			String line = new String();
			char c;
			while (true)
			{
				System.out.flush();
				System.out.println("est-ce que je suis là ?");
				line = "";
				c = (char) System.in.read();
				System.out.println("est-ce que je suis là ??");
				while (c != '\n')
				{
					line = line + c;
					c = (char) System.in.read();
				 }
				canalEcriture.println(line);
				canalEcriture.flush();
				System.out.println("attente");
				line = canalLecture.readLine();
				System.out.println("s : " + line);
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
