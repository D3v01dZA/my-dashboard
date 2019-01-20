package com.altona.html;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @RequestMapping(path = "/", produces = "application/json")
    @Transactional(readOnly = true)
    public String index(Authentication authentication) {
        return "Time Controller " + authentication.getName() + "!";
    }

}
