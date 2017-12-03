/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Executes a command with arguments.
 */
class Command(private val logger: LogWriter) {
    var timeout = DEFAULT_TIMEOUT

    companion object {
        val DEFAULT_TIMEOUT = 10000
    }

    /**
     * Executes a given command with arguments regarding one processed file.
     *
     * @return The exit value of the command
     */
    fun execute(commandToExecute: String,
                arguments: List<String>): Int {

        try {
            val allArgs = ArrayList<String>()
            allArgs.add(commandToExecute)
            allArgs.addAll(arguments)
            val pb = ProcessBuilder(*allArgs.toTypedArray())

            val auxLog = File.createTempFile("librarian-exec-", ".log")
            pb.redirectErrorStream(true)
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(auxLog))

            val p = pb.start()
            p.waitFor(timeout.toLong(), TimeUnit.MILLISECONDS)

            val logLines = Files.readAllLines(auxLog.toPath())
            if (!logLines.isEmpty()) {
                logger.fine("- Command executed. Results: [")
            } else {
                logger.fine("- Command executed")
            }
            logLines.forEach { s -> logger.fine(": " + s) }

            if (!logLines.isEmpty()) {
                logger.fine("- ] end command execution results")
            }

            auxLog.delete()

            return if (!p.isAlive) {
                p.exitValue()
            } else -1

        } catch (e: InterruptedException) {
            logger.warning("- Error executing command '$commandToExecute': ${e.message}")
        } catch (e: IOException) {
            logger.warning("- Cannot execute command '$commandToExecute': ${e.message}")
        }

        return -1
    }

}