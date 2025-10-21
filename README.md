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

### 2. Ajustar Ahorro de Batería y Optimización de Dispositivo

Muchos fabricantes (sobre todo los chinos, como **Xiaomi**, **Huawei**, **OPPO**, etc.) incluyen opciones de **ahorro de energía** o **optimización** que pueden **cerrar** la app en segundo plano o al apagar la pantalla, impidiendo que la aplicación funcione correctamente.

1. Visita [**dontkillmyapp.com**](https://dontkillmyapp.com/)  
2. Selecciona el **fabricante** de tu dispositivo  
3. Sigue las instrucciones para **deshabilitar** o **ajustar** la optimización de batería. Por ejemplo, en MIUI deberás **activar** el permiso de _Autostart_ y marcar el ahorro de batería de la aplicación como **Sin restricciones**.

Si no realizas estos pasos, tu dispositivo podría **cerrar** Auto2FA-US Android, evitando que autocomplete el código de la US en la app Blackboard cuando la inicies.

### 3. Configurar la Aplicación

Para que la aplicación pueda generar tu TOTP, necesita el **secret (en Base32)** asociado a tu usuario de la Universidad de Sevilla.

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

### 4. Habilitar el Servicio de Accesibilidad

Para que la app autocomplete el TOTP en Blackboard, es necesario activar el **Servicio de Accesibilidad**:

1. Ve a **Ajustes** > **Accesibilidad** en tu dispositivo.  
2. Busca “Auto2FA-US” en la lista de servicios disponibles, es posible que tengas que hacer click en `Aplicaciones descargadas`, o algún ajuste similar.  
3. Activa el servicio. Te pedirá confirmar, ya que un servicio de accesibilidad tiene altos privilegios. En caso de que no te deje activarlo porque es un permiso restringido, debes ir a Ajustes > Aplicaciones > Auto2FA-uS > Más > Conceder permisos restringidos
4. Listo: cuando la app de Blackboard muestre la pantalla de login multifactor (US), el servicio detectará el campo de 2FA y lo autocompletará.


### 5. Widget

En caso de que la aplicación te deje de funcionar después de un tiempo (Por ejemplo, al día siguiente), puede deberse a que los ajustes de optimización de batería estén cerrando la aplicación cuando está en segundo plano. Si sigues teniendo problemas, la aplicación tiene un widget, con el logo de Blackboard. Este widget lo que hace es ejecutar primero el servicio de Auto2FA y luego ejecutar Blackboard. De este modo, aseguramos que el servicio se esté ejecutando antes de abrir la aplicación de Blackboard.

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

## Posibles Problemas y Soluciones

1. **Secret TOTP incorrecto**  
   - Asegúrate de **copiar** y **pegar** correctamente el secret en Base32 (en mayúsculas).  
   - Si no coinciden, el código TOTP será inválido y la app no podrá autocompletarlo correctamente.

2. **Servicio de Accesibilidad desactivado**  
   - Verifica en **Ajustes > Accesibilidad** que “Auto2FA-US” esté **activado**.  
   - Algunas veces, tras una actualización o un reinicio, podría desactivarse y requerir tu confirmación de nuevo.

3. **Opciones de batería y optimización**  
   - Si tu dispositivo aplica restricciones (ahorro de energía, “Autostart” desactivado, etc.), la app podría cerrarse en segundo plano y dejar de autocompletar.  
   - Revisa las instrucciones en el apartado de **Ajustar Ahorro de Batería** y [dontkillmyapp.com](https://dontkillmyapp.com/) para tu fabricante.  
   - Tras aplicar los cambios, **reinicia** el dispositivo o reinstala la app y comprueba de nuevo que el servicio de accesibilidad sigue **activado**.


## Acerca de

**Auto2FA-US Android** es una solución práctica para agilizar el login multifactor en la **app Blackboard** de la Universidad de Sevilla. Requiere **responsabilidad** al manejar tus credenciales y tu dispositivo.  

---

**Licencia**: [MIT](./LICENSE)
