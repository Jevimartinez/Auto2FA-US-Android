# Auto2FA-US Android

**Auto2FA-US Android** es una aplicación para dispositivos Android que **autocompleta** el segundo factor de autenticación (TOTP) en la aplicación Blackboard de la Universidad de Sevilla, evitando tener que escribir el código manualmente cada vez. Se basa en un **Servicio de Accesibilidad** que observa la pantalla y rellena automáticamente el campo de 2FA cuando lo detecta.

## Características

- **Genera** automáticamente el código TOTP (sha1, 6 dígitos, 30s).  
- **Detecta** y **rellena** el campo de 2FA en la app Blackboard, en la pantalla de login multifactor de la US.  
- **Pulsa** el botón de “Aceptar”, enviando el formulario.  
- **Almacena** el _secret_ encriptado con [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences), evitando que tengas que introducirlo de nuevo.

## Instalación de la App

### 1. Descargar e Instalar el APK

1. Ve a la sección [**Releases**](https://github.com/Jevimartinez/Auto2FA-US-Android/releases) de este repositorio.  
2. Descarga la versión más reciente del fichero **APK**.  
3. Copia ese APK a tu dispositivo Android o descárgalo directamente en el móvil.  
4. Activa la opción **“Orígenes desconocidos”** (o **“Permitir desde esta fuente”**) en los ajustes de Android, si no la tienes habilitada.  
5. Abre el APK y sigue las instrucciones de instalación.

### 2. Configurar la Aplicación
Para que la extensión pueda generar tu TOTP, necesita el **secret (en Base32)** asociado a tu usuario de la Universidad de Sevilla.

1. **Obtener el secret desde [2FA.US.ES](https://2fa.us.es/)**
   - Inicia sesión y ve a la sección **"Gestionar"**.
   - Pulsa en **"Añadir nuevo dispositivo"**.
   - Localiza y pulsa la opción para **"Mostrar"**, y luego, **"Mostrar parámetros de configuración"**.  
   - Verás tu **secret en Base32** (junto con un QR).  
   - **Copia** el secret.

2. **Introducir el secret en la aplicación**
   - **Abre** la app “Auto2FA-US".
   - Verás un **campo** para introducir tu _secret_ (en Base32). 
   - El campo se muestra como **contraseña** (con puntitos) para proteger la vista del _secret_. 
   - Introduce el **secret**
   - Pulsa **“Guardar”**.
   - Si es válido, se almacenará en `EncryptedSharedPreferences`. Si no lo es, la aplicación mostrará un **error**.

### 3. Habilitar el Servicio de Accesibilidad

Para que la app autocomplete el TOTP en Blackboard, es necesario activar el **Servicio de Accesibilidad**:

1. Ve a **Ajustes** > **Accesibilidad** en tu dispositivo.  
2. Busca “Auto2FA-US” en la lista de servicios disponibles, es posible que tengas que hacer click en `Aplicaciones descargadas`, o algún ajuste similar.  
3. Activa el servicio. Te pedirá confirmar, ya que un servicio de accesibilidad tiene altos privilegios.  
4. Listo: cuando la app de Blackboard muestre la pantalla de login multifactor (US), el servicio detectará el campo de 2FA y lo autocompletará.

## Funcionamiento

1. **Pantalla de login** en Blackboard (US).  
2. **Accesibility Service** (Auto2FA-US) detecta el campo `input2factor` cuando aparece.  
3. **Genera** el TOTP en base al _secret_ cifrado que guardaste.  
4. **Rellena** automáticamente el campo y **pulsa** el botón de “Aceptar”.  
5. Si el código es correcto, continuarás con el login normalmente.

## Riesgos y Advertencias

1. **Auto2FA y la seguridad**  
   - Con **Auto2FA-US (Android)**, tu **doble factor** se vuelve más automático.  
   - Esto **reduce** la seguridad inherente al 2FA (ya no necesitas tu móvil o app externa para generar el código).  
   - Si alguien obtiene acceso a tu dispositivo, podría autenticarse sin tu móvil.

2. **Almacenamiento del secret**  
   - El secret se **almacena cifrado** en `EncryptedSharedPreferences`.  
   - Aun así, cualquier persona con acceso **físico** al dispositivo y con permisos de desbloqueo podría acceder al contenido del teléfono.  
   - Se recomienda no usar esta app en equipos públicos o no confiables.

3. **Servicio de Accesibilidad**  
   - El servicio de accesibilidad tiene amplios permisos para leer la interfaz.  
   - Asegúrate de **confiar** en esta aplicación, pues podrá ver y actuar en la pantalla cuando se active en la app Blackboard.

## Acerca de

**Auto2FA-US Android** es una solución práctica para agilizar el login multifactor en la **app Blackboard** de la Universidad de Sevilla. Requiere **responsabilidad** al manejar tus credenciales y tu dispositivo.  

---

**Licencia**: [MIT](./LICENSE)
