package lsp_proxy_tools

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test


internal class ProxyFunctionsKtTest {


    //@Test
    fun getFile() {
        val client = mockk<Client>()
        every { client.executeRequest(any()).statusCode } returns 500
        FuelManager.instance.client = client

        runBlocking {
            val result = getFile("test", "test")
            println(result)
            assert(result.startsWith("PROXY ERROR:"))
        }
    }
}