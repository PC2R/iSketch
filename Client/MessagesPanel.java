import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.ws.handler.MessageContext.Scope;

/* Zone d'affichage de la liste des joueurs et des différents messages envoyés */

@SuppressWarnings("serial")
public class MessagesPanel extends JPanel {
	
	private JPanel msgPanel = new JPanel(null);
	private JPanel listJoueurPanel = new JPanel(null);
	
	private JTextArea textListJoueur = new JTextArea();
	private JTextArea textMsg = new JTextArea();
	
	private JLabel titleListJoueur = new JLabel("Liste des joueurs");
	private JLabel titleMsg = new JLabel("Propositions");
	
	private JScrollPane jScroll;
	
	MessagesPanel(int wWidth, int wHeight)
	{
		titleListJoueur.setForeground(Color.WHITE);
		titleListJoueur.setFont(new Font("Arial", Font.BOLD, 16));
		
		titleMsg.setForeground(Color.WHITE);
		titleMsg.setFont(new Font("Arial", Font.BOLD, 16));
		
		textListJoueur.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textListJoueur.setMaximumSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textListJoueur.setAlignmentY(TOP_ALIGNMENT);
		textListJoueur.setFont(new Font("Arial", Font.BOLD, 14));
		textListJoueur.setEditable(false);
		textListJoueur.setBackground(Color.lightGray);
		
		jScroll = new JScrollPane(textMsg, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		textMsg.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textMsg.setMaximumSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		textMsg.setAlignmentY(TOP_ALIGNMENT);
		textMsg.setFont(new Font("Arial", Font.BOLD, 14));
		textMsg.setEditable(false);
		textMsg.setBackground(Color.lightGray);
		textMsg.setLineWrap(true);
		
		msgPanel.setBackground(Color.DARK_GRAY);
		msgPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.setMaximumSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		//msgPanel.add(titleMsg);
		msgPanel.add(textMsg);

		listJoueurPanel.setBackground(Color.DARK_GRAY);
		listJoueurPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.setMaximumSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		//listJoueurPanel.add(titleListJoueur);
		listJoueurPanel.add(textListJoueur);
		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setPreferredSize(new Dimension(wWidth / 2, wHeight));
		this.setMaximumSize(new Dimension(wWidth / 2, wHeight));
		this.add(listJoueurPanel);
		this.add(msgPanel);
	}
	
	public void addPlayer(String name, String score)
	{
		this.textListJoueur.append(name);
		this.textListJoueur.append("\t");
		this.textListJoueur.append(score);
		this.textListJoueur.append("\n");
	}
	
	public void setMode(boolean m, String word)
	{
		if (m)
			this.textMsg.append("Vous etes le dessinateur.\nVous devez dessiner le mot " + word + "\n");
		else
			this.textMsg.append("La partie commence\nDevinez le mot qui se dessine à l'écran\n");
	}
	
	public void newProp(String msg)
	{
		this.textMsg.append(msg);
		this.textMsg.append("\n");
	}
	
	public void newProp(String name, String msg)
	{
		this.textMsg.append(name + " : " + msg);
		this.textMsg.append("\n");
	}

	public void wordFound(String name)
	{
		textMsg.append("le mot a été trouvé par ");
		textMsg.append(name);
		//textMsg.append("Bravo");
		textMsg.append("!\n");
	}

	public void wordFoundTimeOut(String time)
	{
		textMsg.append("Il vous reste ");
		textMsg.append(time);
		textMsg.append(" secondes pour trouver le mot\n");
	}
	
	public void scoreout(String[] tab)
	{
		int i;
		
		textListJoueur.removeAll();
		for (i = 1; i < tab.length - 1 ; i = i + 2)
		{
			textListJoueur.append(tab[i] + "\t" + tab[i + 1]);
			textListJoueur.append("\n");
		}
	}
}
