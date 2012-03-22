/*
 * Copyright (c) 2012 Sergey Prilukin
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


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.xml.XmlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * entry point for <b>jstreamserver</b>
 *
 * @author Sergey Prilukin
 */
public final class Runner {
    public static final String SYNTAX = "java -jar jstreamserver.jar";
    public static final String HEADER = "Jstreamserver simple static content HTTP server, version 0.1";
    public static final String FOOTER = "--END--";

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

        //Path to mime config
        Option resources = new Option("r", "resources", true, "Path to folder with static content");
        resources.setArgs(1);
        resources.setOptionalArg(false);
        resources.setArgName("folderName");
        resources.setRequired(false);

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
        Option segmenterDuration = new Option("sd", "sduration", true, "Duration of segment for HTTP Live streaming (in seconds)");
        segmenterDuration.setArgs(1);
        segmenterDuration.setOptionalArg(false);
        segmenterDuration.setArgName("segmentDuration");
        segmenterDuration.setRequired(false);

        //FFmppeg executble location
        Option segmenterWindowSize = new Option("sw", "swindow", true, "Window size of the segmenter for HTTP Live streaming");
        segmenterWindowSize.setArgs(1);
        segmenterWindowSize.setOptionalArg(false);
        segmenterWindowSize.setArgName("windowSize");
        segmenterWindowSize.setRequired(false);

        //FFmppeg executble location
        Option segmenterSearchKillFile = new Option("sk", "skillfile", true, "Search kill file for HTTP Live streaming");
        segmenterSearchKillFile.setArgs(1);
        segmenterSearchKillFile.setOptionalArg(false);
        segmenterSearchKillFile.setArgName("searchKillFile");
        segmenterSearchKillFile.setRequired(false);

        //FFmppeg executble location
        Option segmenterMaxTimeout = new Option("dt", "timeout", true, "Max segmenter timeout in msec for HTTP Live streaming");
        segmenterMaxTimeout.setArgs(1);
        segmenterMaxTimeout.setOptionalArg(false);
        segmenterMaxTimeout.setArgName("timeout");
        segmenterMaxTimeout.setRequired(false);

        Option defaultCharset = new Option("c", "charset", true, "Default text files encoding");
        defaultCharset.setArgs(1);
        defaultCharset.setOptionalArg(false);
        defaultCharset.setArgName("charset");
        defaultCharset.setRequired(false);

        Option ftpPort = new Option("ftp", "ftpport", true, "Port for built-in FTP server");
        ftpPort.setArgs(1);
        ftpPort.setOptionalArg(false);
        ftpPort.setArgName("port");
        ftpPort.setRequired(false);

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
        options.addOption(resources);
        options.addOption(ffmpegLocation);
        options.addOption(ffmpegParams);
        options.addOption(segmenterocation);
        options.addOption(segmenterDuration);
        options.addOption(segmenterWindowSize);
        options.addOption(segmenterSearchKillFile);
        options.addOption(segmenterMaxTimeout);
        options.addOption(locations);
        options.addOption(ftpPort);
        options.addOption(defaultCharset);
    }

    private void start() throws Exception {
        Server jetty = new Server();
        String[] configFiles = {"jetty.xml"};
        for(String configFile : configFiles) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            XmlConfiguration configuration = new XmlConfiguration(is);
            configuration.configure(jetty);
        }

        //configure your web application
        WebAppContext appContext = new WebAppContext();
        appContext.setContextPath("/jstreamserver");
        File warPath = new File("./jstreamserver-war-0.2.war");
        appContext.setWar(warPath.getAbsolutePath());
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{appContext, new DefaultHandler()});
        jetty.setHandler(handlers);

        jetty.start();
        jetty.join();
    }

    /**
     * Put passed params to system properties
     *
     * @param args command-line parameters
     */
    private static void setUpConfig(String[] args) throws ParseException {
        CommandLine commandLine = parseCommandLine(options, args);
        if (commandLine.hasOption("p")) {
            System.setProperty("port", commandLine.getOptionValue("p"));
        }

        if (commandLine.hasOption("h")) {
            System.setProperty("host", commandLine.getOptionValue("h"));
        }

        if (commandLine.hasOption("c")) {
            System.setProperty("defaultTextCharset", commandLine.getOptionValue("charset"));
        }

        if (commandLine.hasOption("ff")) {
            System.setProperty("ffmpegLocation", commandLine.getOptionValue("ff"));
            if (commandLine.hasOption("fp")) {
                System.setProperty("ffmpegParams", commandLine.getOptionValue("fp"));
            }
        }

        if (commandLine.hasOption("ftp")) {
            System.setProperty("ftpPort", commandLine.getOptionValue("ftp"));
        }

        if (commandLine.hasOption("se")) {
            System.setProperty("segmenterLocation", commandLine.getOptionValue("se"));
            if (commandLine.hasOption("sd")) {
                System.setProperty("segmentDurationInSec", commandLine.getOptionValue("sd"));
            }
            if (commandLine.hasOption("sw")) {
                System.setProperty("segmentWindowSize", commandLine.getOptionValue("sw"));
            }
            if (commandLine.hasOption("sk")) {
                System.setProperty("segmenterSearchKillFile", commandLine.getOptionValue("sk"));
            }
            if (commandLine.hasOption("dt")) {
                System.setProperty("segmenterMaxtimeout", commandLine.getOptionValue("dt"));
            }
        }

        if (commandLine.hasOption("f")) {
            for (String path: commandLine.getOptionValues("f")) {
                String[] location = path.split("=");
                System.setProperty("rootdir." + location[1], location[0]);
            }
        }
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
            //setUpConfig(args);

            Runner runner = new Runner();
            runner.start();
        } catch (ParseException e) {
            printHelp();
        }
    }
}
