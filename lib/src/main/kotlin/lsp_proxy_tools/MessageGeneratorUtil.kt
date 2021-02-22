package lsp_proxy_tools

import com.google.gson.Gson
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage

/**
 * A class for generating useful LSP messages to create basic functionality for a client.
 *
 * @property baseUri the root directory as uri used to initialise the language server
 * @property id the starting id of the messages being generated. Defaults to -1, so that the first message has an id of 0
 * @constructor Create MessageGeneratorUtil object
 */
class MessageGeneratorUtil(private var baseUri: String, private var id: Int = -1) {
    private val gson = Gson()
    private val fileVersionMap = mutableMapOf<String, Int>()
    val initialized = NotificationMessage().apply {
        method = "initialized"
        params = InitializedParams().apply {
        }
    }

    /**
     * Creates an "initialized" notification to send to the server as acknowledgment.
     *
     * @return an initialized message
     */
    fun initialized(): String {
        return gson.toJson(initialized)
    }

    /**
     * Creates a "workspace/didCreateFiles" notification to send to a server
     *
     * @param filePath
     * @param fileName
     * @return json message as string
     */
    fun didCreateFiles(filePath: String, fileName: String): String {
        val notification = NotificationMessage().apply {
            method = "workspace/didCreateFiles"
            params = CreateFilesParams().apply {
                files = listOf(FileCreate().apply {
                    uri = "$filePath/$fileName"
                })
            }
        }
        return gson.toJson(notification)
    }

    /**
     * Creates a "textDocument/didChange" notification to send to a server
     *
     * @param filePath path to the file being changed
     * @param content full content of the new files
     * @return json message as string
     */
    fun textDidChange(filePath: String, content: String): String {
        val prevVersion = fileVersionMap[filePath] ?: 0
        fileVersionMap[filePath] = prevVersion
        val notification = NotificationMessage().apply {
            method = "textDocument/didChange"
            params = DidChangeTextDocumentParams().apply {
                textDocument = VersionedTextDocumentIdentifier().apply {
                    uri = "$baseUri/$filePath"
                    version = fileVersionMap[filePath]!!
                }
                contentChanges = listOf(TextDocumentContentChangeEvent().apply {
                    range = null
                    text = content
                })
            }
        }
        return gson.toJson(notification)
    }

    /**
     * Creates a "textDocument/didOpen" notification to send to a server
     *
     * @param filePath path to the file being opened
     * @param content of the file that was opened
     * @return json message as string
     */
    fun textDocOpen(filePath: String, content: String): String {
        var versionId = fileVersionMap.getOrPut(filePath, { 0 })
        versionId++
        fileVersionMap[filePath] = versionId
        val notification = NotificationMessage().apply {
            method = "textDocument/didOpen"
            params = DidOpenTextDocumentParams().apply {
                textDocument = TextDocumentItem().apply {
                    uri = "$baseUri/$filePath"
                    languageId = "java"
                    version = versionId
                    text = content
                }
            }
        }
        return gson.toJson(notification)
    }

    /**
     * Text doc close
     *
     * @param filePath path to the file being closed
     * @return json message as string
     */
    fun textDocClose(filePath: String): String {
        var versionId = fileVersionMap.getOrPut(filePath, { 0 })
        versionId++
        fileVersionMap[filePath] = versionId
        val notification = NotificationMessage().apply {
            method = "textDocument/didClose"
            params = DidCloseTextDocumentParams().apply {
                textDocument = TextDocumentIdentifier().apply {
                    uri = "$baseUri/$filePath"
                }
            }
        }
        return gson.toJson(notification)
    }

    /**
     * Creates an "initialize" message to send to a server
     *
     * @param capabilities list of capabilities specified for the server
     * @param documentSelector identifies the scope of registration to the server
     * @return json message as string
     */
    fun initialize(capabilities: List<String>, documentSelector: String): String {
        return "{\n" +
                "    \"id\": ${++id},\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"method\": \"initialize\",\n" +
                "    \"params\": {\n" +
                "        \"capabilities\": {\n" +
                "            \"documentSelector\": [\n" +
                "                \"$documentSelector\"\n" +
                "            ],\n" +
                "            ${capabilities.joinToString(separator = ",\n")}" +
                "        },\n" +
                "        \"initialization_options\": {},\n" +
                "        \"rootUri\": \"$baseUri\"\n" +
                "    }\n" +
                "}"
    }

    /**
     * Refresh diagnostics
     *
     * @param filePath path to the file being diagnosed
     * @return json message as string
     */
    fun refreshDiagnostics(filePath: String): String {
        val refresh = RequestMessage().also {
            it.method = "workspace/executeCommand"
            it.params = ExecuteCommandParams().apply {
                command = "java.project.refreshDiagnostics"
                arguments = listOf("$baseUri/$filePath", "thisFile", true)
            }
        }
        return gson.toJsonTree(refresh).asJsonObject.apply {
            addProperty("id", "${++id}")
        }.toString()
    }
}