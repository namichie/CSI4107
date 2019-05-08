import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.Font;

public class Description {

	private JFrame frame;
	public JTextArea descriptionTxt;
	private JScrollPane scroll;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Description window = new Description();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Description() {
		initialize();
	}
	
	public void openWindow() {
		frame.setVisible(true);

	}

	/**
	 * Initialize the contents of the frame.
	 */
	
	// https://stackoverflow.com/questions/1052473/scrollbars-in-jtextarea
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 872, 276);
		frame.getContentPane().setLayout(null);
		
		descriptionTxt = new JTextArea();
		descriptionTxt.setLineWrap(true);
		descriptionTxt.setEditable(false);
		descriptionTxt.setBounds(29, 36, 795, 180);

		scroll = new JScrollPane(descriptionTxt, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBounds(29, 36, 795, 180);
		frame.getContentPane().add(scroll);
		scroll.setViewportView(descriptionTxt);
		
		JLabel lblDescription = new JLabel("Description:");
		lblDescription.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblDescription.setBounds(29, 11, 96, 14);
		frame.getContentPane().add(lblDescription);
		
	}
}
