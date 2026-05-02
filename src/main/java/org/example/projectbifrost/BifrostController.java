package org.example.projectbifrost;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BifrostController {

    @GetMapping("/bifrost")
    public String bifrost() {
        return "Welcome to Bifrost, the gateway to the realms!"; }
}
