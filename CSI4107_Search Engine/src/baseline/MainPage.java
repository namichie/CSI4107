package baseline;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class MainPage {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainPage window = new MainPage();
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
	public MainPage() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setForeground(Color.BLACK);
		frame.setBounds(100, 100, 712, 475);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSearchEngine = new JLabel("Search Engine");
		lblSearchEngine.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSearchEngine.setForeground(Color.BLACK);
		lblSearchEngine.setBounds(29, 36, 285, 19);
		frame.getContentPane().add(lblSearchEngine);
		
		JLabel lblQuery = new JLabel("Query:");
		lblQuery.setBounds(27, 82, 49, 14);
		frame.getContentPane().add(lblQuery);
		
		textField = new JTextField();
		textField.setBounds(69, 79, 203, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(275, 78, 89, 23);
		frame.getContentPane().add(btnSearch);
		
		JLabel lblStopwordRemoval = new JLabel("Stopword removal:");
		lblStopwordRemoval.setBounds(27, 126, 117, 14);
		frame.getContentPane().add(lblStopwordRemoval);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Yes", "No"}));
		comboBox.setBounds(154, 122, 69, 22);
		frame.getContentPane().add(comboBox);
		
		JLabel lblStemming = new JLabel("Stemming:");
		lblStemming.setBounds(29, 160, 115, 14);
		frame.getContentPane().add(lblStemming);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"Yes", "No"}));
		comboBox_1.setBounds(154, 152, 69, 22);
		frame.getContentPane().add(comboBox_1);
		
		JLabel lblNormalization = new JLabel("Normalization:");
		lblNormalization.setBounds(29, 195, 115, 14);
		frame.getContentPane().add(lblNormalization);
		
		JComboBox comboBox_2 = new JComboBox();
		comboBox_2.setModel(new DefaultComboBoxModel(new String[] {"Yes", "No"}));
		comboBox_2.setBounds(154, 187, 69, 22);
		frame.getContentPane().add(comboBox_2);
	}
}
