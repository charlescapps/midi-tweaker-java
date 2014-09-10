package capps.midi.equalize;

import com.google.common.base.Preconditions;
import com.leff.midi.event.ChannelEvent;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;

/**
 * Created by charlescapps on 9/9/14.
 */
public class NoteOnOffPair {
    private NoteOn noteOn;
    private ChannelEvent noteOff;
    private final long startTimeMs;

    public NoteOnOffPair(NoteOn noteOn, ChannelEvent noteOff, long startTimeMs) {
        Preconditions.checkArgument(noteOff instanceof NoteOn && ((NoteOn) noteOff).getVelocity() == 0
                                 || noteOff instanceof NoteOff,
                "A NoteOff must be signaled by a NoteOn with 0 velocity or a NoteOff event");
        this.noteOn = noteOn;
        this.noteOff = noteOff;
        this.startTimeMs = startTimeMs;
    }

    public NoteOn getNoteOn() {
        return noteOn;
    }

    public ChannelEvent getNoteOff() {
        return noteOff;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setNoteOn(NoteOn noteOn) {
        this.noteOn = noteOn;
    }

    public void setNoteOff(ChannelEvent noteOff) {
        this.noteOff = noteOff;
    }

    public int getNoteValue() {
        return noteOn.getNoteValue();
    }

}
