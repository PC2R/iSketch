
import java.io.PrintStream;

public class ThreadSender extends Thread 
{
	private PrintStream wStream;
	private Message msg;

	ThreadSender(PrintStream ps, Message var)
	{
		this.wStream = ps;
		this.msg = var;
	}

	public void run()
	{
		while (true)
		{
			System.out.println("Thread pret à envoyer");
			synchronized (msg)
			{
				try 
				{
					msg.wait();
				} 
				catch (InterruptedException e) 
				{
					System.out.println("Probleme dans le thread Sender");
					e.printStackTrace();
				}
				wStream.println(msg.getMsg());
				System.out.println("C->S : " + msg.getMsg());
			}
		}
	}
}
