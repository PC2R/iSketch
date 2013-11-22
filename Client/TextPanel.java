import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/* Zone qui permet d'écrire et d'envoyer des messages */

@SuppressWarnings("serial")
public class TextPanel extends JPanel implements ActionListener , KeyListener{

	private JTextField textField = new JTextField();
	private JButton btnSend = new JButton("Send");
	private MainWindow parent;
	

	TextPanel(int wWidth, int wHeight, MainWindow w)
	{
		this.setLayout(new FlowLayout(FlowLayout.CENTER));

		textField.setPreferredSize(new Dimension(wWidth / 2 - 50, 50));
		textField.setBackground(Color.lightGray);
		textField.setAlignmentX(LEFT_ALIGNMENT);
		textField.setAlignmentY(CENTER_ALIGNMENT);

		btnSend.setPreferredSize(new Dimension(wWidth / 6, 30));
		btnSend.setForeground(Color.black);
		btnSend.addActionListener(this);
		
		this.parent = w;

		this.add(textField);
		this.add(btnSend);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(wWidth / 2, wHeight / 5));
		this.setMinimumSize(new Dimension(wWidth / 2, wHeight / 5));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!this.textField.getText().isEmpty())
		{
			this.parent.sendProposition(this.textField.getText());
			this.textField.setText("");
		}
	}

	@Override
	public void keyPressed(KeyEvent evt)
	{
		if (evt.getKeyCode()==KeyEvent.VK_ENTER)
			if (!this.textField.getText().isEmpty())
			{
				this.parent.sendProposition(this.textField.getText());
				this.textField.setText("");
			}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
}
