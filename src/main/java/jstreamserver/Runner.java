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

package jstreamserver;


import anhttpserver.DefaultSimpleHttpServer;
import anhttpserver.SimpleHttpServer;
import jstreamserver.http.StreamServerHandler;
import jstreamserver.utils.Config;
import jstreamserver.utils.EncodingUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * entry point for <b>jstreamserver</b>
 *
 * @author Sergey Prilukin
 */
public final class Runner {
    public static final String SYNTAX = "java -jar jstreamserver.jar";
    public static final String HEADER = "Jstreamserver simple static content HTTP server, version 0.1";
    public static final String FOOTER = "--END--";

    private static final Object monitor = new Object();

    private static Options options;
    static {

        //Server port
        Option port = new Option("p", "port", true, "Server port. By default: 8888");
        port.setArgs(1);
        port.setOptionalArg(false);
        port.setArgName("port");
        port.setRequired(false);

        //Server host
        Option host = new Option("h", "host", true, "Server host. By default: 0.0.0.0");
        host.setArgs(1);
        host.setOptionalArg(false);
        host.setArgName("host");
        host.setRequired(false);

        //Max threads count
        Option threads = new Option("t", "threads", true, "Max threads count. By default: 10");
        threads.setArgs(1);
        threads.setOptionalArg(false);
        threads.setArgName("threadsCount");
        threads.setRequired(false);

        //Path to mime config
        Option mime = new Option("m", "mime", true, "Path to MIME properties config file. Internal config will be used if not specified");
        mime.setArgs(1);
        mime.setOptionalArg(false);
        mime.setArgName("pathToFile");
        mime.setRequired(false);

        //Response buffer size
        Option bufferSize = new Option("b", "buffer", true, "Max size of response buffer in bytes. By default: 1024 * 1024 (1Mb)");
        bufferSize.setArgs(1);
        bufferSize.setOptionalArg(false);
        bufferSize.setArgName("bufferSize");
        bufferSize.setRequired(false);

        //FFmppeg executble location
        Option ffmpegLocation = new Option("ff", "ffmpeg", true, "Full path to ffmpeg executable. By default not set");
        ffmpegLocation.setArgs(1);
        ffmpegLocation.setOptionalArg(false);
        ffmpegLocation.setArgName("ffmpegLocation");
        ffmpegLocation.setRequired(false);

        //FFmppeg executble location
        Option ffmpegParams = new Option("fp", "ffmpegp", true, "Params for ffmpeg.");
        ffmpegParams.setArgs(1);
        ffmpegParams.setOptionalArg(false);
        ffmpegParams.setArgName("ffmpegParams");
        ffmpegParams.setRequired(false);

        //FFmppeg executble location
        Option segmenterocation = new Option("se", "segmenter", true, "Full path to segmenter executable. By default not set");
        segmenterocation.setArgs(1);
        segmenterocation.setOptionalArg(false);
        segmenterocation.setArgName("segmenterLocation");
        segmenterocation.setRequired(false);

        //FFmppeg executble location
        Option segmenterParams = new Option("sp", "segmenterp", true, "Params for segmenter.");
        segmenterParams.setArgs(1);
        segmenterParams.setOptionalArg(false);
        segmenterParams.setArgName("segmenterParams");
        segmenterParams.setRequired(false);

        //Root locations
        Option locations = new Option("f", "folders", true, "List of folders which will be shown in root.");
        locations.setArgs(Option.UNLIMITED_VALUES);
        locations.setOptionalArg(false);
        locations.setArgName("\"path1=label1\" \"path2=label2\" ...");
        locations.setRequired(true);

        //Create options
        options = new Options();
        options.addOption(port);
        options.addOption(host);
        options.addOption(threads);
        options.addOption(mime);
        options.addOption(bufferSize);
        options.addOption(ffmpegLocation);
        options.addOption(ffmpegParams);
        options.addOption(segmenterocation);
        options.addOption(segmenterParams);
        options.addOption(locations);
    }

    private void start(Config config) {
        System.out.print(config.toString());

        SimpleHttpServer server = new DefaultSimpleHttpServer();
        server.setMaxThreads(config.getMaxThreads());
        server.setHost(config.getHost());
        server.setPort(config.getPort());
        server.setBufferSize(config.getBufferSize());

        server.addHandler("/", new StreamServerHandler(config));
        server.start();
    }

    /**
     * Create {@link Config} instance from passed params
     *
     * @param args command-line parameters
     * @return instance of {@link Config}
     */
    private static Config getConfig(String[] args) throws ParseException {
        CommandLine commandLine = parseCommandLine(options, args);
        Config config = new Config();
        if (commandLine.hasOption("p")) {
            config.setPort(Integer.parseInt(commandLine.getOptionValue("p")));
        }

        if (commandLine.hasOption("h")) {
            config.setHost(commandLine.getOptionValue("h"));
        }

        if (commandLine.hasOption("t")) {
            config.setMaxThreads(Integer.parseInt(commandLine.getOptionValue("t")));
        }

        if (commandLine.hasOption("m")) {
            config.setMimeProperties(commandLine.getOptionValue("m"));
        }

        if (commandLine.hasOption("b")) {
            config.setBufferSize(Integer.parseInt(commandLine.getOptionValue("b")));
        }

        if (commandLine.hasOption("ff")) {
            config.setFfmpegLocation(commandLine.getOptionValue("ff"));
            if (commandLine.hasOption("fp")) {
                config.setFfmpegParams(commandLine.getOptionValue("fp"));
            }
        }

        if (commandLine.hasOption("se")) {
            config.setSegmenterLocation(commandLine.getOptionValue("se"));
            if (commandLine.hasOption("sp")) {
                config.setSegmenterParams(commandLine.getOptionValue("sp"));
            }
        }

        if (commandLine.hasOption("f")) {
            Map<String, String> rotDirs = new TreeMap<String, String>();

            try {
                for (String path: commandLine.getOptionValues("f")) {
                    String[] location = path.split("=");
                    rotDirs.put(location[1], location[0]);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            config.setRootDirs(rotDirs);
        }

        return config;
    }

    private static CommandLine parseCommandLine(Options options, String[] arguments) throws ParseException {
        CommandLineParser parser = new PosixParser();
        return parser.parse(options, arguments);
    }

    private static void printHelp() {
        PrintWriter writer = new PrintWriter(System.out);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 80, SYNTAX, HEADER,
                options, 3, 5, FOOTER, true);
        writer.flush();
    }

    public static void main(String[] args) throws Exception {
        try {
            System.setProperty("file.encoding", EncodingUtil.UTF8_ENCODING);

            Runner runner = new Runner();
            runner.start(getConfig(args));

            synchronized (monitor) {
                monitor.wait();
            }
        } catch (ParseException e) {
            printHelp();
        }
    }
}
