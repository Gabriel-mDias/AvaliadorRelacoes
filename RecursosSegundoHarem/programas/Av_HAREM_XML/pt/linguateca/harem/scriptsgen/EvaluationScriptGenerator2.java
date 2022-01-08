package pt.linguateca.harem.scriptsgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;

import pt.linguateca.harem.GlobalReporter;
import pt.linguateca.harem.LineFormatException;
import pt.linguateca.harem.ParticipationFile;
import pt.linguateca.harem.TagBase;
import pt.linguateca.util.LinkedBucketHashMap;

public class EvaluationScriptGenerator2 implements Runnable
{
	private TagBase _tagBase;
	private String _filePath;
	private boolean _outro = false;
	
	private HashMap<String, String> _categoriesSuffixes;
	
	private static final String COMMENT = "#";
	//private static final String TOTAL_FILTER = "\"*\"";
	private static final String TOTAL_FILTER = "*";
	private static final String TOTAL_SUFFIX = "total";
	private static final String EVERYTHING_INSIDE = "(*)";
	private static final String EVERYTHING_BUT_TEMPO_FILTER
		= "\"ABSTRACCAO(*):ACONTECIMENTO(*):COISA(*):LOCAL(*):OBRA(*):ORGANIZACAO(*):OUTRO:PESSOA(*):VALOR(*)\"";
	private static final String EVERYTHING_BUT_TEMPO_SUFFIX	= "total_stempo";
	
	private static final String DEFAULT_SUFFIX = "cenario";
	private static final String DEFAULT_RESULTS = "resultados";
	private static final String DEFAULT_RESULTS_2 = "resultados_altrel";
	//private static final String MODE_A = "-modo "+AlignmentFilter.MODE_A;
	//private static final String MODE_B = "-modo "+AlignmentFilter.MODE_B;

	private static final String NAME = "[[1]]";
	private static final String GC = "[[2]]";
	private static final String EVAL_FILTER = "[[3]]";
	private static final String SYS_FILTER = "[[4]]";
	private static final String FILTER_SUFFIX = "[[5]]";
	private static final String DIR = "[[6]]";
	private static final String INFO = "[[7]]";
	private static final String RESULTS = "[[8]]";

	//private static final String DEFAULT_GC_NAME = "CDSegundoHAREM.xml";
	private static final String DEFAULT_GC_NAME = "CDSegundoHAREM";
	private static final String SCRIPT_NAME = "avaliar_"+NAME+".bat";
	private static final String RESULTS_SCRIPT_NAME = "gerar_resultados.bat";

	private static final String INVOKE = "CALL";
	
	private static final String ALIGNER = "invocar_alinhador.bat";
	private static final String ALIGNMENT_EVALUATOR = "invocar_avalida.bat";
	private static final String ALIGNMENT_FILTER = "invocar_veus.bat";
	private static final String ALT_ORGANIZER = "invocar_alts.bat";
	private static final String SEMANTIC_EVALUATOR = "invocar_emir.bat";
	private static final String ALT_RELAX = "invocar_altrel.bat";
	private static final String GLOBAL_EVALUATOR = "invocar_ida.bat";
	private static final String SPURIOUS_COLLECTOR = "invocar_espurios.bat";
	private static final String GLOBAL_REPORTER = "invocar_sultao.bat";
	
