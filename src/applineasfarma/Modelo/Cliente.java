package applineasfarma.Modelo;

/**
 * Modelo de TCliente.
 * Campos reales: IdCliente, DniRuc, Nombres, Apellidos, Direccion, Telefono
 */
public class Cliente {

    private int    idCliente;
    private String dni;        // DniRuc
    private String nombre;     // Nombres
    private String apellidos;
    private String direccion;
    private String telefono;

    public Cliente() { }

    public Cliente(int idCliente, String nombre, String dni,
                   String apellidos, String direccion, String telefono) {
        this.idCliente = idCliente;
        this.nombre    = nombre;
        this.dni       = dni;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.telefono  = telefono;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int    getIdCliente()              { return idCliente; }
    public void   setIdCliente(int v)         { this.idCliente = v; }

    public String getDni()                    { return dni; }
    public void   setDni(String v)            { this.dni = v; }

    public String getNombre()                 { return nombre; }
    public void   setNombre(String v)         { this.nombre = v; }

    public String getApellidos()              { return apellidos; }
    public void   setApellidos(String v)      { this.apellidos = v; }

    public String getDireccion()              { return direccion; }
    public void   setDireccion(String v)      { this.direccion = v; }

    public String getTelefono()               { return telefono; }
    public void   setTelefono(String v)       { this.telefono = v; }

    /** Nombre completo para mostrar en formularios. */
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " "
             + (apellidos != null ? apellidos : "");
    }

    /** Alias para compatibilidad con código previo que usaba tipoDoc. */
    public String getTipoDoc()                { return "DNI"; }
    public void   setTipoDoc(String v)        { }

    @Override
    public String toString() {
        return String.format("Cliente{id=%d, dni='%s', nombre='%s %s'}",
                idCliente, dni, nombre, apellidos);
    }
}
