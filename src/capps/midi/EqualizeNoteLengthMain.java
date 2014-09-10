package capps.midi;

import capps.midi.config.EqualizeNoteLengthConfig;
import capps.midi.config.RemoveTracksConfig;
import capps.midi.equalize.EqualizeNoteLength;

/**
 * Created by charlescapps on 9/1/14.
 */
public class EqualizeNoteLengthMain {
    public static void main(String[] args) throws Exception {
        new EqualizeNoteLengthMain().run(args);
    }

    private void run(String[] args) throws Exception {
        EqualizeNoteLengthConfig config = EqualizeNoteLengthConfig.fromArgs(args);
        EqualizeNoteLength equalizeNoteLength = new EqualizeNoteLength(config);
        equalizeNoteLength.equalizeNoteLengthAndWriteToOutputFile();
    }
}
