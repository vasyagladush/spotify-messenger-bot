package com.vasyagladush.spotifymessengerbot.messengers.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vasyagladush.spotifymessengerbot.lyricsproviders.genius.GeniusService;
import com.vasyagladush.spotifymessengerbot.models.MessengerPlatform;
import com.vasyagladush.spotifymessengerbot.models.MusicProviderPlatform;
import com.vasyagladush.spotifymessengerbot.models.User;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.SpotifyService;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyGetCurrentlyPlayingTrackResponse;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyGetCurrentlyPlayingTrackResponse.Artist;
import com.vasyagladush.spotifymessengerbot.services.UserService;

@Component
public class TelegramBot extends TelegramWebhookBot {
    private final String botToken;
    private final String botUsername;
    private final String serverBaseUrl;
    private final String webhookSecretToken;
    private final UserService userService;
    private final SpotifyService spotifyService;
    private final GeniusService geniusService;

    @Autowired
    public TelegramBot(@Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername, @Value("${BASE_URL}") String baseUrl,
            @Value("${TELEGRAM_WEBHOOK_SECRET_TOKEN}") String webhookSecretToken, UserService userService,
            SpotifyService spotifyService, GeniusService geniusService) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.serverBaseUrl = baseUrl;
        this.webhookSecretToken = webhookSecretToken;
        this.userService = userService;
        this.spotifyService = spotifyService;
        this.geniusService = geniusService;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotPath() {
        return "";
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    public String getWebhookBaseUrl() {
        String webhookBaseUrl = this.serverBaseUrl;
        if (!webhookBaseUrl.endsWith("/")) {
            webhookBaseUrl += "/";
        }
        webhookBaseUrl += "webhook/telegram/";
        return webhookBaseUrl;
    }

    public String getWebhookSecretToken() {
        return this.webhookSecretToken;
    }

    @Override
    public void setWebhook(SetWebhook setWebhook) throws TelegramApiException {
        WebhookUtilsV2.setWebhook(this, this, setWebhook);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        final Long chatId = update.getMessage().getChatId();
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {

                String messageText = update.getMessage().getText().trim();
                String response = "Error: invalid input";

                final User user = this.userService.createOrUpdate(MessengerPlatform.TELEGRAM, chatId.toString(),
                        MusicProviderPlatform.SPOTIFY);

                if (messageText.equals("/start") || messageText.equals("/reauthorize")) {

                    response = spotifyService.constructAuthorizationLink(user.getId().toString());
                    this.execute(new SendMessage(chatId.toString(), response));
                    return null;
                }
                if (messageText.equals("/lyrics") || messageText.equals("Lyrics")) {
                    SpotifyGetCurrentlyPlayingTrackResponse currentlyPlayingTrack = spotifyService
                            .getCurrentlyPlayingTrack(user);

                    if (currentlyPlayingTrack == null) {
                        this.execute(new SendMessage(chatId.toString(), "No song's playing."));
                        return null;
                    }

                    final String songName = currentlyPlayingTrack.getItem().getName();
                    final Artist[] artists = currentlyPlayingTrack.getItem().getArtists();

                    String artistNames = "";
                    for (int i = 0; i < artists.length; ++i) {
                        if (i == artists.length - 1) {
                            artistNames += artists[i].getName();
                        } else {
                            artistNames += artists[i].getName() + ", ";
                        }
                    }

                    if (artists.length > 1) {
                        response = String.format("Song: %s\nArtists: %s", songName, artistNames);
                    } else {
                        response = String.format("Song: %s\nArtist: %s", songName, artistNames);
                    }

                    this.execute(new SendMessage(chatId.toString(), response));
                    this.execute(new SendMessage(chatId.toString(),
                            this.geniusService.getSongLyrics(songName, artistNames)));
                    return null;
                }

            } else {
                this.execute(new SendMessage(chatId.toString(), "Error: No message input"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
