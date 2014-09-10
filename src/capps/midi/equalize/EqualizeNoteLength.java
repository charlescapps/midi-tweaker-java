package capps.midi.equalize;

import capps.midi.config.EqualizeNoteLengthConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.ChannelEvent;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.EndOfTrack;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.util.MidiUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by charlescapps on 9/9/14.
 */
public class EqualizeNoteLength {
    private final EqualizeNoteLengthConfig config;

    public EqualizeNoteLength(EqualizeNoteLengthConfig config) {
        this.config = config;
    }

    public void equalizeNoteLengthAndWriteToOutputFile() throws Exception {
        File inputFile = new File(config.getInputFile());
        MidiFile midiFile = new MidiFile(inputFile);
        equalizeNotes(midiFile);
        removeTracks(midiFile);
        File outputFile = new File(config.getOutputFile());
        midiFile.writeToFile(outputFile);
        System.out.printf("Success writing output midi to %s!", outputFile);
    }

    private void equalizeNotes(MidiFile midiFile) {
        final int primaryTrack = config.getPrimaryTrack();
        MidiTrack track = midiFile.getTracks().get(primaryTrack);
        Tempo primaryTempo = simplifyTempo(midiFile);

        Map<Long, List<NoteOnOffPair>> notesByStartTime = parseNotesByStartTime(track, primaryTempo, midiFile.getResolution());

        equalizeTrack(track, notesByStartTime, primaryTempo, midiFile);
    }

    private void equalizeTrack(MidiTrack track, Map<Long, List<NoteOnOffPair>> notesByStartTime, Tempo tempo, MidiFile midiFile) {
        removeNotes(track);
        final int NOTE_DURATION = config.getNoteDurationMs();
        final int GAP_DURATION = config.getGapMs();
        final long GAP_IN_TICKS = (long) MidiUtil.msToTicks(GAP_DURATION, tempo.getMpqn(), midiFile.getResolution());
        final long NOTE_DURATION_IN_TICKS = (long) MidiUtil.msToTicks(NOTE_DURATION, tempo.getMpqn(), midiFile.getResolution());

        List<Long> startTimes = Lists.newArrayList(notesByStartTime.keySet());
        Collections.sort(startTimes); // Sort the notes by start time.

        long currentTick = 0;
        for (long startTime : startTimes) {
            List<NoteOnOffPair> onOffPairs = notesByStartTime.get(startTime);
            if (onOffPairs.isEmpty()) {
                continue;
            }
            // Set the delta to be the gap duration for the first NoteOn
            NoteOnOffPair first = onOffPairs.get(0);
            NoteOn firstOn = first.getNoteOn();
            currentTick += GAP_IN_TICKS;
            NoteOn firstOnModified = new NoteOn(currentTick, GAP_IN_TICKS, firstOn.getChannel(), firstOn.getNoteValue(), firstOn.getVelocity());
            first.setNoteOn(firstOnModified);

            // Set the delta to be 0 for all other NoteOn
            for (int i = 1; i < onOffPairs.size(); i++) {
                NoteOnOffPair onOffPair = onOffPairs.get(i);
                NoteOn noteOn = onOffPair.getNoteOn();
                NoteOn modified = new NoteOn(currentTick, 0, noteOn.getChannel(), noteOn.getNoteValue(), noteOn.getVelocity());
                onOffPair.setNoteOn(modified);
            }

            // Set the delta to be the note duration for the first NoteOff
            ChannelEvent firstOff = first.getNoteOff();
            currentTick += NOTE_DURATION_IN_TICKS;
            ChannelEvent modifiedNoteOff;
            if (firstOff instanceof NoteOff) {
                NoteOff noteOff = (NoteOff) firstOff;
                modifiedNoteOff = new NoteOff(currentTick, NOTE_DURATION_IN_TICKS, firstOff.getChannel(), noteOff.getNoteValue(), noteOff.getVelocity());
            } else {
                NoteOn noteOn = (NoteOn) firstOff;
                modifiedNoteOff = new NoteOn(currentTick, NOTE_DURATION_IN_TICKS, firstOff.getChannel(), noteOn.getNoteValue(), noteOn.getVelocity());
            }
            first.setNoteOff(modifiedNoteOff);

            // Set the delta to be 0 for all other NoteOffs
            for (int i = 1; i < onOffPairs.size(); i++) {
                NoteOnOffPair onOffPair = onOffPairs.get(i);
                ChannelEvent endNote = onOffPair.getNoteOff();

                ChannelEvent modified;
                if (endNote instanceof NoteOff) {
                    NoteOff noteOff = (NoteOff) endNote;
                    modified = new NoteOff(currentTick, 0, endNote.getChannel(), noteOff.getNoteValue(), noteOff.getVelocity());
                } else {
                    NoteOn noteOn = (NoteOn) endNote;
                    modified = new NoteOn(currentTick, 0, endNote.getChannel(), noteOn.getNoteValue(), noteOn.getVelocity());
                }
                onOffPair.setNoteOff(modified);
            }

            // Insert the NoteOn events into the Track, followed by the NoteOff events
            for (NoteOnOffPair onOffPair : onOffPairs) {
                track.insertEvent(onOffPair.getNoteOn());
            }

            for (NoteOnOffPair onOffPair : onOffPairs) {
                track.insertEvent(onOffPair.getNoteOff());
            }
        }
        track.closeTrack();
    }

