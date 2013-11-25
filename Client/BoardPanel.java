import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;

public class BoardPanel extends JPanel implements MouseListener
{
	private DrawPanel drawp;

	private Color gColor;
	private int gsize;

	private boolean actif;

	private ArrayList<DrawPoint> listPoints = new ArrayList<DrawPoint>();

	public BoardPanel(int w, int l, DrawPanel dp)
	{
		this.drawp = dp;
		this.setPreferredSize(new Dimension(w, l));
		this.addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		listPoints.add(new DrawPoint(e.getX(), e.getY(), drawp.getDrawColor(), drawp.getDrawSize()));
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		//System.out.println("Pressed on (" + e.getX() + "," + e.getY() + ")");
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		//System.out.println("Released on (" + e.getX() + "," + e.getY() + ")");
	}

	@Override
	public void mouseEntered(MouseEvent e) 
	{
		//System.out.println("Click on (" + e.getX() + "," + e.getY() + ")");
	}

	@Override
	public void mouseExited(MouseEvent e) 
	{
	}

	public void paintComponent(Graphics g)
	{
		int i = 0;
		Graphics2D g2 = (Graphics2D)g;
		if (listPoints.size() > 1)
		{
			for (i = 0; i < listPoints.size() - 1; i = i +2)
			{
				g2.setColor(listPoints.get(i).getColor());
				g2.setStroke(new BasicStroke(listPoints.get(i).getSize()));
				g2.drawLine(listPoints.get(i).getX(), listPoints.get(i).getY(), 
							listPoints.get(i + 1).getX(), listPoints.get(i + 1).getY());
				drawp.sendCommandSetLine(listPoints.get(i).getX(), listPoints.get(i).getY(),
										 listPoints.get(i + 1).getX(), listPoints.get(i + 1).getY());
			}
		}
	}
}
