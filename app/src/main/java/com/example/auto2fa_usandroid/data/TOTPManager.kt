package com.example.auto2fa_usandroid.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator

object TOTPManager {

    fun generateTOTP(context: Context): String {
        // 1. Build/ open the encrypting prefs
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            "Auto2FAPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // 2. Obtain the secret
        val secret = encryptedPrefs.getString("totp_secret", "") ?: ""

        if (secret.isBlank()) {
            // There is no secret stored
            return "NO_SECRET"
        }

        return try {
            // 3. Create GoogleAuthenticator generator
            val generator = GoogleAuthenticator(secret.toByteArray(Charsets.UTF_8))

            // 4. Generate TOTP code
            val code = generator.generate()

            code.toString() // return as string
        } catch (e: Exception) {
            e.printStackTrace()
            "ERROR"
        }
    }
}
