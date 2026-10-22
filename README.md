# ☕ E-commerce Café Valle Blanco

Proyecto de comercio electrónico desarrollado para la empresa cafetera **Valle Blanco**, enfocado en la exhibición y venta de cafés de especialidad peruanos de orígenes selectos.

Este proyecto ha sido desarrollado como parte del curso **Herramientas de Desarrollo**.

---

## Arquitectura del Proyecto

El sistema está estructurado como una aplicación desacoplada (Frontend SPA + Backend REST API + Base de Datos Relacional):

```
Ecomerce_CafeValleBlanco/
├── Backend/                 # API RESTful con Spring Boot & Java 21
│   ├── src/main/java/com/valleblanco/backend/
│   │   ├── config/          # Seguridad (Spring Security), CORS y Seeder inicial
│   │   ├── controller/      # Endpoints REST (ProductController)
│   │   ├── model/           # Entidades JPA (Product, Precio, NotaCata)
│   │   └── repository/      # Interfaces Spring Data JPA
│   ├── src/main/resources/  # Configuraciones de conexión (application.properties)
│   └── pom.xml              # Dependencias de Maven
│
├── Frontend/                # Aplicación Web con Angular 22 & TypeScript
│   ├── src/app/
│   │   ├── core/            # Modelos, componentes globales y servicios HTTP
│   │   └── modules/         # Módulos de clientes, catálogo y autenticación
│   └── package.json         # Dependencias y scripts de Node.js
└── README.md
```

---

## Tecnologías Utilizadas

### **Backend**

- **Lenguaje:** Java 21 (LTS)
- **Framework:** Spring Boot 4.x
- **Acceso a Datos:** Spring Data JPA / Hibernate
- **Seguridad:** Spring Security con configuración CORS
- **Base de Datos:** PostgreSQL
- **Herramientas:** Lombok, Spring Boot DevTools, Apache Maven

### **Frontend**

- **Framework:** Angular 22
- **Lenguaje:** TypeScript / HTML5 / SCSS
- **Cliente HTTP:** Angular `HttpClient` (`fetch` API)
- **Gestor de Paquetes:** npm / pnpm

---

## Requisitos Previos

Asegúrate de tener instalados los siguientes componentes en tu entorno:

1. **JDK 21** o superior (`java -version`).
2. **Node.js** v20 o superior y **npm** (`node -v`, `npm -v`).
3. **PostgreSQL** v14 o superior en ejecución en el puerto `5432`.

---

## Configuración de la Base de Datos

1. Abre tu gestor de PostgreSQL (pgAdmin, DBeaver o terminal `psql`).
2. Crea una base de datos llamada `ValleBlanco`:
   ```sql
   CREATE DATABASE "ValleBlanco";
   ```
3. Verifica o ajusta tus credenciales en [`Backend/src/main/resources/application.properties`](file:///Backend/src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/ValleBlanco
   spring.datasource.username=postgres
   spring.datasource.password=TU_CONTRASEÑA
   ```
   > _Nota: Al iniciar el Backend por primera vez, Hibernate generará automáticamente las tablas y poblará los cafés iniciales con sus notas de cata y precios mediante el seeder `DataInitializer`._

---

## Instrucciones de Ejecución

### 1. Iniciar el Backend (Spring Boot)

Abre una terminal en la carpeta `Backend`:

```bash
cd Backend
./mvnw spring-boot:run
```

_(En Windows CMD / PowerShell usa `mvnw.cmd spring-boot:run`)_

El servidor backend iniciará en: **`http://localhost:8080`**

#### Endpoints Principales:

- `GET /api/products` - Obtener catálogo completo de cafés.
- `GET /api/products/{id}` - Obtener detalle por ID.
- `GET /api/products/slug/{slug}` - Obtener café por su slug identificador.

---

### 2. Iniciar el Frontend (Angular)

Abre una segunda terminal en la carpeta `Frontend`:

```bash
cd Frontend
npm install
npm start
```

La aplicación web estará disponible en: **`http://localhost:4200`**

---

## Características Principales

- **Catálogo de Especialidad:** Visualización de cafés según variedades y notas sensoriales (chocolate amargo, miel, cítricos, frutos rojos, etc.).
- **Presentaciones y Precios Dinámicos:** Soporte para múltiples pesos (250g, 500g, 1000g) y cálculo de rangos de precio.
- **Control de Stock:** Manejo de disponibilidad por cada producto.
- **Integración Fluida:** Consumo reactivo mediante observables y fallback automático de datos.
- **Seguridad y CORS:** Canales seguros entre el cliente Angular y la API Spring Boot.

---

## Curso & Proyecto

- **Curso:** Herramientas de Desarrollo
- **Organización / Cliente:** Valle Blanco
