import java.awt.Color;

public class DrawPoint 
{
	private int x;
	private int y;
	private Color color;
	private int size;
	
	public DrawPoint(int x, int y, Color c, int s)
	{
		this.x = x;
		this.y = y;
		this.color = c;
		this.size = s;
	}
	
	/* GETTERS */
	
	public int getX(){ return x; }
	public int getY(){ return y; }
	public Color getColor() {return color; }
	public int getSize(){ return size; }
}
