package com.vasyagladush.spotifymessengerbot.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vasyagladush.spotifymessengerbot.models.User;
import com.vasyagladush.spotifymessengerbot.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> getAll() {
        return (List<User>) repository.findAll();
    }
}
