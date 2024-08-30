package umg.progra2.botTelegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import umg.progra2.dao.RespuestaDao;
import umg.progra2.model.Respuesta;
import umg.progra2.model.User;
import umg.progra2.service.RespuestaService;
import umg.progra2.service.UserService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class botCuestionario extends TelegramLongPollingBot {
    private Map<Long, String> estadoConversacion = new HashMap<>();
    User usuarioConectado = null;
    UserService userService = new UserService();
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


    private void inicioCuestionario(long chatId, String section) {
        seccionActiva.put(chatId, section);
        indicePregunta.put(chatId, 0);
        enviarPregunta(chatId);
    }


    @Override
    public void onUpdateReceived(Update actualizacion) {


        if (actualizacion.hasMessage() && actualizacion.getMessage().hasText()) {
            String messageText = actualizacion.getMessage().getText();
            long chatId = actualizacion.getMessage().getChatId();


            if (messageText.equals("/menu")) {
                sendMenu(chatId);
                return;
            } else if (seccionActiva.containsKey(chatId)) {
                manejaCuestionario(chatId, messageText);
                return;
            }
        } else if (actualizacion.hasCallbackQuery()) { //es una respusta de un boton
            String callbackData = actualizacion.getCallbackQuery().getData();
            long chatId = actualizacion.getCallbackQuery().getMessage().getChatId();
            inicioCuestionario(chatId, callbackData);
            return;
        }


        //obtener el nombre y apellido del usuario en una variable
        String userFirstName = actualizacion.getMessage().getFrom().getFirstName();
        String userLastName = actualizacion.getMessage().getFrom().getLastName();
        String nickName = actualizacion.getMessage().getFrom().getUserName();
        long chat_id = actualizacion.getMessage().getChatId();
        String mensaje_Texto = actualizacion.getMessage().getText();


        try {
            String state = estadoConversacion.getOrDefault(chat_id, "");
            usuarioConectado = userService.getUserByTelegramId(chat_id);

            // Verificación inicial del usuario, si usuarioConectado es nullo, significa que no tiene registro de su id de telegram en la tabla
            if (usuarioConectado == null && state.isEmpty()) {
                sendText(chat_id, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", no tienes un usuario registrado en el sistema. Por favor ingresa tu correo electrónico:");
                estadoConversacion.put(chat_id, "ESPERANDO_CORREO");
                return;
            }

            // Manejo del estado ESPERANDO_CORREO
            if (state.equals("ESPERANDO_CORREO")) {
                processEmailInput(chat_id, mensaje_Texto);
                return;
            }

            sendText(chat_id, "Envia /menu para iniciar el cuestionario ");

        } catch (Exception e) {
            sendText(chat_id, "Ocurrió un error al procesar tu mensaje. Por favor intenta de nuevo.");
        }


    }

    //verifica si el usurio está registrado en la tabla con su correo electrónico
    private void processEmailInput(long chat_id, String email) {
        sendText(chat_id, "Recibo su Correo: " + email);
        estadoConversacion.remove(chat_id); // Reset del estado
        try{
            usuarioConectado = userService.getUserByEmail(email);
        } catch (Exception e) {
            System.err.println("Error al obtener el usuario por correo: " + e.getMessage());
            e.printStackTrace();
        }


        if (usuarioConectado == null) {
            sendText(chat_id, "El correo no se encuentra registrado en el sistema, por favor contacte al administrador.");
        } else {
            usuarioConectado.setTelegramid(chat_id);
            try {
                userService.updateUser(usuarioConectado);
            } catch (Exception e) {
                System.err.println("Error al actualizar el usuario: " + e.getMessage());
                e.printStackTrace();
            }

            sendText(chat_id, "Usuario actualizado con éxito!");

        }
    }

    private void sendMenu(long chatId) {
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


    //funcion para formatear la información del usuario
    private String formatUserInfo(String firstName, String lastName, String userName) {
        return firstName + " " + lastName + " (" + userName + ")";
    }


    private String formatUserInfo(long chat_id, String firstName, String lastName, String userName) {
        return chat_id + " " + formatUserInfo(firstName, lastName, userName);
    }



    private List<InlineKeyboardButton> crearFilaBoton (String text, String callbackData){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    private void enviarPregunta (long chatId){
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

        // Validar si la pregunta actual es la de la edad
        if (section.equals("SECTION_4") && index == 1) { // Suponiendo que la edad es la segunda pregunta
            if (!isValidAge(response)) {
                sendText(chatId, "Por favor, ingresa una edad válida (número entre 18 y 50):");
                return; // No continuar hasta que se reciba una respuesta válida
            }
        }

        // Crear una nueva instancia de Respuesta
        RespuestaService respuestaService = new RespuestaService();
        Respuesta nuevaRespuesta = new Respuesta();
        nuevaRespuesta.setSeccion(section); // Establecer la sección
        nuevaRespuesta.setTelegramid(chatId); // Establecer el ID del usuario
        nuevaRespuesta.setPreguntaid(index + 1); // Establecer el ID de la pregunta (suponiendo que las preguntas empiezan en 1)
        nuevaRespuesta.setRespuestatexto(response); // Establecer el texto de la respuesta
        nuevaRespuesta.setFecharespuesta(new Timestamp(System.currentTimeMillis())); // Establecer la fecha actual

        try {
            // Inserta la respuesta en la base de datos
            respuestaService.crearRespuesta(nuevaRespuesta);
            System.out.println("Respuesta guardada exitosamente!");
        } catch (SQLException e) {
            System.err.println("Error al insertar la respuesta: " + e.getMessage());
        }

        sendText(chatId, "Tu respuesta fue: " + response);
        indicePregunta.put(chatId, index + 1);
        enviarPregunta(chatId);
    }

    // Método para validar la edad
    private boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return age >= 18 && age <= 50; // Rango de edad válido
        } catch (NumberFormatException e) {
            return false; // No es un número válido
        }
    }

    //función para enviar mensajes
    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public botCuestionario() {
        // Inicializa los cuestionarios con las preguntas.
        preguntas.put("SECTION_1", new String[]{"🤦‍♂️1.1- Estas aburrido?", "😂😂 1.2- Te bañaste hoy?", "🤡🤡 Pregunta 1.3"});
        preguntas.put("SECTION_2", new String[]{"Pregunta 2.1", "Pregunta 2.2", "Pregunta 2.3"});
        preguntas.put("SECTION_3", new String[]{"Pregunta 3.1", "Pregunta 3.2", "Pregunta 3.3"});
        preguntas.put("SECTION_4", new String[]{"4.1- Esta lloviendo? ", "4.2- Cuantos años tienes? ", "4.3- Sabes manejar carro?", "4.4- Sabes manejar moto?", "4.5- Sabes manejar avion?"});
    }


}











