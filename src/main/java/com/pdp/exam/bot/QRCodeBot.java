package com.pdp.exam.bot;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.pdp.exam.methods.Services;
import com.pdp.exam.model.State;
import com.pdp.exam.model.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class QRCodeBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            List<User> users = null;
            try {
                users = Services.getInstance().getUsers();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Message message = update.getMessage();
            User user = null;
            try {
                user = Services.getInstance().getUser(message.getChat().getId().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String text = message.getText();
            if (text != null && text.equals("/start")) {
                if (user == null) {
                    users.add(new User(message.getChat().getId().toString(), State.CHOOSE_MENU));

                    try {
                        Services.getInstance().writeUsers(users);
                        execute(Services.getInstance().mainMenu(message));
                    } catch (TelegramApiException | IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    SendMessage exist = new SendMessage(message.getChatId().toString(), "You have started before!");
                    try {
                        Services.getInstance().changeState(message, State.CHOOSE_MENU);
                        execute(exist);
                    } catch (IOException | TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (text != null && text.equals("Generate QR-Code") && user.getState().equals(State.CHOOSE_MENU)) {
                String chatId = message.getChat().getId().toString();
                SendMessage sendMessage = new SendMessage(chatId, "Send text which you want to convert to QR Code!");
                try {
                    execute(sendMessage);
                    Services.getInstance().changeState(message, State.QR_GEN);
                } catch (TelegramApiException | IOException e) {
                    throw new RuntimeException(e);
                }

            } else if (text != null && user.getState().equals(State.QR_GEN)) {
                try {
                    SendPhoto photo = Services.getInstance().sendQRCode(message);
                    execute(photo);
                    Services.getInstance().changeState(message, State.CHOOSE_MENU);
                } catch (IOException | WriterException | TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (text != null && text.equals("Read QR-Code") && user.getState().equals(State.CHOOSE_MENU)) {
                String chatId = message.getChat().getId().toString();
                SendMessage sendMessage = new SendMessage(chatId, "Send QR Code Photo!");
                try {
                    execute(sendMessage);
                    Services.getInstance().changeState(message, State.QR_READ);
                } catch (TelegramApiException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (user.getState().equals(State.QR_READ)) {
                List<PhotoSize> photoSizes = message.getPhoto();
                if (photoSizes != null && !photoSizes.isEmpty()) {
                    photoSizes.sort(Comparator.comparing(PhotoSize::getFileId).reversed());
                    PhotoSize photoSize = photoSizes.get(0);
                    GetFile getFile = new GetFile(photoSize.getFileId());
                    try {
                        File file = execute(getFile);
                        UUID uuid = UUID.randomUUID();
                        java.io.File file1 = new java.io.File("src/main/resources/" + "file_" + uuid + ".png");
                        downloadFile(file, file1);
                        SendMessage sendMessage = Services.getInstance().outputQRCode(file1.getPath(), message);
                        execute(sendMessage);
                        Services.getInstance().changeState(message, State.CHOOSE_MENU);
                    } catch (TelegramApiException | NotFoundException | IOException | WriterException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "t.me/netcore4949_bot";
    }

    @Override
    public String getBotToken() {
        return "5720219054:AAFUYRE4Ou1Uq7D5TtQGmIbhKLjuPeFZS3E";
    }


}
