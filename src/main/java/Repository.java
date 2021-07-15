import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Repository {

    private static Connection connection;
    private static Statement statement;

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:weather.db");

            statement = connection.createStatement();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getWeather(String city, String localDate) throws SQLException {
        PreparedStatement getWeatherStatement = connection.prepareStatement("SELECT * FROM weather WHERE city = ? and localDate LIKE ?;");
        //PreparedStatement getWeatherStatement = connection.prepareStatement("SELECT * FROM weather WHERE city = ?");
        List<String> weather = new ArrayList<>();
        ResultSet rs = null;
        try {
            getWeatherStatement.setString(1, city);
            getWeatherStatement.setString(2, localDate + '%');
            rs = getWeatherStatement.executeQuery();
            while (rs.next()){
                StringBuilder sb = new StringBuilder();

                sb.append(rs.getString("city")).append(" ");
                sb.append(rs.getString("localDate")).append(" ");
                sb.append(rs.getString("weatherText")).append(" ");
                sb.append(rs.getString("temperature"));
                weather.add(sb.toString());
            }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            assert rs != null;
            rs.close();
        }

        return weather;
    }

    public static boolean addWeather(String city, String localDate, String weatherText, Double temperature) throws SQLException {

            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO weather (city, localDate, weatherText, temperature) VALUES (?, ?, ?, ?);");
                ps.setString(1, city);
                ps.setString(2, localDate);
                ps.setString(3, weatherText);
                ps.setDouble(4, temperature);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

    }

}
