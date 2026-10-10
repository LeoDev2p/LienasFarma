package applineasfarma.DAO;

import applineasfarma.Conexion.Conexion;
import applineasfarma.Modelo.Empleado;

import java.sql.*;

/**
 * DAO para la tabla TEmpleado.
 * Columnas reales: IdEmpleado, DNI, Nombres, Apellidos, Cargo, Usuario, Contrasena
 */
public class EmpleadoDAO {

    // ── Autenticar ─────────────────────────────────────────────────────────────

    public Empleado autenticar(String usuario, String contrasena) throws SQLException {
        // LCASE() hace la comparación case-insensitive en UCanAccess/Access
        String sql = "SELECT * FROM TEmpleado WHERE LCASE(Usuario) = LCASE(?) AND Contrasena = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── Buscar por ID ──────────────────────────────────────────────────────────

    public Empleado buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM TEmpleado WHERE IdEmpleado = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("IdEmpleado"));
        e.setNombre(rs.getString("Nombres") + " " + rs.getString("Apellidos"));
        e.setUsuario(rs.getString("Usuario"));
        e.setClave(rs.getString("Contrasena"));
        e.setCargo(rs.getString("Cargo"));
        return e;
    }
}
