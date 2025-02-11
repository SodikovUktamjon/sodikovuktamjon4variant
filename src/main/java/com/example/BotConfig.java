package com.example;

import com.example.repositories.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {
    public static final String botToken="7859642042:AAG7HKusE5F0M5gqh05w_W1qmtMx-X8_S5E";

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:languages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public MyTelegramBot bot(UserRepository userRepository, MessageSource messageSource, UserService userService) throws TelegramApiException {
        MyTelegramBot bot = new MyTelegramBot(userRepository, messageSource, userService);
        TelegramBotsApi botsApi;
        botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        return bot;
    }
}
