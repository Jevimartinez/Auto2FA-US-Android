package com.example.auto2fa_usandroid.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.auto2fa_usandroid.data.TOTPManager
import java.util.concurrent.TimeUnit

class TotpAccessibilityService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("TotpService", "onCreate()")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TotpService", "Accesibility Service Conectado")

        val info = serviceInfo
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowChange()
            }
        }
    }

    override fun onInterrupt() {
        Log.d("TotpService", "onInterrupt()")
    }

    private fun handleWindowChange() {
        val rootNode = rootInActiveWindow ?: return

        // Check node to check that we are in US auth page
        if (findNodeByViewId(rootNode, "com.blackboard.android.bbstudent:id/fragment_web_component_content_wv") == null) return
        Log.d("TotpService", "Pantalla auth US")

        // Check OTP wrong code message to prevent loops
        if (findWrongOTPCodeRecursively(rootNode) != null) return
        Log.d("TotpService", "No hay wrong OTP message")

        // Check if we are in the TOTP page
        val inputFactorNode = findInput2FactorNodeRecursively(rootNode) ?: return
        Log.d("TotpService", "Input2factor detectado")

        val totpCode = generateTotp()
        if (totpCode == "NO_CODE") return  // There is no secret or it has raised an error
        Log.d("TotpService", "Código TOTP generado")

        // Fill the TextInput
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                totpCode
            )
        }
        inputFactorNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        Log.d("TotpService", "Código TOTP introducido")

        // Search recursively the button with ID notification_2factor_button_ok
        val okButtonNode = findOkButtonNodeRecursively(rootNode) ?: return

        okButtonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        // OK button pressed and TOTP verification completed

        Log.d("TotpService", "TOTP insertado automáticamente.")
        TimeUnit.MILLISECONDS.sleep(500)  // Prevents crashes
    }

    private fun findWrongOTPCodeRecursively(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className?.contains("View", ignoreCase = true) == true) {
            val viewId = node.viewIdResourceName ?: ""
            if (viewId == "otp_authn_wrong_code") {
                return node
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findWrongOTPCodeRecursively(child)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun findInput2FactorNodeRecursively(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className?.contains("EditText", ignoreCase = true) == true) {
            val viewId = node.viewIdResourceName ?: ""
            if (viewId == "input2factor") {
                return node
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findInput2FactorNodeRecursively(child)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun findOkButtonNodeRecursively(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val viewId = node.viewIdResourceName ?: ""
        if (viewId == "notification_2factor_button_ok") {
            return node
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findOkButtonNodeRecursively(child)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun findNodeByViewId(root: AccessibilityNodeInfo, viewId: String): AccessibilityNodeInfo? {
        val list = root.findAccessibilityNodeInfosByViewId(viewId)
        return if (!list.isNullOrEmpty()) list[0] else null
    }

    private fun generateTotp(): String {
        val totpCode = TOTPManager.generateTOTP(this)
        if (totpCode != "NO_SECRET" && totpCode != "ERROR") {
            return totpCode
        }
        return "NO_CODE"
    }
}
// General note: The nodes for input2factor, notification_2factor_button_ok, and otp_authn_wrong_code
// must be searched recursively as they are inside the WebView, and their ID is not fully displayed.
// This means that the view does not have a fully qualified resource name (with the package name).
