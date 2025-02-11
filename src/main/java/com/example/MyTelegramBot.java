package com.example;

import com.example.domains.Role;
import com.example.domains.User;
import com.example.repositories.UserRepository;
import com.example.util.ExcelWriter;
import com.example.util.ImageToPDF;
import com.example.util.InlineKeyboardUtil;
import com.example.util.JpegToDocxService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MyTelegramBot extends TelegramLongPollingBot {
    private final static Map<String, List<String>> chatIdToPhotoPath = new HashMap<>();
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserService userService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            String chatId = String.valueOf(callbackQuery.getMessage().getChatId());
            if (data.equals("ru") || data.equals("uz")) {
                User user = userRepository.findByChatId(chatId);
                user.setLang(data);
                userRepository.save(user);
                sendMessage(chatId, messageSource.getMessage("share_contact", null, Locale.forLanguageTag(data)));
            } else if (data.equals("users")) {
                sendMessage(chatId, userRepository.countUsers() + " users in bot!");

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
            }
            else if (data.equals("admins")) {
                userRepository.findAllByRole(Role.ADMIN).forEach(user -> {
                    sendMessage(chatId, "First Name: " + user.getFirstName() + "\nLast Name: " + user.getLastName()+"" +
                            "\nUsername: " + user.getUsername() + "\nChatId: " + user.getChatId());
                });

            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();
            String chatId = String.valueOf(message.getChatId());
            if (message.hasPhoto()) {
                if (!userRepository.existsByUserId(chatId)) {
                    sendMessage(chatId, messageSource.getMessage("we_dont_know_you", null, Locale.forLanguageTag("en")) +
                            "\n" + messageSource.getMessage("we_dont_know_you", null, Locale.forLanguageTag("uz")));

                    return;
                }
                String path;
                List<PhotoSize> photos = update.getMessage().getPhoto();
                PhotoSize photo = photos.stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElse(null);
                if (photo != null) {
                    try {
                        path = (downloadPhoto(photo).getPath());
                    } catch (TelegramApiException | IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    List<String> strings;

                    if (chatIdToPhotoPath.containsKey(chatId)) {
                        strings = chatIdToPhotoPath.get(chatId);
                    } else {
                        chatIdToPhotoPath.put(chatId, new ArrayList<>());
                        strings = new ArrayList<>();
                    }
                    strings.add(path);
                    chatIdToPhotoPath.put(chatId, strings);


                        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Uploaded! After generating file, you photos will be automatically deleted!");
                    KeyboardRow r = new KeyboardRow();
                    KeyboardButton b = new KeyboardButton("Generate PDF");
                    KeyboardButton c = new KeyboardButton("Generate DOCX");
                    KeyboardButton delete = new KeyboardButton("Delete");
                    r.add(b);
                    r.add(c);
                    r.add(delete);
                    ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup(List.of(r));
                    replyMarkup.setResizeKeyboard(true);
                    sendMessage.setReplyMarkup(replyMarkup);
                    sendMessage.setChatId(String.valueOf(chatId));
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
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
                        com.example.domains.User user = User.builder()
                                .firstName(from1.getFirstName())
                                .lastName(from1.getLastName())
                                .username(from1.getUserName())
                                .chatId(chatId)
                                .build();
                        userRepository.save(user);
                        sendMessage("6632222728", "New user: " + user.getFirstName() + "\nLast name: " + user.getLastName() + "\nUsername: " + user.getUsername() + "\nChatId: " + user.getChatId() + "\n\n");
                    }
                });

            } else if (text.equals("Generate PDF")) {
                if (chatIdToPhotoPath.containsKey(chatId)) {

                    List<String> paths = chatIdToPhotoPath.get(chatId);
                    ImageToPDF.imageTodPdf(paths);

                    sendPdfToUser(chatId, new File("output.pdf"));

                    chatIdToPhotoPath.remove(chatId);
                    CompletableFuture.runAsync(() -> {
                        paths.forEach(path -> {
                            try {
                                Files.deleteIfExists(Paths.get(path));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    });

                }
            } else if (text.equals("Generate DOCX")) {
                if (chatIdToPhotoPath.containsKey(chatId)) {
                    List<String> paths = chatIdToPhotoPath.get(chatId);
                    JpegToDocxService.convertJpegToDocx(paths);

                    sendPdfToUser(chatId, new File("output.docx"));
                        chatIdToPhotoPath.remove(chatId);
                    CompletableFuture.runAsync(() -> {
                        paths.forEach(path -> {
                            try {
                                Files.deleteIfExists(Paths.get(path));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    });
                } else {
                    sendMessage(chatId, "Please upload your photos!");
                }
            }  else if (text.contains("/setadmin") && userService.isAdmin(chatId)) {
                String substring = text.trim().substring(11);
                if (userRepository.existsByUserId(substring)) {
                    userRepository.setAdmin(substring);
                    sendMessage(chatId, "User " + substring + " is admin now!");
                } else {
                    sendMessage(chatId, "Something wrong happened!");
                }


            } else if (text.contains("/deleteadmin") && userService.isAdmin(chatId)) {
                String substring = text.trim().substring(13);
                if (userRepository.existsByUserId(substring)) {
                    sendMessage(chatId, "User " + substring + " is not admin now!");
                } else {
                    sendMessage(chatId, "Something wrong happened!");
                }
            }else if (text.contains("/ads") && userService.isAdmin(chatId)) {
                sendMessage(chatId, messageSource.getMessage("send_message_to_all_users", null, Locale.forLanguageTag(userRepository.findByChatId(chatId).getLang())));
                sendMessageToAllUsers(message);
            }
            else if (text.equals("Delete")) {
                sendMessage(chatId, "Deleted!");
                if (chatIdToPhotoPath.containsKey(chatId)) {
                    CompletableFuture.runAsync(() -> {
                        chatIdToPhotoPath.remove(chatId);
                        List<String> paths = chatIdToPhotoPath.get(chatId);
                        paths.forEach(path -> {
                            try {
                                Files.deleteIfExists(Paths.get(path));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    });
                }
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
                sendMessage(chatId, "Please upload your photos!");
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


    private File downloadPhoto(PhotoSize photo) throws TelegramApiException, IOException {
        GetFile getFile = new GetFile();
        getFile.setFileId(photo.getFileId());
        org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFile);
        String fileUri = telegramFile.getFilePath();

        URL fileUrl = new URL("https://api.telegram.org/file/bot" + BotConfig.botToken + "/" + fileUri);
        String filePath = getFilePath(photo);

        Path directory = Paths.get(filePath).getParent();
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        File downloadedFile = new File(filePath);

        try (InputStream inputStream = fileUrl.openStream();
             FileOutputStream outputStream = new FileOutputStream(downloadedFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return downloadedFile;
    }

    private String getFilePath(PhotoSize photo) {
        return String.format("src/main/resources/photos/%s.jpg", photo.getFileId());
    }


    private void sendPdfToUser(String chatId, File pdfFile) {
        if (pdfFile != null && pdfFile.exists()) {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(pdfFile));
            try {
                execute(sendDocument);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendMessage(String chatId, String message) {
        try {
            execute(new SendMessage(chatId, message));
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

    @Override
    public String getBotUsername() {
        return "https://t.me/bigbitebot";
    }

    @Override
    public String getBotToken() {
        return BotConfig.botToken;
    }
}