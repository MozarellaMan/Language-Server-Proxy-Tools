package lsp_proxy_tools

import com.github.kittinunf.fuel.core.FuelError
import io.ktor.http.cio.websocket.*

/**
 * Creates a Language Server Proxy access object.
 * This object wraps all possible requests to the language server proxy
 *
 * @property address of the proxy
 * @constructor Create a Language Server Proxy access object
 */
class LanguageServerProxy(private val address: String) {
    private fun errorMessage(error: FuelError): String {
        return errorMessage(error)
    }

    private fun proxyError(error: FuelError): String {
        return proxyError(error)
    }

    /**
     * Get directory information of the proxy server's codebase
     *
     * @return root FileNode of the working directory or empty FileNode
     */
    suspend fun getDirectory(): FileNode {
        return getDirectory(address)
    }

    /**
     * Get file from the proxy server
     *
     * @param path relative to root file
     * @return contents of file or error message
     */
    suspend fun getFile(path: String): String {
        return getFile(address, path)
    }

    /**
     * Sends input to the server to send to a program when/if a program is run
     *
     * @param inputStrings a list of strings to input to the user program
     * @return nothing if successful, or an error message
     */
    suspend fun addInput(inputStrings: List<String>): String {
        return addInput(address, inputStrings)
    }

    /**
     * Get root uri of the proxy's codebase
     *
     * @return root uri or error message
     */
    suspend fun getRootUri(): String {
        return getRootUri(address)
    }

    /**
     * Run a file on the proxy server
     *
     * @param path to file relative to root
     * @return outputs of execution (including errors) or error message
     */
    suspend fun runFile(path: String): DefaultWebSocketSession {
        return runFile(address, path)
    }

    /**
     * Kill the user program running on the server (if it is running)
     *
     * @return confirmation of program being killed or error message
     */
    suspend fun killRunningProgram(): String {
        return killRunningProgram(address)
    }

    /**
     * Health check
     *
     * @return OK if the server is running, error message other wise
     */
    suspend fun healthCheck(): String {
        return healthCheck(address)
    }
}