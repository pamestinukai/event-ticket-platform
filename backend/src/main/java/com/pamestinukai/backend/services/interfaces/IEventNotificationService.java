package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.entities.Event;

public interface IEventNotificationService {

    void notifyCancellation(Event event);

    void notifyReschedule(Event event);

    int retryFailed();
}
