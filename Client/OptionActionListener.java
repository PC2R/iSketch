import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OptionActionListener implements ActionListener
{

	private String type;
	private DrawMenu dm;
	
	public OptionActionListener(String str, DrawMenu dMenu)
	{
		this.type = str;
		this.dm = dMenu;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (this.type.equals("PASS"))
			dm.sendPass();
		else if (this.type.equals("CHEAT"))
			dm.sendCheat();
	}

}
