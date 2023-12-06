package com.vasyagladush.spotifymessengerbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vasyagladush.spotifymessengerbot.messengers.telegram.TelegramBot;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class SpotifyMessengerBotApplication {
	@Autowired
	private TelegramBot telegramBot;

	public static void main(String[] args) {
		SpringApplication.run(SpotifyMessengerBotApplication.class, args);
	}

	@PostConstruct
	public void registerTelegramBot() {
		try {
			SetWebhook setWebhook = SetWebhook.builder().url(telegramBot.getWebhookBaseUrl())
					.secretToken(telegramBot.getWebhookSecretToken()).build();
			telegramBot.onRegister();
			telegramBot.setWebhook(setWebhook);
			System.out.println(
					"Successfully registered Telegram Bot, with webhook URL: " + telegramBot.getWebhookBaseUrl());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
