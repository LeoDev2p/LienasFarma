/*
Evaluación Final - Curs POO
- Isaias Cesar Quintana Errazabal
- Jorge Alexander Rodriguez Jara
- Brayan Jahckson Quispe Rojas
- Jhonatan Riveros Marca
*/

package applineasfarma;

import applineasfarma.Conexion.Conexion;
import applineasfarma.Formulario.Sesion;
import applineasfarma.Formulario.dlgLogin;
import applineasfarma.Formulario.frmDashboard;

public class AppLineasFarma {

    /**
     * Punto de entrada de la aplicación.
     *
     * Flujo:
     *  1. Se muestra dlgLogin (modal).
     *  2. Si el login fue exitoso, Sesion.empleadoActual != null → abrir frmDashboard.
     *  3. Si el usuario cierra el login sin autenticarse → la app termina.
     *  4. Al cerrar el dashboard se cierra la conexión a la BD.
     */
    public static void main(String[] args) {

        // Aplicar Look & Feel Nimbus si está disponible
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info :
                    javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            // Continuar con el Look & Feel por defecto
        }

        javax.swing.SwingUtilities.invokeLater(() -> {

            // ── 1. Mostrar Login ──────────────────────────────────────────────
            dlgLogin login = new dlgLogin(null, true);
            login.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            login.setVisible(true); // bloquea hasta que se cierre (es modal)

            // ── 2. Verificar autenticación ────────────────────────────────────
            if (Sesion.empleadoActual == null) {
                // El usuario cerró el login sin autenticarse → salir
                System.out.println("[App] Login cancelado. Saliendo.");
                System.exit(0);
                return;
            }

            System.out.println("[App] Sesión iniciada como: "
                    + Sesion.empleadoActual.getNombre()
                    + " (" + Sesion.empleadoActual.getCargo() + ")");

            // ── 3. Abrir Dashboard ────────────────────────────────────────────
            frmDashboard dashboard = new frmDashboard(null, true);

            // Al cerrar el dashboard → cerrar conexión BD y salir
            dashboard.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    Conexion.cerrarConexion();
                    System.out.println("[App] Aplicación cerrada.");
                    System.exit(0);
                }
            });

            dashboard.setVisible(true);
        });
    }
}
