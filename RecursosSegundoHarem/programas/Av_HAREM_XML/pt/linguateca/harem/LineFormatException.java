package pt.linguateca.harem;

public class LineFormatException extends Exception{
	public LineFormatException(){
		super("A linha deve ter pelo menos "+ParticipationFile.N_FIELDS+" campos separados por "+ParticipationFile.FIELD_SEP);
	}
}
