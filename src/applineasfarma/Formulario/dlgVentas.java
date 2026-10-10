package applineasfarma.Formulario;

import applineasfarma.DAO.ClienteDAO;
import applineasfarma.DAO.ProductoDAO;
import applineasfarma.DAO.VentaDAO;
import applineasfarma.Modelo.Cliente;
import applineasfarma.Modelo.Producto;
import applineasfarma.Modelo.Venta;
import applineasfarma.Modelo.Venta.DetalleVenta;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class dlgVentas extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(dlgVentas.class.getName());

    // ── DAOs ───────────────────────────────────────────────────────────────────
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO  clienteDAO  = new ClienteDAO();
    private final VentaDAO    ventaDAO    = new VentaDAO();

    // ── Estado interno del carrito ─────────────────────────────────────────────
    private DefaultTableModel modeloCarrito;
    private Producto productoSeleccionado = null; // producto encontrado en búsqueda

    // ── Columnas del carrito ───────────────────────────────────────────────────
    private static final String[] COLS_CARRITO = {
        "Código", "Producto", "Receta", "Cantidad", "P. Unitario", "SubTotal", "Acción"
    };

    private static final double IGV_RATE = 0.18;

    // ── Constructor ────────────────────────────────────────────────────────────

    public dlgVentas(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarCarrito();
        inicializarUI();
    }

    // ── Configuración inicial ──────────────────────────────────────────────────

    private void configurarCarrito() {
        modeloCarrito = new DefaultTableModel(COLS_CARRITO, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jTable1.setModel(modeloCarrito);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    private void inicializarUI() {
        // Mostrar nombre del vendedor desde la sesión
        if (Sesion.empleadoActual != null) {
            jLabel5.setText(Sesion.empleadoActual.getNombre());
        }
        // Mostrar fecha actual
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd 'de' MMMM 'de' yyyy",
                new java.util.Locale("es", "PE"));
        jLabel4.setText(sdf.format(new Date()));

        // Inicializar totales a cero
        actualizarTotales();
    }

    // ── Búsqueda de producto ───────────────────────────────────────────────────

    private void buscarProducto() {
        String texto = txtBuscarProducto.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre o código para buscar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Producto> resultados = productoDAO.buscarPorNombre(texto);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ningún producto con ese criterio.",
                        "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                limpiarProductoSeleccionado();
                return;
            }

            if (resultados.size() == 1) {
                // Encontrado exactamente uno — seleccionar directo
                mostrarProductoEncontrado(resultados.get(0));
            } else {
                // Varios resultados — mostrar selector
                String[] opciones = resultados.stream()
                        .map(p -> p.getIdProducto() + " - " + p.getNombre()
                                + " [" + p.getCategoria() + "] Stock: " + p.getStock())
                        .toArray(String[]::new);
                String eleccion = (String) JOptionPane.showInputDialog(this,
                        "Se encontraron varios productos. Selecciona uno:",
                        "Seleccionar producto", JOptionPane.PLAIN_MESSAGE,
                        null, opciones, opciones[0]);
                if (eleccion == null) return;
                int idx = java.util.Arrays.asList(opciones).indexOf(eleccion);
                mostrarProductoEncontrado(resultados.get(idx));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarProductoEncontrado(Producto p) {
        productoSeleccionado = p;
        txtBuscarProducto.setText(p.getNombre());
        lblStock.setText(String.valueOf(p.getStock()));
        // Marcar receta según categoría del producto
        if (p.isRequiereReceta()) {
            jRadioButton1.setSelected(true);  // Sí
        } else {
            jRadioButton2.setSelected(true);  // No
        }
        // Spinner: máximo = stock disponible, mínimo = 1
        spnCantidad.setModel(new javax.swing.SpinnerNumberModel(1, 1,
                Math.max(1, p.getStock()), 1));
    }

    private void limpiarProductoSeleccionado() {
        productoSeleccionado = null;
        lblStock.setText("");
        spnCantidad.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9999, 1));
        btgReceta.clearSelection();
    }

    // ── Agregar producto al carrito ────────────────────────────────────────────

    private void agregarAlCarrito() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero busca y selecciona un producto.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad = (int) spnCantidad.getValue();
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidad > productoSeleccionado.getStock()) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuficiente. Disponible: " + productoSeleccionado.getStock(),
                    "Sin stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar si el producto ya está en el carrito → actualizar cantidad
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            if ((int) modeloCarrito.getValueAt(i, 0) == productoSeleccionado.getIdProducto()) {
                int cantExistente = (int) modeloCarrito.getValueAt(i, 3);
                int nuevaCant     = cantExistente + cantidad;
                if (nuevaCant > productoSeleccionado.getStock()) {
                    JOptionPane.showMessageDialog(this,
                            "No puedes agregar más unidades. Stock disponible: "
                            + productoSeleccionado.getStock(), "Sin stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double precioExistente = Double.parseDouble(
                        modeloCarrito.getValueAt(i, 4).toString().replace(",", "."));
                double subTotal = nuevaCant * precioExistente;
                modeloCarrito.setValueAt(nuevaCant, i, 3);
                modeloCarrito.setValueAt(String.format("%.2f", subTotal), i, 5);
                actualizarTotales();
                limpiarProductoSeleccionado();
                txtBuscarProducto.setText("");
                return;
            }
        }

        // Usar el precio de venta registrado en TProducto
        double precioUnit = productoSeleccionado.getPrecioVenta();
        if (precioUnit <= 0) {
            JOptionPane.showMessageDialog(this,
                    "El producto no tiene un precio de venta registrado en la base de datos.\n"
                    + "Por favor actualiza el precio en el módulo de Inventario.",
                    "Sin precio", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double subTotal = cantidad * precioUnit;
        modeloCarrito.addRow(new Object[]{
            productoSeleccionado.getIdProducto(),
            productoSeleccionado.getNombre(),
            jRadioButton1.isSelected() ? "Sí" : "No",
            cantidad,
            String.format("%.2f", precioUnit),
            String.format("%.2f", subTotal),
            "Quitar"
        });

        actualizarTotales();
        limpiarProductoSeleccionado();
        txtBuscarProducto.setText("");
        txtBuscarProducto.requestFocus();
    }

    // ── Eliminar fila del carrito ──────────────────────────────────────────────

    private void quitarDelCarrito() {
        int fila = jTable1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila del carrito para quitar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloCarrito.removeRow(fila);
        actualizarTotales();
    }

    // ── Recalcular totales ─────────────────────────────────────────────────────

    private void actualizarTotales() {
        double subTotal = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            subTotal += Double.parseDouble(
                    modeloCarrito.getValueAt(i, 5).toString().replace(",", "."));
        }
        double igv   = subTotal * IGV_RATE;
        double total = subTotal + igv;

        lblSubTotal.setText(String.format("S/. %.2f", subTotal));
        lblIGV.setText     (String.format("S/. %.2f", igv));
        jLabel20.setText   (String.format("S/. %.2f", total));

        // Tarjeta ítems en carrito y total actual
        jLabel6.setText("Ítems en Carrito: " + modeloCarrito.getRowCount());
        jLabel8.setText("Total Actual: S/. " + String.format("%.2f", total));
    }

    // ── Buscar cliente por DNI ─────────────────────────────────────────────────

    private void buscarClientePorDni() {
        String dni = txtDni.getText().trim();
        if (dni.isEmpty()) return;
        try {
            Cliente c = clienteDAO.buscarPorDni(dni);
            if (c != null) {
                txtCliente.setText(c.getNombre()
                        + (c.getApellidos() != null && !c.getApellidos().isBlank()
                           ? " " + c.getApellidos() : ""));
            } else {
                txtCliente.setText("");
                // Preguntar si crear cliente nuevo
                int resp = JOptionPane.showConfirmDialog(this,
                        "No se encontró cliente con DNI/RUC: " + dni + "\n¿Deseas registrarlo?",
                        "Cliente nuevo", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) return;

                // Formulario completo — Access NO acepta strings vacíos en campos de texto
                javax.swing.JTextField txtNombres   = new javax.swing.JTextField(15);
                javax.swing.JTextField txtApellidos = new javax.swing.JTextField(15);
                javax.swing.JTextField txtTelefono  = new javax.swing.JTextField(10);
                javax.swing.JTextField txtDireccion = new javax.swing.JTextField(20);

                javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(0, 2, 5, 5));
                panel.add(new javax.swing.JLabel("Nombres (*):"));    panel.add(txtNombres);
                panel.add(new javax.swing.JLabel("Apellidos (*):"));   panel.add(txtApellidos);
                panel.add(new javax.swing.JLabel("Teléfono:"));        panel.add(txtTelefono);
                panel.add(new javax.swing.JLabel("Dirección:"));       panel.add(txtDireccion);

                int ok = JOptionPane.showConfirmDialog(this, panel,
                        "Registrar cliente (DNI: " + dni + ")",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ok != JOptionPane.OK_OPTION) return;

                String nombres   = txtNombres.getText().trim();
                String apellidos = txtApellidos.getText().trim();
                if (nombres.isEmpty() || apellidos.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Nombres y Apellidos son obligatorios.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Usar " " como fallback para campos opcionales vacíos (Access rechaza "")
                String telefono  = txtTelefono.getText().trim();
                String direccion = txtDireccion.getText().trim();
                if (telefono.isEmpty())  telefono  = " ";
                if (direccion.isEmpty()) direccion = " ";

                Cliente nuevo = new Cliente(0, nombres, dni, apellidos, direccion, telefono);
                int idNuevo = clienteDAO.insertar(nuevo);
                if (idNuevo > 0) {
                    txtCliente.setText(nombres + " " + apellidos);
                    JOptionPane.showMessageDialog(this,
                            "Cliente registrado correctamente.", "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo registrar el cliente.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar cliente:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Procesar venta ────────────────────────────────────────────────────────

    private void procesarVenta() {
        // Validar carrito no vacío
        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar cliente
        String dni      = txtDni.getText().trim();
        String nombreCli = txtCliente.getText().trim();
        if (dni.isEmpty() || nombreCli.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa el DNI del cliente y asegúrate de que el nombre esté cargado.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener idCliente
        int idCliente = -1;
        try {
            Cliente c = clienteDAO.buscarPorDni(dni);
            if (c != null) idCliente = c.getIdCliente();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener cliente:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (idCliente == -1) {
            JOptionPane.showMessageDialog(this,
                    "Guarda el cliente antes de procesar la venta.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener idEmpleado desde sesión (fallback a 1 si no hay sesión)
        int idEmpleado = (Sesion.empleadoActual != null)
                ? Sesion.empleadoActual.getIdEmpleado() : 1;

        // Calcular total
        double subTotal = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            subTotal += Double.parseDouble(
                    modeloCarrito.getValueAt(i, 5).toString().replace(",", "."));
        }
        double total = subTotal + (subTotal * IGV_RATE);

        // Construir objeto Venta con los campos reales
        String tipoDoc = cbxTipoDoc.getSelectedItem().toString();
        Venta venta = new Venta(idCliente, idEmpleado, new Date(),
                tipoDoc, cbxFormaPago.getSelectedItem().toString(),
                subTotal, subTotal * IGV_RATE, total);

        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            int    idProd    = (int)    modeloCarrito.getValueAt(i, 0);
            String nombreProd = (String) modeloCarrito.getValueAt(i, 1);
            int    cantidad  = (int)    modeloCarrito.getValueAt(i, 3);
            double precio    = Double.parseDouble(
                    modeloCarrito.getValueAt(i, 4).toString().replace(",", "."));
            venta.agregarDetalle(new DetalleVenta(idProd, nombreProd, cantidad, precio));
        }

        // Confirmar
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("¿Confirmar venta?\n\nCliente: %s\nTotal: S/. %.2f\nForma de pago: %s",
                        nombreCli, total, cbxFormaPago.getSelectedItem()),
                "Confirmar venta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Ejecutar en BD (transacción)
        try {
            int idVenta = ventaDAO.registrarVenta(venta);
            JOptionPane.showMessageDialog(this,
                    "✅ Venta registrada con éxito.\nN° de venta: " + idVenta,
                    "Venta exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarVenta();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar la venta:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Cancelar / limpiar venta ──────────────────────────────────────────────

    private void limpiarVenta() {
        modeloCarrito.setRowCount(0);
        txtDni.setText("");
        txtCliente.setText("");
        txtBuscarProducto.setText("");
        limpiarProductoSeleccionado();
        actualizarTotales();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CÓDIGO GENERADO POR NETBEANS
    // ═══════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btgReceta = new javax.swing.ButtonGroup();
        pnlContendor = new javax.swing.JPanel();
        pnlHeader = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtCliente = new javax.swing.JTextField();
        txtDni = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        cbxTipoDoc = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        txtBuscarProducto = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        spnCantidad = new javax.swing.JSpinner();
        jPanel7 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        lblStock = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        btnAgregarProducto = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        cbxFormaPago = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        pnlCobro = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        lblSubTotal = new javax.swing.JLabel();
        lblIGV = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        pnlTarjetas = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        pnlBotonesCRUD = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PUNTO DE VENTAS  - LINEAS FARMA");

        pnlContendor.setBackground(new java.awt.Color(255, 255, 255));

        pnlHeader.setBackground(new java.awt.Color(0, 153, 153));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/mas.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Punto de Venta - Lineas farma");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/usuario.png"))); // NOI18N
        jLabel3.setText("Vendedor:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("05 de Octubre de 2026");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Isaias Quintana");

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(35, 35, 35))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel1)
                                .addGroup(pnlHeaderLayout.createSequentialGroup()
                                    .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel5))
                                    .addGap(4, 4, 4)))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel4)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jPanel5.setForeground(new java.awt.Color(0, 153, 153));

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setText("DATOS VENTAS ");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("DNI / RUC");

        txtCliente.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        txtDni.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Cliente");

        cbxTipoDoc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbxTipoDoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Boleta", "Factura" }));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Venta Productos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 14), new java.awt.Color(0, 153, 153))); // NOI18N
        jPanel6.setForeground(new java.awt.Color(0, 153, 153));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Buscar (Código / Sustancia)");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/vaso.png"))); // NOI18N
        jButton1.addActionListener(this::jButton1ActionPerformed);

        txtBuscarProducto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Cantidad: ");

        spnCantidad.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        spnCantidad.setModel(new javax.swing.SpinnerNumberModel());

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Stock:");

        lblStock.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addGap(18, 18, 18)
                .addComponent(lblStock, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setText("Requiere Receta");

        btgReceta.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jRadioButton1.setText("Si");

        btgReceta.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jRadioButton2.setText("No");

        btnAgregarProducto.setBackground(new java.awt.Color(0, 102, 102));
        btnAgregarProducto.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAgregarProducto.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregarProducto.setText("+ AGREGAR PRODUCTO");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(txtBuscarProducto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(32, 32, 32)
                                .addComponent(jRadioButton1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton2)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(btnAgregarProducto)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtBuscarProducto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnCantidad))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRadioButton1)
                        .addComponent(jRadioButton2))
                    .addComponent(jLabel16))
                .addGap(31, 31, 31)
                .addComponent(btnAgregarProducto)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Tipo Doc.");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel22.setText("Forma de Pago");

        cbxFormaPago.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbxFormaPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Yape", "Tarjeta", "Efectivo", "Transferencia" }));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(104, 104, 104)
                            .addComponent(jLabel9))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(17, 17, 17)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel10)
                                .addComponent(jLabel12))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxFormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxTipoDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(9, Short.MAX_VALUE))
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxTipoDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbxFormaPago, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(35, 35, 35)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Código", "Producto", "Receta", "Cantidad", "P. Unitario", "SubTotal", "Acción"
            }
        ));
        jTable1.setAlignmentX(1.0F);
        jTable1.setSelectionBackground(new java.awt.Color(51, 255, 255));
        jScrollPane1.setViewportView(jTable1);

        pnlCobro.setBackground(new java.awt.Color(204, 255, 255));

        jLabel17.setBackground(new java.awt.Color(0, 102, 0));
        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("RESUMEN DE COBRO");
        jLabel17.setOpaque(true);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("SubTotal");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("IGV (18%)");

        lblSubTotal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblSubTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        lblIGV.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblIGV.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jLabel21.setBackground(new java.awt.Color(0, 102, 0));
        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("TOTAL A PAGAR S/.");
        jLabel21.setOpaque(true);

        jLabel20.setBackground(new java.awt.Color(0, 102, 0));
        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setOpaque(true);

        javax.swing.GroupLayout pnlCobroLayout = new javax.swing.GroupLayout(pnlCobro);
        pnlCobro.setLayout(pnlCobroLayout);
        pnlCobroLayout.setHorizontalGroup(
            pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlCobroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCobroLayout.createSequentialGroup()
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                    .addGroup(pnlCobroLayout.createSequentialGroup()
                        .addGroup(pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15))))
        );
        pnlCobroLayout.setVerticalGroup(
            pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCobroLayout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblIGV, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(pnlCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jButton3.setBackground(new java.awt.Color(0, 204, 153));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("PROCESAR VENTA");

        jButton4.setBackground(new java.awt.Color(255, 51, 51));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("CANCELAR VENTA");

        pnlTarjetas.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jPanel2.setForeground(new java.awt.Color(0, 153, 153));

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Ventas Hoy: ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel7)
                .addContainerGap(139, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jPanel3.setForeground(new java.awt.Color(0, 153, 153));

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Ítems en Carrito:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel6)
                .addContainerGap(120, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jPanel4.setForeground(new java.awt.Color(0, 153, 153));

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("Total Actual: S/. ");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel8)
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlTarjetasLayout = new javax.swing.GroupLayout(pnlTarjetas);
        pnlTarjetas.setLayout(pnlTarjetasLayout);
        pnlTarjetasLayout.setHorizontalGroup(
            pnlTarjetasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetasLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(113, Short.MAX_VALUE))
        );
        pnlTarjetasLayout.setVerticalGroup(
            pnlTarjetasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTarjetasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTarjetasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pnlBotonesCRUD.setBackground(new java.awt.Color(255, 255, 255));

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/anadir.png"))); // NOI18N
        jButton2.setText("Agregar Producto");

        jButton5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/borrar.png"))); // NOI18N
        jButton5.setText("Elimminar producto");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/editar.png"))); // NOI18N
        jButton6.setText("Editar product");

        javax.swing.GroupLayout pnlBotonesCRUDLayout = new javax.swing.GroupLayout(pnlBotonesCRUD);
        pnlBotonesCRUD.setLayout(pnlBotonesCRUDLayout);
        pnlBotonesCRUDLayout.setHorizontalGroup(
            pnlBotonesCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBotonesCRUDLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton6)
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlBotonesCRUDLayout.setVerticalGroup(
            pnlBotonesCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBotonesCRUDLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlBotonesCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout pnlContendorLayout = new javax.swing.GroupLayout(pnlContendor);
        pnlContendor.setLayout(pnlContendorLayout);
        pnlContendorLayout.setHorizontalGroup(
            pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContendorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTarjetas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlContendorLayout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlBotonesCRUD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContendorLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(pnlCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(225, 225, 225))
                            .addGroup(pnlContendorLayout.createSequentialGroup()
                                .addGroup(pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 801, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(pnlContendorLayout.createSequentialGroup()
                                        .addGap(259, 259, 259)
                                        .addComponent(jButton3)
                                        .addGap(32, 32, 32)
                                        .addComponent(jButton4)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
            .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlContendorLayout.setVerticalGroup(
            pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContendorLayout.createSequentialGroup()
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTarjetas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlContendorLayout.createSequentialGroup()
                        .addComponent(pnlBotonesCRUD, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlContendorLayout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(31, 31, 31)))
                .addGroup(pnlContendorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContendor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContendor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        buscarProducto();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnAgregarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarProductoActionPerformed
        agregarAlCarrito();
    }//GEN-LAST:event_btnAgregarProductoActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        procesarVenta();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la venta? Se perderán los ítems del carrito.",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) limpiarVenta();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        quitarDelCarrito();
    }//GEN-LAST:event_jButton5ActionPerformed

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
            dlgVentas dialog = new dlgVentas(new javax.swing.JDialog(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btgReceta;
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JComboBox<String> cbxFormaPago;
    private javax.swing.JComboBox<String> cbxTipoDoc;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblIGV;
    private javax.swing.JLabel lblStock;
    private javax.swing.JLabel lblSubTotal;
    private javax.swing.JPanel pnlBotonesCRUD;
    private javax.swing.JPanel pnlCobro;
    private javax.swing.JPanel pnlContendor;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlTarjetas;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCliente;
    private javax.swing.JTextField txtDni;
    // End of variables declaration//GEN-END:variables
}
