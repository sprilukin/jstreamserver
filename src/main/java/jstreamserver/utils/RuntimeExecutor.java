/*
 * Copyright (c) 2011 Sergey Prilukin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jstreamserver.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility method which allows to wrap execution of external binary code
 * and provide input stream and output stream of underlaying process.
 *
 * @author Sergey Prilukin
 */
public final class RuntimeExecutor {

    private InputStream inputStream;
    private OutputStream outputStream;
    private InputStream errorStream;
    private Process process;
    private Thread shutdownHook = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (RuntimeExecutor.class) {
                destroyProcess(process);
                closeCloseable(inputStream);
                closeCloseable(outputStream);
                closeCloseable(errorStream);
            }
        }
    });


    /**
     * Executes the given binary with the given arguments.
     *
     * @param pathToExecutable path to executable file
     * @param args commant-line arguments
     * @param envp environment variables array
     * @param homeDir home dir of process
     * @throws IOException If the process call fails.
     */
    public void execute(String pathToExecutable, String[] args, String[] envp, File homeDir) throws IOException {
        Runtime runtime = Runtime.getRuntime();

        synchronized (RuntimeExecutor.class) {
            runtime.addShutdownHook(shutdownHook);
        }

        process = runtime.exec(getCmdArguments(pathToExecutable, args), envp, homeDir);
        inputStream = process.getInputStream();
        outputStream = process.getOutputStream();
        errorStream = process.getErrorStream();
    }

    /**
     * Same as {@link #execute(String, String[], String[], File)}
     * but home dir is not set
     *
     * @param pathToExecutable path to executable file
     * @param args commant-line arguments
     * @param envp environment variables array
     * @throws IOException If the process call fails.
     */
    public void execute(String pathToExecutable, String[] args, String[] envp) throws IOException {
        execute(pathToExecutable, args, envp, null);
    }

    /**
     * Same as {@link #execute(String, String[], String[])}
     * but home environment variables are not set
     *
     * @param pathToExecutable path to executable file
     * @param args commant-line arguments
     * @throws IOException If the process call fails.
     */
    public void execute(String pathToExecutable, String[] args) throws IOException {
        execute(pathToExecutable, args, null, null);
    }

    /**
     * Suspend current thread untill process execution will be finished
     * @throws InterruptedException if thread was interrupted
     */
    public void waitFor() throws InterruptedException {
        if (process != null) {
            process.waitFor();
        }
    }

    /**
     * Returns exit code of process
     *
     * @return exit code or process
     */
    public int getExitCode() {
        if (process != null) {
            return process.exitValue();
        }

        return 0;
    }

    /**
     * Forces stoping of process execution
     */
    public void destroy() {
        destroyProcess(process);
        synchronized (RuntimeExecutor.class) {
            removeShutdownHook(shutdownHook);
        }

        closeCloseable(inputStream);
        closeCloseable(outputStream);
        closeCloseable(errorStream);
    }

    /**
     * Returns a stream for reading from the process standard output.
     *
     * @return A stream for reading from the process standard output.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns a stream for writing to process standard input.
     *
     * @return A stream for writing to to the process standard input.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Returns a stream for reading from the process standard error input.
     *
     * @return A stream for reading from the process standard error input.
     */
    public InputStream getErrorStream() {
        return errorStream;
    }

    private static String[] getCmdArguments(String pathToExecutable, String[] args) {
        String[] notNullArgs = args != null ? args : new String[0];

        String[] cmd = new String[notNullArgs.length + 1];
        cmd[0] = pathToExecutable;
        System.arraycopy(args, 0, cmd, 1, notNullArgs.length);

        return cmd;
    }

    private static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void removeShutdownHook(Thread shutdownHook) {
        Runtime runtime = Runtime.getRuntime();
        runtime.removeShutdownHook(shutdownHook);
    }

    private static void destroyProcess(Process process) {
        if (process != null) {
            process.destroy();
        }
    }
}
