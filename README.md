# ☕ E-commerce Café Valle Blanco

<p align="center">
  <img src="Frontend/public/assets/images/nc-hero-nuestrocafe.jpg" alt="Café Valle Blanco - Villa Rica, Oxapampa" width="100%" style="border-radius: 12px; max-height: 420px; object-fit: cover;" />
</p>

Plataforma de comercio electrónico integral desarrollada para la empresa cafetalera **Valle Blanco**, especializada en la exhibición, comercialización, control logístico y trazabilidad de cafés de especialidad peruanos cultivados bajo sombra en **Villa Rica, provincia de Oxapampa**.

Este proyecto ha sido desarrollado como parte del curso **Herramientas de Desarrollo**.

---

## 📋 Tabla de Contenidos

1. [Arquitectura del Sistema](#-arquitectura-del-sistema)
2. [Tecnologías Utilizadas](#-tecnologías-utilizadas)
3. [Módulos y Funcionalidades](#-módulos-y-funcionalidades)
   * [1. Portal del Cliente (E-commerce)](#1-portal-del-cliente-e-commerce)
   * [2. Ficha Técnica SCA & Polígono Sensorial](#-ficha-técnica-sca--polígono-sensorial)
   * [3. Nuestro Proceso Artesanal (9 Etapas)](#-nuestro-proceso-artesanal-en-9-etapas)
   * [4. Panel Administrativo (`/admin`)](#2-panel-administrativo-general-admin)
   * [5. Panel de Inventario & Almacén (`/inventario`)](#3-panel-de-inventario--almacén-inventario)
4. [Roles y Permisos del Sistema](#-roles-y-permisos-del-sistema)
5. [Cuentas de Prueba Preconfiguradas](#-cuentas-de-prueba-preconfiguradas)
6. [Requisitos Previos](#-requisitos-previos)
7. [Configuración de Base de Datos](#-configuración-de-la-base-de-datos)
8. [Instrucciones de Ejecución](#-instrucciones-de-ejecución)
9. [Documentación de la API REST](#-documentación-de-la-api-rest)

---

## 🏛 Arquitectura del Sistema

El proyecto opera bajo un modelo desacoplado con arquitectura de tres capas: **Frontend SPA/SSR + Backend RESTful API + Base de Datos Relacional PostgreSQL**.

```
Ecomerce_CafeValleBlanco/
├── Backend/                              # API RESTful con Spring Boot & Java 21
│   ├── src/main/java/com/valleblanco/backend/
│   │   ├── config/                       # Seguridad (Spring Security), CORS, JWT y DataInitializer
│   │   ├── controller/                   # Endpoints REST (Auth, Products, Inventario, Pedidos, etc.)
│   │   ├── dto/                          # Objetos de Transferencia de Datos (DTOs)
│   │   ├── model/                        # Entidades JPA (Product, Usuario, Role, MovimientoStock, etc.)
│   │   ├── repository/                   # Repositorios Spring Data JPA
│   │   └── service/                      # Lógica de negocio y servicios de auditoría
│   ├── src/main/resources/
│   │   ├── db/migration/                 # Scripts de migración versionados con Flyway
│   │   └── application.properties        # Configuración del servidor (puerto 8081, DB, JWT)
│   └── pom.xml                           # Descriptor Maven y dependencias
│
├── Frontend/                             # Aplicación Web con Angular 22 & TypeScript
│   ├── src/app/
│   │   ├── core/                         # Guards de rutas, interceptores HTTP, modelos y servicios
│   │   └── modules/
│   │       ├── auth/                     # Inicio de sesión y registro de usuarios
│   │       ├── client/                   # Tienda pública (Home, Catálogo, Detalle, Nuestro Café,
│   │       │                             # Nuestro Proceso, Sobre Nosotros, Carrito, Checkout, Pedidos)
│   │       ├── admin/                    # Panel administrativo (Dashboard, Productos, Pedidos, Clientes)
│   │       └── stock/                    # Panel de almacén (Dashboard, Control de Stock, Movimientos)
│   ├── public/assets/images/             # Fotografías, sellos y recursos visuales estáticos
│   └── package.json                      # Dependencias y scripts de Angular / Node.js
└── README.md
```

---

## 🛠 Tecnologías Utilizadas

### **Backend**
* **Lenguaje:** Java 21 (LTS).
* **Framework:** Spring Boot 4.1.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`).
* **Migraciones de BD:** Flyway Database Migrations con soporte para PostgreSQL.
* **Seguridad y Cifrado:** Spring Security + JWT (JSON Web Tokens) + BCrypt Password Encoder.
* **Base de Datos:** PostgreSQL 14+.
* **Herramientas de Soporte:** Project Lombok, Spring Boot DevTools, Maven Wrapper (`mvnw`).

### **Frontend**
* **Framework:** Angular 22 (Standalone Components + Signals + Router Guards + SSR con Express).
* **Lenguaje:** TypeScript 6.x / HTML5 / SCSS.
* **Estilos y Diseño:** SCSS modular con paleta artesanal inspirada en la finca cafetalera (`#304E3A`, `#8A674E`, `#F3EEE4`).
* **Gráficos & Visualizaciones:** Polígono de Intensidad Sensorial interactivo en SVG nativo (Radar Chart SCA).
* **Cliente HTTP:** Angular `HttpClient` reactivo con RxJS.

---

## 📦 Módulos y Funcionalidades

### 1. Portal del Cliente (E-commerce)

<p align="center">
  <img src="Frontend/public/assets/images/nc-paisaje-oxapampa.jpg" alt="Montañas y cafetales de Villa Rica, Oxapampa" width="100%" style="border-radius: 8px; max-height: 320px; object-fit: cover;" />
</p>

* **Home:** Hero editorial, manifiesto rústico de finca, guía interactiva del barista con calibración de métodos de extracción (V60, Prensa Francesa, Espresso, Chemex, Aeropress, Moka) y destacados de cosecha.
* **Catálogo & Filtros:** Búsqueda rápida y filtrado por molienda, variedades y tipo de café.
* **Nuestro Café:** Vista territorial de Villa Rica, microclima de bosque de niebla (12°C–18°C), suelos volcánicos y fichas de variedades emblemáticas (*Caturra*, *Typica* y *Bourbon Rosado*).
* **Sobre Nosotros:** Historia familiar, significado del nombre Valle Blanco, localización en la **Reserva de Biósfera Oxapampa-Asháninka-Yánesha (UNESCO)**, Misión, Visión, Valores y diferenciales.
* **Carrito y Checkout:** Gestión de cantidades, validación de stock disponible, resumen de compra y confirmación de pedidos.
* **Mis Pedidos & Perfil:** Historial personal de compras y estado de despacho.

---

### 🔬 Ficha Técnica SCA & Polígono Sensorial

En la vista de detalle de producto se incluye la calibración sensorial de mesa de cata certificada y el polígono interactivo de intensidad en SVG:

<p align="center">
  <img src="Frontend/public/assets/images/screenshot-ficha-tecnica.png" alt="Calibración en Mesa de Cata SCA y Polígono Sensorial" width="92%" style="border-radius: 8px; box-shadow: 0 8px 30px rgba(0,0,0,0.12);" />
</p>

* **Puntuación SCA:** Indicador de calidad (+86 Puntos) y notas de cata oficiales.
* **Barras de Atributos:** Cuerpo sedoso, acidez cítrica, dulzor a caramelo/panela y balance.
* **Polígono de Radar SVG:** Gráfico vectorial de 5 vértices que modela el perfil de taza del café seleccionado.

---

### 🌱 Nuestro Proceso Artesanal en 9 Etapas

Recorrido visual secuencial desde el cultivo en laderas andino-amazónicas hasta el empaque final:

| 01. Cultivo | 02. Cosecha Selectiva | 05. Secado en Camas |
|:---:|:---:|:---:|
| <img src="Frontend/public/assets/images/proceso-01-cultivo.jpg" width="240" style="border-radius: 6px;" /><br>**1,200–1,800 msnm** | <img src="Frontend/public/assets/images/proceso-02-cosecha.jpg" width="240" style="border-radius: 6px;" /><br>**Recolección manual 100%** | <img src="Frontend/public/assets/images/nc-secado-cafe.jpg" width="240" style="border-radius: 6px;" /><br>**Lento al sol (11% humedad)** |
| **07. Tostado en Tambor** | **08. Molienda Calibrada** | **09. Empaque Hermético** |
| <img src="Frontend/public/assets/images/proceso-07-tostado.jpg" width="240" style="border-radius: 6px;" /><br>**Micro-lotes (180–220 °C)** | <img src="Frontend/public/assets/images/proceso-08-molienda.jpg" width="240" style="border-radius: 6px;" /><br>**Muelas cónicas de precisión** | <img src="Frontend/public/assets/images/proceso-09-empaque.jpg" width="240" style="border-radius: 6px;" /><br>**Válvula desgasificadora** |

---

### 2. Panel Administrativo General (`/admin`)

<p align="center">
  <img src="Frontend/public/assets/images/screenshot-admin-productos.png" alt="Panel Administrativo de Productos" width="92%" style="border-radius: 8px; box-shadow: 0 8px 30px rgba(0,0,0,0.12);" />
</p>

* **Dashboard Gerencial:** Métricas de ventas, órdenes recientes, productos más vendidos y resumen financiero.
* **Gestión de Productos:**
  * Alta, edición y baja de productos con validaciones completas.
  * **Subida física de fotografías:** Carga multipart que almacena el archivo en el servidor y asigna la URL relativa.
  * **Eliminación en cascada:** Al borrar un producto se elimina su registro en BD y su archivo de imagen asociado en disco (respetando imágenes maestras del sistema).
  * Menús desplegables controlados para *Tipo de café*, *Molienda*, *Tueste* y *Variedad*.
* **Gestión de Pedidos:** Actualización de estados (Pendiente, Pagado, En Envío, Entregado, Cancelado).
* **Directorio de Clientes:** Consulta de usuarios registrados y actividad de compra.
* **Acceso y Retorno a Inventario:** Enlace bidireccional integrado con el panel de almacén.

---

### 3. Panel de Inventario & Almacén (`/inventario`)
* **KPIs de Almacén:** Existencias físicas totales, alertas de stock bajo y productos agotados (0 unidades).
* **Registro de Movimientos:** Entradas por lote tostado, salidas por mermas/degustación y ajustes de conteo físico en estantería.
* **Trazabilidad y Auditoría:** Cada movimiento queda registrado con usuario responsable, fecha/hora, motivo y variación de stock.
* **Gestión de Alertas:** Configuración de umbral de stock mínimo por producto.

---

## 🔐 Roles y Permisos del Sistema

El sistema implementa el **Principio de Mínimo Privilegio (PoLP)**:

| Rol | Alcance y Responsabilidades | Acceso a Rutas |
|---|---|---|
| **`ROLE_CLIENTE`** | Exploración del catálogo, compra en línea, gestión de carrito y consulta de sus pedidos propios. | `/client/**`, `/auth/**` |
| **`ROLE_ENCARGADO_STOCK`** | Operaciones exclusivas de almacén: entradas de lote, salidas por merma, ajustes físicos y alertas de stock. Sin acceso a ventas globales ni datos privados de clientes. | `/inventario/**`, `/client/**` |
| **`ROLE_ADMIN`** | Control total gerencial: ventas, catálogo, precios, clientes, pedidos, configuración y supervisión completa de inventario. | `/admin/**`, `/inventario/**`, `/client/**` |

---

## 👥 Cuentas de Prueba Preconfiguradas

Al inicializarse la base de datos mediante el seeder automático (`DataInitializer`), se crean las siguientes credenciales para pruebas:

| Rol | Correo Electrónico | Contraseña |
|---|---|---|
| **Administrador** | `admin@valleblanco.pe` | `admin123` |
| **Encargado de Stock** | `stock@valleblanco.pe` | `stock123` |
| **Cliente** | `cliente@valleblanco.pe` | `cliente123` |

---

## ⚙️ Requisitos Previos

* **Java JDK:** Versión 21 o superior (`java -version`).
* **Node.js:** Versión 20 o superior (`node -v`).
* **Gestor de paquetes:** npm o pnpm (`npm -v` o `pnpm -v`).
* **PostgreSQL:** Versión 14 o superior en ejecución en el puerto `5432`.

---

## 🗄️ Configuración de la Base de Datos

1. Abre tu cliente PostgreSQL (`psql`, pgAdmin o DBeaver).
2. Crea la base de datos del proyecto:
   ```sql
   CREATE DATABASE "ValleBlanco";
   ```
3. Verifica la configuración de conexión en [`Backend/src/main/resources/application.properties`](file:///Backend/src/main/resources/application.properties):
   ```properties
   server.port=8081
   spring.datasource.url=jdbc:postgresql://localhost:5432/ValleBlanco
   spring.datasource.username=postgres
   spring.datasource.password=TU_CONTRASEÑA
   ```
   > *Nota: Flyway creará automáticamente las tablas y `DataInitializer` insertará los roles, usuarios de prueba y los 9 cafés oficiales de especialidad.*

---

## 🚀 Instrucciones de Ejecución

### 1. Iniciar el Backend (Spring Boot)

En una terminal ubicada en la carpeta `Backend`:

```bash
cd Backend
./mvnw spring-boot:run
```

*(En Windows PowerShell o CMD usa `mvnw.cmd spring-boot:run`)*

El backend se levantará en: **`http://localhost:8081`**

### 2. Iniciar el Frontend (Angular)

En una segunda terminal ubicada en la carpeta `Frontend`:

```bash
cd Frontend
npm install
npm start
```

La aplicación web estará disponible en: **`http://localhost:4200`**

---

## 📡 Documentación de la API REST

### Autenticación (`/api/auth`)
* `POST /api/auth/login` — Autenticación con credenciales y entrega de token JWT + roles.
* `POST /api/auth/register` — Registro de nuevos clientes.

### Catálogo y Productos (`/api/products`)
* `GET /api/products` — Listar todos los cafés activos del catálogo.
* `GET /api/products/{id}` — Consultar detalle de café por identificador numérico.
* `GET /api/products/slug/{slug}` — Consultar detalle de café por slug amigable.
* `POST /api/products` — *(Solo ADMIN)* Crear un nuevo café de especialidad.
* `PUT /api/products/{id}` — *(Solo ADMIN)* Actualizar información y presentaciones de un café.
* `DELETE /api/products/{id}` — *(Solo ADMIN)* Eliminar producto y su imagen física asociada del servidor.
* `POST /api/products/upload-image` — *(Solo ADMIN)* Subir imagen multipart y almacenar en `public/assets/images/`.

### Inventario y Almacén (`/api/inventario`)
* `GET /api/inventario/dashboard` — *(ADMIN y ENCARGADO_STOCK)* Obtener métricas, existencias y alertas de stock.
* `GET /api/inventario/movimientos` — *(ADMIN y ENCARGADO_STOCK)* Consultar historial de movimientos auditados.
* `POST /api/inventario/movimientos` — *(ADMIN y ENCARGADO_STOCK)* Registrar entrada, salida o ajuste manual.
* `GET /api/inventario/alertas` — *(ADMIN y ENCARGADO_STOCK)* Listar productos con existencias por debajo del umbral mínimo.
* `PUT /api/inventario/productos/{id}/nivel-minimo` — *(ADMIN y ENCARGADO_STOCK)* Configurar umbral de stock mínimo de seguridad.

### Carrito y Pedidos (`/api/carrito`, `/api/pedidos`)
* `GET /api/carrito` — Obtener el carrito activo del usuario autenticado.
* `POST /api/carrito/items` — Agregar presentación y cantidad al carrito.
* `DELETE /api/carrito/items/{id}` — Quitar ítem del carrito.
* `POST /api/pedidos` — Generar orden a partir del carrito de compras.
* `GET /api/pedidos/mis-pedidos` — Consultar órdenes del cliente activo.
* `GET /api/admin/pedidos` — *(Solo ADMIN)* Listado y control global de órdenes de venta.

---

<p align="center">
  <img src="Frontend/public/assets/images/cup.png" alt="Taza de café de especialidad Valle Blanco" width="120" style="border-radius: 50%; box-shadow: 0 4px 20px rgba(0,0,0,0.15);" />
</p>

<p align="center">
  <strong>Valle Blanco — Café de Especialidad</strong><br>
  <em>Cultivado bajo sombra a más de 1,600 msnm en Villa Rica, provincia de Oxapampa, Perú.</em>
</p>
