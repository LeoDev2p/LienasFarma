package applineasfarma.DAO;

import applineasfarma.Conexion.Conexion;
import applineasfarma.Modelo.Venta;
import applineasfarma.Modelo.Venta.DetalleVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para TVenta y TDetalleVenta.
 *
 * TVenta real:       IdVenta, NumComprobante, Fecha, IdCliente, IdEmpleado,
 *                    TipoDoc, FormaPago, NumReceta, SubTotal, Descuento, IGV, Total
 * TDetalleVenta real: IdDetalle, IdVenta, IdProducto, Cantidad, PrecioUnitario, SubTotal
 */
public class VentaDAO {

    // ── Registrar venta completa (transacción) ─────────────────────────────────

    public int registrarVenta(Venta venta) throws SQLException {
        Connection con = Conexion.getConexion();
        boolean autoOrig = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            int idVenta = insertarCabecera(con, venta);
            if (idVenta == -1) throw new SQLException("No se pudo insertar la cabecera de venta.");

            for (DetalleVenta d : venta.getDetalles()) {
                d.setIdVenta(idVenta);
                insertarDetalle(con, d);
            }

            // Decrementar stock
            ProductoDAO pDao = new ProductoDAO();
            for (DetalleVenta d : venta.getDetalles()) {
                if (!pDao.decrementarStock(d.getIdProducto(), d.getCantidad())) {
                    throw new SQLException("Stock insuficiente para producto id=" + d.getIdProducto());
                }
            }

            con.commit();
            return idVenta;

        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoOrig);
        }
    }

    // ── Listar ventas ──────────────────────────────────────────────────────────

    public List<Venta> listarVentas() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM TVenta ORDER BY Fecha DESC";

        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapearCabecera(rs));
        }
        return lista;
    }

    // ── Listar detalles ────────────────────────────────────────────────────────

    public List<DetalleVenta> listarDetalles(int idVenta) throws SQLException {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT d.*, p.Nombre AS NombreProducto "
                   + "FROM TDetalleVenta d "
                   + "INNER JOIN TProducto p ON d.IdProducto = p.IdProducto "
                   + "WHERE d.IdVenta = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearDetalle(rs));
            }
        }
        return lista;
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    private int insertarCabecera(Connection con, Venta v) throws SQLException {
        // Generar NumComprobante automático: B001-XXXXXX
        String numComp = "B001-" + String.format("%06d", System.currentTimeMillis() % 1000000);

        String sql = "INSERT INTO TVenta (NumComprobante, Fecha, IdCliente, IdEmpleado, "
                   + "TipoDoc, FormaPago, NumReceta, SubTotal, Descuento, IGV, Total) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString   (1,  numComp);
            ps.setTimestamp(2,  new java.sql.Timestamp(
                    v.getFecha() != null ? v.getFecha().getTime() : System.currentTimeMillis()));
            ps.setInt      (3,  v.getIdCliente());
            ps.setInt      (4,  v.getIdEmpleado());
            ps.setString   (5,  v.getTipoDoc()   != null ? v.getTipoDoc()   : "Boleta");
            ps.setString   (6,  v.getFormaPago() != null ? v.getFormaPago() : "Efectivo");
            ps.setString   (7,  v.getNumReceta() != null ? v.getNumReceta() : "");
            ps.setDouble   (8,  v.getSubTotalSinIgv());
            ps.setDouble   (9,  0.0);  // Descuento
            ps.setDouble   (10, v.getIgv());
            ps.setDouble   (11, v.getTotal());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    private void insertarDetalle(Connection con, DetalleVenta d) throws SQLException {
        String sql = "INSERT INTO TDetalleVenta (IdVenta, IdProducto, Cantidad, PrecioUnitario, SubTotal) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt   (1, d.getIdVenta());
            ps.setInt   (2, d.getIdProducto());
            ps.setInt   (3, d.getCantidad());
            ps.setDouble(4, d.getPrecioUnit());
            ps.setDouble(5, d.getSubTotal());
            ps.executeUpdate();
        }
    }

    private Venta mapearCabecera(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta   (rs.getInt      ("IdVenta"));
        v.setIdCliente (rs.getInt      ("IdCliente"));
        v.setIdEmpleado(rs.getInt      ("IdEmpleado"));
        v.setFecha     (rs.getTimestamp("Fecha"));
        v.setFormaPago (rs.getString   ("FormaPago"));
        v.setTipoDoc   (rs.getString   ("TipoDoc"));
        v.setTotal     (rs.getDouble   ("Total"));
        return v;
    }

    private DetalleVenta mapearDetalle(ResultSet rs) throws SQLException {
        DetalleVenta d = new DetalleVenta();
        d.setIdDetalle     (rs.getInt   ("IdDetalle"));
        d.setIdVenta       (rs.getInt   ("IdVenta"));
        d.setIdProducto    (rs.getInt   ("IdProducto"));
        d.setNombreProducto(rs.getString("NombreProducto"));
        d.setCantidad      (rs.getInt   ("Cantidad"));
        d.setPrecioUnit    (rs.getDouble("PrecioUnitario"));
        d.setSubTotal      (rs.getDouble("SubTotal"));
        return d;
    }
}
