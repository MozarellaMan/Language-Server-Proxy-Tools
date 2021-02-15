package lsp_proxy_tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.eclipse.lsp4j.Diagnostic

class DefaultClient(private val rootUri: String, val outgoingSocket: SendChannel<Frame>, private var messageHandler: ((String, SendChannel<Frame>) -> Unit)? = null) {
    var diagnostics = emptyList<Diagnostic>()
    private var useDefaultHandler = true
    private val gson: Gson = GsonBuilder().setLenient().create()
    private val messageGeneratorUtil: MessageGeneratorUtil = MessageGeneratorUtil(rootUri)
    private var initialized = false

    suspend fun initialize(capabilities: List<String>, documentSelector: String) {
        outgoingSocket.send(Frame.Text(messageGeneratorUtil.initialize(capabilities, documentSelector)))
    }

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