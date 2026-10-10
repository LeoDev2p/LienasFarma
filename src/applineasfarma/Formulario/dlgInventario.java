package applineasfarma.Formulario;

import applineasfarma.DAO.ProductoDAO;
import applineasfarma.Modelo.Producto;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

/**
 * Formulario de Inventario de Medicamentos.
 * Conectado a ProductoDAO para operaciones CRUD sobre TProducto.
 *
 * Componentes clave:
 *   jTable1      → tabla principal (9 columnas)
 *   jTextField1  → campo de búsqueda
 *   jButton1     → buscar
 *   jButton2     → agregar producto
 *   jButton3     → eliminar producto
 *   jButton4     → editar producto
 *   lblTotalProveedores  → total de productos
 *   lblTotalProveedores1 → stock total acumulado
 *   lblTotalProveedores3 → valor total del inventario (S/.)
 */
public class dlgInventario extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(dlgInventario.class.getName());

    // ── DAO ────────────────────────────────────────────────────────────────────
    private final ProductoDAO productoDAO = new ProductoDAO();

    // ── Modelo de la tabla ─────────────────────────────────────────────────────
    private DefaultTableModel modeloTabla;

    // ── Columnas de la tabla ───────────────────────────────────────────────────
    private static final String[] COLUMNAS = {
        "Código", "Producto", "Categoría", "Lote",
        "Vencimiento", "Stock", "Precio compra", "Precio venta", "Estado"
    };

    // ── Constructor ────────────────────────────────────────────────────────────

    public dlgInventario(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarTabla();
        configurarFiltros();
        cargarProductos();
    }

    // ── Filtros (categoría / estado) ───────────────────────────────────────────

    private static final String TODOS = "Todos";

    /** Estado derivado del stock; coincide con las opciones del combo de estado. */
    private static String calcularEstado(Producto p) {
        return p.getStock() == 0 ? "Sin stock"
                : (p.getStock() <= 5 ? "Stock bajo" : "Activo");
    }

    private void configurarFiltros() {
        jLabel5.setText("Estado");
        cbxEstado.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { TODOS, "Activo", "Stock bajo", "Sin stock" }));
        cbxCategoria.addActionListener(e -> buscarProducto());
        cbxEstado.addActionListener(e -> buscarProducto());
    }

    /** Rellena el combo de categorías con las categorías existentes, conservando la selección. */
    private void cargarCategorias(List<Producto> lista) {
        Object previa = cbxCategoria.getSelectedItem();
        java.util.TreeSet<String> cats = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Producto p : lista) {
            if (p.getCategoria() != null && !p.getCategoria().trim().isEmpty()) {
                cats.add(p.getCategoria().trim());
            }
        }
        java.awt.event.ActionListener[] ls = cbxCategoria.getActionListeners();
        for (java.awt.event.ActionListener l : ls) cbxCategoria.removeActionListener(l);
        javax.swing.DefaultComboBoxModel<String> m = new javax.swing.DefaultComboBoxModel<>();
        m.addElement(TODOS);
        for (String c : cats) m.addElement(c);
        cbxCategoria.setModel(m);
        if (previa != null && m.getIndexOf(previa) >= 0) cbxCategoria.setSelectedItem(previa);
        for (java.awt.event.ActionListener l : ls) cbxCategoria.addActionListener(l);
    }

    // ── Configuración inicial de la tabla ──────────────────────────────────────

    private void configurarTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // solo lectura en la tabla
            }
        };
        jTable1.setModel(modeloTabla);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.getTableHeader().setReorderingAllowed(false);

        // Ancho de columna Código
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(60);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(200);
    }

    // ── Carga / recarga de datos ────────────────────────────────────────────────

    /**
     * Carga todos los productos desde la BD y actualiza la tabla y los contadores.
     */
    private void cargarProductos() {
        try {
            List<Producto> lista = productoDAO.listarTodos();
            cargarCategorias(lista);
            buscarProducto(); // aplica búsqueda y filtros activos
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar el inventario:\n" + e.getMessage(),
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Rellena la tabla con la lista dada y actualiza los contadores de las tarjetas.
     */
    private void actualizarTabla(List<Producto> lista) {
        modeloTabla.setRowCount(0); // limpiar filas existentes

        int stockTotal   = 0;
        double valorTotal = 0.0;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (Producto p : lista) {
            String fecVenc = p.getFechaVencimiento() != null
                    ? sdf.format(p.getFechaVencimiento()) : "-";
            String estado = calcularEstado(p);
            modeloTabla.addRow(new Object[]{
                p.getIdProducto(),
                p.getNombre(),
                p.getCategoria(),
                p.getLote()    != null ? p.getLote() : "-",
                fecVenc,
                p.getStock(),
                p.getSustancia() != null ? p.getSustancia() : "-",
                p.getCodigo()    != null ? p.getCodigo()    : "-",
                estado
            });
            stockTotal  += p.getStock();
            // valorTotal = suma de stocks (sin precio en la BD, mostramos cantidad total)
            valorTotal  += p.getStock();
        }

        // Actualizar tarjetas de resumen
        lblTotalProveedores.setText(String.valueOf(lista.size()));
        lblTotalProveedores1.setText(String.valueOf(stockTotal));
        lblTotalProveedores3.setText(String.format("%.2f", valorTotal));
    }

    // ── Obtener fila seleccionada ──────────────────────────────────────────────

    /**
     * Devuelve el idProducto de la fila seleccionada en la tabla, o -1 si ninguna.
     */
    private int getIdSeleccionado() {
        int fila = jTable1.getSelectedRow();
        if (fila == -1) return -1;
        return (int) modeloTabla.getValueAt(fila, 0);
    }

    // ── Diálogo de Agregar ────────────────────────────────────────────────────

    private void mostrarDialogoAgregar() {
        // Campos del formulario de agregar
        javax.swing.JTextField txtNombre    = new javax.swing.JTextField(20);
        javax.swing.JTextField txtCategoria = new javax.swing.JTextField(20);
        javax.swing.JTextField txtStock     = new javax.swing.JTextField(10);
        javax.swing.JTextField txtSustancia = new javax.swing.JTextField(20);

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(0, 2, 5, 5));
        panel.add(new javax.swing.JLabel("Nombre:"));        panel.add(txtNombre);
        panel.add(new javax.swing.JLabel("Categoría:"));     panel.add(txtCategoria);
        panel.add(new javax.swing.JLabel("Sustancia:"));     panel.add(txtSustancia);
        panel.add(new javax.swing.JLabel("Stock inicial:"));  panel.add(txtStock);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Agregar Producto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) return;

        // Validación
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int stock;
        try {
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock debe ser un número entero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Producto nuevo = new Producto();
        nuevo.setNombre(nombre);
        nuevo.setCategoria(txtCategoria.getText().trim());
        nuevo.setStock(stock);
        nuevo.setSustancia(txtSustancia.getText().trim());
        nuevo.setLote("");
        nuevo.setCodigo("P" + System.currentTimeMillis() % 10000);
        try {
            int id = productoDAO.insertar(nuevo);
            if (id > 0) {
                JOptionPane.showMessageDialog(this, "Producto agregado con ID: " + id);
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al agregar:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Diálogo de Editar ─────────────────────────────────────────────────────

    private void mostrarDialogoEditar() {
        int id = getIdSeleccionado();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Producto p;
        try {
            p = productoDAO.buscarPorId(id);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el producto:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        javax.swing.JTextField txtNombre    = new javax.swing.JTextField(p.getNombre(), 20);
        javax.swing.JTextField txtCategoria = new javax.swing.JTextField(p.getCategoria() != null ? p.getCategoria() : "", 20);
        javax.swing.JTextField txtSustancia = new javax.swing.JTextField(p.getSustancia() != null ? p.getSustancia() : "", 20);
        javax.swing.JTextField txtStock     = new javax.swing.JTextField(String.valueOf(p.getStock()), 10);

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(0, 2, 5, 5));
        panel.add(new javax.swing.JLabel("Nombre:"));        panel.add(txtNombre);
        panel.add(new javax.swing.JLabel("Categoría:"));     panel.add(txtCategoria);
        panel.add(new javax.swing.JLabel("Sustancia:"));     panel.add(txtSustancia);
        panel.add(new javax.swing.JLabel("Stock:"));          panel.add(txtStock);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Editar Producto (ID: " + id + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) return;

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int stock;
        try {
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock debe ser un número entero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        p.setNombre(nombre);
        p.setCategoria(txtCategoria.getText().trim());
        p.setSustancia(txtSustancia.getText().trim());
        p.setStock(stock);

        try {
            if (productoDAO.actualizar(p)) {
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se realizaron cambios.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    private void eliminarProductoSeleccionado() {
        int id = getIdSeleccionado();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = (String) modeloTabla.getValueAt(jTable1.getSelectedRow(), 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el producto \"" + nombre + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (productoDAO.eliminar(id)) {
                JOptionPane.showMessageDialog(this, "Producto eliminado.");
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    /** Aplica texto de búsqueda + filtro de categoría + filtro de estado. */
    private void buscarProducto() {
        String texto = jTextField1.getText().trim();
        Object catSel = cbxCategoria.getSelectedItem();
        String categoria = catSel == null ? TODOS : catSel.toString();
        Object estSel = cbxEstado.getSelectedItem();
        String estado = estSel == null ? TODOS : estSel.toString();
        try {
            List<Producto> base = texto.isEmpty()
                    ? productoDAO.listarTodos()
                    : productoDAO.buscarPorNombre(texto);
            List<Producto> resultado = new java.util.ArrayList<>();
            for (Producto p : base) {
                if (!TODOS.equals(categoria)
                        && !categoria.equalsIgnoreCase(p.getCategoria() != null ? p.getCategoria().trim() : "")) {
                    continue;
                }
                if (!TODOS.equals(estado) && !estado.equals(calcularEstado(p))) {
                    continue;
                }
                resultado.add(p);
            }
            actualizarTabla(resultado);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CÓDIGO GENERADO POR NETBEANS — NO MODIFICAR MANUALMENTE
    // ═══════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbxCategoria = new javax.swing.JComboBox<>();
        cbxEstado = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblTotalProveedores = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblTotalProveedores1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblTotalProveedores2 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblTotalProveedores3 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setText("INVENTARIO DE MEDICAMENTOS");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Admin");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Usuario:");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(232, 232, 232)));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField1.setBorder(null);
        jTextField1.setMargin(new java.awt.Insets(0, 3, 0, 3));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/vaso.png"))); // NOI18N
        jButton1.setMargin(new java.awt.Insets(0, 3, 0, 3));
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTextField1)
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Categoria");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Categoria");

        cbxCategoria.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbxCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Compoisción", "Liquidos", "Solidos", "Cremas", " " }));

        cbxEstado.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbxEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Compoisción", "Liquidos", "Solidos", "Cremas", " " }));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/agregar-producto.png"))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setText("Total de productos");

        lblTotalProveedores.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTotalProveedores.setText("23");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(308, 308, 308))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTotalProveedores)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/en-stock.png"))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setText("Stock");

        lblTotalProveedores1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTotalProveedores1.setText("23");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalProveedores1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(308, 308, 308))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalProveedores1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel8))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/reloj.png"))); // NOI18N

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setText("Proximo a vencer");

        lblTotalProveedores2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTotalProveedores2.setText("23");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalProveedores2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(308, 308, 308))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalProveedores2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel10))
        );

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/bolsa-de-dinero.png"))); // NOI18N

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Valor Inventario");

        lblTotalProveedores3.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTotalProveedores3.setText("23");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setText("S/.");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotalProveedores3, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(308, 308, 308))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTotalProveedores3)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel12)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/anadir.png"))); // NOI18N
        jButton2.setText("Agregar Producto");

        jButton3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/borrar.png"))); // NOI18N
        jButton3.setText("Elimminar producto");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/editar.png"))); // NOI18N
        jButton4.setText("Editar product");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addGap(18, 18, 18))
        );

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Código", "Producto", "Categoria", "Lote", "Vencimiento", "Stock", "Precio compra", "Precio venta", "Estado"
            }
        ));
        jTable1.setAlignmentX(1.0F);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addGap(29, 29, 29))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(cbxEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbxEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // ── Handlers de botones ────────────────────────────────────────────────────

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        buscarProducto();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        mostrarDialogoAgregar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        eliminarProductoSeleccionado();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        mostrarDialogoEditar();
    }//GEN-LAST:event_jButton4ActionPerformed

    // ── main (para prueba directa) ─────────────────────────────────────────────

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
            dlgInventario dialog = new dlgInventario(new javax.swing.JDialog(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            dialog.setVisible(true);
        });
    }

    // ── Variables declaration ──────────────────────────────────────────────────
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cbxCategoria;
    private javax.swing.JComboBox<String> cbxEstado;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblTotalProveedores;
    private javax.swing.JLabel lblTotalProveedores1;
    private javax.swing.JLabel lblTotalProveedores2;
    private javax.swing.JLabel lblTotalProveedores3;
    // End of variables declaration//GEN-END:variables
}
