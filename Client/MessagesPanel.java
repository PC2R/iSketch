import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.ws.handler.MessageContext.Scope;

/* Zone d'affichage de la liste des joueurs et des différents messages envoyés */

@SuppressWarnings("serial")
public class MessagesPanel extends JPanel {
	
	private JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private JPanel listJoueurPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	
	private JTextArea textListJoueur = new JTextArea();
	private JTextArea textMsg = new JTextArea();
	
	private JScrollPane jScroll;
	
	MessagesPanel(int wWidth, int wHeight)
	{
		textListJoueur.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textListJoueur.setMaximumSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textListJoueur.setAlignmentY(TOP_ALIGNMENT);
		textListJoueur.setText("Liste des joueurs : \n");
		textListJoueur.setEditable(false);
		textListJoueur.setBackground(Color.lightGray);
		
		jScroll = new JScrollPane(textMsg, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		textMsg.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textMsg.setMaximumSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textMsg.setAlignmentY(TOP_ALIGNMENT);
		textMsg.setText("Propositions : \n");
		textMsg.setEditable(false);
		textMsg.setBackground(Color.lightGray);
		
		msgPanel.setBackground(Color.white);
		msgPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.setMaximumSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.add(textMsg);

		listJoueurPanel.setBackground(Color.white);
		listJoueurPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.setMaximumSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.add(textListJoueur);
		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setPreferredSize(new Dimension(wWidth / 2, wHeight));
		this.setMaximumSize(new Dimension(wWidth / 2, wHeight));
		this.add(listJoueurPanel);
		this.add(msgPanel);
	}
	
	public void newProp(String msg)
	{
		this.textMsg.append(msg);
		this.textMsg.append("\n");
	}
	
	public void newProp(String name, String msg)
	{
		this.textMsg.append(name + " : " + msg);
	}

}
