package applineasfarma.Modelo;

import java.util.Date;

/**
 * Modelo de TProducto.
 * Campos reales: IdProducto, Codigo, Nombre, Sustancia, Categoria,
 *                Lote, FechaVencimiento, Stock, StockMinimo,
 *                PrecioCompra, PrecioVenta, RequiereReceta
 */
public class Producto {

    private int     idProducto;
    private String  codigo;
    private String  nombre;
    private String  sustancia;
    private String  categoria;
    private String  lote;
    private Date    fechaVencimiento;
    private int     stock;
    private int     stockMini;
    private double  precioCompra;
    private double  precioVenta;
    private boolean requiereReceta;

    public Producto() { }

    public Producto(int idProducto, String codigo, String nombre, String sustancia,
                    String categoria, String lote, Date fechaVencimiento,
                    int stock, int stockMini, double precioCompra,
                    double precioVenta, boolean requiereReceta) {
        this.idProducto      = idProducto;
        this.codigo          = codigo;
        this.nombre          = nombre;
        this.sustancia       = sustancia;
        this.categoria       = categoria;
        this.lote            = lote;
        this.fechaVencimiento = fechaVencimiento;
        this.stock           = stock;
        this.stockMini       = stockMini;
        this.precioCompra    = precioCompra;
        this.precioVenta     = precioVenta;
        this.requiereReceta  = requiereReceta;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int     getIdProducto()                   { return idProducto; }
    public void    setIdProducto(int v)              { this.idProducto = v; }

    public String  getCodigo()                       { return codigo; }
    public void    setCodigo(String v)               { this.codigo = v; }

    public String  getNombre()                       { return nombre; }
    public void    setNombre(String v)               { this.nombre = v; }

    public String  getSustancia()                    { return sustancia; }
    public void    setSustancia(String v)            { this.sustancia = v; }

    public String  getCategoria()                    { return categoria; }
    public void    setCategoria(String v)            { this.categoria = v; }

    public String  getLote()                         { return lote; }
    public void    setLote(String v)                 { this.lote = v; }

    public Date    getFechaVencimiento()             { return fechaVencimiento; }
    public void    setFechaVencimiento(Date v)       { this.fechaVencimiento = v; }

    public int     getStock()                        { return stock; }
    public void    setStock(int v)                   { this.stock = v; }

    public int     getStockMini()                    { return stockMini; }
    public void    setStockMini(int v)               { this.stockMini = v; }

    public double  getPrecioCompra()                 { return precioCompra; }
    public void    setPrecioCompra(double v)         { this.precioCompra = v; }

    public double  getPrecioVenta()                  { return precioVenta; }
    public void    setPrecioVenta(double v)          { this.precioVenta = v; }

    public boolean isRequiereReceta()                { return requiereReceta; }
    public void    setRequiereReceta(boolean v)      { this.requiereReceta = v; }

    @Override
    public String toString() {
        return String.format("Producto{id=%d, codigo='%s', nombre='%s', stock=%d, precio=%.2f}",
                idProducto, codigo, nombre, stock, precioVenta);
    }
}
