package fop.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fop.model.ScoreEntry;

/**
 * 
 * Wird genutzt, um {@link ScoreEntry} Objekte zu schreiben und zu lesen.<br>
 * <br>
 * Es handelt sich um die Datei {@value #PATH}.<br>
 * Mit {@link #loadScoreEntries()} werden die Elemente gelesen.<br>
 * Mit {@link #writeScoreEntries(List)} werden die Elemente geschrieben.
 *
 */
public final class ScoreEntryIO {
	
	/** Der Pfad zur ScoreEntry Datei */
	private static String PATH = "highscores.txt";
	
	private ScoreEntryIO() {}
	
	/**
	 * Liest eine Liste von {@link ScoreEntry} Objekten aus der Datei {@value #PATH}.<br>
	 * Die Liste enthält die Elemente in der Reihenfolge, in der sie in der Datei vorkommen.<br>
	 * Ungültige Einträge werden nicht zurückgegeben.
	 * @return die ScoreEntry Objekte
	 */
	public static List<ScoreEntry> loadScoreEntries() {
		// TODO Aufgabe 4.2.2
		
		// linked list to store all the Score Entries
		List<ScoreEntry> result = new LinkedList<ScoreEntry>();
		
		// read all the Score Entries to the linked list
		try(FileReader reader1 = new FileReader(PATH);
			BufferedReader reader2 = new BufferedReader(reader1)){
			
			String str = "";
			
			// read each Score Entry in each line and add it in the list
			while((str = reader2.readLine()) != null)
			{
				ScoreEntry entry = ScoreEntry.read(str);
				if (entry == null)
				{
					continue;
				}
				result.add(entry);
			}
			
		}
		catch(IOException exc) {	// return a empty list when there is IOError
			return new LinkedList<ScoreEntry>();
		}
		//return the completed list of score entries
		return result;
	}
	
	/**
	 * Schreibt eine Liste von {@link ScoreEntry} Objekten in die Datei {@value #PATH}.<br>
	 * Die Elemente werden in der Reihenfolge in die Datei geschrieben, in der sie in der Liste vorkommen.
	 * @param scoreEntries die zu schreibenden ScoreEntry Objekte
	 */
	public static void writeScoreEntries(List<ScoreEntry> scoreEntries) {
		// TODO Aufgabe 4.2.2	
		
		// write all the score entries in the file
		try(FileWriter writer1 = new FileWriter(PATH);
			PrintWriter writer2 = new PrintWriter(writer1)){
			//make a iterator from the list of score entries
			Iterator<ScoreEntry> iterator = scoreEntries.iterator();
			//loop through the iterator and write each score entry in the file
			while(iterator.hasNext()) {
				ScoreEntry entry = iterator.next();
				if (entry == null)
					continue;
				entry.write(writer2);
			}
		}
		catch(IOException exc) {

		}
	}
	
	/**
	 * Schreibt das übergebene {@link ScoreEntry} Objekt an der korrekten Stelle in die Datei {@value #PATH}.<br>
	 * Die Elemente sollen absteigend sortiert sein. Wenn das übergebene Element dieselbe Punktzahl wie ein
	 * Element der Datei hat, soll das übergebene Element danach eingefügt werden.
	 * @param scoreEntry das ScoreEntry Objekt, das hinzugefügt werden soll
	 */
	public static void addScoreEntry(ScoreEntry scoreEntry) {
		// TODO Aufgabe 4.2.3
		// read all the score entries from file
		List<ScoreEntry> entries = loadScoreEntries();
		
		// create an iterator to traverse through the list
		Iterator<ScoreEntry> iterator = entries.iterator();
		
		// variable index to mark the searched position in the list
		int index = 0;
		
		// check if the correct added position is found
		boolean found = false;
		
		// traverse through the list
		while(iterator.hasNext()) {
			ScoreEntry entry = iterator.next();
			
			// if the entry in list is bigger in placement than the given entry, then we should add the entry in this position
			if (entry.compareTo(scoreEntry) < 0) {
				entries.add(index, scoreEntry);
				found = true;
				break;
			}
			// or if it is the same in placement
			else if (entry.compareTo(scoreEntry) == 0) {
				// go through all the equal entry to add it at the end 
				while((entry.compareTo(scoreEntry) == 0)) {
					index++;
					if (iterator.hasNext())
						entry = iterator.next();
					else
						break;
				}
				entries.add(index, scoreEntry);
				found = true;
				break;
			}
			// increment the the position to track the next element
			index++;
		}
		
		//add in the end if there is no appropriate position between the list
		if (!found) {
			entries.add(index, scoreEntry);
		}
		
		//write the score entries back in the file
		writeScoreEntries(entries);
	}
	
}
