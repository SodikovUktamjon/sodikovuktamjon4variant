package com.example.util;

import com.example.UserServiceApplication;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InlineKeyboardUtil {
    public static InlineKeyboardMarkup getFilesButtons() {
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = getButton(UserServiceApplication.InlineKeyboardConstants.EXCEL, UserServiceApplication.InlineKeyboardConstants.EXCEL_FILE_DATA);
        InlineKeyboardButton button2 = getButton(UserServiceApplication.InlineKeyboardConstants.PDF, UserServiceApplication.InlineKeyboardConstants.PDF_FILE_DATA);
        row.add(button1);
        row.add(button2);

        lists.add(row);
        return new InlineKeyboardMarkup(lists);
    }


    private static InlineKeyboardButton getButton(String demo, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(demo);
        button.setCallbackData(callbackData);
        return button;
    }
    public static InlineKeyboardMarkup getInlineUserLanguages(){
        InlineKeyboardButton inlineKeyboardButton=getButton("Русский\uD83C\uDDF7\uD83C\uDDFA","ru");
        InlineKeyboardButton inlineKeyboardButton1=getButton("Uzbek\uD83C\uDDFA\uD83C\uDDFF","uz");
        return new InlineKeyboardMarkup(Collections.singletonList(List.of(inlineKeyboardButton,inlineKeyboardButton1)));
    }
    public static InlineKeyboardMarkup getAdminKeyboardMarkup(){
        InlineKeyboardButton users= getButton("Users", "users");
        InlineKeyboardButton messages= getButton("Excel Users", "excel");
        InlineKeyboardButton admins= getButton("Admins", "admins");
        return new InlineKeyboardMarkup(Collections.singletonList(List.of(users, messages,admins)));
    }

public static ReplyKeyboardMarkup shareContact(){
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(true);

    KeyboardButton contactButton = new KeyboardButton();
    contactButton.setText("📞 Share Contact");
    contactButton.setRequestContact(true);

    KeyboardRow row = new KeyboardRow();
    row.add(contactButton);
    keyboardMarkup.setKeyboard(Collections.singletonList(row));
    return keyboardMarkup;
}

    public static ReplyKeyboardMarkup menuKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow(
                Collections.singletonList(new KeyboardButton("🍽️ Menu"))
        );

        KeyboardRow row2 = new KeyboardRow(
                Collections.singletonList(new KeyboardButton("📜 Order History"))
        );

        KeyboardRow row3 = new KeyboardRow(
                Arrays.asList(new KeyboardButton("🛒 Basket"), new KeyboardButton("📞 Contact"))
        );
        keyboardMarkup.setKeyboard(Arrays.asList(
                row1, row2, row3));

        return keyboardMarkup;
    }


}
