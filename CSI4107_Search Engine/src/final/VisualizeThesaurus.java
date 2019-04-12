import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class VisualizeThesaurus {

	private JFrame frame;
	private JTable table;
	DictionaryBuilder db;
	Thesaurus th;

	/**
	 * Launch the application.
	 
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VisualizeThesaurus window = new VisualizeThesaurus();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
*/
	/**
	 * Create the application.
	 */
	public VisualizeThesaurus(String[] uniqueTokens, DictionaryBuilder db) {
		initialize(uniqueTokens);
		this.db = db;
		this.th = new Thesaurus(db);
	}

	public void openWindow() {
		frame.setVisible(true);

	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String[] uniqueTokens) {
		frame = new JFrame();
		frame.setBounds(100, 100, 819, 551);
		frame.getContentPane().setLayout(null); //mettre les objets à la position qu'on veuts
		
		JLabel lblVizualizationOfAutomatic = new JLabel("Vizualization of Automatic Thesaurus");
		lblVizualizationOfAutomatic.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblVizualizationOfAutomatic.setBounds(20, 22, 359, 14);
		frame.getContentPane().add(lblVizualizationOfAutomatic);
		
		JLabel lblSelectWhichWord = new JLabel("Select which word you want to see the similarity:");
		lblSelectWhichWord.setBounds(31, 61, 314, 14);
		frame.getContentPane().add(lblSelectWhichWord);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(34, 86, 283, 378);
		frame.getContentPane().add(scrollPane);
		
		JList list = new JList(uniqueTokens); //show uniqueTokens values
		scrollPane.setViewportView(list);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(495, 86, 283, 378);
		frame.getContentPane().add(scrollPane_1);
		
		table = new JTable();
		scrollPane_1.setViewportView(table);
		
		JButton btnViewSimilarity = new JButton("View Similarity");
		btnViewSimilarity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				List lst = list.getSelectedValuesList();
				String selectedVal = "";
				for (int i = 0; i < lst.size(); i++) {
					selectedVal = lst.get(i).toString();
				}
				String[][] description = th.get15Results(selectedVal);
				
				String[] columnNames = {"Word", "Similarity"};
				
				//update table
				table.setModel(new DefaultTableModel(description, columnNames));
				table.getColumnModel().getColumn(0).setPreferredWidth(100);
				table.getColumnModel().getColumn(0).setMinWidth(100);
				table.getColumnModel().getColumn(0).setMaxWidth(200);
				scrollPane_1.setViewportView(table);
				
			}
		});
		btnViewSimilarity.setBounds(340, 232, 135, 23);
		frame.getContentPane().add(btnViewSimilarity);
		
		JLabel lblTopResults = new JLabel("Top 15 Results:");
		lblTopResults.setBounds(495, 61, 287, 14);
		frame.getContentPane().add(lblTopResults);
	}
}
