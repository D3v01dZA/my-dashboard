package com.altona.service.synchronization.test;

import com.altona.service.synchronization.SynchronizationTraceRepository;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.test.model.SucceedingConfiguration;
import com.altona.util.Driver;
import com.altona.util.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class SucceedingBrowser {

    private SynchronizationTraceRepository synchronizationTraceRepository;

    public Result<ChromeDriver, String> login(SynchronizationAttempt attempt, SynchronizationRequest request, SucceedingConfiguration configuration) {
        ChromeDriver chromeDriver = null;
        try {
            chromeDriver = Driver.getChromeDriver();
            chromeDriver.get(configuration.getWebsite());
            synchronizationTraceRepository.trace(attempt, request, "Load", chromeDriver);
            return Result.success(chromeDriver);
        } catch (Exception ex) {
            log.error("Exception opening google", ex);
            if (chromeDriver != null) {
                chromeDriver.quit();
            }
            return Result.failure("Exception occurred while logging in");
        }
    }
}
