import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorActionListener implements ActionListener
{

	private Color clr;
	private DrawMenu dm;
	
	public ColorActionListener(Color c, DrawMenu dm)
	{
		this.clr = c;
		this.dm = dm;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt)
	{	
		dm.setColor(clr);
	}

}