	private static final String INVOKE_ALIGNER = INVOKE+" "+ALIGNER+" "+NAME+" "+GC;
	private static final String INVOKE_ALIGNMENT_EVALUATOR = INVOKE+" "+ALIGNMENT_EVALUATOR+" "+NAME;
	private static final String INVOKE_ALIGNMENT_FILTER = INVOKE+" "+ALIGNMENT_FILTER+" "+NAME+" "+EVAL_FILTER+" "+SYS_FILTER+" "+FILTER_SUFFIX;
	private static final String INVOKE_ALT_ORGANIZER = INVOKE+" "+ALT_ORGANIZER+" "+NAME;
	private static final String INVOKE_SEMANTIC_EVALUATOR = INVOKE+" "+SEMANTIC_EVALUATOR+" "+NAME;
	private static final String INVOKE_GLOBAL_EVALUATOR = INVOKE+" "+GLOBAL_EVALUATOR+" "+NAME;
	private static final String INVOKE_ALT_RELAX = INVOKE+" "+ALT_RELAX+" "+NAME;
	private static final String INVOKE_SPURIOUS_COLLECTOR = INVOKE+" "+SPURIOUS_COLLECTOR+" "+NAME;
	private static final String INVOKE_GLOBAL_REPORTER = INVOKE+" "+GLOBAL_REPORTER+" "+FILTER_SUFFIX+" "+DIR+" "+INFO+" "+RESULTS;
	
	private static final String ALIGNER_SUFFIX = ".alinhado";
	private static final String ALIGNMENT_EVALUATOR_SUFFIX = ".avalida";
	//private static final String ALIGNMENT_FILTER_SUFFIX = ".";
	private static final String ALT_ORGANIZER_SUFFIX = ".alts";
	private static final String SEMANTIC_EVALUATOR_SUFFIX = ".emir";
	private static final String ALT_RELAX_SUFFIX = ".altrel";
	private static final String GLOBAL_EVALUATOR_SUFFIX = ".ida";
	private static final String SPURIOUS_COLLECTOR_SUFFIX = ".espurios";
	
	private static final String BEGIN_TIME = "time \t >> tempo.txt";
	private static final String END_TIME = "time \t >> tempo.txt";
	
	public EvaluationScriptGenerator2(String path){
		
		_tagBase = TagBase.getInstance();
		_filePath = path;
		
		_categoriesSuffixes = getCategoriesAndSuffixes();
		
		new Thread(this).start();
	}

	private HashMap<String, String> getCategoriesAndSuffixes()
	{
		HashMap<String, String> toReturn = new HashMap<String, String>();
		toReturn.putAll(getOtherFilters());
		for(String c : _tagBase.getCategories())
		{
			if(!_outro && !c.equals(_tagBase.getOtherAt()))
				toReturn.put(_tagBase.asQuotedType(c+EVERYTHING_INSIDE), c.toLowerCase());
		}
		
		return toReturn;
	}
	
	//outros filtros
	private HashMap<String, String> getOtherFilters()
	{
		HashMap<String, String> toReturn = new HashMap<String, String>();
		toReturn.put(_tagBase.asQuotedType(TOTAL_FILTER), TOTAL_SUFFIX);
		toReturn.put(EVERYTHING_BUT_TEMPO_FILTER, EVERYTHING_BUT_TEMPO_SUFFIX);
		return toReturn;
	}
	
