
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

	private Color gColor = Color.BLACK;
	private int gsize = 1;
	private int flag = 0;

	private ArrayList<DrawPoint> listPoints = new ArrayList<DrawPoint>();

	public BoardPanel(int w, int l, DrawPanel dp)
	{
		this.drawp = dp;
		this.setPreferredSize(new Dimension(w, l));
	}

	public void setPassiveMode()
	{
		this.removeMouseListener(this);
	}
	
	public void setActifMode()
	{
		this.addMouseListener(this);
	}
	
	/* MOUSELISTENER */
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		int last;
		listPoints.add(new DrawPoint(e.getX(), e.getY(), drawp.getDrawColor(), drawp.getDrawSize()));
		if (flag < 2)
			this.flag = this.flag + 1;
		else
		{
			flag = 0;
			last = listPoints.size();
			drawp.sendCommandSetLine(listPoints.get(last - 2).getX(), listPoints.get(last - 2).getY(),
					 listPoints.get(last - 1).getX(), listPoints.get(last - 1).getY());
		}
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

	/* GRAPHICS */
	
	public void line(String[] tab)
	{
		Color c = new Color(Integer.decode(tab[5]), Integer.decode(tab[6]), Integer.decode(tab[7]));
		int s = Integer.decode(tab[8]);
		DrawPoint p1 = new DrawPoint(Integer.decode(tab[1]), Integer.decode(tab[2]), c, s);
		DrawPoint p2 = new DrawPoint(Integer.decode(tab[3]), Integer.decode(tab[4]), c, s);
		listPoints.add(p1);
		listPoints.add(p2);
		repaint();
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
			}
		}
	}
}
