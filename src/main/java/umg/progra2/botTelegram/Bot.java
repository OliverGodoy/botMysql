package umg.progra2.botTelegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "Legion123_bot"; //Legion123_bot
    }

    @Override
    public String getBotToken() {
        return "7372534321:AAG_8D-keAf6RGgEQ5KcnX9dyg1UH2sVi40"; //7372534321:AAG_8D-keAf6RGgEQ5KcnX9dyg1UH2sVi40
    }

    //El método onUpdateReceived(Update update) de la clase Bot se usa para manejar todas las actualizaciones que el
    // bot recibe.
    // Dependiendo del tipo de actualización, se toman diferentes acciones.

    @Override
    public void onUpdateReceived(Update update) {
        //Se verifica si la actualización contiene un mensaje y si ese mensaje tiene texto.
        //Luego se procesa el contenido del mensaje y se responde según el caso.

        String nombre = update.getMessage().getFrom().getFirstName();
        String apellido = update.getMessage().getFrom().getLastName();
        String nickName = update.getMessage().getFrom().getUserName();


        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            if(message_text.toLowerCase().equals("Hola")){
                sendText(chat_id,"Que ondas "+nombre+ "gusto de saludarte");

            }
            if(message_text.toLowerCase().equals("bye")){
                sendText(chat_id,"Orales" +nombre);
            }

            //System.out.println("User id: " + chat_id + " Message: " + message_text);
            System.out.println("Hola " +nickName+ "Tu nombre es: " +nombre+""+apellido);
            System.out.println("Enviaste:"+message_text);
        }
    }

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


}
