/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.utils

import java.io.File
import java.util.*

/**
 * Kotlin does not support static extensions to Java Classes. They will be defined here.
 */
object FileUtils {

    /**
     * Retrieve all subfolders in certain folders without including them.
     */
    fun findSubfolders(folders: List<File>): List<File> {
        val subFolders = mutableListOf<File>()

        for (folder in folders) {
            val subfolders = folder.absoluteFile.walkTopDown().filter {
                it.isDirectory && it.absolutePath != folder.absolutePath
            }.toList()
            subFolders.addAll(subfolders)
        }
        return subFolders
    }

    /**
     * Retrieve all subfolders in certain folders as a tree.
     */
    fun findSubfoldersTree(folders: List<File>): Map<File, List<File>> {
        val subFolders = LinkedHashMap<File, List<File>>()

        for (folder in folders) {
            val subfolders = folder.absoluteFile.walkTopDown().filter {
                it.isDirectory && it.absolutePath != folder.absolutePath
            }.toList()
            subFolders.put(folder, subfolders)
        }
        return subFolders
    }

    @JvmName("findSubfoldersTreeFromPaths")
    fun findSubfoldersTree(folderPaths: List<String>): Map<File, List<File>> {
        return findSubfoldersTree(folderPaths.map { File(it) })
    }
}
