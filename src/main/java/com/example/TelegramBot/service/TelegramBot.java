package com.example.TelegramBot.service;

import com.example.TelegramBot.config.BotConfig;
import com.example.TelegramBot.dao.UserDao;
import com.example.TelegramBot.models.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

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
        if(update.hasMessage() && update.getMessage().hasText()){
            long chatId = update.getMessage().getChatId();
            String firstname = update.getMessage().getChat().getFirstName();
            String lastname = update.getMessage().getChat().getLastName();
            String username = update.getMessage().getChat().getUserName();
            String messageText = update.getMessage().getText();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId , firstname);
                    break;

                case "/users":
                    showAllUsers(chatId);
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
