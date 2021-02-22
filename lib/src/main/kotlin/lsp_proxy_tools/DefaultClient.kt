package lsp_proxy_tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.eclipse.lsp4j.Diagnostic

/**
 * A class that provides basic functionality for connecting to and receiving messages from a language server websocket.
 *
 * @property rootUri the root directory of the files being initialized with the language server
 * @property outgoingSocket the channel that is used to send messages to the websocket
 * @property messageHandler an optional callback function to add additional functionality to the default client when it receives a  message
 * @property useDefaultHandler flag to set if the default message handler should be used
 * @constructor Create a DefaultClient
 */
class DefaultClient(private val rootUri: String, val outgoingSocket: SendChannel<Frame>, private var messageHandler: ((String, SendChannel<Frame>) -> Unit)? = null) {
    var diagnostics = emptyList<Diagnostic>()
    var useDefaultHandler = true
    private val gson: Gson = GsonBuilder().setLenient().create()
    private val messageGeneratorUtil: MessageGeneratorUtil = MessageGeneratorUtil(rootUri)
    private var initialized = false

    /**
     * Initializes the language server client with the server
     *
     * @param capabilities
     * @param documentSelector
     */
    suspend fun initialize(capabilities: List<String>, documentSelector: String) {
        outgoingSocket.send(Frame.Text(messageGeneratorUtil.initialize(capabilities, documentSelector)))
    }

    /**
     * Handle an incoming message from the language server. If useDefaultHandler is set to true then the default handler
     * can handle responding to the server's initialised message, and also collecting diagnostics published by the server
     *
     * @param webSocketLspMessage string of incoming language server websocket message
     */
    suspend fun handleMessage(webSocketLspMessage: String) {
        if (useDefaultHandler) {
            defaultHandler(webSocketLspMessage)
        }
        messageHandler?.let { it(webSocketLspMessage, outgoingSocket) }
    }

    private suspend fun defaultHandler(webSocketLspMessage: String) {
        if (webSocketLspMessage.contains("language/status") && webSocketLspMessage.contains("Ready") && !initialized) {
            outgoingSocket.send(Frame.Text(gson.toJson(messageGeneratorUtil.initialized())))
            initialized = true
        }
        if (webSocketLspMessage.contains("textDocument/publishDiagnostics") && webSocketLspMessage.contains("range")) {
            val message = webSocketLspMessage.split("Content-Length:")[0]
            val diagnosticsNotification = Json.parseToJsonElement(message)
            val diagnosticsJsonArray = diagnosticsNotification.jsonObject["params"]?.jsonObject?.get("diagnostics")?.jsonArray
            val newDiagnostics = mutableListOf<Diagnostic>()
            diagnosticsJsonArray?.forEach {
                val diagnosticObj = parseDiagnosticJson(it)
                diagnosticObj.message?.let {
                    newDiagnostics.add(diagnosticObj)
                }
            }
            diagnostics = newDiagnostics
        }
    }
}