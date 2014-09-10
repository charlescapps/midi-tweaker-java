package capps.midi.config;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by charlescapps on 9/1/14.
 */
public class RemoveTracksConfig {
    private final String inputFile;
    private final String outputFile;

    // ---- Configs for the REMOVE_TRACKS task
    private final Set<Integer> tracksToKeep;

    public RemoveTracksConfig(String inputFile, String outputFile, Set<Integer> tracksToKeep) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.tracksToKeep = tracksToKeep;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public Set<Integer> getTracksToKeep() {
        return tracksToKeep;
    }

    public static RemoveTracksConfig fromArgs(String[] args) {
        Set<Integer> tracksToKeep = Sets.newHashSet();
        String inputFile = null, outputFile = null;
        for (int i = 0; i < args.length; i++) {
            if ("--tracks".equals(args[i])) {
                String value = getStringValue(args, i, "--tracks");
                String[] tracks = value.split(",");
                for (String track: tracks) {
                    tracksToKeep.add(Integer.parseInt(track));
                }
            } else if ("--inputFile".equals(args[i])) {
                inputFile = getStringValue(args, i, "--inputFile");
            } else if ("--outputFile".equals(args[i])) {
                outputFile = getStringValue(args, i, "--outputFile");
            }
        }
        if (inputFile == null || outputFile == null) {
            throw new RuntimeException("An input file and output file must be given! Args were: " + args);
        }
        if (tracksToKeep.isEmpty()) {
            throw new RuntimeException("Some tracks must be kept with the --tracks param! Args were: " + args);
        }
        return new RemoveTracksConfig(inputFile, outputFile, tracksToKeep);
    }

    private static String getStringValue(String[] args, int i, String param) {
        if (i >= args.length - 1) {
            throw new RuntimeException(
                    String.format("The %s param must be followed by a value!", param));
        }
        return args[i + 1];
    }
}
