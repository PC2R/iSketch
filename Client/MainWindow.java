import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.*;


@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private static int wHeight = 600;
	private static int wWidth = 1100;
	
	private JPanel drawPanel = new JPanel(null);
	private JPanel westPanel = new JPanel(null);
	private JPanel globalPanel = new JPanel(null);
	
	private TextPanel textP = new TextPanel(wWidth, wHeight);
	private MessagesPanel messP = new MessagesPanel(wWidth, wHeight);

	public MainWindow()
	{
		this.setTitle("Joue !");
		this.setSize(wWidth, wHeight);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		drawPanel.setBackground(Color.white);
		drawPanel.setPreferredSize(new Dimension(wWidth / 2, wHeight));
		
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.PAGE_AXIS));
		westPanel.setPreferredSize(new Dimension(wWidth / 2, wHeight));
		westPanel.add(messP);
		westPanel.add(textP, BorderLayout.SOUTH);
		
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.LINE_AXIS));
		globalPanel.add(westPanel);
		globalPanel.add(drawPanel);
		
		this.getContentPane().add(globalPanel);
		this.setVisible(true);
	}

}
