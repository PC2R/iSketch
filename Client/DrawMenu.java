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
	private JMenu mOption = new JMenu("Autres");

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

	private JMenuItem mPass = new JMenuItem("Passer le tour");
	private JMenuItem mCheat = new JMenuItem("Il triche !");

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
		initMenuOption();
		this.add(mOption);

		this.drawp = dp;
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
		
		mYellow.addActionListener(new ColorActionListener(Color.YELLOW, this));
		mOrange.addActionListener(new ColorActionListener(Color.ORANGE, this));
		mRed.addActionListener(new ColorActionListener(Color.RED, this));
		mPink.addActionListener(new ColorActionListener(Color.PINK, this));
		mViolet.addActionListener(new ColorActionListener(Color.MAGENTA, this));
		mBlue.addActionListener(new ColorActionListener(Color.BLUE, this));
		mGreen.addActionListener(new ColorActionListener(Color.GREEN, this));
		mBlack.addActionListener(new ColorActionListener(Color.BLACK, this));
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

		ms2.addActionListener(new SizeActionListener(2, this));
		ms5.addActionListener(new SizeActionListener(5, this));
		ms8.addActionListener(new SizeActionListener(8, this));
		ms10.addActionListener(new SizeActionListener(10, this));
		
		mLine.add(ms2);
		mLine.add(ms5);
		mLine.add(ms8);
		mLine.add(ms10);
	}

	public void initMenuOption()
	{
		mOption.setHorizontalTextPosition(SwingConstants.CENTER);
		mOption.setVerticalTextPosition(SwingConstants.CENTER);
		mOption.setLayout(new FlowLayout(FlowLayout.CENTER));

		mPass.setPreferredSize(new Dimension(150, 30));
		mCheat.setPreferredSize(new Dimension(150, 30));

		mCheat.addActionListener(new OptionActionListener("CHEAT", this));
		mPass.addActionListener(new OptionActionListener("PASS", this));
		
		mOption.add(mPass);
		mOption.add(mCheat);
	}


	public void setPassiveMode()
	{
		mYellow.setEnabled(false);
		mOrange.setEnabled(false);
		mRed.setEnabled(false);
		mPink.setEnabled(false);
		mViolet.setEnabled(false);
		mBlue.setEnabled(false);
		mGreen.setEnabled(false);
		mBlack.setEnabled(false);
		ms2.setEnabled(false);
		ms5.setEnabled(false);
		ms8.setEnabled(false);
		ms10.setEnabled(false);

		mPass.setEnabled(false);
		mCheat.setEnabled(true);
	}

	public void setActifMode()
	{
		mYellow.setEnabled(true);
		mOrange.setEnabled(true);
		mRed.setEnabled(true);
		mPink.setEnabled(true);
		mViolet.setEnabled(true);
		mBlue.setEnabled(true);
		mGreen.setEnabled(true);
		mBlack.setEnabled(true);
		ms2.setEnabled(true);
		ms5.setEnabled(true);
		ms8.setEnabled(true);
		ms10.setEnabled(true);

		mPass.setEnabled(true);
		mCheat.setEnabled(false);
	}


	/* COMMAND */

	public void sendPass(){ drawp.sendCommandPass(); }

	public void sendCheat()
	{ 
		drawp.sendCommandCheat();
		this.mCheat.setEnabled(false);
		this.mCheat.setForeground(Color.WHITE);
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
