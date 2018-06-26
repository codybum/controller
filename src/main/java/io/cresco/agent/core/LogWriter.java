package io.cresco.agent.core;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

public class LogWriter implements LogListener
{
    public LogWriter() {
        System.out.println("Starting Log Writer!");

    }
    // Invoked by the log service implementation for each log entry
    public void logged(LogEntry entry)
    {
        System.out.println("\t" + entry.getMessage());
        /*
        System.out.println("CONTROLLER LOG: \t" + entry.getMessage());
        try {
            System.out.println("CLASS: " + entry.getLocation().getClassName());
            System.out.println("csd " + entry.getThreadInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        */
    }
}