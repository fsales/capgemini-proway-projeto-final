package com.app.gerenciadorcartoes.network.mastra

import android.os.Build
import com.app.gerenciadorcartoes.BuildConfig

object MastraBaseUrlProvider {

    fun resolveBaseUrl(): String =
        if (isProbablyEmulator()) {
            BuildConfig.MASTRA_EMULATOR_BASE_URL
        } else {
            BuildConfig.MASTRA_DEVICE_BASE_URL
        }.ensureTrailingSlash()

    private fun isProbablyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model       = Build.MODEL.lowercase()
        val hardware    = Build.HARDWARE.lowercase()
        val product     = Build.PRODUCT.lowercase()

        return "generic" in fingerprint ||
            "unknown" in fingerprint ||
            "emulator" in model ||
            "android sdk built for" in model ||
            hardware == "goldfish" ||
            hardware == "ranchu" ||
            "sdk" in product
    }

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}
