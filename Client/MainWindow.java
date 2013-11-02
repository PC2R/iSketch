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
	private JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private JPanel listJoueurPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private JPanel eastPanel = new JPanel(null);
	private JPanel joueurMsgPanel = new JPanel(null);
	private JPanel globalPanel = new JPanel(null);
	private JPanel txtPanel = new JPanel(null);

	private JTextField textField = new JTextField();
	private JButton btnSend = new JButton("Send");
	private JTextArea textListJoueur = new JTextArea();
	private JTextArea textMsg = new JTextArea();

	public MainWindow()
	{
		this.setTitle("Joue !");
		this.setSize(wWidth, wHeight);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		textField.setPreferredSize(new Dimension(wWidth / 2 - 50, 50));
		textField.setBackground(Color.lightGray);
		textField.setAlignmentX(LEFT_ALIGNMENT);
		textField.setAlignmentY(CENTER_ALIGNMENT);
		
		textListJoueur.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textListJoueur.setAlignmentY(TOP_ALIGNMENT);
		textListJoueur.setText("Liste des joueurs : ");
		textListJoueur.setEditable(false);
		textListJoueur.setBackground(Color.lightGray);
		
		textMsg.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textMsg.setAlignmentY(TOP_ALIGNMENT);
		textMsg.setText("Propositions : ");
		textMsg.setEditable(false);
		textMsg.setBackground(Color.lightGray);
		
		btnSend.setPreferredSize(new Dimension(wWidth / 6, 30));
		btnSend.setForeground(Color.black);
		
		txtPanel.add(textField);
		txtPanel.add(btnSend);
		txtPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		txtPanel.setBackground(Color.white);
		txtPanel.setPreferredSize(new Dimension(wWidth / 2, wHeight / 5));

		drawPanel.setBackground(Color.white);
		drawPanel.setPreferredSize(new Dimension(wWidth / 2, wHeight));

		msgPanel.setBackground(Color.white);
		msgPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.add(textMsg);

		listJoueurPanel.setBackground(Color.white);
		listJoueurPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.add(textListJoueur);

		// Mise en place des différents panels sur la fenêtre //
		joueurMsgPanel.setLayout(new BoxLayout(joueurMsgPanel, BoxLayout.LINE_AXIS));
		joueurMsgPanel.setPreferredSize(new Dimension(wWidth / 2, wHeight));
		joueurMsgPanel.add(listJoueurPanel);
		joueurMsgPanel.add(msgPanel);
		
		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.PAGE_AXIS));
		eastPanel.setPreferredSize(new Dimension(wWidth / 2, wHeight));
		eastPanel.add(joueurMsgPanel);
		eastPanel.add(txtPanel, BorderLayout.SOUTH);
		
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.LINE_AXIS));
		globalPanel.add(eastPanel);
		globalPanel.add(drawPanel);
		this.getContentPane().add(globalPanel);
		this.setVisible(true);
	}

}
