package umg.progra2.botTelegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class botPregunton extends TelegramLongPollingBot {


    private final Map<Long, Integer> indicePregunta = new HashMap<>();
    private final Map<Long, String> seccionActiva = new HashMap<>();
    private final Map<String, String[]> preguntas = new HashMap<>();


    @Override
    public String getBotUsername() {
        return "Legion123_bot";
    }

    @Override
    public String getBotToken() {
        return "7372534321:AAG_8D-keAf6RGgEQ5KcnX9dyg1UH2sVi40";
    }



    public botPregunton() {
        // Inicializa los cuestionarios con las preguntas.
        preguntas.put("SECTION_1", new String[]{"🤦‍♂️1.1- Estas aburrido?", "😂😂 1.2- Te bañaste hoy?", "🤡🤡 Pregunta 1.3"});
        preguntas.put("SECTION_2", new String[]{"Pregunta 2.1", "Pregunta 2.2", "Pregunta 2.3"});
        preguntas.put("SECTION_3", new String[]{"Pregunta 3.1", "Pregunta 3.2", "Pregunta 3.3"});
        preguntas.put("SECTION_4", new String[]{"4.1- Esta lloviendo? ", "4.2- Cuantos años tienes? ", "4.3- Sabes manejar carro?", "4.4- Sabes manejar moto?", "4.5- Sabes manejar avion?"});
    }

    @Override
    public void onUpdateReceived(Update actualizacion) {
        if (actualizacion.hasMessage() && actualizacion.getMessage().hasText()) {
            String messageText = actualizacion.getMessage().getText();
            long chatId = actualizacion.getMessage().getChatId();

            if (messageText.equals("/menu")) {
                sendMenu(chatId);
            } else if (seccionActiva.containsKey(chatId)) {
                manejaCuestionario(chatId, messageText);
            }
        } else if (actualizacion.hasCallbackQuery()) { //es una respusta de un boton
            String callbackData = actualizacion.getCallbackQuery().getData();
            long chatId = actualizacion.getCallbackQuery().getMessage().getChatId();
            inicioCuestionario(chatId, callbackData);
        }
    }

    void sendMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona una sección:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Crea los botones del menú
        rows.add(crearFilaBoton("Sección 1", "SECTION_1"));
        rows.add(crearFilaBoton("Sección 2", "SECTION_2"));
        rows.add(crearFilaBoton("Sección 3", "SECTION_3"));
        rows.add(crearFilaBoton("Sección 4", "SECTION_4"));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



//    private void sendMenu(long chatId) {
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText("Selecciona una sección:");
//
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
//
//        // Crea los botones del menú
//        rows.add(crearFilaBoton("Sección 1", "SECTION_1"));
//        rows.add(crearFilaBoton("Sección 2", "SECTION_2"));
//        rows.add(crearFilaBoton("Sección 3", "SECTION_3"));
//        rows.add(crearFilaBoton("Sección 4", "SECTION_4"));
//
//        markup.setKeyboard(rows);
//        message.setReplyMarkup(markup);
//
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }

    private List<InlineKeyboardButton> crearFilaBoton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    void inicioCuestionario(long chatId, String section) {
        seccionActiva.put(chatId, section);
        indicePregunta.put(chatId, 0);
        enviarPregunta(chatId);
    }

    private void enviarPregunta(long chatId) {
        String seccion = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        String[] questions = preguntas.get(seccion);

        if (index < questions.length) {
            sendText(chatId, questions[index]);
        } else {
            sendText(chatId, "¡Has completado el cuestionario!");
            seccionActiva.remove(chatId);
            indicePregunta.remove(chatId);
        }
    }

    void manejaCuestionario(long chatId, String response) {
        String section = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);

        sendText(chatId, "Tu respuesta fue: " + response);
        indicePregunta.put(chatId, index + 1);

        enviarPregunta(chatId);
    }

    private void sendText(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
