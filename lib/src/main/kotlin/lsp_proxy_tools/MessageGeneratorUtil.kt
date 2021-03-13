package lsp_proxy_tools

import com.google.gson.Gson
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage

/**
 * Message generator util
 *
 * @property baseUri
 * @property currentId
 * @constructor Create empty Message generator util
 */
class MessageGeneratorUtil(public var baseUri: String, public var currentId: Int = -1) {
    private val gson = Gson()
    private val fileVersionMap = mutableMapOf<String, Int>()
    val initialized = NotificationMessage().apply {
        method = "initialized"
        params = InitializedParams().apply {
        }
    }

    /**
     * Initialized
     *
     * @return
     */
    fun initialized(): String {
        return gson.toJson(initialized)
    }

    /**
     * Did create files
     *
     * @param filePath
     * @param fileName
     * @return
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
     * Text did change
     *
     * @param filePath
     * @param content
     * @return
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
     * Text doc open
     *
     * @param filePath
     * @param content
     * @return
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
     * @param filePath
     * @return
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
     * Initialize
     *
     * @param capabilities
     * @param documentSelector
     * @return
     */
    fun initialize(capabilities: List<String>, documentSelector: String): String {
        return "{\n" +
                "    \"id\": ${++currentId},\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"method\": \"initialize\",\n" +
                "    \"params\": {\n" +
                "        \"capabilities\": {\n" +
                "            \"documentSelector\": [\n" +
                "                \"$documentSelector\"\n" +
                "            ],\n" +
                "            ${capabilities.joinToString(separator = ",\n")}" +
                "        },\n" +
                "        \"initializationOptions\": {\n" +
                "            \"workspaceFolders\": [\n" +
                "              \"$baseUri\"" +
                "            ]\n" +
                "        }\n" +
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
            addProperty("id", "${++currentId}")
        }.toString()
    }

    /**
     * Request semantic tokens for full file
     *
     * @param filePath path to the file being checked for tokens
     * @return json message as string
     */
    fun requestSemanticTokens(filePath: String): String {
        val req = RequestMessage().also {
            it.method = "textDocument/semanticTokens/full"
            it.params = TextDocumentIdentifier().apply {
                uri = "$baseUri/$filePath"
            }
        }
        return gson.toJsonTree(req).asJsonObject.apply {
            addProperty("id", "${++currentId}")
        }.toString()
    }


    /**
    * Request semantic token legend for java (java jdt language server specific)
    *
    * @return id and json message as string pair
    */
    fun javaSemanticTokenLegend(): Pair<Int, String> {
        val semanticTokenLegend = RequestMessage().also {
            it.method = "workspace/executeCommand"
            it.params = ExecuteCommandParams().apply {
                command = "java.project.getSemanticTokensLegend"
            }
        }
        return Pair(++currentId, gson.toJsonTree(semanticTokenLegend).asJsonObject.apply {
            addProperty("id","$currentId")
        }.toString())
    }

    /**
     * Request semantic tokens for full file (java jdt language server specific)
     *
     * @param filePath path to the file being checked for tokens
     * @return json message as string
     */
    fun javaSemanticTokens(filePath: String): Pair<Int,String> {
        val semanticTokens = RequestMessage().also {
            it.method = "workspace/executeCommand"
            it.params = ExecuteCommandParams().apply {
                command = "java.project.provideSemanticTokens"
                arguments = listOf("$baseUri/$filePath")
            }
        }
        return Pair(++currentId, gson.toJsonTree(semanticTokens).asJsonObject.apply {
            addProperty("id","$currentId")
        }.toString())
    }

    /**
     * Increment the current message ID. This is for making custom messages in addition to those not
     * implemented here. Messages generated by the class will automatically increment the current
     * message ID.
     */
    fun incrementMessageId(): Int {
        return ++currentId
    }

}