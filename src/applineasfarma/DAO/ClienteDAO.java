package applineasfarma.DAO;

import applineasfarma.Conexion.Conexion;
import applineasfarma.Modelo.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla TCliente.
 * Columnas reales: IdCliente, DniRuc, Nombres, Apellidos, Direccion, Telefono
 */
public class ClienteDAO {

    // ── Buscar por DNI/RUC ─────────────────────────────────────────────────────

    public Cliente buscarPorDni(String dniRuc) throws SQLException {
        String sql = "SELECT * FROM TCliente WHERE DniRuc = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, dniRuc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── Listar todos ───────────────────────────────────────────────────────────

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM TCliente ORDER BY Nombres";

        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── Buscar por ID ──────────────────────────────────────────────────────────

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM TCliente WHERE IdCliente = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── Insertar ───────────────────────────────────────────────────────────────

    public int insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO TCliente (DniRuc, Nombres, Apellidos, Direccion, Telefono) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            // Access rechaza strings vacíos ("") en campos Texto — usar " " como mínimo
            ps.setString(3, nvl(c.getApellidos()));
            ps.setString(4, nvl(c.getDireccion()));
            ps.setString(5, nvl(c.getTelefono()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /** Devuelve el valor si no está vacío, o un espacio en blanco si lo está.
     *  UCanAccess/Access no acepta la cadena vacía "" en columnas Texto. */
    private String nvl(String s) {
        return (s == null || s.isBlank()) ? " " : s;
    }

    // ── Actualizar ─────────────────────────────────────────────────────────────

    public boolean actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE TCliente SET DniRuc=?, Nombres=?, Apellidos=?, Direccion=?, Telefono=? "
                   + "WHERE IdCliente=?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, nvl(c.getApellidos()));
            ps.setString(4, nvl(c.getDireccion()));
            ps.setString(5, nvl(c.getTelefono()));
            ps.setInt   (6, c.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt   ("IdCliente"));
        c.setDni      (rs.getString("DniRuc"));
        c.setNombre   (rs.getString("Nombres"));
        c.setApellidos(rs.getString("Apellidos"));
        c.setDireccion(rs.getString("Direccion"));
        c.setTelefono (rs.getString("Telefono"));
        return c;
    }
}
