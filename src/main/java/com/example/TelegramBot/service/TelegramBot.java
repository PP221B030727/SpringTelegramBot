package com.example.TelegramBot.service;

import com.example.TelegramBot.config.BotConfig;
import com.example.TelegramBot.dao.UserDao;
import com.example.TelegramBot.models.Currency;
import com.example.TelegramBot.models.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;

    public TelegramBot(BotConfig botConfig){
        this.config = botConfig;
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken(){
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()){
            try {
                botMessages(update.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            sendMessage(callbackQuery.getMessage().getChatId(),callbackData+"KZT");
            // В callbackData будет содержаться значение currency.getDescription()
            // Далее вы можете использовать это значение по вашему усмотрению
        }
    }

    private void botMessages(Message message) throws IOException, TelegramApiException {
        if(message.hasText() && message.hasEntities()){
            Optional <MessageEntity> commandEntity = message.getEntities().stream().filter(e->"bot_command".equals(e.getType())).findFirst();
            if(commandEntity.isPresent()){
                long chatId = message.getChatId();
                String command = message.getText();
                switch (command){
                    case "/get_currency":
                        showAllData(chatId);
                        break;


                    default:


                }
            }

        }
        else if(message.hasText() || message.hasVoice()){
            long chatId = message.getChatId();
            String firstname = message.getChat().getFirstName();
            String lastname = message.getChat().getLastName();
            String username = message.getChat().getUserName();
            String messageText = message.getText();
            Voice voice = message.getVoice();
            switch (messageText){
                case "/start":
                    startCommandReceived(chatId , firstname);
                    break;

                case "/users":
                    showAllUsers(chatId);
                    break;
                case "/data":
                    try {
                        showAllData(chatId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default: forConvert(messageText , chatId);
                    UserDao dao = new UserDao();
                    User user = new User();
                    user.setFirstName(firstname);
                    user.setLastName(lastname);
                    user.setUserName(username);
                    user.setMessage(messageText);
                    dao.save(user);
            }
        }
    }
    private void writeMode(long chatId) throws IOException, TelegramApiException {
        CurrencyService curService = new CurrencyService();
        List<Currency> result = curService.getCurrencies(curService.date);
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (Currency currency : result) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(currency.getTitle())
                    .callbackData(currency.getTitle()) // Описание передается в качестве callbackData
                    .build();
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboardRows.add(row);
        }

        execute(SendMessage.builder()
                .text("Please select a currency:")
                .chatId(chatId)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(keyboardRows).build())
                .build()
        );



    }

    private void showAllData(long chatId) throws IOException, TelegramApiException {
        CurrencyService curService = new CurrencyService();
        List<Currency> result = curService.getCurrencies(curService.date);
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (Currency currency : result) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(currency.getTitle())
                    .callbackData(currency.getDescription()) // Описание передается в качестве callbackData
                    .build();
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboardRows.add(row);
        }

        execute(SendMessage.builder()
                .text("Please select a currency:")
                .chatId(chatId)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(keyboardRows).build())
                .build()
        );
    }

    private void showAllUsers(long chatId){
        UserDao dao = new UserDao();
        List<User> userList = dao.index();
        for(int i = 0 ; i < userList.size(); i++){
            try {
                String message = "user : "+ userList.get(i).getFirstName();
                sendMessage(chatId , message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void startCommandReceived(long chatId , String name){
        String answer = "Hi, " + name + ". nice to meet you!";
        sendMessage(chatId,answer);
    }




    private void sendMessage(long chatId , String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try{
            execute(message);
        }
        catch (TelegramApiException e){
        }
    }

    public  void forConvert(String message , long chatId) throws IOException {
        Pattern pattern = Pattern.compile("^(\\d+(\\.\\d+)?)\\s+([A-Z]{3})$");
        Matcher matcher = pattern.matcher(message);

        if (matcher.matches()) {
            // Извлечение числа и валютного кода
            String number = matcher.group(1);
            String currencyCode = matcher.group(3);
            CurrencyService curService = new CurrencyService();
            List<Currency> result = curService.getCurrencies(curService.date);
            double num = Double.parseDouble(number);
            System.out.println(currencyCode);
            if(currencyCode.equals("KZT")){
                for(Currency currency : result){
                    double currencyDesc = Double.parseDouble(currency.getDescription());
                    sendMessage(chatId , String.valueOf(num/currencyDesc)+" "+currency.getTitle());
                }
            }
            else{
                for(Currency currency : result){
                    if(currency.getTitle().equals(currencyCode)){
                        double currencyDesc = Double.parseDouble(currency.getDescription());
                        sendMessage(chatId , String.valueOf(num*currencyDesc)+" KZT");
                        break;
                    }

                }
            }

            sendMessage(chatId, "YES, IT'S WORK");
        } else {
            sendMessage(chatId, "Command not Found");
        }
    }

}
