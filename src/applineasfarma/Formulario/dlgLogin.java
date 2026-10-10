package applineasfarma.Formulario;

import applineasfarma.DAO.EmpleadoDAO;
import applineasfarma.Modelo.Empleado;

import javax.swing.JOptionPane;
import java.sql.SQLException;

/**
 * Formulario de inicio de sesión.
 * Reemplaza la validación hardcodeada por consulta real a TEmpleado via EmpleadoDAO.
 * El empleado autenticado se guarda en Sesion.empleadoActual para uso en otros formularios.
 */
public class dlgLogin extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(dlgLogin.class.getName());

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // ── Constructor ────────────────────────────────────────────────────────────

    public dlgLogin(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        // Centrar en pantalla
        setLocationRelativeTo(null);
    }

    // ── Lógica de autenticación ────────────────────────────────────────────────

    private void autenticar() {
        String usuario = txtUsuario.getText().trim();
        String clave   = new String(txtContrasena.getPassword()).trim();

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingresa usuario y contraseña.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Empleado emp = empleadoDAO.autenticar(usuario, clave);

            if (emp != null) {
                // Guardar empleado en sesión global
                Sesion.empleadoActual = emp;
                JOptionPane.showMessageDialog(this,
                    "¡Bienvenido, " + emp.getNombre() + "!\nCargo: " + emp.getCargo());
                this.dispose(); // cerrar login y continuar al dashboard

            } else {
                JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos.", "Error de acceso",
                    JOptionPane.ERROR_MESSAGE);
                txtContrasena.setText("");
                txtContrasena.requestFocus();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al conectar con la base de datos:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CÓDIGO GENERADO POR NETBEANS
    // ═══════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlContenedor = new javax.swing.JPanel();
        lblLogin = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        lblContrasena = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        btnOlvidecontrasena = new javax.swing.JLabel();
        btnIngresar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("INICIAR SESSION");

        pnlContenedor.setBackground(new java.awt.Color(255, 255, 255));

        lblLogin.setFont(new java.awt.Font("Yu Gothic UI", 1, 36)); // NOI18N
        lblLogin.setText("login Lineas Farma");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/seguridad.png"))); // NOI18N

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUsuario.setText("Usuario");

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/user-24.png"))); // NOI18N

        txtUsuario.setToolTipText("");
        txtUsuario.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));

        lblContrasena.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblContrasena.setText("Contraseña");

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/padlock-24.png"))); // NOI18N

        txtContrasena.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtContrasena.addActionListener(this::txtContrasenaActionPerformed);

        btnOlvidecontrasena.setForeground(new java.awt.Color(0, 0, 255));
        btnOlvidecontrasena.setText("¿Olvide mi contraseña?");

        btnIngresar.setBackground(new java.awt.Color(0, 153, 153));
        btnIngresar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnIngresar.setForeground(new java.awt.Color(255, 255, 255));
        btnIngresar.setText("   Ingresar   ");
        btnIngresar.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(0, 102, 102)));
        btnIngresar.setFocusPainted(false);
        btnIngresar.addActionListener(this::btnIngresarActionPerformed);

        javax.swing.GroupLayout pnlContenedorLayout = new javax.swing.GroupLayout(pnlContenedor);
        pnlContenedor.setLayout(pnlContenedorLayout);
        pnlContenedorLayout.setHorizontalGroup(
            pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenedorLayout.createSequentialGroup()
                .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContenedorLayout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addComponent(lblContrasena))
                    .addGroup(pnlContenedorLayout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlContenedorLayout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addComponent(jLabel4)
                                    .addGap(30, 30, 30)
                                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(lblUsuario))
                            .addGroup(pnlContenedorLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblLogin)
                                    .addComponent(btnOlvidecontrasena)
                                    .addGroup(pnlContenedorLayout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(24, 24, 24)
                                        .addComponent(txtContrasena, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(pnlContenedorLayout.createSequentialGroup()
                        .addGap(184, 184, 184)
                        .addComponent(jLabel2))
                    .addGroup(pnlContenedorLayout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(btnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(103, Short.MAX_VALUE))
        );
        pnlContenedorLayout.setVerticalGroup(
            pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenedorLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(lblLogin)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(67, 67, 67)
                .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlContenedorLayout.createSequentialGroup()
                        .addComponent(lblUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(lblContrasena)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtContrasena, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnOlvidecontrasena)
                .addGap(45, 45, 45)
                .addComponent(btnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContenedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlContenedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // ── Handler botón Ingresar ─────────────────────────────────────────────────

    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIngresarActionPerformed
        autenticar();
    }//GEN-LAST:event_btnIngresarActionPerformed

    // Enter en el campo de contraseña también inicia sesión
    private void txtContrasenaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtContrasenaActionPerformed
        autenticar();
    }//GEN-LAST:event_txtContrasenaActionPerformed

    // ── main ──────────────────────────────────────────────────────────────────

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            dlgLogin dialog = new dlgLogin(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIngresar;
    private javax.swing.JLabel btnOlvidecontrasena;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblContrasena;
    private javax.swing.JLabel lblLogin;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel pnlContenedor;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
