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

suspend fun getFile(address: String ,path: String): String {
    val (_, _, result) = Fuel.get("http://$address/code/file/$path")
            .awaitStringResponseResult()
    return result.fold(
            { data -> data },
            { error ->
                error.response.body().toString()
            }
    )
}

suspend fun addInput(address: String,inputStrings: List<String>): String {
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

suspend fun healthCheck(address: String): String {
    val (_, _, result) = Fuel.get("http://$address/health").awaitStringResponseResult()
    return result.fold(
            { "OK ✅" },
            { error -> proxyError(error) }
    )
}