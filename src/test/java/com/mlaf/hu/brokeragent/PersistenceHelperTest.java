package com.mlaf.hu.brokeragent;

import com.mlaf.hu.brokeragent.exceptions.TopicNotManagedException;
import jade.core.AID;
import junit.framework.TestCase;

import java.io.*;

public class PersistenceHelperTest extends TestCase {
    String testBrokenFilePath = PersistenceHelper.getBasePath() + "broken.ser";

    public void testStoreObject() {
        AID a = new AID("Test-Topic", true);
        Topic t = new Topic(a, 1);
        Message m1 = new Message("Hoi1");
        Message m2 = new Message("Hoi2");
        Message m3 = new Message("Hoi3");
        Message m4 = new Message("Hoi4");
        t.addToMessages(m1);
        t.addToMessages(m2);
        t.addToMessages(m3);
        t.addToMessages(m4);
        PersistenceHelper.storeObject(t, "Test-Topic");
        Topic loadedTopic = null;
        try {
            loadedTopic = PersistenceHelper.loadTopic("Test-Topic");
        } catch (TopicNotManagedException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(t, loadedTopic);
    }

    public void testLoadTopic() {
        try {
            PersistenceHelper.loadTopic("NonExisting1");
            fail();
        } catch (TopicNotManagedException ignored) {
        }
    }

    public void testLoadBrokenFile() {
        PersistenceHelper.createBasePathDirs();
        setupBrokenFile();
        try {
            Topic failed = PersistenceHelper.loadTopic("broken");
            assertNull(failed);
        } catch (TopicNotManagedException ignored) {
            fail();
        }
    }

    private void setupBrokenFile() {
        System.out.println("Creating testfile " + testBrokenFilePath);
//        File testFile = new File(testBrokenFilePath);
//        //noinspection ResultOfMethodCallIgnored
//        testFile.createNewFile();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(testBrokenFilePath), "utf-8"))) {
            String content = "BROKENFILE BROKENFILE";
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not create test file!");

        }
    }
}