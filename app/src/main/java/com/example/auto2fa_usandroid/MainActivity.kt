package com.example.auto2fa_usandroid

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : AppCompatActivity() {

    private lateinit var secretEditText: EditText
    private lateinit var saveButton: Button

    // Regex for Base32
    private val base32Regex = Regex("^[A-Z2-7]+\$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secretEditText = findViewById(R.id.secretEditText)
        saveButton = findViewById(R.id.saveButton)

        // 1. Make inputType Password
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

        // When clicking "Save"
        saveButton.setOnClickListener {
            val rawSecret = secretEditText.text.toString().trim()

            // A. Convert to uppercase
            val uppercaseSecret = rawSecret.uppercase()

            // B. Validate Base32
            if (!base32Regex.matches(uppercaseSecret)) {
                // Show error
                Toast.makeText(
                    this,
                    "La secret no parece ser Base32. Asegúrate de copiarla correctamente.",
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
}
