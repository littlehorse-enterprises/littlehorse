package io.littlehorse.driver;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TestConfig {

    private static final int DEFAULT_THREADS = 4;
    private CommandLine cli;

    public TestConfig(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(
            Option
                .builder("t")
                .longOpt("threads")
                .hasArg(true)
                .desc("number of threads, default " + DEFAULT_THREADS)
                .required(false)
                .build()
        );
        options.addOption(
            Option
                .builder("h")
                .longOpt("help")
                .hasArg(false)
                .desc("shows this help message")
                .required(false)
                .build()
        );
        options.addOption(
            Option
                .builder("p")
                .longOpt("provision")
                .hasArg(false)
                .desc("provision with test container")
                .required(false)
                .build()
        );

        CommandLineParser parser = new DefaultParser();
        CommandLine cli = parser.parse(options, args);

        // Let's abort in case user is asking for help
        if (cli.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("e2e-tests [OPTIONS] [TESTS]", options);
            System.exit(0);
        }

        this.cli = cli;
    }

    public int getThreads() {
        return cli.hasOption("t")
            ? Integer.parseInt(cli.getOptionValue("t"))
            : DEFAULT_THREADS;
    }

    public boolean shouldProvision() {
        return cli.hasOption("p");
    }

    public Set<String> getTestToRun() {
        return cli.getArgList().stream().collect(Collectors.toSet());
    }
}
