package lsp_proxy_tools

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import kotlin.test.Test
import kotlin.test.assertEquals


internal class LSPUtilKtTest {

    @Test
    fun parseDiagnosticJson() {
    }

    @Test
    fun lspRangesToStringIndices() {
        val input = "Hello\nBye"
        val ranges = listOf(
            Range(Position(0,1), Position(0,3)),
            Range(Position(1,0), Position(1,2))
        )
        val expected = listOf(
            Pair(1,3),
            Pair(6,8)
        )

        assertEquals(expected, lspRangesToStringIndices(ranges, input))
    }
}