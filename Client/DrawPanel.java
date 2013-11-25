import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class DrawPanel extends JPanel
{

	private DrawMenu menu;
	private BoardPanel board;
	private MainWindow mWindow;
	
	private Color color;
	private int size;
	
	public DrawPanel(int w, int l, MainWindow mainWindow) 
	{
		menu = new DrawMenu(w, l / 15, this);
		board = new BoardPanel(w, l * 14 / 15, this );
		this.setLayout(new BorderLayout());
		this.add(menu, BorderLayout.NORTH);
		this.add(board, BorderLayout.SOUTH);
		
		this.setBackground(Color.BLACK);
		this.mWindow = mainWindow;
	}
	
	/* COMMAND TO SERVER */
	
	public void sendCommandSetColor(int r, int g, int b)
	{
		mWindow.sendCommandSetColor(r, g, b);
	}

	public void sendCommandSetSize(int size)
	{
		mWindow.sendCommandSetSize(size);
	}
	
	public void sendCommandSetLine(int x1, int y1, int x2, int y2)
	{
		mWindow.sendCommandSetLine(x1, y1, x2, y2);
	}
	
	/* GETTERS/SETTERS */
	
	public Color getDrawColor() {return color; }
	public void setDrawColor(Color c) { this.color = c; }
	public int getDrawSize() { return this.size; }
	public void setDrawSize(int s) { this.size = s; }
}
