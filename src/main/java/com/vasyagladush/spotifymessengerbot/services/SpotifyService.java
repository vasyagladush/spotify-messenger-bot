package com.vasyagladush.spotifymessengerbot.services;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpotifyService {
    private String apiKey;
    private String apiSecret;

    @Autowired
    public SpotifyService() {
        this.apiKey = "";
        this.apiSecret = "";
    }

    public String constructAuthorizationLink() throws MalformedURLException {
        return new URL("https://google.com" + this.apiKey + this.apiSecret).toString();
    }
}
