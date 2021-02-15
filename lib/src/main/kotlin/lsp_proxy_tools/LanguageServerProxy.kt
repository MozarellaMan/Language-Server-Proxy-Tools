package lsp_proxy_tools

import com.github.kittinunf.fuel.core.FuelError

class LanguageServerProxy(private val address: String) {
    private fun errorMessage(error: FuelError): String {
        return errorMessage(error)
    }

    private fun proxyError(error: FuelError): String {
        return proxyError(error)
    }

    suspend fun getDirectory(): FileNode {
        return getDirectory(address)
    }

    suspend fun getFile(path: String): String {
        return getFile(address, path)
    }

    suspend fun addInput(inputStrings: List<String>): String {
        return addInput(address, inputStrings)
    }

    suspend fun getRootUri(): String {
        return getRootUri(address)
    }

    suspend fun runFile(path: String): String {
        return runFile(address, path)
    }

    suspend fun killRunningProgram(): String {
        return killRunningProgram(address)
    }

    suspend fun healthCheck(): String {
        return healthCheck(address)
    }
}