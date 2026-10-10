package applineasfarma.DAO;

import applineasfarma.Conexion.Conexion;
import applineasfarma.Modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla TProducto.
 * Columnas reales: IdProducto, Codigo, Nombre, Sustancia, Categoria,
 *                  Lote, FechaVencimiento, Stock, StockMinimo,
 *                  PrecioCompra, PrecioVenta, RequiereReceta
 */
public class ProductoDAO {

    private static final String SELECT_BASE =
        "SELECT IdProducto, Codigo, Nombre, Sustancia, Categoria, Lote, " +
        "FechaVencimiento, Stock, StockMinimo, PrecioCompra, PrecioVenta, RequiereReceta " +
        "FROM TProducto";

    // ── Listar todos ───────────────────────────────────────────────────────────

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY Nombre";

        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── Buscar por nombre, sustancia o código ──────────────────────────────────

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE Nombre LIKE ? OR Sustancia LIKE ? OR Codigo LIKE ? ORDER BY Nombre";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ── Buscar por ID ──────────────────────────────────────────────────────────

    public Producto buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE IdProducto = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── Insertar ───────────────────────────────────────────────────────────────

    public int insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO TProducto (Codigo, Nombre, Sustancia, Categoria, Lote, " +
                     "FechaVencimiento, Stock, StockMinimo, PrecioCompra, PrecioVenta, RequiereReceta) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString (1,  p.getCodigo());
            ps.setString (2,  p.getNombre());
            ps.setString (3,  p.getSustancia());
            ps.setString (4,  p.getCategoria());
            ps.setString (5,  p.getLote());
            ps.setDate   (6,  p.getFechaVencimiento() != null
                    ? new java.sql.Date(p.getFechaVencimiento().getTime()) : null);
            ps.setInt    (7,  p.getStock());
            ps.setInt    (8,  p.getStockMini());
            ps.setDouble (9,  p.getPrecioCompra());
            ps.setDouble (10, p.getPrecioVenta());
            ps.setBoolean(11, p.isRequiereReceta());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    // ── Actualizar ─────────────────────────────────────────────────────────────

    public boolean actualizar(Producto p) throws SQLException {
        String sql = "UPDATE TProducto SET Codigo=?, Nombre=?, Sustancia=?, Categoria=?, " +
                     "Lote=?, FechaVencimiento=?, Stock=?, StockMinimo=?, " +
                     "PrecioCompra=?, PrecioVenta=?, RequiereReceta=? WHERE IdProducto=?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString (1,  p.getCodigo());
            ps.setString (2,  p.getNombre());
            ps.setString (3,  p.getSustancia());
            ps.setString (4,  p.getCategoria());
            ps.setString (5,  p.getLote());
            ps.setDate   (6,  p.getFechaVencimiento() != null
                    ? new java.sql.Date(p.getFechaVencimiento().getTime()) : null);
            ps.setInt    (7,  p.getStock());
            ps.setInt    (8,  p.getStockMini());
            ps.setDouble (9,  p.getPrecioCompra());
            ps.setDouble (10, p.getPrecioVenta());
            ps.setBoolean(11, p.isRequiereReceta());
            ps.setInt    (12, p.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Eliminar ───────────────────────────────────────────────────────────────

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM TProducto WHERE IdProducto = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Decrementar stock (al vender) ──────────────────────────────────────────

    public boolean decrementarStock(int idProducto, int cantidad) throws SQLException {
        String sql = "UPDATE TProducto SET Stock = Stock - ? WHERE IdProducto = ? AND Stock >= ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto      (rs.getInt    ("IdProducto"));
        p.setCodigo          (rs.getString ("Codigo"));
        p.setNombre          (rs.getString ("Nombre"));
        p.setSustancia       (rs.getString ("Sustancia"));
        p.setCategoria       (rs.getString ("Categoria"));
        p.setLote            (rs.getString ("Lote"));
        p.setFechaVencimiento(rs.getDate   ("FechaVencimiento"));
        p.setStock           (rs.getInt    ("Stock"));
        p.setStockMini       (rs.getInt    ("StockMinimo"));
        p.setPrecioCompra    (rs.getDouble ("PrecioCompra"));
        p.setPrecioVenta     (rs.getDouble ("PrecioVenta"));
        p.setRequiereReceta  (rs.getBoolean("RequiereReceta"));
        return p;
    }
}
