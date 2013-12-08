import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ChatPanel extends JPanel implements ActionListener, KeyListener
{
	private MainWindow mWindow;

	private int width;
	private int height;

	private JTextArea msgArea;
	private JButton btn = new JButton("Chat");
	private JTextField txtField = new JTextField();
	private JScrollPane scrollPane;
	private JPanel sendPanel = new JPanel(null);

	public ChatPanel(MainWindow mw, int w, int h)
	{
		this.mWindow = mw;
		this.width = w;
		this.height = h;
		this.setBackground(Color.DARK_GRAY);

		this.setPreferredSize(new Dimension(this.width, this.height));

		this.initTextArea();
		this.initTextField();
		this.initSendZone();

		this.add(scrollPane);
		this.add(sendPanel);
	}

	/* INITIALISATION */

	private void initTextArea()
	{
		this.msgArea = new JTextArea(5, 45);
		this.msgArea.setEditable(false);
		this.msgArea.setLineWrap(true);
		this.msgArea.setBackground(Color.LIGHT_GRAY);
		this.scrollPane = new JScrollPane(msgArea);
		this.scrollPane.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
	}

	private void initTextField()
	{
		txtField.setPreferredSize(new Dimension( (this.width * 3 / 4 - 10) * 3/4, this.height/ 4));
		txtField.setBackground(Color.lightGray);
		txtField.setAlignmentX(LEFT_ALIGNMENT);
		txtField.setAlignmentY(CENTER_ALIGNMENT);
		txtField.addKeyListener(this);
	}

	private void initSendZone()
	{
		this.sendPanel.setLayout(new BorderLayout());
		this.sendPanel.setBackground(Color.DARK_GRAY);
		this.sendPanel.setPreferredSize(new Dimension((this.width * 3) / 4 - 10, this.height/ 4));
		this.btn.addActionListener(this);
		this.sendPanel.add(txtField, BorderLayout.WEST);
		this.sendPanel.add(btn, BorderLayout.EAST);
	}

	
	/* COMMAND */
	
	public void chat(String[] tab)
	{
		this.msgArea.append(tab[1] + ": ");
		this.msgArea.append(tab[2] + "\n");
	}
	

	/* BUTTONS */
	
	public void disableZone()
	{
		this.btn.setEnabled(false);
		this.txtField.removeKeyListener(this);
	}
	
	public void availableZone()
	{
		this.btn.setEnabled(true);
		this.txtField.addKeyListener(this);
	}
	
	
	/* LISTENER */

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (e.getKeyCode()==KeyEvent.VK_ENTER)
			if (!this.txtField.getText().isEmpty())
			{
				this.mWindow.talk(this.txtField.getText());
				this.txtField.setText("");
			}	
	}

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void actionPerformed(ActionEvent e) 
	{ 
		if (!this.txtField.getText().isEmpty())
		{
			this.mWindow.talk(this.txtField.getText());
			this.txtField.setText("");
		}
	}

}
