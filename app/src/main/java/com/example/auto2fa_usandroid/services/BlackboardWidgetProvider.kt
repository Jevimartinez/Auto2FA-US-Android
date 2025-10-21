// En: BlackboardWidgetProvider.kt
package com.example.auto2fa_usandroid.services

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.auto2fa_usandroid.MainActivity // Importa MainActivity
import com.example.auto2fa_usandroid.R

class BlackboardWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.blackboard_widget_layout)

            // 1. Crea un Intent que apunta a MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                // Añadimos una acción personalizada para que MainActivity sepa que es un widget
                action = MainActivity.ACTION_LAUNCH_FROM_WIDGET
                // Flags para que se lance como una tarea nueva
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // 2. Cambia getBroadcast por getActivity
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0, // requestCode
                launchIntent,
                pendingIntentFlags
            )


            views.setOnClickPendingIntent(R.id.widget_image_button, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}