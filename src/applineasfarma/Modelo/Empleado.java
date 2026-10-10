package applineasfarma.Modelo;

/**
 * Modelo que representa un registro de la tabla TEmpleado.
 * Se usa principalmente para la autenticación en dlgLogin.
 *
 * Columnas esperadas en Access:
 *   idEmpleado  (Autonumérico / Long)
 *   nombre      (Texto)
 *   usuario     (Texto)
 *   clave       (Texto)
 *   cargo       (Texto)
 */
public class Empleado {

    private int    idEmpleado;
    private String nombre;
    private String usuario;
    private String clave;
    private String cargo;

    // ── Constructores ──────────────────────────────────────────────────────────

    public Empleado() { }

    public Empleado(int idEmpleado, String nombre, String usuario,
                    String clave, String cargo) {
        this.idEmpleado = idEmpleado;
        this.nombre     = nombre;
        this.usuario    = usuario;
        this.clave      = clave;
        this.cargo      = cargo;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public int getIdEmpleado()              { return idEmpleado; }
    public void setIdEmpleado(int id)       { this.idEmpleado = id; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public String getUsuario()              { return usuario; }
    public void setUsuario(String usuario)  { this.usuario = usuario; }

    public String getClave()                { return clave; }
    public void setClave(String clave)      { this.clave = clave; }

    public String getCargo()                { return cargo; }
    public void setCargo(String cargo)      { this.cargo = cargo; }

    @Override
    public String toString() {
        return String.format("Empleado{id=%d, nombre='%s', usuario='%s', cargo='%s'}",
                idEmpleado, nombre, usuario, cargo);
    }
}
