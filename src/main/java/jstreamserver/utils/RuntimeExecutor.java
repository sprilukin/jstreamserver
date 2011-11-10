package jstreamserver.utils;

import java.io.Closeable;
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
     * @throws IOException If the process call fails.
     */
    public void execute(String pathToExecutable, String[] args) throws IOException {
        Runtime runtime = Runtime.getRuntime();

        synchronized (RuntimeExecutor.class) {
            runtime.addShutdownHook(shutdownHook);
        }

        process = runtime.exec(getCmdArguments(pathToExecutable, args));
        inputStream = process.getInputStream();
        outputStream = process.getOutputStream();
        errorStream = process.getErrorStream();
    }

    /**
     * Forces stoping of process execution
     */
    public void destroy() {
        closeCloseable(inputStream);
        closeCloseable(outputStream);
        closeCloseable(errorStream);
        destroyProcess(process);

        synchronized (RuntimeExecutor.class) {
            removeShutdownHook(shutdownHook);
        }
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
