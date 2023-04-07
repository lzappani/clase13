import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class Main {
    static final String DRIVER = "org.sqlite.JDBC";
    static final String DIR = "db/";
    static final String DBNAME = "mydb.db";
    static final String URL = "jdbc:sqlite:" + DIR + DBNAME;
    static final String PATH = DIR + DBNAME;


    public static void createNewDatabase() throws IOException {
        Path dir = Paths.get(DIR);
        Path path = Paths.get(PATH);

        if (!Files.exists(dir)) Files.createDirectories(dir);
        if (Files.exists(path)) Files.delete(path);

        try (Connection conn = getConnection()) {
            if (conn != null) {
                ArrayList<String> commands = new ArrayList<>();

                // Crea las tablas
                commands.add("""
                    CREATE TABLE IF NOT EXISTS Departamentos (
                        id_departamento INTEGER NOT NULL PRIMARY KEY,
                        nombre TEXT NOT NULL,
                        presupuesto REAL NOT NULL
                    );""");

                commands.add("""
                    CREATE TABLE IF NOT EXISTS Empleados (
                        id_empleado INTEGER NOT NULL PRIMARY KEY,
                        dni TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        apellido TEXT NOT NULL,
                        nacionalidad TEXT NOT NULL,
                        id_departamento INTEGER NULL REFERENCES Departamentos(id_departamento)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE
                    );""");

                for (String command : commands) {
                    Statement stmt = conn.createStatement();
                    stmt.execute(command);
                }

                // Inserta 3 departamentos
                String insert = """
                    INSERT INTO Departamentos (nombre, presupuesto)
                    VALUES (?, ?);""";
                PreparedStatement insertPS = conn.prepareStatement(insert);

                insertPS.setString(1, "Logistica");
                insertPS.setDouble(2, 1000000);
                insertPS.execute();

                insertPS.setString(1, "Sistemas");
                insertPS.setDouble(2, 2000000);
                insertPS.execute();

                insertPS.setString(1, "Compras");
                insertPS.setDouble(2, 3000000);
                insertPS.execute();

                // Inserta 3 empleados
                insert = """
                    INSERT INTO Empleados (dni, nombre, apellido, nacionalidad, id_departamento)
                    VALUES (?, ?, ?, ?, ?);""";

                insertPS = conn.prepareStatement(insert);

                insertPS.setInt(1, 33333333);
                insertPS.setString(2, "Luis");
                insertPS.setString(3, "Perez");
                insertPS.setString(4, "Argentina");
                insertPS.setInt(5, 1);
                insertPS.execute();

                insertPS.setInt(1, 11111111);
                insertPS.setString(2, "Carlos");
                insertPS.setString(3, "Gutierrez");
                insertPS.setString(4, "Uruguaya");
                insertPS.setInt(5, 2);
                insertPS.execute();

                insertPS.setInt(1, 22222222);
                insertPS.setString(2, "Santiago");
                insertPS.setString(3, "Rodriguez");
                insertPS.setString(4, "Argentina");
                insertPS.setInt(5, 3);
                insertPS.execute();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws IOException {

        // Crea la base de datos
        createNewDatabase();

        try (Connection conn = getConnection()){

            Statement stmt = conn.createStatement();

            ArrayList<String> commands = new ArrayList<>();
            // Modifica la nacionalidad del empleado 1
            commands.add("""
                    UPDATE Empleados
                    SET nacionalidad = 'Brasileña'
                    WHERE id_empleado = 1;""");

            // Elimina el departamento Compras
            commands.add("""
                    DELETE FROM Departamentos
                    WHERE id_departamento = 3;""");

            for (String command : commands) {
                stmt.execute(command);
            }

            commands.clear();

            // Devuelve los empleados del departamento Logistica
            commands.add("""
                    SELECT *
                    FROM Empleados
                    WHERE id_departamento = (
                        SELECT id_departamento
                        FROM Departamentos
                        WHERE nombre = 'Logistica');""");

            // Devuelve todos los departamentos
            commands.add("""
                    SELECT *
                    FROM  Departamentos;""");

            commands.add("""
                    SELECT *
                    FROM  Empleados;""");

            System.out.println();
            for (String command : commands) {
                ResultSet rs = stmt.executeQuery(command);
                printTable(rs);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    // Este método imprime el resultado completo de una query
    public static void printTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        System.out.println("Tabla: " + metadata.getTableName(1));
        int columnCount = metadata.getColumnCount();
        for (int j = 1; j <= columnCount; j++) {
            System.out.printf("%-20s", metadata.getColumnName(j));
        }
        System.out.println();
        while (rs.next()) {
            for (int k = 1; k <= columnCount; k++) {
                System.out.printf("%-20s", rs.getString(k));
            }
            System.out.println();
        }
        System.out.println();
    }

    // Este método estático maneja la conexión a la base de datos
    public static Connection getConnection() throws ClassNotFoundException {
        Class.forName(DRIVER);
        Connection connection = null;
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);  // Configuración para usar claves foráneas con SQLite
            connection = DriverManager.getConnection(URL, config.toProperties());
        } catch (SQLException ignored) {}
        return connection;
    }

}