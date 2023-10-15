package com.example.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InlineKeyboardUtil {
    public static InlineKeyboardMarkup getFilesButtons() {
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = getButton(InlineKeyboardConstants.EXCEL, InlineKeyboardConstants.EXCEL_FILE_DATA);
        InlineKeyboardButton button2 = getButton(InlineKeyboardConstants.PDF, InlineKeyboardConstants.PDF_FILE_DATA);
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
        InlineKeyboardButton inlineKeyboardButton=getButton("English\uD83C\uDDEC\uD83C\uDDE7","en");
        InlineKeyboardButton inlineKeyboardButton1=getButton("Uzbek\uD83C\uDDFA\uD83C\uDDFF","uz");
        return new InlineKeyboardMarkup(Collections.singletonList(List.of(inlineKeyboardButton,inlineKeyboardButton1)));
    }
    public static InlineKeyboardMarkup getAdminKeyboardMarkup(){
        InlineKeyboardButton users= getButton("Users", "users");
        InlineKeyboardButton messages= getButton("Excel Users", "excel");
        InlineKeyboardButton admins= getButton("Admins", "admins");
        return new InlineKeyboardMarkup(Collections.singletonList(List.of(users, messages,admins)));
    }

}
