# AppLineasFarma

Sistema de escritorio para la gestión de inventario, proveedores y ventas de una farmacia, desarrollado en **Java Swing** con base de datos **Microsoft Access**.

> Proyecto académico — Evaluación Final del curso de **Programación Orientada a Objetos (POO)**.

---

## Tabla de contenido

- [Descripción](#-descripción)
- [Funcionalidades](#-funcionalidades)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos](#-requisitos)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Módulos del sistema](#-módulos-del-sistema)
- [Mejoras futuras](#-mejoras-futuras)
- [Autores](#-autores)

---

## Descripción

**AppLineasFarma** es una aplicación de escritorio que permite administrar las operaciones diarias de una farmacia: controlar el stock de medicamentos, gestionar proveedores y registrar ventas a clientes, todo desde una interfaz gráfica sencilla y organizada por módulos.

Los datos se almacenan en una base de datos Access (`BDLineasFarma.accdb`) a la que la aplicación se conecta mediante **UCanAccess** (driver JDBC), por lo que no requiere instalar un motor de base de datos adicional.

## Funcionalidades

| Módulo | Qué permite |
|---|---|
| **Inicio de sesión** | Autenticación de empleados con usuario y contraseña. |
| **Dashboard** | Panel principal con acceso rápido a todos los módulos e indicadores del negocio. |
| **Inventario** | Listar, buscar, agregar, editar y eliminar productos; control de stock y de productos que requieren receta. |
| **Proveedores** | Consulta de proveedores, órdenes pendientes y total de pedidos. |
| **Ventas** | Registro de ventas con detalle de productos, búsqueda/registro de clientes y descuento automático de stock. |

## Tecnologías

- **Lenguaje:** Java
- **Interfaz gráfica:** Swing (`JFrame`, `JDialog`, `JPanel`) con Look & Feel Nimbus y NetBeans GUI Builder
- **Base de datos:** Microsoft Access (`.accdb`)
- **Conectividad:** JDBC con [UCanAccess](https://ucanaccess.sourceforge.net/site.html) 5.0.1
- **Librerías (carpeta `lib/`):** Jackcess 4.0.5, HSQLDB 2.7.3, Commons Lang 3.14.0, Commons Logging 1.3.1
- **IDE / Build:** Apache NetBeans + Ant

## Arquitectura

El proyecto separa responsabilidades en capas siguiendo el patrón **DAO**:

```text
 Formulario (Vista)  ──►  DAO (Acceso a datos)  ──►  Conexion  ──►  BDLineasFarma.accdb
        │                         │
        └────────► Modelo (Entidades) ◄────────┘
```

- **Formulario:** ventanas Swing que interactúan con el usuario.
- **Modelo:** clases de entidad (`Producto`, `Cliente`, `Empleado`, `Venta`).
- **DAO:** clases que ejecutan las consultas SQL de cada entidad.
- **Conexion:** conexión única a la base de datos (patrón *Singleton*).
- **Sesion:** guarda el empleado que inició sesión mientras la aplicación está abierta.

## Estructura del proyecto

```text
AppLineasFarma/
├── src/
│   ├── applineasfarma/
│   │   ├── AppLineasFarma.java        # Clase principal (punto de entrada)
│   │   ├── Conexion/
│   │   │   └── Conexion.java          # Conexión Singleton a la BD
│   │   ├── DAO/
│   │   │   ├── ClienteDAO.java
│   │   │   ├── EmpleadoDAO.java
│   │   │   ├── ProductoDAO.java
│   │   │   ├── ProveedorDAO.java
│   │   │   └── VentaDAO.java
│   │   ├── Modelo/
│   │   │   ├── Cliente.java
│   │   │   ├── Empleado.java
│   │   │   ├── Producto.java
│   │   │   └── Venta.java
│   │   └── Formulario/
│   │       ├── dlgLogin.java
│   │       ├── frmDashboard.java
│   │       ├── dlgInventario.java
│   │       ├── dlgProveedor.java
│   │       ├── dlgVentas.java
│   │       └── Sesion.java
│   └── Assets/                        # Íconos de la interfaz
├── lib/                               # Librerías JAR (UCanAccess y dependencias)
├── BDLineasFarma.accdb                # Base de datos Access
├── build.xml                          # Script de compilación Ant
├── nbproject/                         # Configuración de NetBeans
└── README.md
```

## Requisitos

- **JDK** compatible con el nivel de código del proyecto (configurado en `nbproject/project.properties`; si usas un JDK anterior, ajusta *Source/Binary Format* en las propiedades del proyecto).
- **Apache NetBeans** (recomendado) o cualquier IDE compatible con proyectos Ant.
- Las librerías de la carpeta `lib/` agregadas al classpath (ya vienen configuradas en el proyecto).

## Instalación y ejecución

1. **Clona el repositorio**
   ```bash
   git clone https://github.com/LeoDev2p/LienasFarma.git
   ```
2. **Abre el proyecto** en NetBeans: `File → Open Project` y selecciona la carpeta `AppLineasFarma`.
3. **Verifica la base de datos:** el archivo `BDLineasFarma.accdb` debe estar en la **raíz del proyecto**, ya que la conexión usa una ruta relativa.
4. **Compila** el proyecto (`Clean and Build`, o `F11`).
5. **Ejecuta** la clase principal `applineasfarma.AppLineasFarma` (`Run`, o `F6`).
6. Inicia sesión con un empleado registrado en la tabla de empleados de la base de datos.
> User: admin  ; Password: admin123

> Si aparece el error *"Driver UCanAccess no encontrado"*, revisa que los JAR de `lib/` estén agregados en **Libraries** del proyecto.

## Módulos del sistema

### 1. Login
Primera ventana de la aplicación. Valida las credenciales contra la base de datos; si el usuario cierra la ventana sin autenticarse, la aplicación termina.

### 2. Dashboard
Pantalla principal tras el inicio de sesión. Muestra la información general del negocio y da acceso a Ventas, Proveedores, Inventario y Ayuda. Al cerrarse, libera la conexión a la base de datos.

### 3. Inventario
Administración del catálogo de productos: búsqueda por nombre, alta, edición y eliminación, además del control de stock.

### 4. Proveedores
Consulta de proveedores y seguimiento de órdenes pendientes y montos de pedidos.

### 5. Ventas
Registro de transacciones con cabecera y detalle de venta, asociación a un cliente (por DNI/RUC) y actualización automática del stock.


## Autores

Proyecto desarrollado por estudiantes de la asignatura de **Programación Orientada a Objetos**:

- Isaias Cesar Quintana Errazabal
- Jorge Alexander Rodriguez Jara
- Brayan Jahckson Quispe Rojas
- Jhonatan Riveros Marca


