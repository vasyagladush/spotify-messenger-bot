package com.vasyagladush.spotifymessengerbot.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyagladush.spotifymessengerbot.services.UserService;

@RestController
public class IndexController {
    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
        ;
    }

    @GetMapping("/")
    @ResponseBody
    public String getIndex() {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final String result = objectMapper.writeValueAsString(userService.getAll());
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}