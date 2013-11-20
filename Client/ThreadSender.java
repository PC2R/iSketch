import java.io.PrintStream;

public class ThreadSender implements Runnable 
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
			while (msg.getMsg().isEmpty()) { }
			synchronized (msg)
			{
				System.out.println("C->S : " + msg.getMsg());
				wStream.println(msg.getMsg());
				msg.setMsg("");
			}
		}
	}

}
