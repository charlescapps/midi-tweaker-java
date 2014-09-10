package capps.midi.config;

/**
 * Created by charlescapps on 9/1/14.
 */
public class EqualizeNoteLengthConfig {
    private final String inputFile;
    private final String outputFile;

    // ---- Configs for the REMOVE_TRACKS task
    private final int primaryTrack;
    private final int noteDurationMs;
    private final int gapMs;

    public EqualizeNoteLengthConfig(String inputFile, String outputFile, int primaryTrack, int noteDurationMs, int gapMs) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.primaryTrack = primaryTrack;
        this.noteDurationMs = noteDurationMs;
        this.gapMs = gapMs;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public int getPrimaryTrack() {
        return primaryTrack;
    }

    public int getNoteDurationMs() {
        return noteDurationMs;
    }

    public int getGapMs() {
        return gapMs;
    }

    public static EqualizeNoteLengthConfig fromArgs(String[] args) {
        Integer primaryTrack = null, noteDurationsMs = null, gapMs = null;
        String inputFile = null, outputFile = null;
        for (int i = 0; i < args.length; i++) {
            if ("--track".equals(args[i])) {
                String value = getStringValue(args, i, "--track");
                primaryTrack = Integer.parseInt(value);
            } else if ("--noteDurationMs".equals(args[i])) {
                String value = getStringValue(args, i, "--noteDurationMs");
                noteDurationsMs = Integer.parseInt(value);
            } else if ("--gapMs".equals(args[i])) {
                String value = getStringValue(args, i, "--gapMs");
                gapMs = Integer.parseInt(value);
            } else if ("--inputFile".equals(args[i])) {
                inputFile = getStringValue(args, i, "--inputFile");
            } else if ("--outputFile".equals(args[i])) {
                outputFile = getStringValue(args, i, "--outputFile");
            }
        }
        if (inputFile == null || outputFile == null) {
            throw new RuntimeException("An input file and output file must be given! Args were: " + args);
        }
        if (primaryTrack == null) {
            throw new RuntimeException("The primary track must be specified with the --track param! Args were: " + args);
        }
        if (noteDurationsMs == null) {
            throw new RuntimeException("The note duration must be specified with the --noteDurationMs param! Args were: " + args);
        }
        if (gapMs == null) {
            throw new RuntimeException("The gap duration must be specified with the --gapMs param! Args were: " + args);
        }
        return new EqualizeNoteLengthConfig(inputFile, outputFile, primaryTrack, noteDurationsMs, gapMs);
    }

    private static String getStringValue(String[] args, int i, String param) {
        if (i >= args.length - 1) {
            throw new RuntimeException(
                    String.format("The %s param must be followed by a value!", param));
        }
        return args[i + 1];
    }
}
