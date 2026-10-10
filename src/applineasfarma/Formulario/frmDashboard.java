package applineasfarma.Formulario;

import applineasfarma.Conexion.Conexion;

import javax.swing.JOptionPane;

/**
 * Dashboard principal de Lineas Farma.
 *
 * Botones y menú conectados:
 *   btnVentas      → dlgVentas
 *   btnInventario  → dlgInventario
 *   btnProveedor   → dlgProveedor
 *   btnAyuda       → diálogo "Acerca de"
 *
 * Menú:
 *   Archivo  → Cerrar sesión | Salir
 *   Inventario → Gestionar producto | Alerta de Stock
 *   Proveedores → Directorio de proveedores
 *   Ventas → Nueva venta | Historial ventas (informativo por ahora)
 *   Ayuda → Acerca de Lineas Farma
 */
public class frmDashboard extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(frmDashboard.class.getName());

    // ── Constructor ────────────────────────────────────────────────────────────

    public frmDashboard(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarVentana();
    }

    // ── Configuración post-init ────────────────────────────────────────────────

    private void configurarVentana() {
        setLocationRelativeTo(null);
        configurarMenu();

        // Mostrar saludo en el título con el empleado autenticado
        if (Sesion.empleadoActual != null) {
            setTitle("DASHBOARD LINEAS FARMA  —  " + Sesion.empleadoActual.getNombre()
                    + "  (" + Sesion.empleadoActual.getCargo() + ")");
        }
    }

    // ── Menú ───────────────────────────────────────────────────────────────────

    private void configurarMenu() {
        jMenuItem1.addActionListener(e -> cerrarSesion());          // Cerrar sesión
        jMenuItem2.addActionListener(e -> salir());                 // Salir
        jMenuItem3.addActionListener(e -> abrirInventario());       // Gestionar producto
        jMenuItem4.addActionListener(e -> mostrarAlertaStock());    // Alerta de Stock
        jMenuItem5.addActionListener(e -> abrirProveedor());        // Directorio de proveedores
        jMenuItem6.addActionListener(e -> abrirVentas());           // Nueva venta
        jMenuItem7.addActionListener(e -> proximamente("Historial de ventas")); // sin función aún
        jMenuItem8.addActionListener(e -> mostrarAcercaDe());       // Acerca de
    }

    private void proximamente(String modulo) {
        JOptionPane.showMessageDialog(this,
                modulo + ": próximamente en funcionamiento.",
                "Próximamente", JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Deseas salir de la aplicación?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;
        Conexion.cerrarConexion();
        System.exit(0);
    }

    // ── Apertura de formularios ────────────────────────────────────────────────

    private void abrirVentas() {
        new dlgVentas(this, true).setVisible(true);
    }

    private void abrirInventario() {
        new dlgInventario(this, true).setVisible(true);
    }

    private void abrirProveedor() {
        new dlgProveedor(this, true).setVisible(true);
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
            "Lineas Farma — Sistema de Gestión Farmacéutica\n\n"
            + "Versión: 1.0\n"
            + "Desarrollado para: Evaluación Final POO\n"
            + "Autor:\n         - Isaias Cesar Quintana Errazabal\n         - Jhonatan riveros marca\n         - Brayan Jahckson Quispe Rojas\n         - Jorge Alexander Rodriguez Jara\n\n"
            + "Módulos: Inventario | Ventas | Proveedores",
            "Acerca de Lineas Farma",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Cerrar sesión ──────────────────────────────────────────────────────────

    private void cerrarSesion() {
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        Sesion.empleadoActual = null;
        Conexion.cerrarConexion();
        this.dispose(); // cierra dashboard → AppLineasFarma.main detecta el cierre y sale
    }

    // ── Alerta de stock ────────────────────────────────────────────────────────

    private void mostrarAlertaStock() {
        try {
            java.util.List<applineasfarma.Modelo.Producto> lista =
                    new applineasfarma.DAO.ProductoDAO().listarTodos();
            long sinStock = lista.stream().filter(p -> p.getStock() == 0).count();
            long stockBajo = lista.stream()
                    .filter(p -> p.getStock() > 0 && p.getStock() <= 5).count();
            JOptionPane.showMessageDialog(this,
                String.format("Resumen de stock:\n\n"
                    + "  Sin stock (0 unidades): %d producto(s)\n"
                    + "  Stock bajo (≤ 5 unidades): %d producto(s)\n\n"
                    + "Ve a Inventario para revisar el detalle.",
                    sinStock, stockBajo),
                "Alerta de Stock", JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al consultar el stock:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CÓDIGO GENERADO POR NETBEANS
    // ═══════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnVentas = new javax.swing.JButton();
        btnProveedor = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnAyuda = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DASHBOARD LINEAS FARMA");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/ventas.png"))); // NOI18N
        btnVentas.setAlignmentX(0.5F);
        btnVentas.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnVentas.setContentAreaFilled(false);
        btnVentas.addActionListener(this::btnVentasActionPerformed);

        btnProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/cadena-de-suministro.png"))); // NOI18N
        btnProveedor.setAlignmentX(0.5F);
        btnProveedor.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnProveedor.setContentAreaFilled(false);
        btnProveedor.addActionListener(this::btnProveedorActionPerformed);

        btnInventario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/inventario.png"))); // NOI18N
        btnInventario.setAlignmentX(0.5F);
        btnInventario.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnInventario.setContentAreaFilled(false);
        btnInventario.addActionListener(this::btnInventarioActionPerformed);

        btnAyuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/informacion.png"))); // NOI18N
        btnAyuda.setAlignmentX(0.5F);
        btnAyuda.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnAyuda.setContentAreaFilled(false);
        btnAyuda.addActionListener(this::btnAyudaActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("VENTAS");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("PROVEEDOR");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("INVENTARIO");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("AYUDA");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAyuda))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addComponent(btnVentas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                        .addComponent(btnProveedor)))
                .addGap(126, 126, 126)
                .addComponent(btnInventario)
                .addGap(210, 210, 210))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(192, 192, 192)
                .addComponent(jLabel1)
                .addGap(249, 249, 249)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(265, 265, 265))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(512, 512, 512)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(212, 212, 212)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnProveedor)
                            .addComponent(btnVentas)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(204, 204, 204)
                        .addComponent(btnInventario)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(39, 39, 39)
                .addComponent(btnAyuda)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addContainerGap(74, Short.MAX_VALUE))
        );

        jMenu2.setText("Archivo");

        jMenuItem1.setText("Cerrar session");
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText("Salir");
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Inventario");

        jMenuItem3.setText("Gestionar producto");
        jMenu3.add(jMenuItem3);

        jMenuItem4.setText("Alerta de Stock");
        jMenu3.add(jMenuItem4);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Proveedores");

        jMenuItem5.setText("Direcotrio de proveedores");
        jMenu4.add(jMenuItem5);

        jMenuBar1.add(jMenu4);

        jMenu5.setText("Ventas");

        jMenuItem6.setText("Nueva venta");
        jMenu5.add(jMenuItem6);

        jMenuItem7.setText("Hisotiral ventas");
        jMenu5.add(jMenuItem7);

        jMenuBar1.add(jMenu5);

        jMenu6.setText("Ayuda");

        jMenuItem8.setText("Acera de Lineas Farma");
        jMenu6.add(jMenuItem8);

        jMenuBar1.add(jMenu6);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 32, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // ── Handlers de botones ────────────────────────────────────────────────────

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentasActionPerformed
        abrirVentas();
    }//GEN-LAST:event_btnVentasActionPerformed

    private void btnProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedorActionPerformed
        abrirProveedor();
    }//GEN-LAST:event_btnProveedorActionPerformed

    private void btnInventarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInventarioActionPerformed
        abrirInventario();
    }//GEN-LAST:event_btnInventarioActionPerformed

    private void btnAyudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAyudaActionPerformed
        mostrarAcercaDe();
    }//GEN-LAST:event_btnAyudaActionPerformed

    // ── main (para prueba directa desde NetBeans) ──────────────────────────────

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
            frmDashboard dialog = new frmDashboard(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAyuda;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnProveedor;
    private javax.swing.JButton btnVentas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
