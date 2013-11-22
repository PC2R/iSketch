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
			while (msg.getMsg().isEmpty()) 
			{ 
				try 
				{
					sleep(1000);
				}
				catch (InterruptedException e) { }
			}
			synchronized (msg)
			{
				wStream.println(msg.getMsg());
				System.out.println("C->S : " + msg.getMsg());
				msg.setMsg("");
			}
		}
	}
}
