import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private static int wHeight = 600;
	private static int wWidth = 1100;
	
	private JPanel drawPanel = new JPanel(null);
	private JPanel westPanel = new JPanel(null);
	private JPanel globalPanel = new JPanel(null);
	
	private TextPanel textP = new TextPanel(wWidth, wHeight, this);
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
	
	public void sendProposition(String prop)
	{
		this.messP.newProp(prop);
	}

	public void guessed(String[] tab)
	{
		System.out.println("Le mot " + tab[1] + " a été proposé par " + tab[2]);
		this.messP.newProp(tab[2], tab[1]);
		
	}
	
	public void wordFound(String[] tab)
	{
		System.out.println("Le mot a été trouvé par " + tab[1]);
	}
	
	public void wordFoundTimeOut(String[] tab)
	{
		System.out.println("Temps impartis écoulé!");
	}
	
	public void scoreOut(String[] tab)
	{
		System.out.println("Voici les scores");
	}
	
	public void endRound(String[] tab)
	{
		System.out.println("Le round est terminé");
		System.out.println("Le gagnant est " + tab[1]);
		System.out.println("Le mot à trouver était " + tab[2]);
	}
}
