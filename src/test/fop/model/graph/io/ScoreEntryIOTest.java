package fop.model.graph.io;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import fop.io.ScoreEntryIO;
import fop.model.ScoreEntry;

public class ScoreEntryIOTest {
    
    private final String PATH = "highscores.txt";
    private ArrayList<String> memo = new ArrayList <String> ();

    ScoreEntry entry0 = new ScoreEntry ("A", LocalDateTime.now(), 100);
    ScoreEntry entry1 = new ScoreEntry ("B", LocalDateTime.MIN, 70);
    ScoreEntry entry2 = new ScoreEntry ("C", LocalDateTime.MAX, 30);
    ScoreEntry entry3 = new ScoreEntry ("D", LocalDateTime.of(2020,12,12,3,45), 120);
    ScoreEntry entry4 = new ScoreEntry ("E", LocalDateTime.of(2019,4,30,12,5), 90);
    ScoreEntry entry5 = new ScoreEntry ("F", LocalDateTime.of(2018,6,6,1,30), 200);
    ScoreEntry entry6 = new ScoreEntry ("H", LocalDateTime.of(2010,10,05,2,2,2), 250);
    ScoreEntry entry7 = new ScoreEntry ("I", LocalDateTime.now(), 150);
    ScoreEntry entry8 = new ScoreEntry ("J", LocalDateTime.of (1999,1,23,23,59,3,3), 190);
    ScoreEntry entry9 = new ScoreEntry ("K", LocalDateTime.MIN, 190);

    ScoreEntry[] entries00 = {};
    ScoreEntry[] entries01 = {entry0};
    ScoreEntry[] entries02 = {entry7};
    ScoreEntry[] entries03 = {entry1, entry8};
    ScoreEntry[] entries05 = {entry3, entry1, entry8};
    ScoreEntry[] entries06 = {entry8, entry1, entry3};
    ScoreEntry[] entries07 = {entry4, entry4, entry4};
    ScoreEntry[] entries08 = {entry9, entry8, entry2, entry4};
    ScoreEntry[] entries09 = {entry1, entry8, entry5, entry6};

    public String readFile () {
        String readString = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(PATH))) {
            for (String data; (data = reader.readLine()) != null;) {
                readString += data + " ";
            }
        } catch (IOException e) {

        }
        return readString;
    }

    public void writeFile (String... input) {
        try (PrintWriter writer = new PrintWriter (new FileWriter(PATH), true)) {
            for (String i : input)
                writer.println(i);
        } catch (IOException e) {

        }
    }

    @BeforeEach
    public void setUp () {
        try (BufferedReader reader = new BufferedReader(new FileReader(PATH))) {
            for (String data; (data = reader.readLine()) != null;) {
                memo.add(data);
            }
        } catch (IOException e) {
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(PATH))) {
            writer.write("");
        } catch (IOException e) {
        }
    }

    @AfterEach
    public void reset () {
        try (PrintWriter writer1 = new PrintWriter(new FileWriter(PATH, true));
            PrintWriter writer2 = new PrintWriter(new FileWriter(PATH))) {
            writer2.write("");
            for (String i : memo) {
                writer1.println(i);
            }
        } catch (IOException e) {
        }
    }

    @Test
    public void testAddScoreEntry() {
        ScoreEntryIO.addScoreEntry(entry3);
        assertEquals("D;2020-12-12T03:45;120 ", readFile());
        ScoreEntryIO.addScoreEntry(entry5);
        assertEquals("F;2018-06-06T01:30;200 D;2020-12-12T03:45;120 ", readFile());
        ScoreEntryIO.addScoreEntry(entry9);
        assertEquals("F;2018-06-06T01:30;200 K;-999999999-01-01T00:00;190 D;2020-12-12T03:45;120 ", readFile());
        ScoreEntryIO.addScoreEntry(entry8);
        assertEquals("F;2018-06-06T01:30;200 K;-999999999-01-01T00:00;190 J;1999-01-23T23:59:03.000000003;190 D;2020-12-12T03:45;120 ", readFile());
        ScoreEntryIO.addScoreEntry(entry6);
        assertEquals("H;2010-10-05T02:02:02;250 F;2018-06-06T01:30;200 K;-999999999-01-01T00:00;190 J;1999-01-23T23:59:03.000000003;190 D;2020-12-12T03:45;120 ", readFile());

    }

    @Test
    public void testWriteScoreEntries() {
        List <ScoreEntry> entries = Arrays.asList(entries05);
        ScoreEntryIO.writeScoreEntries(entries);
        assertEquals("D;2020-12-12T03:45;120 B;-999999999-01-01T00:00;70 J;1999-01-23T23:59:03.000000003;190 ", readFile());
        entries = Arrays.asList(entries08);
        ScoreEntryIO.writeScoreEntries(entries);
        assertEquals("K;-999999999-01-01T00:00;190 J;1999-01-23T23:59:03.000000003;190 C;+999999999-12-31T23:59:59.999999999;30 E;2019-04-30T12:05;90 ", readFile());
        entries = Arrays.asList(entries06);
        ScoreEntryIO.writeScoreEntries(entries);
        assertEquals("J;1999-01-23T23:59:03.000000003;190 B;-999999999-01-01T00:00;70 D;2020-12-12T03:45;120 ", readFile());
        entries = Arrays.asList(entries02);
        ScoreEntryIO.writeScoreEntries(entries);
        assertEquals("I;"+entry7.getDateTime().toString()+";150 ", readFile());
        entries = Arrays.asList(entries00);
        ScoreEntryIO.writeScoreEntries(entries);
        assertEquals("", readFile());

    }

    @Test
    public void testLoadScoreEntries() {
        writeFile("K;-999999999-01-01T00:00;190", "J;1999-01-23T23:59:03.000000003;190", "C;+999999999-12-31T23:59:59.999999999;30", "E;2019-04-30T12:05;90");
        assertEquals(entries08, ScoreEntryIO.loadScoreEntries().toArray());
        writeFile("B;-999999999-01-01T00:00;70", "J;1999-01-23T23:59:03.000000003;190");
        assertEquals(entries03, ScoreEntryIO.loadScoreEntries().toArray());
        writeFile("J;1999-01-23T23:59:03.000000003;190","M;1999-01-23T23:59:03;15.4","B;-999999999-01-01T00:00;70","D;2020-12-12T03:45;120");
        assertEquals(entries06,ScoreEntryIO.loadScoreEntries().toArray());
        writeFile("E;2019-04-30T12:05;90","E;2019-04-30T12:05;90","E;2019-04-30T12:05;90");
        assertEquals(entries07,ScoreEntryIO.loadScoreEntries().toArray());
        writeFile("K;12.7-01-01T00:00;190","Z;-1999-13-23T23:59:03.000000003;190","E2019-04-30T12:05;90",";2019-04-30T12:05;90");
        assertEquals(entries00,ScoreEntryIO.loadScoreEntries().toArray());

    }

}
