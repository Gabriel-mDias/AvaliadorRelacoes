package pt.linguateca.harem.scriptsgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class RelationEvaluationScriptGenerator2 implements Runnable
{
	private static final String TOTAL_FILTER = "*";

	private static final String NAME = "[[1]]";
	private static final String GC = "[[2]]";
	private static final String SYS_FILTER = "[[3]]";
	private static final String EVAL_FILTER = "[[4]]";
	private static final String FILTER_SUFFIX = "[[5]]";
	private static final String REL_FILTER = "[[6]]";
	private static final String REL_FILTER_SUFFIX = "[[7]]";

	private static final String DEFAULT_GC_NAME = "cdharem.rerelem.t1.v11.xml";
	private static final String SCRIPT_NAME = "rerelemM2_"+NAME+".bat";

	private List<RerelemParticipation> _participations;
	private List<String[]> _relationFilters;
	private List<String[]> _classicFilters;

	private static final String INVOKE_ALIGNER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar -Xmx512M pt.linguateca.harem.Aligner -submissao "+NAME+"_t1.xml -cd "+GC+" > "+NAME+".rerelem2.alinhado";
	private static final String INVOKE_ALIGNMENT_EVALUATOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.IndividualAlignmentEvaluator -alinhamento "+NAME+".rerelem2.alinhado > "+NAME+".rerelem2.alinhado.avalida";
	private static final String INVOKE_ALIGNMENT_FILTER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentFilter -alinhamento "+NAME+".rerelem2.alinhado.avalida -estilo muc -avaliacao "+EVAL_FILTER+" -sistema "+ SYS_FILTER +" > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX;
	private static final String INVOKE_ALT_ORGANIZER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AltAlignmentOrganizer -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+" > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts";

	private static final String INVOKE_EXPANDER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.RelationExpanderM2 -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp";
	
	private static final String INVOKE_SELECTOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentSelector -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec";
	
	private static final String INVOKE_ID_NORMALIZER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.IDNormalizer -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm";

	private static final String INVOKE_ALIGNMENTS_TO_TRIPLES =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentsToTriples -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas";
	
	private static final String INVOKE_RELATIONS_FILTER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.RelationsFilter -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas "+REL_FILTER+" > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas."+REL_FILTER_SUFFIX;

	private static final String INVOKE_RELATIONS_EVALUATOR =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas."+REL_FILTER_SUFFIX+" > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas."+REL_FILTER_SUFFIX+".aval";

	private static final String INVOKE_RELATIONS_REPORTER =
		"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalRelationsEvaluatorM2 -alinhamento "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas."+REL_FILTER_SUFFIX+".aval > "+NAME+".rerelem2.alinhado.avalida."+FILTER_SUFFIX+".alts.exp.selec.norm.triplas."+REL_FILTER_SUFFIX+".aval.resumo";

	public RelationEvaluationScriptGenerator2(){

		_participations = new LinkedList<RerelemParticipation>();
		initParticipations();

		_relationFilters = new LinkedList<String[]>();
		initRelationFilters();

		_classicFilters = new LinkedList<String[]>();
		initClassicFilters();

		new Thread(this).start();
	}

	private void initParticipations()
	{
		_participations = new LinkedList<RerelemParticipation>();
		_participations.add(new RerelemParticipation("participacoes/partic10/partic10_1", asQuotedType(TOTAL_FILTER) ));
		_participations.add(new RerelemParticipation("participacoes/partic10/partic10_2", asQuotedType(TOTAL_FILTER) ));
		_participations.add(new RerelemParticipation("participacoes/partic10/partic10_3_corr", asQuotedType(TOTAL_FILTER) ));
		_participations.add(new RerelemParticipation("participacoes/partic13/partic13_1", "LOCAL(FISICO{*};HUMANO{*})"));
		_participations.add(new RerelemParticipation("participacoes/partic13/partic13_2", "LOCAL(FISICO{*};HUMANO{*})"));
		_participations.add(new RerelemParticipation("participacoes/partic13/partic13_3", "LOCAL(FISICO{*};HUMANO{*})"));
		_participations.add(new RerelemParticipation("participacoes/partic13/partic13_4", "LOCAL(FISICO{*};HUMANO{*})"));
		_participations.add(new RerelemParticipation("participacoes/partic15/partic15_1", asQuotedType(TOTAL_FILTER) ));
		_participations.add(new RerelemParticipation("participacoes/partic15/partic15_no", asQuotedType(TOTAL_FILTER) ));
	}

	private void initRelationFilters()
	{
		String[] todas = {"", "todas"};
		String[] soutra = {"-filtro ident;incluido;inclui;sede_de;ocorre_em", "soutra"};
		String[] ident = {"-filtro ident", "ident"};
		String[] inclusao = {"-filtro incluido;inclui", "inclusao"};
		String[] localizacao = {"-filtro ocorre_em;sede_de", "localizacao"};

		_relationFilters.add(todas);
		_relationFilters.add(soutra);
		_relationFilters.add(ident);
		_relationFilters.add(inclusao);
		_relationFilters.add(localizacao);
	}

	private void initClassicFilters()
	{
		String[] total = {asQuotedType(TOTAL_FILTER) , "total"};
		String[] c5 = {"LOCAL(FISICO{*};HUMANO{*})", "cenario5"};

		_classicFilters.add(total);
		_classicFilters.add(c5);
	}

	public void run(){

		//LinkedList<ParticipationFile> participations = getFilesInfo();
		//System.out.println(filters.toString().replaceAll(", ", "\n"));

		PrintStream filePrinter = null;
		PrintStream participationPrinter = null;

		try {

			filePrinter = new PrintStream("rerelem_tudo.bat");
			
			for(RerelemParticipation p : _participations)
			{
				participationPrinter = new PrintStream(getParticipationScriptName(p));
				//participationPrinter = System.out;

				participationPrinter.println(invokeAligner(p.getFilePath(), DEFAULT_GC_NAME));
				participationPrinter.println(invokeAlignmentEvaluator(p.getFilePath()));

				for(String[] filter : _classicFilters)
				{
					participationPrinter.println("");
					participationPrinter.println("");
					participationPrinter.println(invokeAlignmentFilter(p.getFilePath(), filter[0], p.getSystemFilter(), filter[1]));
					participationPrinter.println(invokeAltOrganizer(p.getFilePath(), filter[1]));
					participationPrinter.println(invokeRelationExpander(p.getFilePath(), filter[1]));
					participationPrinter.println(invokeSelector(p.getFilePath(), filter[1]));
					participationPrinter.println(invokeIDNormalizer(p.getFilePath(), filter[1]));
					participationPrinter.println(invokeAlignmentsToTriples(p.getFilePath(), filter[1]));
			

					for(String[] relFilter : _relationFilters)
					{
						participationPrinter.println("");
						participationPrinter.println(invokeRelationsFilter(p.getFilePath(), filter[1], relFilter[0], relFilter[1]));
						participationPrinter.println(invokeRelationsEvaluator(p.getFilePath(), filter[1], relFilter[1]));
						participationPrinter.println(invokeRelationsReporter(p.getFilePath(), filter[1], relFilter[1]));
					}
				}
				
				filePrinter.println("CALL "+getParticipationScriptName(p));
			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String asQuotedType(String type)
	{
		return "\"" + type + "\"";
	}
	
	private String getParticipationScriptName(RerelemParticipation part){
		return SCRIPT_NAME.replace(NAME, part.getParticipationName());
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

	private String invokeAltOrganizer(String name, String suffix){
		return INVOKE_ALT_ORGANIZER.replace(NAME, name).replace(FILTER_SUFFIX, suffix);
	}

	private String invokeSelector(String name, String suffix){
		return INVOKE_SELECTOR.replace(NAME, name).replace(FILTER_SUFFIX, suffix);
	}
	
	private String invokeIDNormalizer(String name, String suffix){
		return INVOKE_ID_NORMALIZER.replace(NAME, name).replace(FILTER_SUFFIX, suffix);
	}

	private String invokeAlignmentsToTriples(String name, String suffix){
		return INVOKE_ALIGNMENTS_TO_TRIPLES.replace(NAME, name).replace(FILTER_SUFFIX, suffix);
	}
	
	private String invokeRelationExpander(String name, String suffix){
		return INVOKE_EXPANDER.replace(NAME, name).replace(FILTER_SUFFIX, suffix);
	}

	private String invokeRelationsFilter(String name, String suffix1, String relFilter, String suffix2){
		return INVOKE_RELATIONS_FILTER.replace(NAME, name).replace(FILTER_SUFFIX, suffix1)
		.replace(REL_FILTER, relFilter).replace(REL_FILTER_SUFFIX, suffix2);
	}

	private String invokeRelationsEvaluator(String name, String suffix1, String suffix2){
		return INVOKE_RELATIONS_EVALUATOR.replace(NAME, name).replace(FILTER_SUFFIX, suffix1)
		.replace(REL_FILTER_SUFFIX, suffix2);
	}

	private String invokeRelationsReporter(String name, String suffix1, String suffix2){
		return INVOKE_RELATIONS_REPORTER.replace(NAME, name).replace(FILTER_SUFFIX, suffix1)
		.replace(REL_FILTER_SUFFIX, suffix2);
	}

	public static void main(String args[]){

		new RelationEvaluationScriptGenerator2();
	}

	class RerelemParticipation{

		private File _file;
		private String _systemFilter;

		public RerelemParticipation(String file, String filter)
		{
			_file = new File(file);
			_systemFilter = filter;
		}

		public File getFile()
		{
			return _file;
		}

		public String getFilePath()
		{
			return _file.getPath();
		}

		public String getSystemFilter()
		{
			return _systemFilter;
		}

		public String getParticipationName()
		{
			int index = _file.getPath().lastIndexOf(File.separator);
			return _file.getPath().substring(index+1);
		}
		
		/*public String getParticipantName()
		{
			return _file.getParentFile().getName();
		}*/
	}

}
