package fop.model;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * 
 * Speichert einen Highscore-Eintrag, der aus dem Namen des Spielers,
 * einem Zeitstempel und der erzielten Punktzahl besteht.
 *
 */
public class ScoreEntry implements Comparable<ScoreEntry> {
	
	protected String name;
	protected LocalDateTime dateTime;
	protected int score;
	
	/**
	 * Erstellt eine neue ScoreEntry.
	 * @param name der Name des Spielers
	 * @param dateTime Datum und Zeit des Spiels
	 * @param score die erreichte Punktzahl
	 */
	public ScoreEntry(String name, LocalDateTime dateTime, int score) {
		this.name = name;
		this.dateTime = dateTime;
		this.score = score;
	}
	
	// load and save //
	
	/**
	 * split the input string into 3 substring as an array, 
	 * each substring in the input string should then be separated by the semicolon ";"
	 * @param line: input string that should contain 2 semicolon and 1 line
	 * @return 3 substrings as an array
	 * @return {@code null}, if there are more or less than 2 semicolon, or the input string contains more than 1 line
	 */
	public static String[] split (String line){
        String [] splitLine = {"","",""};
        int count = 0;
        //Split the String line
        for (char i : line.toCharArray()) {
            if (i == ';') {
                count++;
                continue;
            }
            if (count > 2 || i == '\n') {
                return null;
            }
            splitLine[count] += i;
        }
		if (count != 2) {
			return null;
		}
        return splitLine;
    }

	/**
	 * Wandelt eine Zeile in ein ScoreEntry Objekt.<br>
	 * Gibt {@code null} zurück, wenn die Zeile nicht in ein
	 * ScoreEntry Objekt umgewandelt werden kann.<br>
	 * Format: {@code name;dateTime;score}
	 * @param line die zu lesende Zeile
	 * @return das neue ScoreEntry Objekt; oder {@code null}
	 */
	public static ScoreEntry read(String line) {
		// TODO Aufgabe 4.2.1

		String[] entryArray = split(line);
		if (entryArray == null) {
			return null;
		}
		String newName = entryArray[0];

		int newScore = 0;
		LocalDateTime newTime = null;

		try {
			newScore = Integer.parseInt(entryArray[2]);
			if (newScore < 0) {
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		
		try {
			newTime = LocalDateTime.parse(entryArray[1]);
		} catch (DateTimeParseException e) {
			return null;
		}
		
		return new ScoreEntry (newName, newTime, newScore);
	}
	
	/**
	 * Schreibt das ScoreEntry Objekt mit dem übergebenen {@link PrintWriter}.<br>
	 * Format: {@code name;dateTime;score}
	 * @param printWriter der PrintWriter
	 */
	public void write(PrintWriter printWriter) {
		// TODO Aufgabe 4.2.1
		String entryString = name + ";" + dateTime.toString() + ";" + Integer.toString(score);
		printWriter.println(entryString);
	}
	
	// get //
	
	public String getName() {
		return name;
	}
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	public int getScore() {
		return score;
	}
	
	// Comparable //
	
	@Override
	public int compareTo(ScoreEntry other) {
		return Integer.compare(score, other.score);
	}
	
	// Object //
	
	@Override
	public String toString() {
		return "ScoreEntry [name=" + name + ", dateTime=" + dateTime + ", score=" + score + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dateTime == null ? 0 : dateTime.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + score;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ScoreEntry other = (ScoreEntry) obj;
		if (dateTime == null) {
			if (other.dateTime != null) return false;
		} else if (!dateTime.equals(other.dateTime)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (score != other.score) return false;
		return true;
	}
	
}
