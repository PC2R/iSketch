import java.io.BufferedReader;
import java.io.IOException;

public class ThreadListener extends Thread
{

	private BufferedReader buff;
	private Message msg;
	private Messenger msn;
	
	public ThreadListener(BufferedReader bf, Message var, Messenger messeng) 
	{
		this.buff = bf;
		this.msg = var;
		this.msn = messeng;
	}
	
	public void run()
	{
		String line = new String();
		while (true)
		{
			System.out.println("Thread à l'écoute");
			try 
			{
				line = buff.readLine();
			}
			catch (IOException e) 
			{
				System.out.println("Probleme dans le ThreadListener");
				e.printStackTrace();
			}
			if (line == null)
				break;
			synchronized (msg)
			{
				System.out.println("S->C : " + line);
				msg.setMsg(line);
			}
			msn.interpretCommand();
		}
	}

}
