package com.vasyagladush.spotifymessengerbot.services;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vasyagladush.spotifymessengerbot.models.MessengerPlatform;
import com.vasyagladush.spotifymessengerbot.models.MusicProviderPlatform;
import com.vasyagladush.spotifymessengerbot.models.User;
import com.vasyagladush.spotifymessengerbot.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createOrUpdate(final MessengerPlatform messengerPlatform, final String messengerUserId,
            final MusicProviderPlatform musicProviderPlatform) {
        final Optional<User> existingUserOptional = this.get(messengerPlatform, messengerUserId, musicProviderPlatform);
        if (existingUserOptional.isPresent()) {
            final User existingUser = existingUserOptional.get();
            existingUser.setMusicProviderPlatform(musicProviderPlatform.name());
            return existingUser;
        }

        User newUser = new User();
        newUser.setMessengerPlatform(messengerPlatform.name());
        newUser.setMessengerUserId(messengerUserId);
        newUser.setMusicProviderPlatform(musicProviderPlatform.name());
        repository.save(newUser);
        return newUser;
    }

    public User updateWithMusicProviderAccessTokens(final String messengerUserId,
            final String accessToken, final String refreshToken,
            final Date accessTokenExpiresAt) throws NoSuchElementException {
        final User user = this.get(UUID.fromString(messengerUserId)).get();
        user.setMusicProviderAccessToken(accessToken);
        user.setMusicProviderAccessTokenExpiresAt(accessTokenExpiresAt);
        user.setMusicProviderRefreshToken(refreshToken);
        repository.save(user);
        return user;
    }

    public User updateWithMusicProviderAccessTokens(final User user,
            final String accessToken, final String refreshToken,
            final Date accessTokenExpiresAt) {
        user.setMusicProviderAccessToken(accessToken);
        user.setMusicProviderAccessTokenExpiresAt(accessTokenExpiresAt);
        if (refreshToken != null) {
            user.setMusicProviderRefreshToken(refreshToken);
        }
        repository.save(user);
        return user;
    }

    public Optional<User> get(final UUID id) {
        return repository.findById(id);
    }

    public Optional<User> get(final MessengerPlatform messengerPlatform, final String messengerUserId,
            final MusicProviderPlatform musicProviderPlatform) {
        return repository.findFirstByMessengerPlatformAndMessengerUserIdAndMusicProviderPlatform(
                messengerPlatform.name(), messengerUserId, musicProviderPlatform.name());
    }
}
