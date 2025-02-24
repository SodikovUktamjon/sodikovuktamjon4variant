package com.example.util;

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

        public static InlineKeyboardMarkup orderMenuInlineKeyboardMarkup(String language) {
            // Define the menu items for each language
            String hotDogMini = getItemName(language, "Ход дог мини", "Hot Dog Mini", "Hot Dog Mini");
            String hotDogClassic = getItemName(language, "Ход дог классический", "Hot Dog Classic", "Hot Dog Classic");
            String hotDogBig = getItemName(language, "Ход дог биг", "Hot Dog Big", "Hot Dog Big");
            String haggy = getItemName(language, "Хагги", "Haggy", "Haggy");
            String danar = getItemName(language, "Данар", "Danar", "Danar");
            String lavashClassic = getItemName(language, "Лаваш классический", "Lavash Classic", "Lavash Classic");
            String lavashBig = getItemName(language, "Лаваш биг", "Lavash Big", "Lavash Big");
            String shawarmaClassic = getItemName(language, "Шаурма классическая", "Shawarma Classic", "Shawarma Classic");
            String shawarmaBig = getItemName(language, "Шаурма биг", "Shawarma Big", "Shawarma Big");
            String fries = getItemName(language, "Фри", "Fries", "Fries");

            // Create inline buttons for menu
            InlineKeyboardButton button1 = new InlineKeyboardButton("🌭 " + hotDogMini + " - 10,000");
            button1.setCallbackData("hot_dog_mini");

            InlineKeyboardButton button2 = new InlineKeyboardButton("🌭 " + hotDogClassic + " - 15,000");
            button2.setCallbackData("hot_dog_classic");

            InlineKeyboardButton button3 = new InlineKeyboardButton("🌭 " + hotDogBig + " - 25,000");
            button3.setCallbackData("hot_dog_big");

            InlineKeyboardButton button4 = new InlineKeyboardButton("🍔 " + haggy + " - 30,000");
            button4.setCallbackData("haggy");

            InlineKeyboardButton button5 = new InlineKeyboardButton("🍖 " + danar + " - 30,000");
            button5.setCallbackData("danar");

            InlineKeyboardButton button6 = new InlineKeyboardButton("🥙 " + lavashClassic + " - 30,000");
            button6.setCallbackData("lavash_classic");

            InlineKeyboardButton button7 = new InlineKeyboardButton("🥙 " + lavashBig + " - 40,000");
            button7.setCallbackData("lavash_big");

            InlineKeyboardButton button8 = new InlineKeyboardButton("🥙 " + shawarmaClassic + " - 30,000");
            button8.setCallbackData("shawarma_classic");

            InlineKeyboardButton button9 = new InlineKeyboardButton("🥙 " + shawarmaBig + " - 40,000");
            button9.setCallbackData("shawarma_big");

            InlineKeyboardButton button10 = new InlineKeyboardButton("🍟 " + fries + " - 15,000");
            button10.setCallbackData("fries");

            // Create rows of buttons
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(button1);
            row1.add(button2);
            rows.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(button3);
            row2.add(button4);
            rows.add(row2);

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(button5);
            row3.add(button6);
            rows.add(row3);

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            row4.add(button7);
            row4.add(button8);
            rows.add(row4);

            List<InlineKeyboardButton> row5 = new ArrayList<>();
            row5.add(button9);
            row5.add(button10);
            rows.add(row5);

            // Set up the inline keyboard markup
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(rows);

            return inlineKeyboardMarkup;
        }

        private static String getItemName(String language, String ru, String en, String uz) {
            return switch (language) {
                case "ru" -> ru;
                case "uz" -> uz;
                default -> en;
            };
        }
    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static InlineKeyboardMarkup createCounterInlineKeyboard(int counterValue, String itemName) {
        InlineKeyboardButton minusButton = createButton("➖", "counter_minus_" + counterValue);
        InlineKeyboardButton counterButton = createButton(  ""+counterValue, "counter_" + counterValue); // Display counter value
        InlineKeyboardButton plusButton = createButton("➕", "counter_plus_" + counterValue);
        // Create a row for the counter and action buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(minusButton);
        row1.add(counterButton);
        row1.add(plusButton);

        // Create a row for the item (optional)
        InlineKeyboardButton itemButton = createButton("✅ Confirm", "item_" + itemName);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(itemButton);

        // Add rows to the list
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);

        // Create inline keyboard markup
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    }

