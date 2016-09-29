/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Executes a command with arguments.
 */
public class Command {

    public static final int DEFAULT_TIMEOUT = 10000;
    private final Log logger;
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * @param logger Logger to receive results.
     */
    public Command(Log logger) {

        this.logger = logger;
    }

    /**
     * Optionally set the timeout.
     *
     * @param timeout In milliseconds
     */
    public void setTimeout(int timeout) {

        this.timeout = timeout;
    }

    /**
     * Executes a given command with arguments regarding one processed file.
     *
     * @param commandToExecute The command (line) to execute
     * @param arguments        The arguments
     * @return The exit value of the command
     */
    int execute(
            String commandToExecute,
            List<String> arguments) {

        try {
            List<String> allArgs = new ArrayList<>();
            allArgs.add(commandToExecute);
            allArgs.addAll(arguments);
            ProcessBuilder pb = new ProcessBuilder(allArgs.toArray(new String[0]));

            File auxLog = File.createTempFile("librarian-exec-", ".log");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(auxLog));

            Process p = pb.start();
            p.waitFor(timeout, TimeUnit.MILLISECONDS);

            List<String> logLines = Files.readAllLines(auxLog.toPath());
            if (!logLines.isEmpty()) {
                logger.getLogger().fine("- Command executed. Results: [");
            } else {
                logger.getLogger().fine("- Command executed");
            }
            logLines.forEach(s -> {
                logger.getLogger().fine(": " + s);
            });

            if (!logLines.isEmpty()) {
                logger.getLogger().fine("- ] end command execution results");
            }

            auxLog.delete();

            if (!p.isAlive()) {
                return p.exitValue();
            }

            return -1;

        } catch (InterruptedException e) {
            logger.getLogger().warning(
                    String.format("- Error executing command '%s': %s", commandToExecute, e.getMessage()));
        } catch (IOException e) {
            logger.getLogger().warning(
                    String.format("- Cannot execute command '%s': %s", commandToExecute, e.getMessage()));
        }

        return -1;
    }
}