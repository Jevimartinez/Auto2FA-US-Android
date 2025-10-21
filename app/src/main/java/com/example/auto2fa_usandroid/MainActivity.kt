package com.example.auto2fa_usandroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.auto2fa_usandroid.services.TotpAccessibilityService
import xyz.kumaraswamy.autostart.Autostart

class MainActivity : AppCompatActivity() {

    private lateinit var secretEditText: EditText
    private lateinit var saveButton: Button

    // Vistas para los avisos de permisos
    private lateinit var accessibilityWarningTextView: TextView // <-- NUEVO
    private lateinit var accessibilitySettingsButton: Button  // <-- NUEVO
    private lateinit var miuiWarningTextView: TextView
    private lateinit var autostartSettingsButton: Button
    private lateinit var batteryWarningTextView: TextView
    private lateinit var batterySettingsButton: Button

    // Regex para Base32 (sin espacios)
    private val base32Regex = Regex("^[A-Z2-7]+=*\$")

    companion object {
        // 1. Definimos la acción que usará el widget
        const val ACTION_LAUNCH_FROM_WIDGET = "com.example.auto2fa_usandroid.ACTION_LAUNCH_FROM_WIDGET"
        const val BLACKBOARD_PACKAGE = "com.blackboard.android.bbstudent"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Comprobamos si nos ha llamado el widget ANTES de hacer nada
        if (intent.action == ACTION_LAUNCH_FROM_WIDGET) {
            // ¡Nos ha llamado el widget!

            // 3. Despertamos el servicio (esto ahora es legal, estamos en una Activity)
            try {
                val serviceIntent = Intent(this, TotpAccessibilityService::class.java)
                startService(serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Lanzamos Blackboard
            val launchIntent = packageManager.getLaunchIntentForPackage(BLACKBOARD_PACKAGE)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "App de Blackboard no encontrada", Toast.LENGTH_SHORT).show()
            }

            // 5. Cerramos esta Activity invisible y salimos de onCreate
            finish()
            return
        }

        // --- Carga normal de la app ---
        setContentView(R.layout.activity_main)

        // Vistas de la App
        secretEditText = findViewById(R.id.secretEditText)
        saveButton = findViewById(R.id.saveButton)

        // Vistas de Permisos
        accessibilityWarningTextView = findViewById(R.id.accessibilityWarningTextView) // <-- NUEVO
        accessibilitySettingsButton = findViewById(R.id.accessibilitySettingsButton)  // <-- NUEVO
        miuiWarningTextView = findViewById(R.id.miuiWarningTextView)
        autostartSettingsButton = findViewById(R.id.autostartSettingsButton)
        batteryWarningTextView = findViewById(R.id.batteryWarningTextView)
        batterySettingsButton = findViewById(R.id.batterySettingsButton)


        // Mostrar la clave como contraseña
        secretEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val encryptedPrefs = try {
            // ... (Tu código de EncryptedSharedPreferences - sin cambios) ...
            // Crear clave maestra
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Crear preferencias cifradas
            EncryptedSharedPreferences.create(
                this,
                "Auto2FAPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Si el archivo cifrado anterior no puede descifrarse (clave rota),
            // se elimina y se vuelve a crear limpio
            e.printStackTrace()
            applicationContext.deleteSharedPreferences("Auto2FAPrefs")

            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                this,
                "Auto2FAPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }


        // Cargar secret existente
        val savedSecret = encryptedPrefs.getString("totp_secret", "")
        if (!savedSecret.isNullOrEmpty()) {
            secretEditText.setText(savedSecret)
        }

        // --- INICIO DE LÓGICA DE PERMISOS ---
        // 1. Comprobamos la accesibilidad PRIMERO
        checkAccessibility()
        // --- FIN DE LÓGICA DE PERMISOS ---


        // Guardar secret
        saveButton.setOnClickListener {
            // ... (Tu código de saveButton - sin cambios) ...
            val rawSecret = secretEditText.text.toString().trim()
            val uppercaseSecret = rawSecret.uppercase()

            // Validar Base32
            if (!base32Regex.matches(uppercaseSecret)) {
                Toast.makeText(
                    this,
                    "El secret parece no ser Base32. Asegúrate de copiarla correctamente.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            encryptedPrefs.edit()
                .putString("totp_secret", uppercaseSecret)
                .apply()

            Toast.makeText(this, "Secret guardada correctamente.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Comprueba si el servicio de accesibilidad está activado.
     * Muestra/oculta los avisos correspondientes.
     */
    private fun checkAccessibility() {
        if (!isAccessibilityServiceEnabled()) {
            // --- SERVICIO DESACTIVADO: Mostrar aviso y botón ---
            accessibilityWarningTextView.visibility = View.VISIBLE
            accessibilitySettingsButton.visibility = View.VISIBLE

            // Ocultamos los otros avisos para no molestar
            batteryWarningTextView.visibility = View.GONE
            batterySettingsButton.visibility = View.GONE
            miuiWarningTextView.visibility = View.GONE
            autostartSettingsButton.visibility = View.GONE

            // Asignar el click para ir a Ajustes
            accessibilitySettingsButton.setOnClickListener {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        } else {
            // --- SERVICIO ACTIVADO: Ocultar aviso ---
            accessibilityWarningTextView.visibility = View.GONE
            accessibilitySettingsButton.visibility = View.GONE

            // Ahora que sabemos que el permiso principal está, comprobamos los secundarios
            checkBatteryOptimizations()
            checkMIUIAutostart()
        }
    }

    /**
     * Comprueba si la app tiene permisos para ignorar las optimizaciones de batería.
     * (Esta función solo se llama si la accesibilidad ya está activada)
     */
    @SuppressLint("BatteryLife")
    private fun checkBatteryOptimizations() {
        // ... (Tu función checkBatteryOptimizations - sin cambios) ...
        // Este ajuste solo existe desde Android M (API 23) en adelante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringOptimizations = pm.isIgnoringBatteryOptimizations(packageName)

            if (!isIgnoringOptimizations) {
                // Si la app está siendo optimizada, mostramos el aviso y el botón
                batteryWarningTextView.visibility = View.VISIBLE
                batterySettingsButton.visibility = View.VISIBLE

                batterySettingsButton.setOnClickListener {
                    try {
                        val intent = Intent()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            // Intento 1 (Android 9+): Va directo a la config de batería de la app.
                            // Ideal para Samsung One UI y Pixel.
                            intent.action = "android.settings.MANAGE_BACKGROUND_USAGE"
                            intent.data = Uri.parse("package:$packageName")
                        } else {
                            // Intento 2 (Android 6-8): Muestra el diálogo del sistema.
                            // Ahora debería funcionar al tener el permiso en el Manifest.
                            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            intent.data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Fallback (Si todo falla, p.ej. el Intento 1 falla en Android 9)
                        // Lo mandamos a la pantalla de "Ajustes de la Aplicación".
                        e.printStackTrace()
                        try {
                            // Intentamos el fallback más universal primero
                            val intentFallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intentFallback.data = Uri.parse("package:$packageName")
                            startActivity(intentFallback)
                        } catch (e2: ActivityNotFoundException) {
                            // Si ni eso funciona (extremadamente raro)
                            Toast.makeText(
                                this,
                                "No se pudieron abrir los ajustes.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } else {
                // Si ya tiene el permiso, ocultamos todo
                batteryWarningTextView.visibility = View.GONE
                batterySettingsButton.visibility = View.GONE
            }
        }
    }

    /**
     * Comprueba el autostart de MIUI.
     * (Esta función solo se llama si la accesibilidad ya está activada)
     */
    private fun checkMIUIAutostart() {
        if (isMIUIDevice()) {
            try {
                // Usamos la librería SOLO para comprobar el estado
                val enabled: Boolean = Autostart.getSafeState(applicationContext)

                if (!enabled) {
                    showAutostartDisabledMessage() // Muestra el texto de aviso
                    autostartSettingsButton.visibility = View.VISIBLE // Muestra el botón

                    autostartSettingsButton.setOnClickListener {
                        try {
                            // Intento específico para MIUI
                            val intent = Intent("miui.intent.action.OP_AUTO_START")
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            startActivity(intent)
                        } catch (e: Exception) {
                            // Si falla (no es MIUI o da error), vamos a los ajustes
                            // generales de la app, donde el usuario puede buscar "Autostart"
                            e.printStackTrace()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Detecta si el dispositivo es un Xiaomi (MIUI)
     */
    private fun isMIUIDevice(): Boolean {
        // ... (Tu función isMIUIDevice - sin cambios) ...
        val manufacturer = Build.MANUFACTURER
        return manufacturer.equals("Xiaomi", ignoreCase = true)
    }

    /**
     * Muestra el texto de advertencia para MIUI (el botón se gestiona en onCreate)
     */
    @SuppressLint("SetTextI1n")
    private fun showAutostartDisabledMessage() {
        // ... (Tu función showAutostartDisabledMessage - sin cambios) ...
        miuiWarningTextView.visibility = View.VISIBLE
        miuiWarningTextView.text = """
        Atención: Tu dispositivo Xiaomi puede cerrar Auto2FA-US cuando está en segundo plano si Autostart está desactivado. 
        
        Esto impide que la app funcione correctamente cuando se apaga la pantalla o se cierra la aplicación.
        
        Por favor, habilita el autostart en los ajustes de tu dispositivo y sigue los pasos de https://dontkillmyapp.com/
        para asegurar que la aplicación siga activa.
    """.trimIndent()
    }

    /**
     * Volvemos a comprobar los permisos cuando el usuario vuelve a la app,
     * por si los ha activado en los ajustes.
     */
    override fun onResume() {
        super.onResume()
        // Re-comprobar permisos cuando la app vuelve a primer plano
        // Esto refrescará la UI si el usuario acaba de activar la accesibilidad
        checkAccessibility()
    }

    /**
     * Comprueba si el servicio de accesibilidad está habilitado en los Ajustes del sistema.
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        // ... (Tu función isAccessibilityServiceEnabled - sin cambios) ...
        val service = "$packageName/${TotpAccessibilityService::class.java.canonicalName}"
        try {
            val accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
                    mStringColonSplitter.setString(settingValue)
                    while (mStringColonSplitter.hasNext()) {
                        val accessibilityService = mStringColonSplitter.next()
                        if (accessibilityService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

}