/*
 * Created on Jun 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;


/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class GlobalRelationsReporter extends GlobalReporter
{
	private EntitiesAttributesTree _filter;
	private String _relationsFilter;
	private HashMap<String, ParticipationFile> _participationInfo;
	private boolean _showFilter;
	private boolean _deprecated;

	public GlobalRelationsReporter(String filter, boolean debug, int submission_types,
			String unofficialNames,	String dir, String info, boolean showFilter, boolean deprecated)
	{
		super(filter, debug, submission_types, unofficialNames, dir);

		if(info != null)
			_participationInfo = getParticipationInfo(info);

		_showFilter = showFilter;
		_deprecated = deprecated;

		new Thread(this).start();
	}

	protected void writeOut(LinkedList<? extends Report> reports)
	{
		Iterator<? extends Report> i;
		RelationsReport current;

		LinkedList<String> shuffled = getShuffled(_names);
		LinkedHashMap<RelationsReport, String> codeNames = new LinkedHashMap<RelationsReport, String>();

		for (i = reports.iterator(); i.hasNext();)
		{
			current = (RelationsReport) i.next();

			if (_debug)
			{
				codeNames.put(current, current.toString());
			}
			else
			{
				codeNames.put(current, shuffled.removeFirst());
			}
		}

		if (!_debug)
		{
			System.out.println(codeNames);
		}

		writeHeader();

		if(_showFilter)
			writeFilter();

		try
		{
			writeTable(reports, codeNames, GlobalRelationsEvaluatorM2.RELATIONS_HEADER);
			
			if(_deprecated)
			{
				writeTable(reports, codeNames, GlobalRelationsEvaluatorM2.CORELS_HEADER);
				writeTable(reports, codeNames, GlobalRelationsEvaluatorM2.SCORE_HEADER);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		writeFooter();
	}

	private void writeFilter()
	{
		if(_filter != null)
		{
			System.out.print("<br>");
			System.out.println("Cen&aacute;rio HAREM:");
			System.out.print("<br>");
			System.out.println(_filter);
		}
		
		if(_relationsFilter != null)
		{
			System.out.print("<br>");
			System.out.println("Rela&ccedil;&otilde;es:");
			System.out.print("<br>");
			System.out.println(_relationsFilter);
		}
	}

	private void writeTable(LinkedList<? extends Report> reports, LinkedHashMap<RelationsReport, String> codeNames, String type) throws Exception
	{
		int colspan = 8;
		System.out.println("<br>");
		System.out.println("<br>");
		System.out.println("<table border>");
		System.out.println("<tr>");

		if (_debug)
		{
			colspan += 2;
			// hack to keep results from same system together
			Collections.sort(reports);
			Collections.sort(reports, new NameComparator());
		}

		if (type == GlobalRelationsEvaluatorM2.HEADER)
		{
			colspan -= 2;
		}

		System.out.println("<tr> <TH COLSPAN=" + colspan + ">" + type + "</TH> </tr>");
		System.out.println("<td>" + SYSTEM + "</td>");

		/*System.out.println("<td>" + GlobalRelationsEvaluatorM2.PRECISION + " (%) </td>");
		  System.out.println("<td>" + GlobalRelationsEvaluatorM2.RECALL + " (%) </td>");*/
		System.out.println("<td>" + GlobalRelationsEvaluatorM2.PRECISION + " </td>");
		System.out.println("<td>" + GlobalRelationsEvaluatorM2.RECALL + " </td>");
		System.out.println("<td>" + GlobalRelationsEvaluatorM2.F_MEASURE + "</td>");

		if (_debug)
		{
			if (type != GlobalRelationsEvaluatorM2.SCORE_HEADER)
			{
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.SPURIOUS + "</td>");
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.MISSING + "</td>");
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.TOTAL_IN_GC + "</td>");
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.TOTAL_IDENTIFIED + "</td>");
				if (type == GlobalRelationsEvaluatorM2.CORELS_HEADER)
				{
					System.out.println("<td>" + GlobalRelationsEvaluatorM2.TOTAL_CORRECT_M + "</td>");
				}
				else
				{
					System.out.println("<td>" + GlobalRelationsEvaluatorM2.TOTAL_CORRECT_F + "</td>");
				}
			}
			else
			{
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.SCORE_IN_GC + "</td>");
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.MAXIMUM_SYSTEM_SCORE + "</td>");
				System.out.println("<td>" + GlobalRelationsEvaluatorM2.SYSTEM_SCORE + "</td>");
			}
		}

		System.out.println("</tr>");

		Method method = null;

		if( GlobalRelationsEvaluatorM2.CORELS_HEADER.equals(type) ){
			method = RelationsReport.class.getDeclaredMethod("getValueInCorelsMap", new Class[]{ String.class });
			Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.F_MEASURE,
					ScoreComparator.CORELS_DIMENSION));
		} else if( GlobalRelationsEvaluatorM2.RELATIONS_HEADER.equals(type)){
			method = RelationsReport.class.getDeclaredMethod("getValueInRelationsMap", new Class[]{ String.class });
			Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.F_MEASURE,
					ScoreComparator.RELATIONS_DIMENSION));
		} else if( GlobalRelationsEvaluatorM2.SCORE_HEADER.equals(type)){
			method = RelationsReport.class.getDeclaredMethod("getValueInScoreMap", new Class[]{ String.class });
			Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.F_MEASURE,
					ScoreComparator.SCORE_DIMENSION));
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}

		Collections.reverse(reports);

		RelationsReport current;
		for (Iterator<? extends Report> i = reports.iterator(); i.hasNext();)
		{
			current = (RelationsReport) i.next();

			if (!current.isPrintable())
				continue;

			System.out.print("<tr>");
			System.out.print("<td>");
			if (isOfficial(current.getSystemName()))
			{
				System.out.print("<b>");
			}
			if (current.getSystemName().indexOf(SELECTIVE) != -1)
			{
				System.out.print("<i>");
			}
			System.out.print((String) codeNames.get(current));
			if (current.getSystemName().indexOf(SELECTIVE) != -1)
			{
				System.out.print("</i>");
			}
			if (isOfficial(current.getSystemName()))
			{
				System.out.print("</b>");
			}
			System.out.print("</td>");
			// Number

			// com percentagem
			/* writeValueInCell(System.out.print(getPercentage(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.PRECISION })))),
			   _bestResults.get(type + GlobalRelationsEvaluatorM2.PRECISION).contains(current));*/
			writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.PRECISION }))),
					_bestResults.get(type + GlobalRelationsEvaluatorM2.PRECISION).contains(current));

			// com percentagem
			/* writeValueInCell(System.out.print(getPercentage(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.RECALL })))),
			   _bestResults.get(type + GlobalRelationsEvaluatorM2.RECALL).contains(current));*/
			writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.RECALL }))),
					_bestResults.get(type + GlobalRelationsEvaluatorM2.RECALL).contains(current));

			writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.F_MEASURE }))),
					_bestResults.get(type + GlobalRelationsEvaluatorM2.F_MEASURE).contains(current));

			if (_debug)
			{
				if (type != GlobalRelationsEvaluatorM2.SCORE_HEADER)
				{
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.SPURIOUS }))),false);
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.MISSING }))),false);
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.TOTAL_IN_GC }))),false);
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.TOTAL_IDENTIFIED }))),false);
					if (type == GlobalRelationsEvaluatorM2.CORELS_HEADER)
					{
						writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.TOTAL_CORRECT_M }))),false);
					}
					else
					{
						writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.TOTAL_CORRECT_F }))),false);
					}
				}
				else
				{
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.SCORE_IN_GC }))),false);
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.MAXIMUM_SYSTEM_SCORE }))),false);
					writeValueInCell(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalRelationsEvaluatorM2.SYSTEM_SCORE }))),false);
				}
			}
			System.out.println("</tr>"); 
		}

		System.out.println("</table>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.linguateca.harem.GlobalReporter#setBestResults(java.util.LinkedList)
	 */
	protected void setBestResults(LinkedList<? extends Report> reports)
	{
		///////////////////
		// CORELS_DIMENSION 
		//////////////////
		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.PRECISION,
				ScoreComparator.CORELS_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.CORELS_HEADER, GlobalRelationsEvaluatorM2.PRECISION,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.CORELS_HEADER + GlobalRelationsEvaluatorM2.PRECISION,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.RECALL,
				ScoreComparator.CORELS_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.CORELS_HEADER, GlobalRelationsEvaluatorM2.RECALL,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.CORELS_HEADER + GlobalRelationsEvaluatorM2.RECALL,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.F_MEASURE,
				ScoreComparator.CORELS_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.CORELS_HEADER, GlobalRelationsEvaluatorM2.F_MEASURE,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.CORELS_HEADER + GlobalRelationsEvaluatorM2.F_MEASURE,
		// reports.getLast());

		///////////////////
		// RELATIONS_DIMENSION 
		//////////////////
		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.PRECISION,
				ScoreComparator.RELATIONS_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.RELATIONS_HEADER, GlobalRelationsEvaluatorM2.PRECISION,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.RELATIONS_HEADER + GlobalRelationsEvaluatorM2.PRECISION,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.RECALL,
				ScoreComparator.RELATIONS_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.RELATIONS_HEADER, GlobalRelationsEvaluatorM2.RECALL,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.RELATIONS_HEADER + GlobalRelationsEvaluatorM2.RECALL,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.F_MEASURE,
				ScoreComparator.RELATIONS_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.RELATIONS_HEADER, GlobalRelationsEvaluatorM2.F_MEASURE,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.RELATIONS_HEADER + GlobalRelationsEvaluatorM2.F_MEASURE,
		// reports.getLast());

		///////////////////
		// SCORE_DIMENSION 
		//////////////////
		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.PRECISION,
				ScoreComparator.SCORE_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.SCORE_HEADER, GlobalRelationsEvaluatorM2.PRECISION,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.SCORE_HEADER + GlobalRelationsEvaluatorM2.PRECISION,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.RECALL,
				ScoreComparator.SCORE_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.SCORE_HEADER, GlobalRelationsEvaluatorM2.RECALL,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.SCORE_HEADER + GlobalRelationsEvaluatorM2.RECALL,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalRelationsEvaluatorM2.F_MEASURE,
				ScoreComparator.SCORE_DIMENSION));
		addToBestResults(GlobalRelationsEvaluatorM2.SCORE_HEADER, GlobalRelationsEvaluatorM2.F_MEASURE,
				(RelationsReport) reports.getLast(), (LinkedList<RelationsReport>)reports);
		// _bestResults.put(GlobalRelationsEvaluatorM2.SCORE_HEADER + GlobalRelationsEvaluatorM2.F_MEASURE,
		// reports.getLast());
	}

	private void addToBestResults(String map, String key, RelationsReport best, LinkedList<RelationsReport> reports)
	{
		RelationsReport current;
		if (key.equals(GlobalRelationsEvaluatorM2.PRECISION) || key.equals(GlobalRelationsEvaluatorM2.RECALL)
				|| key.equals(GlobalRelationsEvaluatorM2.F_MEASURE))
		{
			Collections.reverse(reports);
		}

		for (Iterator<RelationsReport> i = reports.iterator(); i.hasNext();)
		{
			current = i.next();

			if(getValueInMap(map, best, key) == null || getValueInMap(map, current, key) == null)
				break;

			if (!getValueInMap(map, best, key).equals(getValueInMap(map, current, key)))
				break;

			_bestResults.put(map + key, current);
		}
	}

	private String getValueInMap(String type, RelationsReport report, String key)
	{
		if (GlobalRelationsEvaluatorM2.CORELS_HEADER.equals(type)) {
			return report.getValueInCorelsMap(key);
		}
		else if (GlobalRelationsEvaluatorM2.RELATIONS_HEADER.equals(type)) {
			return report.getValueInRelationsMap(key);
		}
		else if (GlobalRelationsEvaluatorM2.SCORE_HEADER.equals(type)) {
			return report.getValueInScoreMap(key);
		}
		else{
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	protected Report createReport(String file)
	{	
		RelationsReport report = new RelationsReport(file);
		BufferedReader reader;
		String buffer;
		String[] tokens;
		int state = -1;

		try
		{
			//reader = new BufferedReader(new FileReader(_dir+System.getProperty("file.separator")+file));
			reader = new BufferedReader(new FileReader(file));

			while ((buffer = reader.readLine()) != null)
			{

				if (buffer.startsWith(GlobalRelationsEvaluatorM2.FILTER1))
				{
					state = 0;
					continue;
				}

				if (buffer.startsWith(GlobalRelationsEvaluatorM2.FILTER2))
				{
					state = 1;
					continue;
				}

				if (state == 0 && buffer.startsWith("\t#"))
				{
					loadCategoryAndTypes(buffer);
					continue;
				}
				
				if (state == 1 && buffer.startsWith("\t#"))
				{
					loadRelationTypes(buffer);
					continue;
				}

				if (state == 0)
				{
					state = 1;
					continue;
				}


				if (buffer.equals(GlobalRelationsEvaluatorM2.CORELS_HEADER))
				{
					state = 2;
					continue;
				}

				if (buffer.equals(GlobalRelationsEvaluatorM2.RELATIONS_HEADER))
				{
					state = 3;
					continue;
				}

				if (buffer.equals(GlobalRelationsEvaluatorM2.SCORE_HEADER))
				{
					state = 4;
					continue;
				}

				tokens = buffer.split(": ");
				if (tokens.length != 2)
					continue;

				switch (state) {
				case 2: report.putInCorelsMap(tokens[0].trim(), tokens[1]); break;
				case 3: report.putInRelationsMap(tokens[0].trim(), tokens[1]); break;
				case 4: report.putInScoreMap(tokens[0].trim(), tokens[1]); break;
				}
			}
			reader.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		file = fileToSystem(file);
		report.setSystemName(file);

		return report;
	}

	private String fileToSystem(String file){

		file = removeExtensions(file);

		if(_participationInfo == null)
			return file;

		ParticipationFile part = _participationInfo.get(file);		
		return (part != null ? part.getSystemName()+part.getRunNo() : removeExtensions(file));
	}

	private String removeExtensions(String fileName){
		int index = fileName.indexOf(".");
		return (index < 1 ? fileName : fileName.substring(0, index));
	}

	private void loadCategoryAndTypes(String buffer)
	{
		buffer = buffer.replaceAll("\t", "");
		_filter =  new EntitiesAttributesTree(buffer, TagBase.getInstance());
	}

	private void loadRelationTypes(String buffer)
	{
		buffer = buffer.replaceAll("\t", "");
		_relationsFilter = buffer.substring(1);
	}

	/**
	 * Cria um dicionario para converter o nome dos ficheiros
	 * numa ficha com toda a informacao do ficheiro
	 * @param infoFile
	 * @return
	 */
	private HashMap<String, ParticipationFile> getParticipationInfo(String infoFile){

		HashMap<String, ParticipationFile> map = new HashMap<String, ParticipationFile>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(infoFile));

			String line = null;
			ParticipationFile part = null;

			while((line = reader.readLine()) != null){
				part = new ParticipationFile(line);
				map.put(part.getPath(), part);
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

		return map;
	}

	public static void main(String[] args)
	{
		String filter = null;
		int submissions = 0;
		String unofficialNames = null;
		boolean debug = false;
		String dir = null;
		String info = null;
		boolean showFilter = false;
		boolean deprecated = false;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-filtro"))
			{
				if(i < args.length)
					filter = args[++i];

				continue;
			}

			if (args[i].equals("-depurar"))
			{
				debug = true;
				continue;
			}

			if (args[i].equals("-saidas"))
			{
				i++;
				if (args[i].equals("oficiais"))
					submissions = OFFICIAL_SUBMISSIONS;

				if (args[i].equals("naooficiais"))
					submissions = UNOFFICIAL_SUBMISSIONS;

				continue;
			}

			if (args[i].equals("-naooficiais"))
			{
				i++;
				unofficialNames = args[i];
				continue;
			}

			if (args[i].equals("-cenario"))
			{
				showFilter = true;
				continue;
			}

			if (args[i].equals("-dir"))
			{
				if(i < args.length)
					dir = args[++i];

				continue;
			}

			if (args[i].equals("-info"))
			{
				if(i < args.length)
					info = args[++i];

				continue;
			}
			
			if (args[i].equals("-desaconselhadas"))
			{
				deprecated = true;
				continue;
			}
		}

		if (filter == null)
		{
			printSynopsis();
			return;
		}

		new GlobalRelationsReporter(filter, debug, submissions, unofficialNames, dir, info, showFilter, deprecated);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalRelationsReporter [-depurar] " +
				"-filtro <filtro> -dir <directorio_participacoes> [-info lista_participantes.csv] [-desaconselhadas]");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalRelationsReporter -depurar -filtro .expandido-partnao.normalizado.avaliado.resumo -dir participacoes -info lista_participantes.csv");
	}

	class ScoreComparator implements Comparator
	{
		private final static int SCORE_DIMENSION = 0;

		private final static int CORELS_DIMENSION = 1;

		private final static int RELATIONS_DIMENSION = 2;

		private String _key;

		private int _dimension;

		public ScoreComparator(String key, int dimension)
		{
			_key = key;
			_dimension = dimension;
		}

		public int compare(Object o1, Object o2)
		{
			RelationsReport r1 = (RelationsReport) o1;
			RelationsReport r2 = (RelationsReport) o2;

			String s1 = null;
			String s2 = null;

			double score1 = 0;
			double score2 = 0;
			double diff = 0;

			switch (_dimension){
			case SCORE_DIMENSION:
				s1 = r1.getValueInScoreMap(_key);
				s2 = r2.getValueInScoreMap(_key);
				break;
			case CORELS_DIMENSION:
				s1 = r1.getValueInCorelsMap(_key);
				s2 = r2.getValueInCorelsMap(_key);
				break;
			case RELATIONS_DIMENSION:
				s1 = r1.getValueInRelationsMap(_key);
				s2 = r2.getValueInRelationsMap(_key);
				break;
			}
			score1 = (s1 != null ? Double.parseDouble(s1) : -1);
			score2 = (s2 != null ? Double.parseDouble(s2) : -1);

			if (Double.isNaN(score1))
				score1 = getNaNMask();

			if (Double.isNaN(score2))
				score2 = getNaNMask();

			diff = score1 - score2;

			if (diff < 0)
				return -1;

			if (diff > 0)
				return 1;

			return 0;
		}

		private double getNaNMask()
		{
			if (_key.equals(GlobalRelationsEvaluatorM2.PRECISION) || 
					_key.equals(GlobalRelationsEvaluatorM2.RECALL)
					|| _key.equals(GlobalRelationsEvaluatorM2.F_MEASURE))

			{
				return Double.MIN_VALUE;
			}
			return Double.MAX_VALUE;
		}
	}

	class NameComparator implements Comparator
	{

		public int compare(Object o1, Object o2)
		{
			RelationsReport r1 = (RelationsReport) o1;
			RelationsReport r2 = (RelationsReport) o2;
			return stripDirectory(r1).compareTo(stripDirectory(r2));
		}

		private String stripDirectory(RelationsReport report)
		{		
			String[] stripped = report.getSystemName().split("\\"+File.separator);
			return stripped[stripped.length - 1];
		}

	}
}
