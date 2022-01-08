package pt.linguateca.harem.scriptsgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import pt.linguateca.harem.GlobalReporter;
import pt.linguateca.harem.LineFormatException;
import pt.linguateca.harem.ParticipationFile;
import pt.linguateca.util.LinkedBucketHashMap;

public class EvaluationScriptGenerator implements Runnable
{
	private String _filePath;

	private static final String COMMENT = "#";
	private static final String SYSTEM_FILTER_FLAG = "-sistema";
	private static final String EVALUATION_FILTER_FLAG = "-avaliacao";
	//private static final String TOTAL_FILTER = "\"*\"";
	public static final String TOTAL_FILTER = "*";
	private static final String TOTAL_PREFIX = ".total";
	
	private static final String ID_FILTER = "-id";
	private static final String ID_PREFIX = ".id";
	private static final String DEFAULT_PREFIX = ".cenario";
	private static final String DEFAULT_RESULTS = "resultados";
	private static final String DEFAULT_RESULTS_2 = "resultados_altrel";
	//private static final String MODE_A = "-modo "+AlignmentFilter.MODE_A;
	//private static final String MODE_B = "-modo "+AlignmentFilter.MODE_B;

	private static final String NAME = "[[1]]";
	private static final String GC = "[[2]]";
	//private static final String MODE = "[[3]]";
	private static final String FLAGS = "[[4]]";
	private static final String FILTER_PREFIX = "[[5]]";
	private static final String DIR = "[[6]]";
	private static final String INFO = "[[7]]";
	private static final String RESULTS = "[[8]]";

	private static final String DEFAULT_GC_NAME = "CDSegundoHAREM.xml";
	private static final String SCRIPT_NAME = "avaliar_"+NAME+".bat";
	private static final String RESULTS_SCRIPT_NAME = "gerar_resultados.bat";

	private static final String INVOKE_ALIGNER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar -Xmx512M pt.linguateca.harem.Aligner -submissao "+NAME+".xml -cd "+GC+" > "+NAME+".alinhado";
	private static final String INVOKE_ALIGNMENT_EVALUATOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.IndividualAlignmentEvaluator -alinhamento "+NAME+".alinhado > "+NAME+".alinhado.avalida";
	private static final String INVOKE_ALIGNMENT_FILTER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentFilter -alinhamento "+NAME+".alinhado.avalida -estilo muc "+FLAGS+" > "+NAME+".alinhado.avalida"+FILTER_PREFIX;
	private static final String INVOKE_ALT_ORGANIZER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AltAlignmentOrganizer -alinhamento "+NAME+".alinhado.avalida"+FILTER_PREFIX+" > "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts";
	private static final String INVOKE_SEMANTIC_EVALUATOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.SemanticAlignmentEvaluator -alinhamento "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts > "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir";
	private static final String INVOKE_GLOBAL_EVALUATOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir > "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir.ida";
	private static final String INVOKE_ALT_RELAX =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.SemanticAltAlignmentSelector -alinhamento "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir > "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir.altrel";
	private static final String INVOKE_GLOBAL_EVALUATOR_ALT_RELAX =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir.altrel > "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.emir.altrel.ida";
	private static final String INVOKE_SPURIOUS_COLLECTOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.SpuriousAlignmentsCollector -alinhamento "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts > "+NAME+".alinhado.avalida"+FILTER_PREFIX+".alts.espurios";
	private static final String INVOKE_GLOBAL_REPORTER =
		"java -Dfile.encoding=ISO-8859-1 pt.linguateca.harem.GlobalSemanticReporter -depurar -cenario -filtro "+FILTER_PREFIX+".alts.emir.ida -dir "+DIR+" -info "+INFO+"> "+RESULTS+".html";
	private static final String INVOKE_GLOBAL_REPORTER_ALT_REL =
		"java -Dfile.encoding=ISO-8859-1 pt.linguateca.harem.GlobalSemanticReporter -depurar -cenario -filtro "+FILTER_PREFIX+".alts.emir.altrel.ida -dir "+DIR+" -info "+INFO+"> "+RESULTS+".html";

	public EvaluationScriptGenerator(String path){
		_filePath = path;

		new Thread(this).start();
	}

