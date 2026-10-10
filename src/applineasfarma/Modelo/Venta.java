package applineasfarma.Modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo de TVenta + TDetalleVenta.
 *
 * TVenta real: IdVenta, NumComprobante, Fecha, IdCliente, IdEmpleado,
 *              TipoDoc, FormaPago, NumReceta, SubTotal, Descuento, IGV, Total
 */
public class Venta {

    private int          idVenta;
    private String       numComprobante;
    private int          idCliente;
    private int          idEmpleado;
    private Date         fecha;
    private String       tipoDoc;
    private String       formaPago;
    private String       numReceta;
    private double       subTotalSinIgv;
    private double       descuento;
    private double       igv;
    private double       total;
    private List<DetalleVenta> detalles;

    public Venta() {
        this.detalles = new ArrayList<>();
    }

    public Venta(int idCliente, int idEmpleado, Date fecha,
                 String tipoDoc, String formaPago, double subTotal, double igv, double total) {
        this.idCliente      = idCliente;
        this.idEmpleado     = idEmpleado;
        this.fecha          = fecha;
        this.tipoDoc        = tipoDoc;
        this.formaPago      = formaPago;
        this.subTotalSinIgv = subTotal;
        this.igv            = igv;
        this.total          = total;
        this.detalles       = new ArrayList<>();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int    getIdVenta()                      { return idVenta; }
    public void   setIdVenta(int v)                 { this.idVenta = v; }

    public String getNumComprobante()               { return numComprobante; }
    public void   setNumComprobante(String v)       { this.numComprobante = v; }

    public int    getIdCliente()                    { return idCliente; }
    public void   setIdCliente(int v)               { this.idCliente = v; }

    public int    getIdEmpleado()                   { return idEmpleado; }
    public void   setIdEmpleado(int v)              { this.idEmpleado = v; }

    public Date   getFecha()                        { return fecha; }
    public void   setFecha(Date v)                  { this.fecha = v; }

    public String getTipoDoc()                      { return tipoDoc; }
    public void   setTipoDoc(String v)              { this.tipoDoc = v; }

    public String getFormaPago()                    { return formaPago; }
    public void   setFormaPago(String v)            { this.formaPago = v; }

    public String getNumReceta()                    { return numReceta; }
    public void   setNumReceta(String v)            { this.numReceta = v; }

    public double getSubTotalSinIgv()               { return subTotalSinIgv; }
    public void   setSubTotalSinIgv(double v)       { this.subTotalSinIgv = v; }

    public double getDescuento()                    { return descuento; }
    public void   setDescuento(double v)            { this.descuento = v; }

    public double getIgv()                          { return igv; }
    public void   setIgv(double v)                  { this.igv = v; }

    public double getTotal()                        { return total; }
    public void   setTotal(double v)                { this.total = v; }

    public List<DetalleVenta> getDetalles()         { return detalles; }
    public void setDetalles(List<DetalleVenta> d)   { this.detalles = d; }

    public void agregarDetalle(DetalleVenta d) { this.detalles.add(d); }

    // ═══════════════════════════════════════════════════════════════════════════
    // Clase interna: DetalleVenta
    // ═══════════════════════════════════════════════════════════════════════════

    public static class DetalleVenta {
        private int    idDetalle;
        private int    idVenta;
        private int    idProducto;
        private String nombreProducto;
        private int    cantidad;
        private double precioUnit;
        private double subTotal;

        public DetalleVenta() { }

        public DetalleVenta(int idProducto, String nombreProducto,
                            int cantidad, double precioUnit) {
            this.idProducto     = idProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad       = cantidad;
            this.precioUnit     = precioUnit;
            this.subTotal       = cantidad * precioUnit;
        }

        public int    getIdDetalle()                 { return idDetalle; }
        public void   setIdDetalle(int v)            { this.idDetalle = v; }
        public int    getIdVenta()                   { return idVenta; }
        public void   setIdVenta(int v)              { this.idVenta = v; }
        public int    getIdProducto()                { return idProducto; }
        public void   setIdProducto(int v)           { this.idProducto = v; }
        public String getNombreProducto()            { return nombreProducto; }
        public void   setNombreProducto(String v)    { this.nombreProducto = v; }
        public int    getCantidad()                  { return cantidad; }
        public void   setCantidad(int v)             { this.cantidad = v; this.subTotal = v * precioUnit; }
        public double getPrecioUnit()                { return precioUnit; }
        public void   setPrecioUnit(double v)        { this.precioUnit = v; this.subTotal = cantidad * v; }
        public double getSubTotal()                  { return subTotal; }
        public void   setSubTotal(double v)          { this.subTotal = v; }
    }
}
