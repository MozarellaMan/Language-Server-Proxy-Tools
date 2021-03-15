package lsp_proxy_tools
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.Test


class MessageGeneratorTest {

    private val testBaseUri = "file:///test"
    private val testFile = "src/Hello.java"
    private val testNewFile = "New.java"
    private val testSource = "Hello World!\n New line\n Print(50); int { do };\n\r"
    private val testLangId = "java"
    private val messageGenerator = MessageGeneratorUtil(baseUri = testBaseUri, languageId = testLangId)

    @Test
    fun didCreateFiles() {
        val expected = "{\n" +
                "   \"method\":\"workspace/didCreateFiles\",\n" +
                "   \"params\":{\n" +
                "      \"files\":[\n" +
                "         {\n" +
                "            \"uri\":\"$testBaseUri/$testNewFile\"\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"jsonrpc\":\"2.0\"\n" +
                "}"

        val actual = messageGenerator.didCreateFiles(testBaseUri, testNewFile)

        JSONAssert.assertEquals(expected, actual, false)
    }

    @Test
    fun didOpen() {
        val expected = "{\n" +
                "   \"method\":\"textDocument/didOpen\",\n" +
                "   \"params\":{\n" +
                "      \"textDocument\":{\n" +
                "         \"languageId\":\"$testLangId\",\n" +
                "         \"text\":\"$testSource\",\n" +
                "         \"uri\":\"$testBaseUri/$testFile\",\n" +
                "         \"version\":1\n" +
                "      }\n" +
                "   },\n" +
                "   \"jsonrpc\":\"2.0\"\n" +
                "}"

        val actual = messageGenerator.textDocOpen(testFile, testSource)

        JSONAssert.assertEquals(expected, actual, false)
    }

    @Test
    fun didClose() {
        val expected = "{\n" +
                "   \"method\":\"textDocument/didClose\",\n" +
                "   \"params\":{\n" +
                "      \"textDocument\":{\n" +
                "         \"uri\":\"$testBaseUri/$testFile\"\n" +
                "      }\n" +
                "   },\n" +
                "   \"jsonrpc\":\"2.0\"\n" +
                "}"

        val actual = messageGenerator.textDocClose(testFile)

        JSONAssert.assertEquals(expected, actual, false)
    }

    @Test
    fun didChange() {
        val messageGenerator = MessageGeneratorUtil(testBaseUri)
        val expected = "{\n" +
                "   \"method\":\"textDocument/didChange\",\n" +
                "   \"params\":{\n" +
                "      \"contentChanges\":[\n" +
                "         {\n" +
                "            \"text\":\"$testSource\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"textDocument\":{\n" +
                "         \"version\":0,\n" +
                "         \"uri\":\"$testBaseUri/$testFile\"\n" +
                "      }\n" +
                "   },\n" +
                "   \"jsonrpc\":\"2.0\"\n" +
                "}"

        val actual = messageGenerator.textDidChange(testFile, testSource)
        JSONAssert.assertEquals(expected, actual, false)
    }

    @Test
    fun javaRefreshDiagnostics() {
        val messageGenerator = MessageGeneratorUtil(testBaseUri)
        val expected = "{\n" +
                "   \"method\":\"workspace/executeCommand\",\n" +
                "   \"params\":{\n" +
                "      \"arguments\":[\n" +
                "         \"$testBaseUri/$testFile\",\n" +
                "         \"thisFile\",\n" +
                "         true\n" +
                "      ],\n" +
                "      \"command\":\"java.project.refreshDiagnostics\"\n" +
                "   },\n" +
                "   \"jsonrpc\":\"2.0\",\n" +
                "   \"id\":\"0\"\n" +
                "}"
        val actual = messageGenerator.refreshDiagnostics(testFile)
        JSONAssert.assertEquals(expected, actual, false)
    }

    @Test
    fun initialize() {
        val messageGenerator = MessageGeneratorUtil(testBaseUri)
        val expected = "{\n" +
                "   \"id\":0,\n" +
                "   \"jsonrpc\":\"2.0\",\n" +
                "   \"method\":\"initialize\",\n" +
                "   \"params\":{\n" +
                "      \"capabilities\":{\n" +
                "         \"documentSelector\":[\n" +
                "            \"$testLangId\"\n" +
                "         ],\n" +
                "         \"hoverProvider\":\"true\",\n" +
                "         \"semanticTokensProvider\":\"true\"\n" +
                "      },\n" +
                "      \"initializationOptions\":{\n" +
                "         \"workspaceFolders\":[\n" +
                "            \"$testBaseUri\"\n" +
                "         ]\n" +
                "      }\n" +
                "   }\n" +
                "}"

        val actual = messageGenerator.initialize(listOf("\"hoverProvider\" : \"true\"", "\"semanticTokensProvider\" : \"true\""),testLangId)
        JSONAssert.assertEquals(expected, actual, false)
    }
}