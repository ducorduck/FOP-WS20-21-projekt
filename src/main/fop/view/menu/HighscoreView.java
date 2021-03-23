package fop.view.menu;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;

import fop.io.ScoreEntryIO;
import fop.model.ScoreEntry;
import fop.view.MainFrame;

import java.util.List;

public class HighscoreView extends MenuView {

    /**
	 *
	 */
	private static final long serialVersionUID = -8498104564307083598L;

	public HighscoreView(MainFrame window) {
        super(window, "Highscores");
    }

    @Override
    protected void addContent(JPanel contentPanel) {
        // TODO Auto-generated method stub
		contentPanel.setLayout(new GridBagLayout());

		GridBagConstraints tableConstraints = new GridBagConstraints();
		tableConstraints.weightx = 1.0;
		tableConstraints.weighty = 1.0;
		tableConstraints.fill = GridBagConstraints.BOTH;
		tableConstraints.insets = new Insets(0, 2, 2, 2);
		tableConstraints.gridx = 0;
		tableConstraints.gridy = 0;

		//create table
		DefaultTableModel tableModel = new DefaultTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = -2776817223569832037L;
			private List<String> columnNames = List.of("Datum und Uhrzeit", "Name", "Punkte");
			@Override
			public String getColumnName(int columnIndex) {
				return columnNames.get(columnIndex);
			}
			@Override
			public int getColumnCount() {
				return columnNames.size();
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable scoreTable = new JTable(tableModel);
		scoreTable.setColumnSelectionAllowed(false);
		scoreTable.setRowSelectionAllowed(false);
		scoreTable.getTableHeader().setReorderingAllowed(false);
		List<ScoreEntry> scoreEntries = ScoreEntryIO.loadScoreEntries();
		for (ScoreEntry i : scoreEntries) {
			String name = i.getName();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
			String date = i.getDateTime().format(formatter);
			String score = Integer.toString(i.getScore());
			String[] scoreDate = {date,name,score};
			tableModel.addRow(scoreDate);
		}
		JScrollPane scrollPane = new JScrollPane(scoreTable);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		contentPanel.add(scrollPane,tableConstraints);
        

        // button panel //
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(2, 2, 0, 2);
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 1;
		JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 0));
		// back button //
		JButton backButton = createButton("Zurück");
		backButton.addActionListener(evt -> getWindow().setView(new MainMenu(getWindow())));
		buttonPanel.add(backButton, buttonGBC);
		//clear button //
		JButton clearButton = createButton("Löschen");
		clearButton.addActionListener(evt -> { ScoreEntryIO.clearHighScore();
												getWindow().setView(new HighscoreView(getWindow()));});
		buttonPanel.add(clearButton, buttonGBC);

		contentPanel.add(buttonPanel, buttonGBC);

    }
}
