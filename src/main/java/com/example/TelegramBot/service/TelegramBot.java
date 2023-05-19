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
                    default: sendMessage(chatId , "Command not Found");
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
                default: sendMessage(chatId , "Sorry command was not recognized");
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


//    private void showAllData(long chatId) throws IOException {
//        CurrencyService curService = new CurrencyService();
//        List<Currency> result = curService.getCurrencies(curService.date);
//        for(int i = 0 ; i < result.size() ; i++){
//            try{
//                sendMessage(chatId,(result.get(i).getTitle()+"="+result.get(i).getDescription()+"KZT"));
//            }
//            catch (Exception e){
//                throw new RuntimeException(e);
//            }
//
//        }
//    }
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

}
