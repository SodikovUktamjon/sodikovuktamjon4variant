package com.example;

import com.example.domains.*;
import com.example.domains.User;
import com.example.repositories.OrderRepository;
import com.example.repositories.ProductRepository;
import com.example.repositories.UserRepository;
import com.example.util.ExcelWriter;
import com.example.util.InlineKeyboardUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MyTelegramBot extends TelegramLongPollingBot {
    private final static Map<String, Status> userStatuses = new HashMap<>();
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            String chatId = String.valueOf(callbackQuery.getMessage().getChatId());
            User user = userRepository.findByChatId(chatId);
            switch (data) {
                case "hot_dog_mini", "hot_dog_classic", "hot_dog_big", "fries",
                     "danar", "shawarma_classic", "haggy", "shawarma_big", "lavash_classic", "lavash_big" ->
                        sendItemWithTextAndCounter(chatId, 1, productRepository.findByProductData(data), user.getLang());
                case "ru", "en", "uz" -> {
                    user.setLang(data);
                    userRepository.save(user);
                    sendMessage(chatId, messageSource.getMessage("share_contact", null, Locale.forLanguageTag(data)), InlineKeyboardUtil.shareContact(), null);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setReplyMarkup(InlineKeyboardUtil.shareContact());
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "users" -> sendMessage(chatId, userRepository.countUsers() + " users in bot!", null, null);
                case "excel" -> {
                    InputFile inputFile = new InputFile(ExcelWriter.generateUsersExcelFile(userRepository.findAll(), "src/main/resources/excel/excel.xlsx"));
                    SendDocument sendDocument = new SendDocument();
                    sendDocument.setChatId(chatId);
                    sendDocument.setCaption("Users");
                    sendDocument.setDocument(inputFile);

                    try {
                        execute(sendDocument);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                case "admins" -> userRepository.findAllByRole(Role.ADMIN).forEach(user1 -> {
                    sendMessage(chatId, "First Name: " + user1.getFirstName() + "\nLast Name: " + user1.getLastName() +
                            "\nUsername: " + user1.getUsername() + "\nChatId: " + user1.getChatId(), null, null);
                });
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();
            String chatId = String.valueOf(message.getChatId());
            User user = userRepository.findByChatId(chatId);
            if (message.hasContact()) {
                sendMessage(chatId, messageSource.getMessage("send_message_choose_menu", null, Locale.forLanguageTag(user.getLang())), InlineKeyboardUtil.menuKeyboardMarkup(), null);
                CompletableFuture.runAsync(() -> {
                    Contact contact = message.getContact();
                    user.setFirstName(contact.getFirstName());
                    user.setLastName(contact.getLastName() != null ? message.getFrom().getLastName() : "");
                    user.setUserId(contact.getUserId());
                    userRepository.save(user);
                });

            } else if (text.equals("🍽️ Menu") && (!userStatuses.containsKey(chatId) || userStatuses.get(chatId) == Status.MAIN_MENU)) {
                deleteMessage(message, chatId);
                sendMessage(chatId, messageSource.getMessage("menu.prompt", null, Locale.forLanguageTag(user.getLang())), null, InlineKeyboardUtil.orderMenuInlineKeyboardMarkup(user.getLang()));
                userStatuses.put(chatId, Status.MENU);
            } else if (text.equals("📞 Contact") && userStatuses.get(chatId) == Status.MAIN_MENU) {
                deleteMessage(message, chatId);
                sendMessage(chatId, messageSource.getMessage("contact.details", null, Locale.forLanguageTag(user.getLang())), InlineKeyboardUtil.menuKeyboardMarkup(), null);
                userStatuses.put(chatId, Status.MAIN_MENU);
            } else if (text.equals("🛒 Basket")) {

            } else if (text.equals("📜 Order History") && userStatuses.get(chatId) == Status.MAIN_MENU) {
                deleteMessage(message, chatId);
                List<Order> byUserChatId = orderRepository.findByUser_ChatId(chatId);
                if (byUserChatId == null || byUserChatId.isEmpty()) {
                    sendMessage(chatId, messageSource.getMessage("no.order", null, Locale.forLanguageTag(user.getLang())), InlineKeyboardUtil.menuKeyboardMarkup(), null);
                }
                if (byUserChatId != null) {
                    byUserChatId.forEach(order -> {
                        sendMessage(chatId, createOrderMessage(order, Locale.forLanguageTag(user.getLang())), InlineKeyboardUtil.menuKeyboardMarkup(), null);
                    });
                }
                userStatuses.put(chatId, Status.MAIN_MENU);
            } else if (text.equals("/start")) {
                org.telegram.telegrambots.meta.api.objects.User from1 = message.getFrom();
                SendMessage sendMessage = new SendMessage(String.valueOf(chatId), messageSource.getMessage("choose_language_message", new Object[]{from1.getFirstName()}, Locale.forLanguageTag(from1.getLanguageCode())));
                sendMessage.setReplyMarkup(InlineKeyboardUtil.getInlineUserLanguages());
                sendMessage.setParseMode("Markdown");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                CompletableFuture.runAsync(() -> {
                    if (!userRepository.existsByUserId(chatId)) {
                        com.example.domains.User newUser = User.builder()
                                .firstName(from1.getFirstName())
                                .lastName(from1.getLastName())
                                .username(from1.getUserName())
                                .chatId(chatId)
                                .build();
                        userRepository.save(newUser);
                        sendMessage("6632222728", "New user: " + user.getFirstName() + "\nLast name: " + user.getLastName() + "\nUsername: " + user.getUsername() + "\nChatId: " + user.getChatId() + "\n\n", null, null);
                    }
                });

            } else if (text.equals("/admin") && userService.isAdmin(chatId)) {
                SendMessage sendMessage = new SendMessage(chatId, "Admin panel");
                sendMessage.setReplyMarkup(InlineKeyboardUtil.getAdminKeyboardMarkup());
                sendMessage.setParseMode("Markdown");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                deleteMessage(message, chatId);
                sendMessage(chatId, "Please upload your photos!", null, null);
            }
        }


    }


    private void deleteMessage(Message message, String chatId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(message.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String chatId, String message, ReplyKeyboardMarkup replyKeyboardMarkup, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage(chatId, message);
            if (replyKeyboardMarkup != null) {
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
            }
            if (inlineKeyboardMarkup != null) {
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            }
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageToAllUsers(Message message) {
        CompletableFuture.runAsync(() -> {
            List<String> allChatIds = userRepository.findUsersChatId();
            System.out.println(allChatIds.size());
            for (String allChatId : allChatIds) {
                ForwardMessage forwardMessage = new ForwardMessage();
                forwardMessage.setChatId(allChatId);
                forwardMessage.setFromChatId(String.valueOf(message.getChatId()));
                forwardMessage.setMessageId(message.getMessageId());
                try {
                    execute(forwardMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String createOrderMessage(Order order, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = order.getOrderDate().format(formatter);

        StringBuilder productList = new StringBuilder();
        for (OrderProduct product : order.getProductsList()) {
            productList.append("📦 ").append(product.toString()).append("\n");
        }

        return MessageFormat.format(
                messageSource.getMessage("order.created", null, locale),
                order.getId(),
                formattedDate,
                order.getAddress(),
                order.getTotalPrice(),
                productList.toString()
        );
    }

    public void sendItemWithTextAndCounter(String chatId, int counterValue, Product product, String language) {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardUtil.createCounterInlineKeyboard(counterValue, product.getProductData());
        SendPhoto sendMessage = new SendPhoto();
        sendMessage.setChatId(chatId);
        sendMessage.setPhoto(new InputFile(new File(product.getImagePath())));
        try {

            switch (language) {
                case "en":
                    sendMessage.setCaption("*" + product.getName() + "*\n\n" +
                            "🍴 *Description:*\n" + product.getDescription() + "\n\n" +
                            "💰 *Price:* " + product.getPrice() + " UZS\n\n" +
                            "➡️ *Order now and enjoy your meal!*");
                    break;
                case "uz":
                    sendMessage.setCaption("*" + product.getNameUz() + "*\n\n" +
                            "🍴 *Tavsif:*\n" + product.getDescriptionUz() + "\n\n" +
                            "💰 *Narx:* " + product.getPrice() + " UZS\n\n" +
                            "➡️ *Buyurtma bering va taomingizdan rohatlaning!*");
                    break;
                case "ru":
                    sendMessage.setCaption("*" + product.getNameRu() + "*\n\n" +
                            "🍴 *Описание:*\n" + product.getDescriptionRu() + "\n\n" +
                            "💰 *Цена:* " + product.getPrice() + " UZS\n\n" +
                            "➡️ *Закажите сейчас и наслаждайтесь своей едой!*");
                    break;
            }

            sendMessage.setReplyMarkup(inlineKeyboardMarkup);

            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "https://t.me/bigbitebot";
    }

    @Override
    public String getBotToken() {
        return BotConfig.botToken;
    }
}