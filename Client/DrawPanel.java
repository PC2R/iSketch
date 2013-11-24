import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

public class DrawPanel extends JPanel
{

	private DrawMenu menu;
	private MainWindow mWindow;
	
	public DrawPanel(int w, int l, MainWindow mainWindow) 
	{
		menu = new DrawMenu(w, l, this);
		this.setLayout(new BorderLayout());
		this.add(menu, BorderLayout.NORTH);
		this.setBackground(Color.BLACK);
		this.mWindow = mainWindow;
	}
	
	public void sendCommandSetColor(int r, int g, int b)
	{
		mWindow.sendCommandSetColor(r, g, b);
	}

	public void sendCommandSetLine(int size)
	{
		mWindow.sendCommandSetLine(size);
	}
}
