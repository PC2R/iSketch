import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class DrawMenu extends JMenuBar {
	
	private JMenu mColors = new JMenu("Couleur");
	private JMenu mLine = new JMenu("Taille");
	
	private JMenuItem mYellow = new JMenuItem();
	private JMenuItem mOrange = new JMenuItem();
	private JMenuItem mRed = new JMenuItem();
	private JMenuItem mPink = new JMenuItem();
	private JMenuItem mBlue = new JMenuItem();
	private JMenuItem mViolet = new JMenuItem();
	private JMenuItem mGreen = new JMenuItem();
	private JMenuItem mBlack = new JMenuItem();
	
	private JMenuItem ms2 = new JMenuItem();
	private JMenuItem ms5 = new JMenuItem();
	private JMenuItem ms8 = new JMenuItem();
	private JMenuItem ms10 = new JMenuItem();
	
	private Color color;
	private int size;
	
	public DrawMenu(int w, int h)
	{
		super();
		this.setPreferredSize(new Dimension(w , h / 15));
		
		initMenuColors();
		this.add(mColors);
		
		initMenuLine();
		this.add(mLine);
	}
	
	public void initMenuColors()
	{	
		mColors.setHorizontalTextPosition(SwingConstants.CENTER);
		mColors.setVerticalTextPosition(SwingConstants.CENTER);
		mColors.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		mYellow.setBackground(Color.YELLOW);
		mYellow.setPreferredSize(new Dimension(150, 30));
		mColors.add(mYellow);
		
		mOrange.setBackground(Color.ORANGE);
		mOrange.setPreferredSize(new Dimension(150, 30));
		mColors.add(mOrange);
		
		mRed.setBackground(Color.RED);
		mRed.setPreferredSize(new Dimension(150, 30));
		mColors.add(mRed);
		
		mPink.setBackground(Color.PINK);
		mPink.setPreferredSize(new Dimension(150, 30));
		mColors.add(mPink);
		
		mViolet.setBackground(Color.MAGENTA);
		mViolet.setPreferredSize(new Dimension(150, 30));
		mColors.add(mViolet);
		
		mBlue.setBackground(Color.BLUE);
		mBlue.setPreferredSize(new Dimension(150, 30));
		mColors.add(mBlue);
		
		mGreen.setBackground(Color.GREEN);
		mGreen.setPreferredSize(new Dimension(150, 30));
		mColors.add(mGreen);
		
		mBlack.setBackground(Color.BLACK);
		mBlack.setPreferredSize(new Dimension(150, 30));
		mColors.add(mBlack);
	}
	
	public void initMenuLine()
	{
		mLine.setHorizontalTextPosition(SwingConstants.CENTER);
		mLine.setVerticalTextPosition(SwingConstants.CENTER);
		mLine.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		ms2.setPreferredSize(new Dimension(150, 30));
		ms2.setText("Fin");
		ms5.setPreferredSize(new Dimension(150, 30));
		ms5.setText("Moyen");
		ms8.setPreferredSize(new Dimension(150, 30));
		ms8.setText("Large");
		ms10.setPreferredSize(new Dimension(150, 30));
		ms10.setText("Très large");
		
		mLine.add(ms2);
		mLine.add(ms5);
		mLine.add(ms8);
		mLine.add(ms10);
	}

	public Color getColor() { return this.color; }
	public void setColor(Color c) { this.color = c; }
	public int getsize() { return this.size; }
	public void setSize(int s){ this.size = s; }
}
