package com.pdp.exam.methods;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.pdp.exam.model.State;
import com.pdp.exam.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Services {
    public User getUser(String chatId) throws IOException {
        return getUsers().stream().filter(u -> chatId.equals(u.getChatId())).findFirst().orElse(null);
    }

    public List<User> getUsers() throws IOException {
        Type type = new TypeToken<List<User>>() {
        }.getType();
        Gson gson = new Gson();
        BufferedReader reader = Files.newBufferedReader(Path.of("src/main/resources/users.json"));
        List<User> users = gson.fromJson(reader, type);
        return users == null ? new ArrayList<>() : users;
    }

    public void changeState(Message message, State state) throws IOException {
        List<User> users = getUsers();
        User user = getUser(message.getChat().getId().toString());
        users.remove(user);
        user.setState(state);
        users.add(user);
        writeUsers(users);
    }

    public void writeUsers(List<User> users) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String s = gson.toJson(users);
        Files.write(Path.of("src/main/resources/users.json"), s.getBytes());
    }

    public SendMessage mainMenu(Message message) {

        ReplyKeyboardMarkup menu = new ReplyKeyboardMarkup();

        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(new KeyboardButton("Generate QR-Code"));
        keyboardRow.add(new KeyboardButton("Read QR-Code"));

        menu.setKeyboard(Collections.singletonList(keyboardRow));
        menu.setResizeKeyboard(true);

        SendMessage welcome = new SendMessage();
        welcome.setText("Welcome to QR-Code Generator Bot!");
        welcome.setChatId(message.getChatId());
        welcome.setReplyMarkup(menu);
        return welcome;
    }

    public SendPhoto sendQRCode(Message message) throws IOException, WriterException {

        String data = message.getText();

        String path = "/Users/Asliddin/Desktop/Java/3-modul/exam_module_3/src/main/resources/" + message.getChatId() + "_demo.png";

        String charset = "UTF-8";

        createQR(data, path, charset,400, 400);
        InputFile inputFile = new InputFile(new File(path));
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption("Your QR Code!");
        sendPhoto.setChatId(message.getChatId());
        return sendPhoto;
    }

    public void createQR(String data, String path, String charset, int height, int width) throws WriterException, IOException {

        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(data.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, width, height);

        MatrixToImageWriter.writeToFile(
                matrix,
                path.substring(path.lastIndexOf('.') + 1),
                new File(path));
    }

    public SendMessage outputQRCode(String path,Message message) throws WriterException, IOException, NotFoundException {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(readQR(path));
        return sendMessage;
    }


    public String readQR(String path) throws IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer
                (new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(path)))));

        Result result = new MultiFormatReader().decode(binaryBitmap);

        return result.getText();
    }


    private static final Services SERVICES = new Services();

    public static Services getInstance() {
        return SERVICES;
    }
}
