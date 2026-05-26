package com.app.gerenciadorcartoes.network.ssl

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Configuração SSL permissiva para uso em ambiente de desenvolvimento/treinamento.
 *
 * ⚠️ ATENÇÃO: NÃO use em produção — aceita qualquer certificado SSL, incluindo
 * certificados inválidos ou autoassinados. Isso elimina a proteção contra ataques
 * de interceptação (MITM). Use apenas em projetos de estudo ou ambientes controlados.
 */
object UnsafeSslConfig {

    /** [X509TrustManager] que aceita qualquer certificado sem validar a cadeia. */
    val trustManager: X509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    /** [SSLSocketFactory] construído a partir do [trustManager] permissivo. */
    val sslSocketFactory: SSLSocketFactory by lazy {
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        }.socketFactory
    }
}

