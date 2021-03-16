package fop.view.menu;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import fop.io.*;
import fop.model.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import fop.view.MainFrame;

import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		
		//Score table
		GridBagConstraints tableConstraints = new GridBagConstraints ();
		tableConstraints.weightx = 1.0;
		tableConstraints.weighty = 9.0;
		tableConstraints.ipady = 0;
		tableConstraints.fill = GridBagConstraints.BOTH;
		tableConstraints.insets = new Insets(0, 2, 2, 2);
		tableConstraints.gridwidth = 2;
		tableConstraints.gridx = 0;
		tableConstraints.gridy = 0;
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
		contentPanel.add(new JScrollPane(scoreTable),tableConstraints);
        


        // back button //
		/*
		GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.fill = GridBagConstraints.BOTH;
        buttonGBC.anchor = GridBagConstraints.CENTER;
		buttonGBC.insets = new Insets (5,2,0,2);
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 1;
		*/
		tableConstraints.gridx = 0;
		tableConstraints.gridy = 1;
		tableConstraints.gridwidth = 1;
		tableConstraints.anchor = GridBagConstraints.SOUTHEAST;
		tableConstraints.fill = GridBagConstraints.NONE;
		tableConstraints.ipady = 0;
		JButton backButton = createButton("Zurück");
		backButton.addActionListener(evt -> getWindow().setView(new MainMenu(getWindow())));
		contentPanel.add(backButton, tableConstraints);

		tableConstraints.anchor = GridBagConstraints.SOUTHWEST;
		tableConstraints.gridx = 1;
		JButton clearButton = createButton("Löschen");
		clearButton.addActionListener(evt -> { ScoreEntryIO.clearHighScore();
												getWindow().setView(new HighscoreView(getWindow()));});
		contentPanel.add(clearButton, tableConstraints);

		/*
		GridBagConstraints buttonGBC2 = new GridBagConstraints();
        buttonGBC2.fill = GridBagConstraints.BOTH;
        buttonGBC2.anchor = GridBagConstraints.CENTER;
		buttonGBC2.insets = new Insets (5,2,0,2);
		buttonGBC2.gridx = 1;
		buttonGBC2.gridy = 1;
		buttonGBC2.insets = new Insets (5,2,0,2);
		buttonGBC.fill = GridBagConstraints.NONE;*/

    }
}
