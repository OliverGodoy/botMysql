package umg.progra2.service;

import umg.progra2.dao.RespuestaDao;
import umg.progra2.db.DatabaseConnection;
import umg.progra2.db.TransactionManager;
import umg.progra2.model.Respuesta;
import umg.progra2.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RespuestaService {

    private RespuestaDao respuestaDao = new RespuestaDao();


    public void crearRespuesta(Respuesta respuesta) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            TransactionManager tm = new TransactionManager(connection);
            tm.beginTransaction();
            try {
                respuestaDao.insertarRespuesta(respuesta);
                tm.commit();
            } catch (SQLException e) {
                tm.rollback();
                throw e;
            }
        }

    }

    public void actualizarRespuesta(Respuesta respuesta) throws SQLException {
        respuestaDao.actualizarRespuesta(respuesta);
    }

    public Respuesta obtenerRespuestaPorId(int id) throws SQLException {
        return respuestaDao.getRespuestaPorId(id);
    }

    public Respuesta obtenerRespuestaPorSeccion(String seccion) throws SQLException {
        return respuestaDao.getRespuestaPorSeccion(seccion);
    }

    public Respuesta obtenerRespuestaPorTelegramId(long telegramId) throws SQLException {
        return respuestaDao.getRespuestaPorTelegramId(telegramId);
    }

    public Respuesta obtenerRespuestaPorFechaRespuesta(Timestamp fechaRespuesta) throws SQLException {
        return respuestaDao.getRespuestaPorFechaRespuesta(fechaRespuesta);
    }

}
