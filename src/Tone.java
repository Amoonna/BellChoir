
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * 
 * @author Amoonna: Written by Nate, Plays the song 'Mary Had a Little Lamb'
 *
 */
public class Tone {

	// Mary had a little lamb notes and lengths
	private static final List<BellNote> song = new ArrayList<BellNote>() {
		{
			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));
			add(new BellNote(Note.F4, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));

			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.HALF));

			add(new BellNote(Note.G4, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.HALF));

			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.HALF));

			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));
			add(new BellNote(Note.F4, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));

			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.QUARTER));

			add(new BellNote(Note.G4, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));
			add(new BellNote(Note.A5, NoteLength.QUARTER));
			add(new BellNote(Note.G4, NoteLength.QUARTER));

			add(new BellNote(Note.F4, NoteLength.WHOLE));
		}
	};

	/**
	 * calls the methods to find each note and plays the song
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// sets the parameters for the note based on presets
		final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
		Tone t = new Tone(af);
		t.playSong(song); // plays the notes in order of line
	}

	private final AudioFormat af;

	Tone(AudioFormat af) {
		this.af = af;
	}

	/**
	 * plays the next note and then moves to the next line
	 * 
	 * @param song
	 * @throws LineUnavailableException
	 */
	void playSong(List<BellNote> song) throws LineUnavailableException {
		try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {// starts the tone
			line.open();
			line.start();

			for (BellNote bn : song) {
				playNote(line, bn);// plays the line (note)
			}
			line.drain(); // removes the line from the queue
		}
	}

	/**
	 * gathers data for the pitch and length of the note
	 * 
	 * @param line
	 * @param bn
	 */
	private void playNote(SourceDataLine line, BellNote bn) {
		final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000); // makes the note long enough to
																						// actually create a noise
		final int length = Note.SAMPLE_RATE * ms / 1000;
		line.write(bn.note.sample(), 0, length); // note played
		line.write(Note.REST.sample(), 0, 50); // rest between the notes
	}
}

/**
 * 
 * @author Amoonna: created by Nate, creates the notes and defines parameters of
 *         a "note"
 *
 */
class BellNote {
	final Note note; // note
	final NoteLength length; // length of the note

	BellNote(Note note, NoteLength length) { // applies both to the object of the note
		this.note = note;
		this.length = length;
	}
}

/**
 * 
 * @author Amoonna: made by Nate, sets the lengths of the notes
 *
 */
enum NoteLength {
	WHOLE(1.0f), HALF(0.5f), QUARTER(0.25f), EIGTH(0.125f);

	private final int timeMs;

	private NoteLength(float length) {
		timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000); // how long the note plays
	}

	/**
	 * checks for the length of the note and returns the value
	 * 
	 * @return timeMs
	 */
	public int timeMs() {
		return timeMs;
	}
}

/**
 * 
 * @author Amoonna: made by Nate, defines the valid notes and values
 *
 */
enum Note {
	// REST Must be the first 'Note'
	REST, A4, A4S, B4, C4, C4S, D4, D4S, E4, F4, F4S, G4, G4S, A5;

	public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
	public static final int MEASURE_LENGTH_SEC = 1;

	// Circumference of a circle divided by # of samples
	private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

	private final double FREQUENCY_A_HZ = 440.0d;
	private final double MAX_VOLUME = 127.0d;

	private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

	/**
	 * calculates and sets the sound value of each note
	 */
	private Note() {
		int n = this.ordinal();
		if (n > 0) {
			// Calculate the frequency!
			final double halfStepUpFromA = n - 1;
			final double exp = halfStepUpFromA / 12.0d;
			final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

			// Create sinusoidal data sample for the desired frequency
			final double sinStep = freq * step_alpha;
			for (int i = 0; i < sinSample.length; i++) {
				sinSample[i] = (byte) (Math.sin(i * sinStep) * MAX_VOLUME);
			}
		}
	}

	/**
	 * checks and returns the note's sound value
	 * 
	 * @return sinSample
	 */
	public byte[] sample() {
		return sinSample;
	}
}