package io.cresco.agent.core;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogReader implements LogListener
{
    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(LogReader.class);
    }

    LogReader()
    {

    }

    @Override
    public void logged(final LogEntry entry)
    {

        final int level = entry.getLevel();
        switch (level) {
            case LogService.LOG_DEBUG: {
                LogReader.debug(entry);
                break;
            }
            case LogService.LOG_ERROR: {
                LogReader.error(entry);
                break;
            }
            case LogService.LOG_INFO: {
                LogReader.info(entry);
                break;
            }
            case LogService.LOG_WARNING: {
                LogReader.warn(entry);
                break;
            }
            default: {
                LogReader.warn(entry);
                break;
            }
        }
    }

    private static void warn(final LogEntry entry)
    {
        final String name = entry.getBundle().getSymbolicName();
        final String message = entry.getMessage();
        final Throwable ex = entry.getException();
        if (ex != null) {
            LogReader.LOG.warn("[{}]: {}: ", name, message, ex);
        } else {
            LogReader.LOG.warn("[{}]: {}", name, message);
        }
    }

    private static void info(final LogEntry entry)
    {
        final String name = entry.getBundle().getSymbolicName();
        final String message = entry.getMessage();
        final Throwable ex = entry.getException();
        if (ex != null) {
            LogReader.LOG.info("[{}]: {}: ", name, message, ex);
        } else {
            LogReader.LOG.info("[{}]: {}", name, message);
        }
    }

    private static void error(final LogEntry entry)
    {
        final String name = entry.getBundle().getSymbolicName();
        final String message = entry.getMessage();
        final Throwable ex = entry.getException();
        if (ex != null) {
            LogReader.LOG.error("[{}]: {}: ", name, message, ex);
        } else {
            LogReader.LOG.error("[{}]: {}", name, message);
        }
    }

    private static void debug(final LogEntry entry)
    {
        final String name = entry.getBundle().getSymbolicName();
        final String message = entry.getMessage();
        final Throwable ex = entry.getException();
        if (ex != null) {
            LogReader.LOG.debug("[{}]: {}: ", name, message, ex);
        } else {
            LogReader.LOG.debug("[{}]: {}", name, message);
        }
    }
}