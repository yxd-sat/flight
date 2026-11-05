package com.flight.booking.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class IndexController {

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {
    }

    @GetMapping(value = {"/", "/index", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> index() {
        try {
            Resource resource = new ClassPathResource("static/index.html");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    return ResponseEntity.ok(html);
                }
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok("forward:/index.html");
    }
}