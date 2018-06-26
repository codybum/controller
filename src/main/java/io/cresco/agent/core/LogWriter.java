package io.cresco.agent.core;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogWriter implements LogListener
{
    //private Logger logMessages;
    private static final Logger logMessages = LoggerFactory.getLogger("Logging");


    public LogWriter() {
        //System.out.println("Starting Log Writer!");
        //logMessages = LoggerFactory.getLogger("agentLogger");

    }
    // Invoked by the log service implementation for each log entry
    public void logged(LogEntry entry)
    {
        String logMessage = entry.getMessage();

        if(entry.getMessage().startsWith("[")) {
            System.out.println("\t" + entry.getMessage());

            switch (entry.getLogLevel().toString().toLowerCase()) {
                case "error":
                    logMessages.error(logMessage);
                    break;
                case "warn":
                    logMessages.warn(logMessage);
                    break;
                case "info":
                    logMessages.info(logMessage);
                    break;
                case "debug":
                    logMessages.debug(logMessage);
                    break;
                case "trace":
                    logMessages.trace(logMessage);
                    break;
                default:
                    logMessages.error("Unknown log_level [{}]", logMessage);
                    break;
            }

        }
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