import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.xml.ws.handler.MessageContext.Scope;

/* Zone d'affichage de la liste des joueurs et des différents messages envoyés */

@SuppressWarnings("serial")
public class MessagesPanel extends JPanel {
	
	private JPanel msgPanel = new JPanel(null);
	private JPanel listJoueurPanel = new JPanel(null);
	
	private JTextArea textListJoueur;
	private JTextArea textMsg;
	
	private JLabel titleListJoueur = new JLabel("Liste des joueurs");
	private JLabel titleMsg = new JLabel("Propositions");
	
	private JScrollPane scrollMsg;
	private JScrollPane scrollList;
	
	MessagesPanel(int wWidth, int wHeight)
	{
		titleListJoueur.setForeground(Color.WHITE);
		titleListJoueur.setFont(new Font("Arial", Font.BOLD, 16));
		
		titleMsg.setForeground(Color.WHITE);
		titleMsg.setFont(new Font("Arial", Font.BOLD, 16));
		
		//textListJoueur.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		//textListJoueur.setMaximumSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		//textListJoueur.setAlignmentY(TOP_ALIGNMENT);
		textListJoueur = new JTextArea("", 20, 10);
		textListJoueur.setFont(new Font("Arial", Font.BOLD, 14));
		textListJoueur.setEditable(false);
		textListJoueur.setBackground(Color.lightGray);
		
		scrollList = new JScrollPane(textListJoueur);
		scrollList.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollList.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		
		//textMsg.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		//textMsg.setMaximumSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		//textMsg.setAlignmentY(TOP_ALIGNMENT);
		textMsg = new JTextArea("", 20, 10);
		textMsg.setFont(new Font("Arial", Font.BOLD, 14));
		textMsg.setEditable(false);
		textMsg.setBackground(Color.lightGray);
		textMsg.setLineWrap(true);
		
		scrollMsg = new JScrollPane(textMsg);
		scrollMsg.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollMsg.setPreferredSize(new Dimension(wWidth / 4 - 20, 4 * wHeight / 5 - 20));
		
		msgPanel.setBackground(Color.DARK_GRAY);
		msgPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.setMaximumSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		msgPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		//msgPanel.add(titleMsg);
		msgPanel.add(scrollMsg);

		listJoueurPanel.setBackground(Color.DARK_GRAY);
		listJoueurPanel.setPreferredSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.setMaximumSize(new Dimension(wWidth / 4, 4 * wHeight / 5));
		listJoueurPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		//listJoueurPanel.add(titleListJoueur);
		listJoueurPanel.add(scrollList);
		
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
		this.textMsg.append("Un nouveau round commence\n");
		if (m)
			this.textMsg.append("Vous etes le dessinateur.\nVous devez dessiner le mot :\n" + word + "\n\n");
		else
			this.textMsg.append("Devinez le mot\n\n");
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
		textMsg.append("\nLe mot a été trouvé par ");
		textMsg.append(name);
		textMsg.append("!\n");
	}

	public void wordFoundTimeOut(String time)
	{
		textMsg.append("\nIl vous reste ");
		textMsg.append(time);
		textMsg.append(" secondes pour trouver le mot\n");
	}
	
	public void endRound(String[] tab)
	{
		textMsg.append("\nLe round est terminé\n");
		if (tab[1].isEmpty())
			textMsg.append("Le dessinateur choisit de passer son tour\n");
		else
			textMsg.append("Le gagnant est :\n" + tab[1] + "\n");
		textMsg.append("Le mot a trouver était :\n" + tab[2] + "\n\n");
	}
	
	public void scoreout(ArrayList<Player> list)
	{
		int i;
		
		textListJoueur.setText("");
		for (i = 0; i < list.size() ; i = i + 1)
		{
			textListJoueur.append(list.get(i).getPseudo() + "\t" + list.get(i).getScore());
			textListJoueur.append("\n");
		}
	}
	
	public void broadcast(String[] tab)
	{
		textMsg.append(tab[1]);
		textMsg.append("\n");
	}
	
	public void exitFinder(String[] tab) { textMsg.append("\n" + tab[1] + " a quitté le jeu\n"); }
	
	public void exitDrawer(String[] tab) { textMsg.append("\nLe dessinateur a quitté la partie\n"); }
}
