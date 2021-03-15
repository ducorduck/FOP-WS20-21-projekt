package fop.view.menu;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import fop.controller.*;
import fop.io.*;
import fop.model.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import fop.io.IconReader;
import fop.view.MainFrame;
import fop.view.View;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HighscoreView extends MenuView {

    public HighscoreView(MainFrame window) {
        super(window, "Highscores");
    }


	private static final String [] columnNames = {"Datum und Uhrzeit", "Name", "Punkte"};
	Object [][] scoreEntries = {{"2020","A","2"},{"2020","B","1"}};

    @Override
    protected void addContent(JPanel contentPanel) {
        // TODO Auto-generated method stub
		
		//Table thing
		JTable scoreTable = new JTable (scoreEntries, columnNames);
		GridBagConstraints tableConstraints = new GridBagConstraints ();
		tableConstraints.fill = GridBagConstraints.BOTH;
		tableConstraints.insets = new Insets(0,2,2,2);
		tableConstraints.gridx = 0;
		tableConstraints.gridy = 0;
		JScrollPane scroll = new JScrollPane(scoreTable);
		scoreTable.setFillsViewportHeight(true);
		//contentPanel.add(scroll, tableConstraints);
        
        // back button //
		GridBagConstraints rightImageConstraints = new GridBagConstraints();
		rightImageConstraints.insets = new Insets(2, 2, 0, 2);
		rightImageConstraints.gridx = 0;
		rightImageConstraints.gridy = 1;
		JButton backButton = createButton("Zurück");
		backButton.addActionListener(evt -> getWindow().setView(new MainMenu(getWindow())));
		contentPanel.add(backButton, rightImageConstraints);
        
    }
}
