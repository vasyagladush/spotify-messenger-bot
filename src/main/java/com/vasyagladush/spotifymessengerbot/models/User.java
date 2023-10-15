package com.vasyagladush.spotifymessengerbot.models;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(name = "messenger_user_id", nullable = false)
    public String messengerUserId;

    public UUID getId() {
        return this.id;
    }

    public User() {
    }

    public User(String messengerUserId) {
        this.messengerUserId = messengerUserId;
    }
}
