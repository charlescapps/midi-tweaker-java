package capps.midi;

import capps.midi.config.RemoveTracksConfig;

/**
 * Created by charlescapps on 9/1/14.
 */
public class RemoveTracksMain {
    public static void main(String[] args) throws Exception {
        new RemoveTracksMain().run(args);
    }

    private void run(String[] args) throws Exception {
        RemoveTracksConfig removeTracksConfig = RemoveTracksConfig.fromArgs(args);
        TrackRemover trackRemover = new TrackRemover(removeTracksConfig);
        trackRemover.removeTracksAndWriteToFile();
    }
}
