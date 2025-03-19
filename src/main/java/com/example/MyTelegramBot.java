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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class MyTelegramBot extends TelegramLongPollingBot {
    private final static Map<String, Status> userStatuses = new HashMap<>();
    private final static Map<String,Order> orders = new HashMap<>();
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
            if (data.equals("hot_dog_mini") || data.equals("hot_dog_classic") || data.equals("hot_dog_big") || data.equals("fries") || data.equals("danar") || data.equals("shawarma_classic") || data.equals("haggy") || data.equals("shawarma_big") || data.equals("lavash_classic") || data.equals("lavash_big")) {
                deleteMessage(callbackQuery.getMessage(),chatId);
                sendItemWithTextAndCounter(chatId, 1, productRepository.findByProductData(data), user.getLang());
            } else if (data.equals("ru") || data.equals("en") || data.equals("uz")) {
                deleteMessage(callbackQuery.getMessage(),chatId);
                sendMessage(chatId, messageSource.getMessage("share_contact", null, Locale.forLanguageTag(data)), InlineKeyboardUtil.shareContact(), null);
                user.setLang(data);
                userRepository.save(user);
            } else if (data.contains("plus") || data.contains("minus")) {
                String[] dataSplit = data.split("_");
                int counterValue = Integer.parseInt(dataSplit[4]);
                if (data.contains("counter_minus")) counterValue--;
                else if(data.contains("counter_plus")) counterValue++;
                EditMessageReplyMarkup replyMarkup = new EditMessageReplyMarkup();
                replyMarkup.setReplyMarkup(InlineKeyboardUtil.createCounterInlineKeyboard(counterValue,dataSplit[3]));
                replyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
                replyMarkup.setChatId(chatId);
                try {
                    execute(replyMarkup);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (data.contains("countItem_")) {
                deleteMessage(callbackQuery.getMessage(), chatId);
                String[] item = data.split("_");
                sendMessage(chatId, messageSource.getMessage("menu.prompt", null, Locale.forLanguageTag(user.getLang())), null, InlineKeyboardUtil.orderMenuInlineKeyboardMarkup(user.getLang()));
                CompletableFuture.runAsync(() -> {

                    Product byProductData = productRepository.findByProductData(item[2]);
                    int quantity = Integer.parseInt(item[3]);
                    int totalPrice=quantity*byProductData.getPrice();
                    OrderProduct orderProduct = OrderProduct.builder()
                            .product(byProductData)
                            .quantity(quantity)
                            .build();
                    if(orders.containsKey(chatId)){
                        Order order = orders.get(chatId);
                        order.setProductsList(Stream.concat(order.getProductsList().stream(), Stream.of(orderProduct))
                                .collect(Collectors.toCollection(ArrayList::new)));
                        order.setTotalPrice(order.getTotalPrice()+totalPrice);
                        return;
                    }
                    orders.put(chatId, Order.builder()
                            .productsList(
                                    Collections.singletonList(orderProduct)
                            )
                            .totalPrice(totalPrice)
                            .build());
                });
            }
            else if (data.equals("users")) {
                sendMessage(chatId, userRepository.countUsers() + " users in bot!", null, null);
            } else if (data.equals("excel")) {
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
            } else if (data.equals("admins")) {
                userRepository.findAllByRole(Role.ADMIN).forEach(user1 -> {
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
            } else if (text.equals("📞Contact") && userStatuses.get(chatId) == Status.MAIN_MENU) {
                deleteMessage(message, chatId);
                sendMessage(chatId, messageSource.getMessage("contact.details", null, Locale.forLanguageTag(user.getLang())), InlineKeyboardUtil.menuKeyboardMarkup(), null);
            } else if (text.contains("Basket")&& userStatuses.get(chatId) == Status.MAIN_MENU) {
               deleteMessage(message, chatId);
               sendMessage(chatId,generateBasketMessage(orders.get(chatId), user.getLang()),null,null);
            } else if (text.equals("📜Order History") && userStatuses.get(chatId) == Status.MAIN_MENU) {
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
    public String generateBasketMessage(Order order, String language) {
        StringBuilder message = new StringBuilder();

        // Language-specific labels
        String basketTitle, totalLabel, addressLabel, contactLabel, pcsLabel, checkoutLabel, clearLabel;

        switch (language) {
            case "ru":
                basketTitle = "🛒 Ваша Корзина:";
                totalLabel = "🧾 Итого:";
                addressLabel = "📍 Адрес доставки:";
                contactLabel = "📞 Контакт:";
                pcsLabel = "шт";
                checkoutLabel = "✅ Оформить заказ";
                clearLabel = "🗑 Очистить корзину";
                break;
            case "uz":
                basketTitle = "🛒 Savatingiz:";
                totalLabel = "🧾 Jami:";
                addressLabel = "📍 Yetkazib berish manzili:";
                contactLabel = "📞 Aloqa:";
                pcsLabel = "dona";
                checkoutLabel = "✅ Buyurtma berish";
                clearLabel = "🗑 Savatni tozalash";
                break;
            default: // English
                basketTitle = "🛒 Your Basket:";
                totalLabel = "🧾 Total:";
                addressLabel = "📍 Delivery Address:";
                contactLabel = "📞 Contact:";
                pcsLabel = "pcs";
                checkoutLabel = "✅ Checkout";
                clearLabel = "🗑 Clear Basket";
                break;
        }

        // Build message
        message.append("*").append(basketTitle).append("*\n");
        message.append("------------------------------------------------\n");

        for (OrderProduct item : order.getProductsList()) {
            String productName = (language.equals("ru")) ? item.getProduct().getNameRu() :
                    (language.equals("uz")) ? item.getProduct().getNameUz() :
                            item.getProduct().getName();

            message.append("🍽 *")
                    .append(escapeMarkdown(productName))
                    .append("* - ")
                    .append(item.getQuantity())
                    .append(" ")
                    .append(pcsLabel)
                    .append(" x ")
                    .append(item.getProduct().getPrice())
                    .append(" UZS = ")
                    .append(item.getQuantity() * item.getProduct().getPrice())
                    .append(" UZS\n");
        }

        message.append("------------------------------------------------\n");
        message.append("*").append(totalLabel).append("* ").append(order.getTotalPrice()).append(" UZS\n");
        message.append("*").append(addressLabel).append("* ").append(escapeMarkdown(order.getAddress())).append("\n");
        message.append("*").append(contactLabel).append("* ").append(escapeMarkdown(order.getUser().getPhoneNumber())).append("\n");
        return message.toString();
    }

    // Helper function to escape special characters in MarkdownV2
    private String escapeMarkdown(String text) {
        return text.replaceAll("([_\\*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

    public void sendItemWithTextAndCounter(String chatId, int counterValue, Product product, String language) {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardUtil.createCounterInlineKeyboard(counterValue, product.getProductData());
        SendPhoto sendMessage = new SendPhoto();
        sendMessage.setChatId(chatId);
        sendMessage.setPhoto(new InputFile(new File("/Users/macstore.uz/IdeaProjects/sodikovuktamjon4variant/src/main/resources/maxresdefault.jpg")));
        try {

            switch (language) {
                case "en":
                    sendMessage.setCaption(product.getName() + "\n\n" +
                            "🍴 Description:\n" + product.getDescription() + "\n\n" +
                            "💰 Price: " + product.getPrice() + " UZS\n\n" +
                            "➡️ Order now and enjoy your meal!");
                    break;
                case "uz":
                    sendMessage.setCaption(product.getNameUz() + "\n\n" +
                            "🍴 Tavsif:\n" + product.getDescriptionUz() + "\n\n" +
                            "💰 Narx: " + product.getPrice() + " UZS\n\n" +
                            "➡️ Buyurtma bering va taomingizdan rohatlaning!");
                    break;
                case "ru":
                    sendMessage.setCaption(product.getNameRu() + "\n\n" +
                            "🍴 Описание:\n" + product.getDescriptionRu() + "\n\n" +
                            "💰 Цена: " + product.getPrice() + " UZS\n\n" +
                            "➡️ Закажите сейчас и наслаждайтесь своей едой!");
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