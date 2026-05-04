package com.pamestinukai.backend.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/count")
public class CountController {

    private int count = 0;

    @GetMapping
    public int getCount() {
        return count;
    }

    @PostMapping
    public int incrementCount() {
        return ++count;
    }
}
