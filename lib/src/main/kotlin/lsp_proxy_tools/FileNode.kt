package lsp_proxy_tools

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * FileNode is used to represent directory and file information on the remote proxy.
 * This can be used to describe and present the remote filesystem on a client if needed.
 *
 * @property path to the file
 * @property name of the file
 * @property fileType refers to the file's extension or "directory" for directories
 * @property children list of the file's children
 * @property parent the child's parent, this is null by default
 * @constructor Create empty FileNode
 */
@Serializable
data class FileNode(
        val path: String = "",
        val name: String = "",
        @SerialName("type") val fileType: String = "",
        val children: List<FileNode> = emptyList(),
        @Transient var parent: FileNode? = null
) {
    /**
     * Checks if this file is a directory or not
     *
     * @return true if the file is a directory
     */
    fun isDirectory(): Boolean {
        return this.fileType == "directory"
    }

    /**
     * Checks if this FileNode is blank or empty
     *
     * @return
     */
    fun isEmpty(): Boolean {
        return this.name.isBlank() && this.path.isBlank() && this.fileType.isBlank() && this.children.isEmpty()
    }

    /**
     * Checks if this FileNode is the root FileNode which all other FileNodes are children of.
     *
     * @return
     */
    fun isRoot(): Boolean {
        return this.parent == null
    }

    /**
     * This gets the path to the particular FileNode, relative to the FileNode passed in as an argument.
     * The FileNode passed in as an argument should normally be the root FileNode
     *
     * @param root the root FileNode of all your FileNodes
     * @return String of path to this FileNode relative to the root
     */
    fun getPath(root: FileNode): String {
        return when {
            this.path.startsWith("${root.path}/") -> {
                this.path.removePrefix("${root.path}/")
            }
            this.path == root.path -> {
                this.path
            }
            else -> {
                this.path.removePrefix("${root.path}\\")
            }
        }
    }
}