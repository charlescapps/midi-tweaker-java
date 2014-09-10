package capps.midi;

import capps.midi.config.RemoveTracksConfig;
import com.google.common.collect.Lists;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;

import java.io.File;
import java.util.List;

/**
 * Created by charlescapps on 9/1/14.
 */
public class TrackRemover {
    private final RemoveTracksConfig config;

    public TrackRemover(RemoveTracksConfig config) {
        this.config = config;
    }

    public void removeTracksAndWriteToFile() throws Exception {
        File inputFile = new File(config.getInputFile());
        MidiFile midiFile = new MidiFile(inputFile);
        List<MidiTrack> tracks = midiFile.getTracks();
        List<Integer> tracksToRemove = Lists.newArrayList();
        for (int i = 0; i < tracks.size(); i++) {
            if (!config.getTracksToKeep().contains(i)) {
                tracksToRemove.add(i);
            }
        }
        for (int i: tracksToRemove) {
            midiFile.removeTrack(i);
        }
        File outputFile = new File(config.getOutputFile());
        midiFile.writeToFile(outputFile);
        System.out.printf("Success writing output midi to %s!", outputFile);
    }
}
