package pt.linguateca.harem;

import java.io.File;

public class ParticipationFile {

	public static final String FIELD_SEP = "\t";
	public static final String RUN_SEP = "_";
	public static final int N_FIELDS = 4;

	private File _file;
	private String _filter;
	private String _system;
	private String _filterDescription;

	public ParticipationFile(String line) throws LineFormatException{

		String[] fields = line.split(FIELD_SEP);

		if(fields.length < N_FIELDS)
			throw new LineFormatException();
		
		/*_file = new File(semPlicas(fields[0]));
		_filterArguments = semPlicas(fields[1]);
		_filterDescription = semPlicas(fields[2]);
		_system = semPlicas(fields[3]);*/
		
		_file = new File(semAspas(fields[0]));
		
		//if(semAspas(fields[1]).equals(EvaluationScriptGenerator.TOTAL_FILTER))
		_filter = entreAspas(fields[1]);
		/*else
			_filter = semAspas(fields[1]);*/
		
		_filterDescription = semAspas(fields[2]);
		_system = semAspas(fields[3]);
	}

	private String semPlicas(String s){
		return s.replace("'", "");
	}

	private String semAspas(String s){
		return s.replace("\"", "");
	}
	
	private String entreAspas(String s){
		
		if(!s.startsWith("\""))
			s = "\""+ s;
		if(!s.endsWith("\""))
			s = s + "\"";

		return s;
	}
	
	public File getFile() {
		return _file;
	}

	public String getPath(){
		return _file.getPath();
	}

	public String getRunNo(){
		int index = _file.getName().indexOf(RUN_SEP);
		return _file.getName().substring(index, _file.getName().length());
	}

	public String getFilter() {
		return _filter;
	}

	public String getFilterDescription() {
		return _filterDescription;
	}

	public String getSystemName() {
		return _system;
	}

	public String getFileName(){
		return _file.getName();
	}

	public String getParticipationString(){
		return _file.getParentFile().getName();
	}
	
	public String toString(){
		return getSystemName()+"_"+getFileName();
	}
}