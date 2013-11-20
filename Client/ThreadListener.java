import java.io.BufferedReader;
import java.io.IOException;

public class ThreadListener implements Runnable
{

	private BufferedReader buff;
	private Message msg;
	
	public ThreadListener(BufferedReader bf, Message var) 
	{
		this.buff = bf;
		this.msg = var;
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
				e.printStackTrace();
			}
			synchronized (msg)
			{
				System.out.println("S->C : " + line);
				msg.setMsg(line); 
			}
		}
	}

}
