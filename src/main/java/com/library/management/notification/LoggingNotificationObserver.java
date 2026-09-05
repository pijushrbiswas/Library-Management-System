package com.library.management.notification;

import com.library.management.model.Patron;

import java.util.logging.Logger;

public final class LoggingNotificationObserver implements NotificationObserver {
    private static final Logger LOGGER = Logger.getLogger(LoggingNotificationObserver.class.getName());

    @Override
    public void notify(Patron patron, String message) {
        patron.receiveNotification(message);
        LOGGER.info(() -> "Notification sent to patron " + patron.getPatronId() + ": " + message);
    }
}
