package umg.progra2.dao;
import java.sql.Connection;
import umg.progra2.db.DatabaseConnection;
import umg.progra2.model.Respuesta;

import java.sql.*;

public class RespuestaDao {



    public void insertarRespuesta(Respuesta respuesta) throws SQLException {
        String query = "INSERT INTO tb_respuestas (seccion, telegram_id, pregunta_id, respuesta_texto) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, respuesta.getSeccion());
            statement.setLong(2, respuesta.getTelegramid());
            statement.setInt(3, respuesta.getPreguntaid());
            statement.setString(4, respuesta.getRespuestatexto());
            statement.executeUpdate();
        }
    }

    public void actualizarRespuesta(Respuesta respuesta) throws SQLException {
        String query = "UPDATE tb_respuestas SET seccion = ?, telegram_id = ?, pregunta_id = ?, respuesta_texto = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, respuesta.getSeccion());
            statement.setLong(2, respuesta.getTelegramid());
            statement.setInt(3, respuesta.getPreguntaid());
            statement.setString(4, respuesta.getRespuestatexto());
            statement.setInt(5, respuesta.getId());
            statement.executeUpdate();
        }
    }

    public Respuesta getRespuestaPorId(int id) throws SQLException {
        String query = "SELECT * FROM tb_respuestas WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapRowToRespuesta(resultSet);
            }
            return null;
        }
    }

    public Respuesta getRespuestaPorSeccion(String seccion) throws SQLException {
        String query = "SELECT * FROM tb_respuestas WHERE seccion = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, seccion);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapRowToRespuesta(resultSet);
            }
            return null;
        }
    }

    public Respuesta getRespuestaPorTelegramId(long TelegramId) throws SQLException {
        String query = "SELECT * FROM tb_respuestas WHERE telegramid = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, TelegramId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapRowToRespuesta(resultSet);
            }
            return null;
        }
    }

    public Respuesta getRespuestaPorFechaRespuesta(Timestamp fechaRespuesta) throws SQLException {
        String query = "SELECT * FROM tb_respuestas WHERE fecha_respuesta = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, fechaRespuesta);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapRowToRespuesta(resultSet);
            }
            return null;
        }
    }

    private Respuesta mapRowToRespuesta(ResultSet resultSet) throws SQLException {
        return new Respuesta(
                resultSet.getInt("id"),
                resultSet.getString("seccion"),
                resultSet.getLong("telegram_id"),
                resultSet.getInt("pregunta_id"),
                resultSet.getString("respuesta_texto"),
                resultSet.getTimestamp("fecha_respuesta")
        );
    }

}