//package umg.progra2.botTelegram;
//
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import umg.progra2.dao.RespuestaDao;
//import umg.progra2.model.Respuesta;
//import umg.progra2.model.User;
//import umg.progra2.service.UserService;
//
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//
//
//public class botCuestionario extends TelegramLongPollingBot {
//    @Override
//    public String getBotUsername() {
//        return "Legion123_bot";
//    }
//
//    @Override
//    public String getBotToken() {
//        return "7372534321:AAG_8D-keAf6RGgEQ5KcnX9dyg1UH2sVi40";
//    }
//
//    User usuarioConectado = null;
//    UserService userService = new UserService();
//    private Map<Long, String> seccionActiva = new HashMap<>();
//    private Map<Long, Integer> indicePregunta = new HashMap<>();
//    private RespuestaDao respuestaDAO = new RespuestaDao();
//
//    private botPregunton botPregunton = new botPregunton();
//
//    public void iniciarChat(long chatId, String userEmail) {
//        try {
//            if (!isUserRegistered(userEmail)) {
//                User nuevoUsuario = new User();
//                nuevoUsuario.setCorreo(userEmail);
//                userService.createUser(nuevoUsuario); // Llamar al método createUser para registrar al usuario
//                usuarioConectado = nuevoUsuario; // Actualizar el usuario conectado
//                sendText(chatId, "Usuario registrado exitosamente. Envía /menu para iniciar el cuestionario.");
//            } else {
//                sendText(chatId, "Envía /menu para iniciar el cuestionario.");
//            }
//        } catch (SQLException e) {
//            sendText(chatId, "Ocurrió un error al registrar el usuario. Por favor, inténtalo de nuevo más tarde.");
//            System.err.println("Error al iniciar el chat: " + e.getMessage());
//        }
//    }
//
//    private boolean isUserRegistered(String email) {
//        try {
//            usuarioConectado = userService.getUserByEmail(email);
//            return usuarioConectado != null; // Devuelve true si el usuario está registrado
//        } catch (Exception e) {
//            System.err.println("Error al verificar el registro del usuario: " + e.getMessage());
//            return false; // En caso de error, asumimos que no está registrado
//        }
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            String messageText = update.getMessage().getText();
//            long chatId = update.getMessage().getChatId();
//
//           if (messageText.equals("/menu")) {
//                // Aquí puedes crear una instancia de botPregunton y enviar el menú
//                botPregunton.sendMenu(chatId); // Llama al método para enviar el menú
//           }else if(seccionActiva.containsKey(chatId)) {
//                botPregunton.manejaCuestionario(chatId, messageText);
//           }
//        }else if (update.hasCallbackQuery()) { // Manejo de respuestas de botones
//            String callbackData = update.getCallbackQuery().getData();
//            long chatId = update.getCallbackQuery().getMessage().getChatId();
//            // Llama al método para iniciar el cuestionario en la sección seleccionada
//            botPregunton.inicioCuestionario(chatId, callbackData);
//        }
//    }
//
//    public void manejarCuestionario(long chatId, String response) {
//        String section = seccionActiva.get(chatId);
//        Integer index = indicePregunta.get(chatId);
//
//        // Verifica que section e index no sean nulos
//        if (section == null || index == null) {
//            sendText(chatId, "No se ha iniciado el cuestionario correctamente.");
//            return;
//        }
//
//        // Lógica para manejar las respuestas del cuestionario
//        if (section.equals("cuartaSeccion") && index == 2) {
//            try {
//                int edad = Integer.parseInt(response);
//                if (validarEdad(edad)) {
//                    guardarRespuesta(chatId, section, index, response);
//                    // Continuar con el cuestionario
//                } else {
//                    sendText(chatId, "Por favor, introduce una edad válida.");
//                }
//            } catch (NumberFormatException e) {
//                sendText(chatId, "Por favor, introduce un número válido para la edad.");
//            }
//        }
//    }
//
//    private boolean validarEdad(int edad) {
//        return edad >= 0 && edad <= 120; // Rango de edad razonable
//    }
//
//    private void guardarRespuesta(long chatId, String section, int index, String respuesta) {
//        try {
//            Respuesta respuestaObj = new Respuesta(chatId, section, index, respuesta);
//            respuestaDAO.insertarRespuesta(respuestaObj);
//        } catch (SQLException e) {
//            sendText(chatId, "Ocurrió un error al guardar la respuesta. Por favor, inténtalo de nuevo más tarde.");
//            System.err.println("Error al guardar la respuesta: " + e.getMessage());
//        }
//    }
////    private Map<Long, String> seccionActiva = new HashMap<>();
////    private Map<Long, Integer> indicePregunta = new HashMap<>();
////    private RespuestaDao respuestaDAO = new RespuestaDao();
////
////    public void iniciarChat(long chatId, String userEmail) throws SQLException {
////        if (!isUserRegistered(userEmail)) {
////            userService.createUser(usuarioConectado); //No estoy seguro si funcionara esta parte
////        } else {
////            sendText(chatId, "Envía /menu para iniciar el cuestionario.");
////        }
////    }
////
////
////
////    // Método para verificar si el usuario está registrado
////    private boolean isUserRegistered(String email) {
////        try {
////            usuarioConectado = userService.getUserByEmail(email);
////            return usuarioConectado != null; // Devuelve true si el usuario está registrado
////        } catch (Exception e) {
////            System.err.println("Error al verificar el registro del usuario: " + e.getMessage());
////            return false; // En caso de error, asumimos que no está registrado
////        }
////    }
////
////
////
////    public void manejarCuestionario(long chatId, String response) throws SQLException {
////        String section = seccionActiva.get(chatId);
////        int index = indicePregunta.get(chatId);
////
////        // Lógica para manejar las respuestas del cuestionario
////        // Aquí puedes agregar la lógica para la cuarta sección y la validación de la edad
////        if (section.equals("cuartaSeccion") && index == 2) {
////            int edad = Integer.parseInt(response);
////            if (validarEdad(edad)) {
////                // Guardar respuesta en la base de datos
////                guardarRespuesta(chatId, section, index, response);
////                // Continuar con el cuestionario
////            } else {
////                sendText(chatId, "Por favor, introduce una edad válida.");
////            }
////        }
////    }
////
////    private boolean validarEdad(int edad) {
////        return edad >= 0 && edad <= 120; // Rango de edad razonable
////    }
////
////    private void guardarRespuesta(long chatId, String section, int index, String respuesta) throws SQLException {
////        Respuesta respuestaObj = new Respuesta(chatId, section, index, respuesta);
////        respuestaDAO.insertarRespuesta(respuestaObj);
////    }
//
////función para enviar mensajes
//public void sendText(Long who, String what){
//    SendMessage sm = SendMessage.builder()
//            .chatId(who.toString()) //Who are we sending a message to
//            .text(what).build();    //Message content
//    try {
//        execute(sm);                        //Actually sending the message
//    } catch (TelegramApiException e) {
//        throw new RuntimeException(e);      //Any error will be printed here
//    }
//}
//
//}
