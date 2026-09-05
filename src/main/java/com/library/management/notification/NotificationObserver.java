package com.library.management.notification;

import com.library.management.model.Patron;

public interface NotificationObserver {
    void notify(Patron patron, String message);
}
