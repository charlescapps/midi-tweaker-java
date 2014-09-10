package capps.midi.equalize;

import com.google.common.base.Preconditions;
import com.leff.midi.event.ChannelEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;

/**
 * Created by charlescapps on 9/9/14.
 */
public class EndNoteEvent {
    private final NoteOn noteOn;
    private final NoteOff noteOff;

    public EndNoteEvent(ChannelEvent event) {
        Preconditions.checkArgument(event instanceof NoteOn || event instanceof NoteOff);
        if (event instanceof NoteOn) {
            NoteOn noteOn = (NoteOn) event;
            Preconditions.checkArgument(noteOn.getVelocity() == 0);
            this.noteOn = noteOn;
            this.noteOff = null;
        } else {
            this.noteOn = null;
            this.noteOff = (NoteOff) event;
        }
    }

    public boolean isNoteOn() {
        return noteOn != null;
    }

    public boolean isNoteOff() {
        return noteOff != null;
    }

}
