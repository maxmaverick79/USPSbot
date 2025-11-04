package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class MyAwesomeBot extends TelegramLongPollingBot {

    public static Set<String> selectedStates= new HashSet<>();

    private final Map<Long, Set<String>> userSelections = new HashMap<>();

    private static final List<String> ABBREVIATIONS = Arrays.asList(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
    );

    @Override
    public String getBotUsername() {
        return "postTruckUsps_bot"; // Replace with your bot username
    }

    @Override
    public String getBotToken() {
        return "8400749422:AAGq7VJF8u_QdB978CiH_9wtSqhsT1aRrZg"; // Replace with your bot token
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            System.out.println(chatId);
            userSelections.putIfAbsent(chatId, new HashSet<>());
            sendAbbreviationButtons(chatId, null); // first time, no messageId
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    public static void startBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyAwesomeBot());
            System.out.println("Bot started successfully!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        if (data.equals("next")) {
            Set<String> selected = userSelections.getOrDefault(chatId, new HashSet<>());

            // Update original inline keyboard message with selection result
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId(messageId);
            editMessageText.setText("✅ You selected: " + String.join(", ", selected) +
                    "\n\nLoad details sent to this chat.");
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        } else if (data.startsWith("abbr_")) {
            String abbr = data.substring("abbr_".length());
            Set<String> selected = userSelections.computeIfAbsent(chatId, k -> new HashSet<>());
            selectedStates.addAll(selected);
            if (selected.contains(abbr)) {
                selected.remove(abbr);
            } else {
                selected.add(abbr);
            }
            sendAbbreviationButtons(chatId, messageId);
        }
    }

    private void sendAbbreviationButtons(long chatId, Integer messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        Set<String> selected = userSelections.get(chatId);

        for (int i = 0; i < ABBREVIATIONS.size(); i += 5) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = i; j < i + 5 && j < ABBREVIATIONS.size(); j++) {
                String abbr = ABBREVIATIONS.get(j);
                boolean isSelected = selected.contains(abbr);

                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText((isSelected ? "✅ " : "") + abbr);
                btn.setCallbackData("abbr_" + abbr);
                row.add(btn);
            }
            rows.add(row);
        }

        if (!selected.isEmpty()) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Next ➡️");
            nextButton.setCallbackData("next");
            rows.add(Collections.singletonList(nextButton));
        }

        markup.setKeyboard(rows);

        try {
            if (messageId == null) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Select states (multiple allowed):");
                message.setReplyMarkup(markup);
                execute(message);
            } else {
                EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
                editMarkup.setChatId(String.valueOf(chatId));
                editMarkup.setMessageId(messageId);
                editMarkup.setReplyMarkup(markup);
                execute(editMarkup);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

     public static void sendToChat(long chatId, int loadId, int totalMiles, String pickup, String delivery,
                            String fromCity, String fromState, String toCity, String toState) {
        StringBuilder sb = new StringBuilder();

        Instant instant = Instant.parse(pickup);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        String datePart = zonedDateTime.toLocalDate().toString();
        String timePart = zonedDateTime.toLocalTime().toString();

         Instant instant1 = Instant.parse(pickup);
         ZonedDateTime zonedDateTime1 = instant1.atZone(ZoneId.of("UTC"));
         String datePart1 = zonedDateTime1.toLocalDate().toString();
         String timePart1 = zonedDateTime1.toLocalTime().toString();


         sb.append("📦 *Load Details:*\n")
                .append("🆔 Load ID: ").append(loadId).append("\n")
                .append("\n")
                .append("📏 Distance: ").append(totalMiles).append("\n")
                .append("\n")
                .append("🚚 Pickup: ").append(datePart).append("  ").append(timePart).append("\n")
                .append("📦 Delivery: ").append(datePart1).append("  ").append(timePart1).append("\n")
                .append("\n")
                .append("📍 From: ").append(fromCity).append(", ").append(fromState).append("\n")
                .append("🏁 To: ").append(toCity).append(", ").append(toState).append("\n");

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sb.toString());
        message.setParseMode("Markdown");

        try {
            new MyAwesomeBot().execute(message); // needs an instance to send
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyAwesomeBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}