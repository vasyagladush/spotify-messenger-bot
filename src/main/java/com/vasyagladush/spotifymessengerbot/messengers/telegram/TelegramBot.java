package com.vasyagladush.spotifymessengerbot.messengers.telegram;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private static final String[] AUTH_MESSAGE_INPUTS = { "/start" };
    private static final String[] LYRICS_MESSAGE_INPUTS = { "/lyrics", "Lyrics" };

    private static final Logger logger = LogManager.getLogger(TelegramBot.class);

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

        logger.info("Platform: " + MessengerPlatform.TELEGRAM + ": message received from chat id " + chatId);

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText().trim();

                final User user = this.userService.createOrUpdate(MessengerPlatform.TELEGRAM, chatId.toString(),
                        MusicProviderPlatform.SPOTIFY);

                if (Arrays.stream(TelegramBot.AUTH_MESSAGE_INPUTS).anyMatch(messageText::equals)) {
                    processAuthorizationRequestMessage(user, chatId);
                    return null;
                }

                else if (Arrays.stream(TelegramBot.LYRICS_MESSAGE_INPUTS).anyMatch(messageText::equals)) {
                    processLyricsRequestMessage(user, chatId);
                    return null;
                }

                else {
                    this.execute(new SendMessage(chatId.toString(), "Unprocessable input"));
                }
            } else {
                this.execute(new SendMessage(chatId.toString(), "Error: no text input"));
            }

            return null;
        } catch (Throwable e) {
            logger.error("Platform: " + MessengerPlatform.TELEGRAM + ": error with chat: " + chatId
                    + ", error message: " + e.getMessage());
            logger.trace(e.getStackTrace());
            try {
                this.execute(new SendMessage(chatId.toString(),
                        "An unexpected error occured. Please try again. In case the error keeps persisting, try following the authorization process again: /start"));
            } catch (Throwable e2) {
                logger.error("Platform: " + MessengerPlatform.TELEGRAM + ": error with chat: " + chatId
                        + ", error message: " + e2.getMessage());
                logger.trace(e2.getStackTrace());
            }
        }

        return null;
    }

    // TODO: in future, when there's not only Spotify, add musicProviderPlatform
    // argument
    private void processAuthorizationRequestMessage(final User user, final Long chatId) throws TelegramApiException {
        this.execute(new SendMessage(chatId.toString(), "Please follow the link to authorize Spotify\n"
                + "\nAfter you authorize Spotify, just send /lyrics command or type in \"Lyrics\" to get them\n\n"
                + spotifyService.constructAuthorizationLink(user.getId().toString())));
    }

    // TODO: in future, when there's not only Spotify, add musicProviderPlatform
    // argument
    private void processLyricsRequestMessage(final User user, final Long chatId)
            throws JsonProcessingException, ClientProtocolException, IOException, TelegramApiException {
        SpotifyGetCurrentlyPlayingTrackResponse currentlyPlayingTrack = spotifyService
                .getCurrentlyPlayingTrack(user);

        if (currentlyPlayingTrack == null) {
            this.execute(new SendMessage(chatId.toString(), "No song is currently playing"));
            return;
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

        String songInfoMessage = "";

        if (artists.length > 1) {
            songInfoMessage = String.format("Song: %s\nArtists: %s", songName, artistNames);
        } else {
            songInfoMessage = String.format("Song: %s\nArtist: %s", songName, artistNames);
        }

        this.execute(new SendMessage(chatId.toString(), songInfoMessage));

        try {
            final String lyrics = this.geniusService.getSongLyrics(songName, artistNames);
            this.execute(new SendMessage(chatId.toString(),
                    lyrics));

        } catch (IndexOutOfBoundsException noLyricsException) {
            logger.debug("Platform: " + MessengerPlatform.TELEGRAM + ": no lyrics found");
            this.execute(new SendMessage(chatId.toString(),
                    "No lyrics found for this song"));
        } catch (IOException lyricsFetchException) {
            logger.error("Platform: " + MessengerPlatform.TELEGRAM + ": error with chat: " + chatId
                    + ", error fetchingg lyrics, error message: " + lyricsFetchException.getMessage());
            logger.trace(lyricsFetchException.getStackTrace());
            this.execute(new SendMessage(chatId.toString(),
                    "Error occured while trying to find lyrics"));
        }
    }
}
