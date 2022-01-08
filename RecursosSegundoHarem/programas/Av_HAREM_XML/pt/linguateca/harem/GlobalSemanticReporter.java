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
import java.util.Collection;
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
public class GlobalSemanticReporter extends GlobalReporter
{
	private EntitiesAttributesTree _filter;
	private HashMap<String, ParticipationFile> _participationInfo;
	private boolean _showFilter;

	public GlobalSemanticReporter(String filter, boolean debug, int submission_types,
			String unofficialNames,	String dir, String info, boolean showFilter)
	{
		super(filter, debug, submission_types, unofficialNames, dir);

		if(info != null)
			_participationInfo = getParticipationInfo(info);
		
		_showFilter = showFilter;
		
		new Thread(this).start();
	}

	protected void writeOut(LinkedList<? extends Report> reports)
	{
		Iterator<? extends Report> i;
		SemanticReport current;

		LinkedList<String> shuffled = getShuffled(_names);
		LinkedHashMap<SemanticReport, String> codeNames = new LinkedHashMap<SemanticReport, String>();

		for (i = reports.iterator(); i.hasNext();)
		{
			current = (SemanticReport) i.next();

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
			writeTable(reports, codeNames, GlobalSemanticEvaluator.COMBINED_TOKEN);
			writeTable(reports, codeNames, GlobalSemanticEvaluator.IDENTIFICATION_TOKEN);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		writeFooter();
	}

	private void writeFilter()
	{
		System.out.print("<br>");
		System.out.println("Cen&aacute;rio:");
		System.out.print("<br>");
		System.out.println(_filter);
	}

	private void writeTable(LinkedList<? extends Report> reports, LinkedHashMap<SemanticReport, String> codeNames, String type) throws Exception
	{
		int colspan = 6;
		System.out.println("<br>");
		System.out.println("<br>");
		System.out.println("<table border>");
		System.out.println("<tr>");
		Collection bucket;

		if (_debug)
		{
			colspan += 2;
			// hack to keep results from same system together
			Collections.sort(reports);
			Collections.sort(reports, new NameComparator());
		}

		if (type == GlobalSemanticEvaluator.COMBINED_TOKEN)
		{
			colspan -= 2;
		}

		System.out.println("<tr> <TH COLSPAN=" + colspan + ">" + type + "</TH> </tr>");
		System.out.println("<td>" + SYSTEM + "</td>");

		if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
		{
			/*System.out.println("<td>" + GlobalAlignmentEvaluator.PRECISION + " (%) </td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.RECALL + " (%) </td>");*/
			System.out.println("<td>" + GlobalAlignmentEvaluator.PRECISION + " </td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.RECALL + " </td>");
		}
		else
		{
			/*System.out.println("<td>" + GlobalSemanticEvaluator.MAXIMUM_PRECISION + " (%) </td>");
			System.out.println("<td>" + GlobalSemanticEvaluator.MAXIMUM_RECALL + " (%) </td>");*/
			System.out.println("<td>" + GlobalSemanticEvaluator.MAXIMUM_PRECISION + " </td>");
			System.out.println("<td>" + GlobalSemanticEvaluator.MAXIMUM_RECALL + " </td>");
		}
		System.out.println("<td>" + GlobalAlignmentEvaluator.F_MEASURE + "</td>");
		if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
		{
			// System.out.println("<td>" + GlobalAlignmentEvaluator.COMBINED_ERROR + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.OVER_GENERATION + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.UNDER_GENERATION + "</td>");
		}
		if (_debug)
		{
			if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
			{
				System.out.println("<td>" + GlobalSemanticEvaluator.SEMANTIC_TOTAL_IN_GC + "</td>");
				System.out.println("<td>" + GlobalSemanticEvaluator.SEMANTIC_TOTAL_IDENTIFIED + "</td>");
			}
			else
			{
				System.out.println("<td>" + GlobalSemanticEvaluator.MAXIMUM_CLASSIFICATION_IN_GC + "</td>");
				System.out.println("<td>" + GlobalSemanticEvaluator.MAXIMUM_CLASSIFICATION_IN_SYSTEM + "</td>");
			}
		}

		System.out.println("</tr>");

		Method method = null;

		if (type == GlobalSemanticEvaluator.IDENTIFICATION_TOKEN)
		{
			method = SemanticReport.class.getDeclaredMethod("getValueInIdMap", new Class[]
			                                                                             { String.class });
			Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.F_MEASURE,
					ScoreComparator.ID_DIMENSION));
		}
		else if (type == GlobalSemanticEvaluator.COMBINED_TOKEN)
		{
			method = SemanticReport.class.getDeclaredMethod("getValueInCombinedMap", new Class[]
			                                                                                   { String.class });
			Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.F_MEASURE,
					ScoreComparator.COMBINED_DIMENSION));
		}

		Collections.reverse(reports);

		SemanticReport current;
		for (Iterator<? extends Report> i = reports.iterator(); i.hasNext();)
		{
			current = (SemanticReport) i.next();
			
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
			System.out.print("<td>");
			if ((((bucket = _bestResults.get(type + GlobalAlignmentEvaluator.PRECISION)) != null) && bucket
					.contains(current))
					|| (((bucket = _bestResults.get(type + GlobalSemanticEvaluator.MAXIMUM_PRECISION)) != null) && bucket
							.contains(current)))
			{
				System.out.print("<font color=green><b>");
			}

			if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
			{
				//com percentagem
				/*System.out.print(getPercentage(getZerofiedNaN((String) method.invoke(current, new Object[]
				                                                                                         { GlobalAlignmentEvaluator.PRECISION }))));*/
				System.out.print(pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[] { GlobalAlignmentEvaluator.PRECISION }))));
			}
			else
			{
				//com percentagem
				/*System.out.print(getPercentage((String) method.invoke(current, new Object[]
				                                                                          { GlobalSemanticEvaluator.MAXIMUM_PRECISION })));*/
				System.out.print( pointToComma((String) method.invoke(current, new Object[] { GlobalSemanticEvaluator.MAXIMUM_PRECISION })));
			}

			if ((((bucket = _bestResults.get(type + GlobalAlignmentEvaluator.PRECISION)) != null) && bucket
					.contains(current))
					|| (((bucket = _bestResults.get(type + GlobalSemanticEvaluator.MAXIMUM_PRECISION)) != null) && bucket
							.contains(current)))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if ((((bucket = _bestResults.get(type + GlobalAlignmentEvaluator.RECALL)) != null) && bucket
					.contains(current))
					|| (((bucket = _bestResults.get(type + GlobalSemanticEvaluator.MAXIMUM_RECALL)) != null) && bucket
							.contains(current)))
			{
				System.out.print("<font color=green><b>");
			}

			if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
			{
				//com percentagem
				/*System.out.print(getPercentage((String) method.invoke(current, new Object[]
				                                                                          { GlobalAlignmentEvaluator.RECALL })));*/
				System.out.print( pointToComma((String) method.invoke(current, new Object[] { GlobalAlignmentEvaluator.RECALL })));
			}
			else
			{
				//com percentagem
				/*System.out.print(getPercentage((String) method.invoke(current, new Object[]
				                                                                          { GlobalSemanticEvaluator.MAXIMUM_RECALL })));*/
				System.out.print( pointToComma((String) method.invoke(current, new Object[] { GlobalSemanticEvaluator.MAXIMUM_RECALL })));
			}

			if ((((bucket = _bestResults.get(type + GlobalAlignmentEvaluator.RECALL)) != null) && bucket
					.contains(current))
					|| (((bucket = _bestResults.get(type + GlobalSemanticEvaluator.MAXIMUM_RECALL)) != null) && bucket
							.contains(current)))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if (_bestResults.get(type + GlobalAlignmentEvaluator.F_MEASURE).contains(current))
			{
				System.out.print("<font color=green><b>");
			}

			System.out.print( pointToComma(getZerofiedNaN((String) method.invoke(current, new Object[]
			                                                                           { GlobalAlignmentEvaluator.F_MEASURE }))));

			if (_bestResults.get(type + GlobalAlignmentEvaluator.F_MEASURE).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");

			if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
			{
				System.out.print("<td>");
				if (_bestResults.get(type + GlobalAlignmentEvaluator.OVER_GENERATION).contains(current))
				{
					System.out.print("<font color=green><b>");
				}

				System.out.print(pointToComma((String) method.invoke(current, new Object[]
				                                                            { GlobalAlignmentEvaluator.OVER_GENERATION })));

				if (_bestResults.get(type + GlobalAlignmentEvaluator.OVER_GENERATION).contains(current))
				{
					System.out.print("</b></font>");
				}
				System.out.print("</td>");
				System.out.print("<td>");
				if (_bestResults.get(type + GlobalAlignmentEvaluator.UNDER_GENERATION).contains(current))
				{
					System.out.print("<font color=green><b>");
				}

				System.out.print(pointToComma((String) method.invoke(current, new Object[]
				                                                            { GlobalAlignmentEvaluator.UNDER_GENERATION })));

				if (_bestResults.get(type + GlobalAlignmentEvaluator.UNDER_GENERATION).contains(current))
				{
					System.out.print("</b></font>");
				}
				System.out.print("</td>");
			}

			if (_debug)
			{
				System.out.print("<td>");
				if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
				{
					System.out.print(pointToComma((String) method.invoke(current, new Object[]
					                                                            { GlobalSemanticEvaluator.SEMANTIC_TOTAL_IN_GC })));
				}
				else
				{
					System.out.print(pointToComma((String) method.invoke(current, new Object[]
					                                                            { GlobalSemanticEvaluator.MAXIMUM_CLASSIFICATION_IN_GC })));
				}

				System.out.print("</td>");
				System.out.print("<td>");
				if (type != GlobalSemanticEvaluator.COMBINED_TOKEN)
				{
					System.out.print(pointToComma((String) method.invoke(current, new Object[]
					                                                            { GlobalSemanticEvaluator.SEMANTIC_TOTAL_IDENTIFIED })));
				}
				else
				{
					System.out.print(pointToComma((String) method.invoke(current, new Object[]
					                                                            { GlobalSemanticEvaluator.MAXIMUM_CLASSIFICATION_IN_SYSTEM })));
				}

				System.out.print("</td>");

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
		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.PRECISION,
				ScoreComparator.ID_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN, GlobalAlignmentEvaluator.PRECISION,
				(SemanticReport) reports.getLast(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN + GlobalAlignmentEvaluator.PRECISION,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.RECALL,
				ScoreComparator.ID_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN, GlobalAlignmentEvaluator.RECALL,
				(SemanticReport) reports.getLast(), (LinkedList<SemanticReport>)reports); // _bestResults.put(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN
		// +
		// GlobalAlignmentEvaluator.RECALL,
		// reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.F_MEASURE,
				ScoreComparator.ID_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN, GlobalAlignmentEvaluator.F_MEASURE,
				(SemanticReport) reports.getLast(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN + GlobalAlignmentEvaluator.F_MEASURE,
		// reports.getLast());

		// Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.COMBINED_ERROR,
		// ScoreComparator.ID_DIMENSION));
		// _bestResults.put(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN +
		// GlobalAlignmentEvaluator.COMBINED_ERROR, reports.getFirst());

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.OVER_GENERATION,
				ScoreComparator.ID_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN, GlobalAlignmentEvaluator.OVER_GENERATION,
				(SemanticReport) reports.getFirst(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN +
		// GlobalAlignmentEvaluator.OVER_GENERATION, reports.getFirst());

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.UNDER_GENERATION,
				ScoreComparator.ID_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN, GlobalAlignmentEvaluator.UNDER_GENERATION,
				(SemanticReport) reports.getFirst(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN +
		// GlobalAlignmentEvaluator.UNDER_GENERATION, reports.getFirst());

		Collections.sort(reports, new ScoreComparator(GlobalSemanticEvaluator.MAXIMUM_PRECISION,
				ScoreComparator.COMBINED_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.COMBINED_TOKEN, GlobalSemanticEvaluator.MAXIMUM_PRECISION,
				(SemanticReport) reports.getLast(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.COMBINED_TOKEN +
		// GlobalSemanticEvaluator.MAXIMUM_PRECISION, reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalSemanticEvaluator.MAXIMUM_RECALL,
				ScoreComparator.COMBINED_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.COMBINED_TOKEN, GlobalSemanticEvaluator.MAXIMUM_RECALL,
				(SemanticReport) reports.getLast(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.COMBINED_TOKEN +
		// GlobalSemanticEvaluator.MAXIMUM_RECALL, reports.getLast());

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.F_MEASURE,
				ScoreComparator.COMBINED_DIMENSION));
		addToBestResults(GlobalSemanticEvaluator.COMBINED_TOKEN, GlobalAlignmentEvaluator.F_MEASURE,
				(SemanticReport) reports.getLast(), (LinkedList<SemanticReport>)reports);
		// _bestResults.put(GlobalSemanticEvaluator.COMBINED_TOKEN + GlobalAlignmentEvaluator.F_MEASURE,
		// reports.getLast());

	}

	private void addToBestResults(String map, String key, SemanticReport best, LinkedList<SemanticReport> reports)
	{
		SemanticReport current;
		if (key.equals(GlobalAlignmentEvaluator.PRECISION) || key.equals(GlobalAlignmentEvaluator.RECALL)
				|| key.equals(GlobalAlignmentEvaluator.F_MEASURE)
				|| key.equals(GlobalSemanticEvaluator.MAXIMUM_RECALL)
				|| key.equals(GlobalSemanticEvaluator.MAXIMUM_PRECISION))
		{
			Collections.reverse(reports);
		}

		for (Iterator<SemanticReport> i = reports.iterator(); i.hasNext();)
		{
			current = i.next();

			if(getValueInMap(map, best, key) == null || getValueInMap(map, current, key) == null)
				break;

			if (!getValueInMap(map, best, key).equals(getValueInMap(map, current, key)))
				break;

			_bestResults.put(map + key, current);
		}
	}

	private String getValueInMap(String type, SemanticReport report, String key)
	{
		if (type.equals(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN))
		{
			return report.getValueInIdMap(key);
		}
		else if (type.equals(GlobalSemanticEvaluator.COMBINED_TOKEN))
		{
			return report.getValueInCombinedMap(key);
		}

		return null;
	}

	protected Report createReport(String file)
	{	
		SemanticReport report = new SemanticReport(file);
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

				if (buffer.startsWith(GlobalSemanticEvaluator.FILTER))
				{
					state = 0;
					continue;
				}

				if (state == 0 && !buffer.equals(""))
				{
					loadCategoryAndTypes(buffer);
					continue;
				}

				if (state == 0)
				{
					state = 1;
					continue;
				}

				if (buffer.equals(GlobalSemanticEvaluator.IDENTIFICATION_TOKEN))
				{
					state = 2;
					continue;
				}

				if (buffer.equals(GlobalSemanticEvaluator.COMBINED_TOKEN))
				{
					state = 3;
					continue;
				}

				tokens = buffer.split(": ");
				if (tokens.length != 2)
					continue;

				if (state == 2)
				{
					report.putInIdMap(tokens[0].trim(), tokens[1]);
					continue;
				}

				if (state == 3)
				{
					report.putInCombinedMap(tokens[0].trim(), tokens[1]);
					continue;
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
		}

		if (filter == null)
		{
			printSynopsis();
			return;
		}

		new GlobalSemanticReporter(filter, debug, submissions, unofficialNames, dir, info, showFilter);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalSemanticReporter [-depurar] -filtro <filtro> -dir <directorio_participacoes> [-info lista_participantes.csv]");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.GlobalSemanticReporter -depurar -filtro .selectivo.alts.emir.ida -dir participacoes -info lista_participantes.csv");
	}

	class ScoreComparator implements Comparator
	{
		private final static int ID_DIMENSION = 0;

		private final static int TYPE_DIMENSION = 1;

		private final static int COMBINED_DIMENSION = 2;

		private final static int FLAT_DIMENSION = 3;

		private String _key;

		private int _dimension;

		public ScoreComparator(String key, int dimension)
		{
			_key = key;
			_dimension = dimension;
		}

		public int compare(Object o1, Object o2)
		{
			SemanticReport r1 = (SemanticReport) o1;
			SemanticReport r2 = (SemanticReport) o2;

			String s1 = null;
			String s2 = null;

			double score1 = 0;
			double score2 = 0;
			double diff = 0;

			if (_dimension == ID_DIMENSION)
			{
				s1 = r1.getValueInIdMap(_key);
				s2 = r2.getValueInIdMap(_key);

				score1 = (s1 != null ? Double.parseDouble(s1) : -1);
				score2 = (s2 != null ? Double.parseDouble(s2) : -1);
			}

			if (_dimension == COMBINED_DIMENSION)
			{
				s1 = r1.getValueInCombinedMap(_key);
				s2 = r2.getValueInCombinedMap(_key);

				score1 = (s1 != null ? Double.parseDouble(s1) : -1);
				score2 = (s2 != null ? Double.parseDouble(s2) : -1);
			}

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
			if (_key.equals(GlobalAlignmentEvaluator.PRECISION) || _key.equals(GlobalAlignmentEvaluator.RECALL)
					|| _key.equals(GlobalAlignmentEvaluator.F_MEASURE)
					|| _key.equals(GlobalSemanticEvaluator.MAXIMUM_PRECISION)
					|| _key.equals(GlobalSemanticEvaluator.MAXIMUM_RECALL)
					|| _key.equals(GlobalSemanticEvaluator.OVER_GENERATION))
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
			SemanticReport r1 = (SemanticReport) o1;
			SemanticReport r2 = (SemanticReport) o2;
			return stripDirectory(r1).compareTo(stripDirectory(r2));
		}

		private String stripDirectory(SemanticReport report)
		{		
			String[] stripped = report.getSystemName().split("\\"+File.separator);
			return stripped[stripped.length - 1];
		}

	}
}
