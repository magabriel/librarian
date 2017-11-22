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
class Command(private val logger: Log) {
    public var timeout = DEFAULT_TIMEOUT

    /**
     * Executes a given command with arguments regarding one processed file.
     *
     * @param commandToExecute The command (line) to execute
     * @param arguments        The arguments
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
                logger.logger.fine("- Command executed. Results: [")
            } else {
                logger.logger.fine("- Command executed")
            }
            logLines.forEach { s -> logger.logger.fine(": " + s) }

            if (!logLines.isEmpty()) {
                logger.logger.fine("- ] end command execution results")
            }

            auxLog.delete()

            return if (!p.isAlive) {
                p.exitValue()
            } else -1

        } catch (e: InterruptedException) {
            logger.logger.warning(
                    String.format("- Error executing command '%s': %s", commandToExecute,
                                  e.message))
        } catch (e: IOException) {
            logger.logger.warning(
                    String.format("- Cannot execute command '%s': %s", commandToExecute, e.message))
        }

        return -1
    }

    companion object {

        val DEFAULT_TIMEOUT = 10000
    }
}