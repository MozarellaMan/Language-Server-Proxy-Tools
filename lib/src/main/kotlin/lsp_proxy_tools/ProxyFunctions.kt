package lsp_proxy_tools

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf

private fun errorMessage(error: FuelError): String {
    return "An error of type ${error.exception} happened: ${error.message}, ${error.response}"
}

private fun proxyError(error: FuelError): String {
    val errorBody = error.response.body().asString("text/html")
    val msg = if (errorBody == "(empty)") {
        "unidentified error: ${error.response.responseMessage}"
    } else {
        errorBody
    }
    return "PROXY ERROR: $msg"
}

/**
 * Gets the remote proxy's directory and file information, in a nested structure of FileNodes
 *
 * @param address of the proxy server
 * @return root FileNode or an empty FileNode if the request failed
 */
suspend fun getDirectory(address: String): FileNode {
    val (_, _, result) = Fuel.get("http://$address/code/directory")
            .awaitObjectResponseResult<FileNode>(kotlinxDeserializerOf())
    return result.fold(
            { data -> data},
            { error ->
                println(errorMessage(error))
                FileNode()
            }
    )
}

/**
 * Gets the file from the proxy server, when given a file path relative to the codebase root directory
 *
 * @param address of proxy server
 * @param path to file relative to codebase root file
 * @return the requested file from the proxy, or an error message
 */
suspend fun getFile(address: String, path: String): String {
    val (_, _, result) = Fuel.get("http://$address/code/file/$path")
            .awaitStringResponseResult()
    return result.fold(
            { data -> data },
            { error ->
                error.response.body().toString()
            }
    )
}

/**
 * Sends input to the server to send to a program when/if a program is run
 *
 * @param address of proxy server
 * @param inputStrings a list of strings to input to the user program
 * @return nothing if successful, or an error message
 */
suspend fun addInput(address: String, inputStrings: List<String>): String {
    val (_, _, result) = Fuel.post("http://$address/code/input")
            .body(inputStrings.joinToString(separator = "\n"))
            .awaitStringResponseResult()
    return result.fold(
            { "" },
            { error ->
                println(errorMessage(error))
                proxyError(error)
            }
    )
}

/**
 * Gets the root directory in the form of a URI for use with initializing the language server
 *
 * @param address of the proxy
 * @return URI directory to the codebase root, or an error message
 */
suspend fun getRootUri(address: String): String {
    val (_, _, result) = Fuel.get("http://$address/code/directory/root")
            .awaitStringResponseResult()
    return result.fold(
            { data -> data },
            { error ->
                println(errorMessage(error))
                proxyError(error)
            }
    )
}

/**
 * Attempts to run the specified file on the proxy
 *
 * @param address of the proxy server
 * @param path to the file relative to root
 * @return returns program's errors and/or output, or an error message
 */
suspend fun runFile(address: String, path: String): String {
    val (_, _, result) = Fuel.get("http://$address/code/run/$path")
            .awaitStringResponseResult()
    return result.fold(
            { data -> data },
            { error ->
                println(errorMessage(error))
                proxyError(error)
            }
    )
}

/**
 * Kill the user program running on the server (if it is running)
 *
 * @param address of the proxy
 * @return confirmation of program being killed or error message
 */
suspend fun killRunningProgram(address: String): String {
    val (_, _, result) = Fuel.get("http://$address/code/kill")
            .awaitStringResponseResult()
    return result.fold(
            { data -> data },
            { error ->
                println(errorMessage(error))
                proxyError(error)
            }
    )
}

/**
 * Health check
 *
 * @param address of the proxy
 * @return 200 if the proxy is running
 */
suspend fun healthCheck(address: String): String {
    val (_, _, result) = Fuel.get("http://$address/health").awaitStringResponseResult()
    return result.fold(
            { "OK ✅" },
            { error -> proxyError(error) }
    )
}