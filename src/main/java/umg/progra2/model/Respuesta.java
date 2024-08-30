package umg.progra2.model;

import java.sql.Timestamp;

public class Respuesta {

    private int id;
    private String seccion;
    private long telegramid;
    private int preguntaid;
    private String respuestatexto;
    private Timestamp fecharespuesta;

    public Respuesta(int id, String seccion, long telegramId, int preguntaId, String respuestaTexto, Timestamp fechaRespuesta) {
    }

    public Respuesta(long chatId, String section, int index, String respuesta) {
    }

    public Respuesta() {

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public long getTelegramid() {
        return telegramid;
    }

    public void setTelegramid(long telegramid) {
        this.telegramid = telegramid;
    }

    public int getPreguntaid() {
        return preguntaid;
    }

    public void setPreguntaid(int preguntaid) {
        this.preguntaid = preguntaid;
    }

    public String getRespuestatexto() {
        return respuestatexto;
    }

    public void setRespuestatexto(String respuestatexto) {
        this.respuestatexto = respuestatexto;
    }

    public Timestamp getFecharespuesta() {
        return fecharespuesta;
    }

    public void setFecharespuesta(Timestamp fecharespuesta) {
        this.fecharespuesta = fecharespuesta;
    }
}
