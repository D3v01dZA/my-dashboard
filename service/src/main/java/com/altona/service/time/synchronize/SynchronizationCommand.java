package com.altona.service.time.synchronize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizationCommand {

    public static SynchronizationCommand current() {
        return new SynchronizationCommand(0);
    }

    public static SynchronizationCommand previous(int periodsBack) {
        return new SynchronizationCommand(periodsBack);
    }

    private int periodsBack;

}
