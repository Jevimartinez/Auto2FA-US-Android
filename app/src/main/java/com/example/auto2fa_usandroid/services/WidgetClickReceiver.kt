package com.example.auto2fa_usandroid.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.auto2fa_usandroid.services.TotpAccessibilityService // Importa tu servicio

class WidgetClickReceiver : BroadcastReceiver() {

    // Define un nombre de acción único
    companion object {
        const val ACTION_LAUNCH = "com.example.auto2fa_usandroid.ACTION_LAUNCH_BLACKBOARD"
        const val BLACKBOARD_PACKAGE = "com.blackboard.android.bbstudent"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != ACTION_LAUNCH) {
            return
        }

        // 1. "Despertar" el servicio de accesibilidad
        try {
            val serviceIntent = Intent(context, TotpAccessibilityService::class.java)
            context.startService(serviceIntent)
        } catch (e: Exception) {
            // Manejar error si el servicio no puede iniciarse (raro)
            e.printStackTrace()
        }

        // 2. Lanzar la aplicación de Blackboard
        val launchIntent = context.packageManager.getLaunchIntentForPackage(BLACKBOARD_PACKAGE)
        if (launchIntent != null) {
            // IMPORTANTE: Añadir esta flag
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            // Si Blackboard no está instalado
            Toast.makeText(context, "App de Blackboard no encontrada", Toast.LENGTH_SHORT).show()
        }
    }
}