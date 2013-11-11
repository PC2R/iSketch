import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/* Zone qui permet d'écrire et d'envoyer des messages */

@SuppressWarnings("serial")
public class TextPanel extends JPanel{

	private JTextField textField = new JTextField();
	private JButton btnSend = new JButton("Send");

	TextPanel(int wWidth, int wHeight)
	{
		this.setLayout(new FlowLayout(FlowLayout.CENTER));

		textField.setPreferredSize(new Dimension(wWidth / 2 - 50, 50));
		textField.setBackground(Color.lightGray);
		textField.setAlignmentX(LEFT_ALIGNMENT);
		textField.setAlignmentY(CENTER_ALIGNMENT);

		btnSend.setPreferredSize(new Dimension(wWidth / 6, 30));
		btnSend.setForeground(Color.black);

		this.add(textField);
		this.add(btnSend);
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(wWidth / 2, wHeight / 5));
	}
}