	public void run(){

		//LinkedList<ParticipationFile> participations = getFilesInfo();

		LinkedBucketHashMap participations = getFilesInfo();
		LinkedList<String> filters = getDistinctFilters(participations);
		//System.out.println(filters.toString().replaceAll(", ", "\n"));

		PrintStream filePrinter = null;
		PrintStream particpantPrinter = null;
		LinkedList<ParticipationFile> tmp = null;

		String filter = null;
		String filterPrefix = null;
		String fileScriptName = null;
		String participantScriptName = null;

		LinkedList<String> allParticipantFiles = new LinkedList<String>();
		LinkedList<String> filterPrefixes = new LinkedList<String>();

		for(Object key : participations.keySet()){

			tmp = (LinkedList<ParticipationFile>)participations.get(key);
			try {

				participantScriptName = getParticipantScriptName(tmp.get(0).getParticipationString());
				particpantPrinter = new PrintStream(participantScriptName);

				for(ParticipationFile p : tmp){

					for(int i = 0; i < filters.size(); i++){

						if(filters.get(i).equals("\""+TOTAL_FILTER+"\""))
							filterPrefix = TOTAL_PREFIX;
						else
							filterPrefix = DEFAULT_PREFIX+(i+1);
						
						filter = getFilterFlags(filters.get(i), p.getFilter());
						
						if(!filterPrefixes.contains(filterPrefix))
							filterPrefixes.add(filterPrefix);

						fileScriptName = getFileScriptName(p.getParticipationString(), p.getFileName(), filterPrefix.substring(1));
						filePrinter = new PrintStream(fileScriptName);

						//o alinhador e avalida so' precisam de ser invocados uma vez, mas...
						filePrinter.println(invokeAligner(p.getPath(), DEFAULT_GC_NAME));
						filePrinter.println(invokeAlignmentEvaluator(p.getPath()));

						filePrinter.println(invokeAlignmentFilter(p.getPath(), filter, filterPrefix));

						filePrinter.println(invokeAltOrganizer(p.getPath(), filterPrefix));
						
						filePrinter.println(invokeSemanticEvaluator(p.getPath(), filterPrefix));
						
						filePrinter.println(invokeAltRelax(p.getPath(), filterPrefix));
						
						filePrinter.println(invokeGlobalEvaluator(p.getPath(), filterPrefix));	
						filePrinter.println(invokeGlobalEvaluatorAltRelax(p.getPath(), filterPrefix));
						
						filePrinter.println(invokeSpuriousCollector(p.getPath(), filterPrefix));
						
						filePrinter.println();

						//filePrinter.println("pause");
						filePrinter.flush();
						filePrinter.close();

						particpantPrinter.println("CALL "+fileScriptName);
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
		generateReporterScript(filterPrefixes);
	}

	private String getFilterFlags(String filter1, String filter2)
	{
		return EVALUATION_FILTER_FLAG+" "+filter1+" "+SYSTEM_FILTER_FLAG+" "+filter2;
	}
	
	private void generateEvaluateAllScript(LinkedList<String> files){

		try {
			PrintStream printer = new PrintStream(SCRIPT_NAME.replace(NAME, "tudo"));

			for(String file : files){

				printer.println("CALL "+file);
				printer.flush();
			}

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
						filter, GlobalReporter.DEFAULT_DIR, DEFAULT_RESULTS+"_"+filter.substring(1)));
				printer.println(invokeGlobalReporterAltRel(
						filter, GlobalReporter.DEFAULT_DIR, DEFAULT_RESULTS_2+"_"+filter.substring(1)));
				printer.flush();
			}

			printer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		filters.add("\""+TOTAL_FILTER+"\"");
		//filters.add(ID_FILTER);

		String filter = null;
		LinkedList<ParticipationFile> tmp;
		for(Object o : participations.values()){

			tmp = (LinkedList<ParticipationFile>)o;
			for(ParticipationFile p : tmp){
				filter = p.getFilter();

				//System.out.println(p.getFileName()+" "+p.getParticipationString());
				if(!filters.contains(filter)){				
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

	private String invokeAlignmentFilter(String name, String filter, String prefix){
		return INVOKE_ALIGNMENT_FILTER.replace(NAME, name).replace(FLAGS, filter)
		.replace(FILTER_PREFIX, prefix);
	}

	private String invokeAltOrganizer(String name, String prefix){
		return INVOKE_ALT_ORGANIZER.replace(NAME, name).replace(FILTER_PREFIX, prefix);
	}

	private String invokeSemanticEvaluator(String name, String prefix){
		return INVOKE_SEMANTIC_EVALUATOR.replace(NAME, name).replace(FILTER_PREFIX, prefix);
	}

	private String invokeGlobalEvaluator(String name, String prefix){		
		return INVOKE_GLOBAL_EVALUATOR.replace(NAME, name).replace(FILTER_PREFIX, prefix);
	}

	private String invokeAltRelax(String name, String prefix){
		return INVOKE_ALT_RELAX.replace(NAME, name).replace(FILTER_PREFIX, prefix);
	}

	private String invokeGlobalEvaluatorAltRelax(String name, String prefix){
		return INVOKE_GLOBAL_EVALUATOR_ALT_RELAX.replace(NAME, name).replace(FILTER_PREFIX, prefix);
	}

	private String invokeSpuriousCollector(String name, String prefix){
		return INVOKE_SPURIOUS_COLLECTOR.replace(NAME, name).replace(FILTER_PREFIX, prefix);
	}

	private String invokeGlobalReporter(String filter, String dir, String results){
		return INVOKE_GLOBAL_REPORTER.replace(FILTER_PREFIX, filter)
		.replace(DIR, dir).replace(INFO, _filePath).replace(RESULTS, results);
	}

	private String invokeGlobalReporterAltRel(String filter, String dir, String results){
		return INVOKE_GLOBAL_REPORTER_ALT_REL.replace(FILTER_PREFIX, filter)
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

		new EvaluationScriptGenerator(systemsFile);
	}

}

