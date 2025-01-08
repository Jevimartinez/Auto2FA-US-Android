package com.example.auto2fa_usandroid

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import xyz.kumaraswamy.autostart.Autostart


class MainActivity : AppCompatActivity() {

    private lateinit var secretEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var miuiWarningTextView: TextView

    // Regex for Base32
    private val base32Regex = Regex("^[A-Z2-7]+\$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secretEditText = findViewById(R.id.secretEditText)
        saveButton = findViewById(R.id.saveButton)
        miuiWarningTextView = findViewById(R.id.miuiWarningTextView)

        // 1. Make the EditText appear as password
        secretEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // 2. Build masterKey for EncryptedSharedPreferences
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // 3. Create/Link encrypting prefs
        val encryptedPrefs = EncryptedSharedPreferences.create(
            this,
            "Auto2FAPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // 4. Load existing secret (If it exists)
        val savedSecret = encryptedPrefs.getString("totp_secret", "")
        if (!savedSecret.isNullOrEmpty()) {
            secretEditText.setText(savedSecret)
        }

        // 5. Power management fixes

        // A. Check if device is MIUI (manufacturer = "Xiaomi") and handle Autostart
        if (isMIUIDevice()) {
            try {
                val enabled: Boolean = Autostart.getSafeState(applicationContext)
                if (!enabled) {
                    showAutostartDisabledMessage()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Check if the device is from Huawei/Honor and handle wakelock tag

        var tag = "com.example.auto2fa_usandroid:LOCK"

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && Build.MANUFACTURER == "Huawei") {
            tag = "LocationManagerService"
        }

        val wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(1, tag)
        if (!wakeLock.isHeld) { wakeLock.acquire() }


        // 6. Save button
        saveButton.setOnClickListener {
            val rawSecret = secretEditText.text.toString().trim()

            // A. Convert to uppercase
            val uppercaseSecret = rawSecret.uppercase()

            // B. Validate Base32
            if (!base32Regex.matches(uppercaseSecret)) {
                // Show error
                Toast.makeText(
                    this,
                    "La secret parece no ser Base32. Asegúrate de copiarla correctamente.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // C. Save in EncryptedSharedPreferences
            encryptedPrefs.edit()
                .putString("totp_secret", uppercaseSecret)
                .apply()

            Toast.makeText(this, "Secret guardada correctamente.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Check if the device manufacturer is Xiaomi (MIUI).
     */
    private fun isMIUIDevice(): Boolean {
        val manufacturer = android.os.Build.MANUFACTURER
        return manufacturer.equals("Xiaomi", ignoreCase = true)
    }

    /**
     * Show a message in the TextView about Autostart being disabled,
     * with a link to dontkillmyapp.com (autoLink="web").
     */
    @SuppressLint("SetTextI18n")
    private fun showAutostartDisabledMessage() {
        miuiWarningTextView.visibility = View.VISIBLE
        miuiWarningTextView.text = """
            Atención: Tu dispositivo Xiaomi cerrará Auto2FA-US cuando está en segundo plano si Autostart está desactivado. Esto impide que la app funcione correctamente cuando se apaga la pantalla o cierra la aplicación.

            Por favor, habilita el autostart en los ajustes de tu dispositivo y sigue los pasos de https://dontkillmyapp.com/ para asegurar que la aplicación siga activa.
            """.trimIndent()
    }
}