    private void removeNotes(MidiTrack track) {
        List<MidiEvent> eventsToRemove = Lists.newArrayList();
        for (MidiEvent e : track.getEvents()) {
            if (e instanceof NoteOn || e instanceof NoteOff) {
                eventsToRemove.add(e);
            }
        }

        for (MidiEvent e : eventsToRemove) {
            track.removeEvent(e);
        }
    }

    private Map<Long, List<NoteOnOffPair>> parseNotesByStartTime(MidiTrack midiTrack, Tempo tempo, int resolution) {
        Map<Long, List<NoteOnOffPair>> notesByStartTime = Maps.newHashMap();
        Map<Integer, Stack<NoteOn>> noteValueToNoteOns = Maps.newHashMap();
        for (MidiEvent e : midiTrack.getEvents()) {
            if (e instanceof NoteOff || e instanceof NoteOn && ((NoteOn) e).getVelocity() == 0) {
                int noteValue;
                if (e instanceof NoteOn) {
                    noteValue = ((NoteOn) e).getNoteValue();
                } else {
                    noteValue = ((NoteOff) e).getNoteValue();
                }

                Stack<NoteOn> noteOnStack = noteValueToNoteOns.get(noteValue);
                if (noteOnStack == null || noteOnStack.isEmpty()) {
                    throw new RuntimeException("Error -- Found NoteOff or 0 velocity NoteOn event without a corresponding NoteOn: " + e);
                }
                NoteOn noteOn = noteOnStack.pop();
                long ms = MidiUtil.ticksToMs(noteOn.getTick(), tempo.getMpqn(), resolution);
                NoteOnOffPair onOffPair = new NoteOnOffPair(noteOn, (ChannelEvent) e, ms);
                List<NoteOnOffPair> notesForStartTime = notesByStartTime.get(ms);
                if (notesForStartTime == null) {
                    notesForStartTime = Lists.newArrayList();
                    notesByStartTime.put(ms, notesForStartTime);
                }
                notesForStartTime.add(onOffPair);
            } else if (e instanceof NoteOn) {
                NoteOn noteOn = (NoteOn) e;
                Stack<NoteOn> noteOnStack = noteValueToNoteOns.get(noteOn.getNoteValue());
                if (noteOnStack == null) {
                    noteOnStack = new Stack<NoteOn>();
                    noteValueToNoteOns.put(noteOn.getNoteValue(), noteOnStack);
                }
                noteOnStack.push(noteOn);
            }
        }
        return notesByStartTime;
    }

    private void removeTracks(MidiFile midiFile) {
        List<MidiTrack> tracks = midiFile.getTracks();
        MidiTrack primaryTrack = tracks.get(config.getPrimaryTrack());

        while (midiFile.getTracks().size() > 1) {
            midiFile.removeTrack(1);
        }

        midiFile.addTrack(primaryTrack);
    }

    private Tempo simplifyTempo(MidiFile midiFile) {
        MidiTrack tempoTrack = midiFile.getTracks().get(0);
        List<Tempo> tempoEventsToRemove = Lists.newArrayList();
        boolean first = true;
        Tempo firstTempo = null;
        for (MidiEvent e : tempoTrack.getEvents()) {
            if (e instanceof Tempo) {
                if (first) {
                    first = false;
                    firstTempo = (Tempo) e;
                } else {
                    tempoEventsToRemove.add((Tempo) e);
                }
            }
        }

        if (firstTempo == null) {
            throw new RuntimeException("0 tempos found in the MidiTrack!");
        }

        for (Tempo tempo : tempoEventsToRemove) {
            tempoTrack.removeEvent(tempo);
        }

        return firstTempo;
    }

}
