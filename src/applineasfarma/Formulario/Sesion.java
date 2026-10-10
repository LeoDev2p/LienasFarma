package applineasfarma.Formulario;

import applineasfarma.Modelo.Empleado;

/**
 * Clase de sesión global.
 * Almacena el empleado autenticado tras el login exitoso,
 * disponible para todos los formularios durante la ejecución.
 */
public class Sesion {

    /** Empleado que inició sesión. null = no autenticado. */
    public static Empleado empleadoActual = null;

    private Sesion() { } // no instanciar
}
