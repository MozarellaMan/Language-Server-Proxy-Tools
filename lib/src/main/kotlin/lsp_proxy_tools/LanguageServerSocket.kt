package lsp_proxy_tools

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*

/**
 * Starts a websocket session, connecting to the language server proxy
 *
 * @param address of the language server proxy
 * @return the websocket session
 */
suspend fun StartLanguageServerSession(address: String): DefaultClientWebSocketSession {
    val client = HttpClient(CIO) {
        install(WebSockets)
    }
    val (host, port) = address.split(':', limit = 2)
    return client.webSocketSession(
            method = HttpMethod.Get,
            host = host,
            port = port.toIntOrNull() ?: 0, path = "/ls"
    )
}

