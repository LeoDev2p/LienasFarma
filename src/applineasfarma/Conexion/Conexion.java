package applineasfarma.Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase Singleton para gestionar la conexión a la base de datos
 * BDLineasFarma.accdb usando UCanAccess (JDBC para Microsoft Access).
 *
 * La ruta se resuelve de forma relativa al directorio de trabajo del proyecto,
 * por lo que el archivo .accdb debe estar en la raíz del proyecto.
 */
public class Conexion {

    // Ruta relativa al archivo Access (raíz del proyecto)
    private static final String DB_PATH = "BDLineasFarma.accdb";

    // Cadena de conexión UCanAccess
    // keepMirror=false evita crear archivos temporales adicionales
    private static final String URL =
            "jdbc:ucanaccess://" + DB_PATH + ";keepMirror=false;immediatelyReleaseResources=true";

    // Instancia única (Singleton)
    private static Connection conexion = null;

    /** Constructor privado — no se instancia directamente. */
    private Conexion() { }

    /**
     * Establecemos la conexión y captura de excepxiones
     */
    public static Connection getConexion() throws SQLException {
        try {
            if (conexion == null || conexion.isClosed()) {
                // Cargar el driver de UCanAccess
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                conexion = DriverManager.getConnection(URL);
                System.out.println("[Conexion] Conexión establecida con " + DB_PATH);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver UCanAccess no encontrado. Verifica que los JARs estén en lib/.", e);
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                conexion = null;
                System.out.println("[Conexion] Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("[Conexion] Error al cerrar: " + e.getMessage());
        }
    }
}
