import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SizeActionListener implements ActionListener 
{
	private int size;
	private DrawMenu dm;

	public SizeActionListener(int s, DrawMenu dm) 
	{
		this.size = s;
		this.dm = dm;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		dm.setSize(size);
	}

}
