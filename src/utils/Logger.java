package utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe logger for the traffic simulation.
 * Stores log messages and notifies listeners (GUI) of new entries.
 * Uses CopyOnWriteArrayList for thread-safe listener management.
 */
public class Logger {

    /**
     * Listener interface for receiving log messages.
     */
    public interface LogListener {
        void onLogMessage(String message);
    }

    private final List<String> logMessages = new ArrayList<>();
    private final List<LogListener> listeners = new CopyOnWriteArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static Logger instance;

    private Logger() {}

    /**
     * Singleton pattern - only one logger instance in the system.
     */
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * Adds a listener that will be notified of new log messages.
     */
    public void addListener(LogListener listener) {
        listeners.add(listener);
    }

    /**
     * Logs a message with timestamp. Thread-safe.
     */
    public synchronized void log(String message) {
        String timestamp = timeFormat.format(new Date());
        String fullMessage = "[" + timestamp + "] " + message;
        logMessages.add(fullMessage);

        // Keep only last 500 messages to prevent memory issues
        if (logMessages.size() > 500) {
            logMessages.remove(0);
        }

        // Notify all listeners
        for (LogListener listener : listeners) {
            listener.onLogMessage(fullMessage);
        }
    }

    /**
     * Returns a copy of all log messages.
     */
    public synchronized List<String> getMessages() {
        return new ArrayList<>(logMessages);
    }

    /**
     * Clears all log messages.
     */
    public synchronized void clear() {
        logMessages.clear();
    }
}