	public void run(){

		//LinkedList<ParticipationFile> participations = getFilesInfo();

		LinkedBucketHashMap participations = getFilesInfo();
		LinkedList<String> filters = getDistinctFilters(participations);

		//juntar filtros de categoria com os dos participantes
		filters.addAll(_categoriesSuffixes.keySet());		
		//System.out.println(filters.toString().replaceAll(", ", "\n"));

		PrintStream filePrinter = null;
		PrintStream particpantPrinter = null;
		LinkedList<ParticipationFile> tmp = null;

		String fileName;
		String evalFilter = null;
		String sysFilter = null;
		String filterSuffix = null;
		
		String nameBeforeFilters;
		String nameAfterAlts;
		
		String fileScriptName = null;
		String participantScriptName = null;

		LinkedList<String> allParticipantFiles = new LinkedList<String>();
		LinkedList<String> filterSuffixes = new LinkedList<String>();

		for(Object key : participations.keySet()){

			tmp = (LinkedList<ParticipationFile>)participations.get(key);
			try {

				participantScriptName = getParticipantScriptName(tmp.get(0).getParticipationString());
				particpantPrinter = new PrintStream(participantScriptName);

				for(ParticipationFile p : tmp){

					fileName = p.getPath();
					sysFilter = p.getFilter();
					
					//TODO: o alinhador e avalida so' precisam de ser invocados uma vez, mas pode dar mais jeito estar em todos
					particpantPrinter.println(invokeAligner(fileName, DEFAULT_GC_NAME));
					fileName += ALIGNER_SUFFIX;
					nameBeforeFilters = fileName;
					particpantPrinter.println(invokeAlignmentEvaluator(fileName));
										
					for(int i = 0, c = 1; i < filters.size(); i++){

						fileName = nameBeforeFilters;	
						evalFilter = filters.get(i);
						
						if((filterSuffix = _categoriesSuffixes.get(evalFilter))== null)
							filterSuffix = DEFAULT_SUFFIX+(++c);
						
						if(!filterSuffixes.contains(filterSuffix))
							filterSuffixes.add(filterSuffix);

						fileScriptName = getFileScriptName(p.getParticipationString(), p.getFileName(), filterSuffix);
						filePrinter = new PrintStream(fileScriptName);

						fileName += ALIGNMENT_EVALUATOR_SUFFIX;
						filePrinter.println(invokeAlignmentFilter(fileName, evalFilter, sysFilter, filterSuffix));

						fileName += "."+filterSuffix;
						filePrinter.println(invokeAltOrganizer(fileName));
						
						fileName += ALT_ORGANIZER_SUFFIX;
						nameAfterAlts = fileName;
						filePrinter.println(invokeSemanticEvaluator(fileName));
						
						fileName += SEMANTIC_EVALUATOR_SUFFIX;
						filePrinter.println(invokeAltRelax(fileName));
						
						filePrinter.println(invokeGlobalEvaluator(fileName)); //ida do alts.emir
						filePrinter.println(invokeGlobalEvaluator(fileName+ALT_RELAX_SUFFIX)); //ida do alts.emir.altrel
							
						filePrinter.println(invokeSpuriousCollector(nameAfterAlts));
						
						filePrinter.println();

						//filePrinter.println("pause");
						filePrinter.flush();
						filePrinter.close();

						particpantPrinter.println(INVOKE+" "+fileScriptName);
					}
				}

				particpantPrinter.flush();
				particpantPrinter.close();
				allParticipantFiles.add(participantScriptName);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		generateEvaluateAllScript(allParticipantFiles);
		generateReporterScript(filterSuffixes);
	}
	
	private void generateEvaluateAllScript(LinkedList<String> files){

		try {
			PrintStream printer = new PrintStream(SCRIPT_NAME.replace(NAME, "tudo"));

			printer.println(BEGIN_TIME);
			for(String file : files){

				printer.println(INVOKE+" "+file);
				printer.flush();
			}
			printer.println(END_TIME);

			printer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateReporterScript(LinkedList<String> filters){

		try {
			PrintStream printer = new PrintStream(RESULTS_SCRIPT_NAME);

			for(String filter : filters){
				
				/*if(filter.equals(TOTAL_FILTER))
					filter = "TOTAL";*/
								
				printer.println(invokeGlobalReporter(
						getSuffixFromFilterTilEnd(filter, false), GlobalReporter.DEFAULT_DIR, DEFAULT_RESULTS+"_"+filter));
				printer.println(invokeGlobalReporter(
						getSuffixFromFilterTilEnd(filter, true), GlobalReporter.DEFAULT_DIR, DEFAULT_RESULTS_2+"_"+filter));
				printer.flush();
			}

			printer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getSuffixFromFilterTilEnd(String filter, boolean altRel)
	{
		if(!altRel)
			return "."+filter+ALT_ORGANIZER_SUFFIX+SEMANTIC_EVALUATOR_SUFFIX+GLOBAL_EVALUATOR_SUFFIX;
		else
			return "."+filter+ALT_ORGANIZER_SUFFIX+SEMANTIC_EVALUATOR_SUFFIX+ALT_RELAX_SUFFIX+GLOBAL_EVALUATOR_SUFFIX;
	}

	private LinkedBucketHashMap getFilesInfo(){

		LinkedBucketHashMap participations  = new LinkedBucketHashMap(new LinkedList<ParticipationFile>());

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(_filePath));

			String line = null;
			ParticipationFile part = null;

			//saltar o cabecalho
			reader.readLine();

			while((line = reader.readLine()) != null){
				if(line.startsWith(COMMENT))
					continue;
				else {
					part = new ParticipationFile(line);
					participations.put(part.getSystemName(), part);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (LineFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return participations;
	}

	private LinkedList<String> getDistinctFilters(LinkedBucketHashMap participations){

		LinkedList<String> filters = new LinkedList<String>();

		String filter = null;
		LinkedList<ParticipationFile> tmp;
		for(Object o : participations.values()){

			tmp = (LinkedList<ParticipationFile>)o;
			for(ParticipationFile p : tmp){
				filter = p.getFilter();

				//System.out.println(p.getFileName()+" "+p.getParticipationString());
				if(!filters.contains(filter) && !_categoriesSuffixes.keySet().contains(filter)){				
					filters.add(filter);
				}
			}
		}
		return filters;
	}

	/*private LinkedList<ParticipationFile> getFilesInfo(){

		LinkedList<ParticipationFile> participations = new LinkedList<ParticipationFile>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(_filePath));

			String line = null;

			//saltar o cabecalho
			reader.readLine();

			while((line = reader.readLine()) != null){
				if(line.startsWith(COMMENT))
					continue;
				else
					participations.add(new ParticipationFile(line));
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (LineFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return participations;
	}

	private LinkedList<String> getDistinctFilters(LinkedList<ParticipationFile> participations){

		LinkedList<String> filters = new LinkedList<String>();
		filters.add(TOTAL_FILTER);
		filters.add(ID_FILTER);

		String filter = null;
		for(ParticipationFile p : participations){
			filter = p.getFilterArguments();

			if(!filters.contains(filter))
				filters.add(filter);
		}

		return filters;
	}*/

	private String getParticipantScriptName(String part){
		return SCRIPT_NAME.replace(NAME, part);
	}

	private String getFileScriptName(String part, String file, String filter){
		return SCRIPT_NAME.replace(NAME, file+"_"+filter);
	}

	private String invokeAligner(String sub, String gc){
		return INVOKE_ALIGNER.replace(NAME, sub).replace(GC, gc);
	}

	private String invokeAlignmentEvaluator(String name){
		return INVOKE_ALIGNMENT_EVALUATOR.replace(NAME, name);
	}

	private String invokeAlignmentFilter(String name, String evalFilter, String sysFilter, String suffix){
		return INVOKE_ALIGNMENT_FILTER.replace(NAME, name).replace(EVAL_FILTER, evalFilter)
		.replace(SYS_FILTER, sysFilter).replace(FILTER_SUFFIX, suffix);
	}

	private String invokeAltOrganizer(String name){
		return INVOKE_ALT_ORGANIZER.replace(NAME, name);
	}

	private String invokeSemanticEvaluator(String name){
		return INVOKE_SEMANTIC_EVALUATOR.replace(NAME, name);
	}

	private String invokeAltRelax(String name){
		return INVOKE_ALT_RELAX.replace(NAME, name);
	}

	private String invokeGlobalEvaluator(String name){
		return INVOKE_GLOBAL_EVALUATOR.replace(NAME, name);
	}
	
	private String invokeSpuriousCollector(String name){
		return INVOKE_SPURIOUS_COLLECTOR.replace(NAME, name);
	}

	private String invokeGlobalReporter(String filter, String dir, String results){
		return INVOKE_GLOBAL_REPORTER.replace(FILTER_SUFFIX, filter)
		.replace(DIR, dir).replace(INFO, _filePath).replace(RESULTS, results);
	}

	public static void main(String args[]){

		String systemsFile = null;

		if(args.length > 1)
		{
			if (args[0].equals("-participacoes"))
			{
				systemsFile = args[1];
			}
		}

		new EvaluationScriptGenerator2(systemsFile);
	}

}

