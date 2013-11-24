import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

public class DrawPanel extends JPanel{

	DrawMenu menu;
	
	public DrawPanel(int w, int l) 
	{
		menu = new DrawMenu(w, l);
		this.setLayout(new BorderLayout());
		this.add(menu, BorderLayout.NORTH);
		this.setBackground(Color.BLACK);
	}
}
