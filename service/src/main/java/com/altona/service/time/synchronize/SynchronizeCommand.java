package com.altona.service.time.synchronize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynchronizeCommand {

    public static SynchronizeCommand current() {
        return new SynchronizeCommand(0);
    }

    public static SynchronizeCommand previous(int periodsBack) {
        return new SynchronizeCommand(periodsBack);
    }

    private int periodsBack;

}
