package com.altona;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Base64;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackageClasses = Main.class)
public class SpringTest {

    public SpringTest() {
        System.setProperty("webdriver.chrome.driver", "NONE");
        System.setProperty("webdriver.chrome.headless", "NONE");
        System.setProperty("webdriver.chrome.silent", "NONE");
        System.setProperty("webdriver.chrome.linux", "NONE");
    }

    protected String testAuth() {
        return "Basic " + Base64.getEncoder().encodeToString("test:password".getBytes());
    }

}
