import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DrawMenu extends JMenuBar
{

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
	private DrawPanel drawp;

	public DrawMenu(int w, int l, DrawPanel dp)
	{
		super();
		this.setPreferredSize(new Dimension(w , l));
		initMenuColors();
		this.add(mColors);
		initMenuLine();
		this.add(mLine);
		this.drawp = dp;
	}

	public void initMenuColors()
	{	
		mColors.setHorizontalTextPosition(SwingConstants.CENTER);
		mColors.setVerticalTextPosition(SwingConstants.CENTER);
		mColors.setLayout(new FlowLayout(FlowLayout.CENTER));

		mYellow.setBackground(Color.YELLOW);
		mYellow.setPreferredSize(new Dimension(150, 30));
		mYellow.addActionListener(new ColorActionListener(Color.YELLOW, this));
		mColors.add(mYellow);

		mOrange.setBackground(Color.ORANGE);
		mOrange.setPreferredSize(new Dimension(150, 30));
		mOrange.addActionListener(new ColorActionListener(Color.ORANGE, this));
		mColors.add(mOrange);

		mRed.setBackground(Color.RED);
		mRed.setPreferredSize(new Dimension(150, 30));
		mRed.addActionListener(new ColorActionListener(Color.RED, this));
		mColors.add(mRed);

		mPink.setBackground(Color.PINK);
		mPink.setPreferredSize(new Dimension(150, 30));
		mPink.addActionListener(new ColorActionListener(Color.PINK, this));
		mColors.add(mPink);

		mViolet.setBackground(Color.MAGENTA);
		mViolet.setPreferredSize(new Dimension(150, 30));
		mViolet.addActionListener(new ColorActionListener(Color.MAGENTA, this));
		mColors.add(mViolet);

		mBlue.setBackground(Color.BLUE);
		mBlue.setPreferredSize(new Dimension(150, 30));
		mBlue.addActionListener(new ColorActionListener(Color.BLUE, this));
		mColors.add(mBlue);

		mGreen.setBackground(Color.GREEN);
		mGreen.setPreferredSize(new Dimension(150, 30));
		mGreen.addActionListener(new ColorActionListener(Color.GREEN, this));
		mColors.add(mGreen);

		mBlack.setBackground(Color.BLACK);
		mBlack.setPreferredSize(new Dimension(150, 30));
		mBlack.addActionListener(new ColorActionListener(Color.BLACK, this));
		mColors.add(mBlack);
	}

	public void initMenuLine()
	{
		mLine.setHorizontalTextPosition(SwingConstants.CENTER);
		mLine.setVerticalTextPosition(SwingConstants.CENTER);
		mLine.setLayout(new FlowLayout(FlowLayout.CENTER));

		ms2.setPreferredSize(new Dimension(150, 30));
		ms2.setText("Fin");
		ms2.addActionListener(new SizeActionListener(2, this));
		ms5.setPreferredSize(new Dimension(150, 30));
		ms5.setText("Moyen");
		ms5.addActionListener(new SizeActionListener(5, this));
		ms8.setPreferredSize(new Dimension(150, 30));
		ms8.setText("Large");
		ms8.addActionListener(new SizeActionListener(8, this));
		ms10.setPreferredSize(new Dimension(150, 30));
		ms10.setText("Très large");
		ms10.addActionListener(new SizeActionListener(10, this));

		mLine.add(ms2);
		mLine.add(ms5);
		mLine.add(ms8);
		mLine.add(ms10);
	}

	public Color getColor() { return this.color; }
	public int getsize() { return this.size; }

	public void setColor(Color c)
	{
		int red, green, blue;
		this.color = c; 
		red = c.getRed();
		green = c.getGreen();
		blue = c.getBlue();
		this.drawp.setDrawColor(c);
		this.drawp.sendCommandSetColor(red, green, blue);
	}
	
	public void setSize(int s)
	{ 
		this.size = s;
		this.drawp.setDrawSize(s);
		this.drawp.sendCommandSetSize(s);
	}
}
