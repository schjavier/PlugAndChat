# PlugAndChat Backend

PlugAndChat es un motor de mensajería y chat multi-tenant en tiempo real desarrollado con **Spring Boot (Java 25)**. Este backend está diseñado para proveer salas de chat seguras y aisladas para múltiples organizaciones (tenants), permitiendo la comunicación interactiva entre agentes de soporte y visitantes temporales (guests) a través del protocolo **STOMP sobre WebSockets** y una **API REST**.

---

## 🏛️ Decisiones de Arquitectura y Diseño

El backend se ha estructurado siguiendo principios de **Domain-Driven Design (DDD)** y arquitectura modular, organizando el código en torno a contextos de negocio en lugar de capas puramente técnicas.

### 1. Aislamiento Multi-tenancy (Shared Database, Discriminador por Hilo)
Para garantizar el aislamiento de datos entre inquilinos sin incurrir en la complejidad de múltiples bases de datos físicas:
- Se utiliza un discriminador `tenant_id` en las tablas clave.
- El contexto del tenant se propaga en el hilo de ejecución mediante `TenantContext` (`ThreadLocal<UUID>`).
- En peticiones públicas o de autenticación inicial, el tenant se identifica mediante la cabecera HTTP `X-Tenant-ID`.
- En peticiones autenticadas, el JWT es la **Única Fuente de Verdad** (Single Source of Truth), abstrayendo el `tenantId` en sus claims para evitar ataques de manipulación o *Tenant Spoofing*.

### 2. Estructura de Módulos (Paquetes)
- **`core/`**: Configuración transversal, seguridad stateless (Spring Security, JWT) e interceptores de WebSockets.
- **`tenant/`**: Gestión y provisión de inquilinos (Tenants).
- **`identity/`**: Gestión de cuentas de usuario (`UserAccount`), almacenamiento aislado de credenciales (`Credential`) y flujo de login.
- **`messaging/`**: Dominio de negocio del chat. Contiene la gestión de salas de chat (`Room`), la persistencia de mensajes (`Message`), agentes (`Agent`), visitantes temporales (`Guest`) y las reglas de negocio en `MessagingValidations`.

### 3. Modelo de Datos (Esquema Relacional)
La base de datos relacional (migrada automáticamente por **Flyway**) cuenta con las siguientes tablas principales:
- `tenant`: Representa cada organización registrada.
- `user_account` y `credential`: Separación limpia entre el perfil del usuario de un inquilino y sus credenciales de acceso para mayor seguridad.
- `agent` y `guest`: Roles de mensajería asociados a salas de chat. Los guests no tienen cuentas en `user_account` y se manejan con identidad temporal.
- `room`: Salas de chat que asocian a un visitante con un agente y pertenecen a un inquilino específico.
- `message`: Historial persistido de los chats con aislamiento por inquilino.

---

## 🛠️ Tecnologías y Requisitos

- **Java:** Versión 25 (utiliza características modernas de la plataforma).
- **Framework:** Spring Boot 4.0.3.
- **Seguridad:** Spring Security (Stateless) + JWT (JJWT 0.12.6).
- **Comunicación en tiempo real:** Spring WebSocket + Broker STOMP.
- **Migraciones:** Flyway.
- **Base de Datos:** MySQL / H2 (desarrollo y pruebas).

---

## ⚙️ Variables de Entorno Requeridas

Asegúrate de configurar las siguientes variables de entorno antes de ejecutar la aplicación:

| Variable | Descripción | Ejemplo / Valor |
| :--- | :--- | :--- |
| `DB_CHAT_URL` | URL JDBC de la base de datos | `jdbc:mysql://localhost:3306/plugandchat_db?useSSL=false&serverTimezone=UTC` |
| `DB_USERNAME` | Usuario de la base de datos | `root` |
| `DB_PASS` | Contraseña de la base de datos | `tu_contrasena_segura` |
| `SECRET_KEY_STRING` | Clave secreta HMAC para firmar los JWT | *Una clave criptográfica segura en Base64* |

---

## 🚦 Ejecución

Para iniciar el servidor de desarrollo, ejecuta el siguiente comando en el directorio raíz del backend:

```bash
./mvnw spring-boot:run
```

*Nota: Durante el arranque inicial, Flyway creará automáticamente todas las tablas requeridas si se utiliza una base de datos limpia.*

---

## 🔌 Contratos de la API (REST & WebSocket)

### Endpoints REST Principales

#### 1. Autenticación e Inquilinos
- **`POST /tenant`**: Crea y aprovisiona un nuevo inquilino junto con su cuenta de administrador primaria.
  - *Cabecera:* `X-Tenant-ID` (UUID aleatorio sugerido para el nuevo inquilino)
- **`POST /login`**: Autenticación de usuarios (agentes y administradores).
  - *Cabecera:* `X-Tenant-ID`
  - *Retorna:* Token JWT con claims de rol y tenant.

#### 2. Gestión de Agentes y Salas
- **`POST /agents`**: Registra un nuevo agente de soporte en el tenant.
  - *Seguridad:* Requiere JWT de administrador.
- **`POST /rooms`**: Crea una nueva sala de chat para un visitante (Guest).
  - *Cabecera:* `X-Tenant-ID`
  - *Retorna:* Datos de la sala y un **JWT temporal (Guest Token)** para el visitante.
- **`POST /rooms/assign`**: Asigna un agente disponible a una sala de chat abierta.
  - *Seguridad:* Requiere JWT de agente/administrador.
- **`POST /rooms/{roomId}/close`**: Cierra la sala de chat de forma lógica.
  - *Seguridad:* Requiere JWT de agente/administrador.
- **`GET /rooms/{roomId}/messages`**: Obtiene el historial de mensajes de la sala.
  - *Seguridad:* Requiere JWT válido asociado a la sala (Guest o Agente asignado).

---

### Comunicación en Tiempo Real (WebSocket STOMP)

El endpoint para iniciar el handshake de WebSockets es `/chat`.

1. **Handshake de Conexión:**
   - La conexión debe pasar el JWT en las cabeceras de conexión STOMP (usualmente en la cabecera `Authorization: Bearer <token>`), la cual es validada por el interceptor `WebSocketJwtInterceptor`.
2. **Suscripción a Mensajes Recibidos:**
   - Los clientes deben suscribirse al canal específico de su sala:
     ```text
     /topic/tenants/{tenantId}/rooms/{roomId}
     ```
3. **Envío de Mensajes:**
   - Los mensajes se envían al prefijo de destino de la aplicación con la estructura de la sala correspondiente:
     ```text
     /app/tenants/{tenantId}/rooms/{roomId}/send
     ```
     *Payload sugerido:* `{ "content": "Hola, ¿cómo puedo ayudarte?" }`

---
