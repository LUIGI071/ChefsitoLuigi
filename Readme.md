*Chefsito 🍽️

Chefsito es una app web inteligente que te ayuda a llevar el control de tu despensa 
y te sugiere recetas con ayuda de la Inteligencia Artificial.  
  
Su función principal es facilitar la gestión de los ingredientes que tienes en casa, 
permitir crear un perfil culinario a tu medida y ofrecer recetas generadas automáticamente mediante IA, 
considerando tus gustos, alergias y tipo de alimentación.

*Para empezar 🚀  
Aquí dejamos los pasos necesarios para que puedas ejecutar el proyecto en tu computadora en un entorno local.

¿Qué necesitas tener antes? 📋  
*Para que todo funcione correctamente, necesitas tener instaladas las siguientes herramientas en tu equipo:

-Java JDK 21 o más reciente  
-Maven  
-Node.js (versión 18 o más nueva)  
-Angular CLI  
-PostgreSQL

Una cuenta de OpenAI y la respectiva API Key  
-(OPENAI_API_KEY)

Puedes verificar que  tienes todo instalado con los siguientes comandos:

-java -version  
-mvn -version  
-node -v  
-npm -v  
-ng version

Instalación 🔧  
A continuación se detallan los pasos necesarios para disponer de un entorno de desarrollo completamente funcional.

1. Clonar el repositorio  
   git clone https://github.com/LUIGI071/ChefsitoLuigi.git  
   cd ChefsitoLuigi

2. Configuración de la base de datos  
   Crear la base de datos en PostgreSQL:

CREATE DATABASE chefsito;

3. Configuración del backend

Acceder al directorio del backend:

-cd chefsito-backend

Configurar las siguientes variables de entorno:

-DB_HOST=localhost  
-DB_PORT=5432  
-DB_NAME=chefsito  
-DB_USER=postgres  
-DB_PASSWORD=tu_password  
-OPENAI_API_KEY=tu_api_key  
-SPRING_PROFILES_ACTIVE=local

Ejecutar el backend:

-mvn spring-boot:run

El backend quedará disponible en:

-http://localhost:8080

4. Configuración del frontend

Acceder al directorio del frontend:

-cd ../chefsito-ui

Instalar dependencias:

-npm install

Ejecutar la aplicación Angular:

-ng serve

El frontend estará disponible en:

-http://localhost:4200

Nota: si no tienes Angular CLI instalado, puedes instalarlo con:
-npm install -g @angular/cli
y comprobarlo con:
-ng version

5. Ejemplo de uso

-Acceder a la aplicación desde el navegador.  
-Registrarse como nuevo usuario.  
-Añadir ingredientes a la despensa.  
-Configurar el perfil culinario.  
-Acceder a la sección de recetas sugeridas y generar recetas mediante IA.

Arquitectura del sistema 🏗️  
Chefsito sigue una arquitectura web en tres capas.  
-El frontend, desarrollado en Angular, funciona como una aplicación SPA que gestiona la interfaz de usuario
y se comunica con el backend mediante peticiones HTTP.  
-El backend, desarrollado con Spring Boot, expone una API REST que gestiona la lógica de negocio,
la seguridad mediante JWT, la persistencia de datos y la integración con la API de OpenAI.  

-La información se almacena en una base de datos PostgreSQL.  
-El flujo principal es: Angular → Spring Boot → PostgreSQL y Angular → Spring Boot → OpenAI API.

Contenedorización con Docker 🐳  
El backend del proyecto está preparado para ejecutarse en un contenedor Docker, 
lo que facilita su despliegue en producción. El repositorio incluye un Dockerfile 
que permite construir la imagen del backend y ejecutarla configurando las variables de entorno necesarias. 
El uso de Docker garantiza portabilidad y coherencia entre los entornos de desarrollo y producción.

Entorno de producción (Render) ☁️  
El proyecto se encuentra desplegado en la plataforma cloud Render.
El backend se ejecuta como un servicio contenedorizado, 
el frontend como una aplicación web estática y la base de datos PostgreSQL como un servicio gestionado.
Las variables de entorno se configuran desde el panel de Render, 
manteniendo separados los entornos local y de producción.

Endpoints principales del backend 🔌  
Algunos de los endpoints REST más relevantes del sistema son los siguientes:  
-POST /api/auth/login para el inicio de sesión.  
-POST /api/auth/register para el registro de usuarios.  
-GET /api/pantry para obtener los ingredientes de la despensa.  
-POST /api/pantry para añadir ingredientes.  
-DELETE /api/pantry/{id} para eliminar ingredientes.  
-GET /api/recipes/suggest para la generación de recetas mediante Inteligencia Artificial.

Los endpoints protegidos requieren autenticación mediante JWT.
