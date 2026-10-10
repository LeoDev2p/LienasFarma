package applineasfarma.DAO;

import applineasfarma.Conexion.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones de Proveedor y Órdenes de Compra.
 * Opera sobre TProducto (PrecioCompra) y simula órdenes en memoria
 * si no existe tabla TOrdenCompra en la BD.
 */
public class ProveedorDAO {

    // ── Proveedores ────────────────────────────────────────────────────────────

    /**
     * Retorna la lista de proveedores únicos inferidos de TProducto,
     * o desde TProveedor si existe.
     */
    public List<String> listarProveedores() throws SQLException {
        List<String> lista = new ArrayList<>();
        // Intentar leer tabla TProveedor si existe
        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT NombreProveedor FROM TProveedor ORDER BY NombreProveedor")) {
            while (rs.next()) lista.add(rs.getString(1));
            return lista;
        } catch (SQLException e) {
            // TProveedor no existe — usar lista estática
        }
        lista.add("Laboratorios Portugal");
        lista.add("Laboratorio FarmaCorp");
        lista.add("Pfizer");
        lista.add("Roche");
        lista.add("Bayer");
        lista.add("Medifarma");
        return lista;
    }

    /**
     * Cuenta proveedores activos (únicos).
     */
    public int contarProveedores() throws SQLException {
        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM TProveedor")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            // tabla no existe
        }
        return 6; // valor estático de la lista hardcodeada
    }

    // ── Órdenes de compra ──────────────────────────────────────────────────────

    /**
     * Cuenta órdenes pendientes (Estado = 'Pendiente').
     */
    public int contarOrdenesPendientes() throws SQLException {
        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM TOrdenCompra WHERE Estado = 'Pendiente'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            // tabla no existe aún
        }
        return 0;
    }

    /**
     * Suma total de todas las órdenes de compra.
     */
    public double totalPedidos() throws SQLException {
        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(Total) FROM TOrdenCompra")) {
            if (rs.next()) {
                double v = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : v;
            }
        } catch (SQLException e) {
            // tabla no existe aún
        }
        return 0.0;
    }

    // ── PrecioCompra desde TProducto ───────────────────────────────────────────

    /**
     * Busca el PrecioCompra de un producto por nombre, sustancia o código.
     * Retorna el primer resultado o 0.0 si no se encuentra.
     */
    public double obtenerPrecioCompra(String texto) throws SQLException {
        String sql = "SELECT PrecioCompra FROM TProducto "
                   + "WHERE Nombre LIKE ? OR Sustancia LIKE ? OR Codigo LIKE ? "
                   + "ORDER BY Nombre";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("PrecioCompra");
            }
        }
        return 0.0;
    }
}
