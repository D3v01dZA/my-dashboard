package com.altona.html;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinanceController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
