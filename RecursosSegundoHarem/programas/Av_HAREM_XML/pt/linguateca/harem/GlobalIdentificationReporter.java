/*
 * Created on Jun 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class GlobalIdentificationReporter extends GlobalReporter
{

	public GlobalIdentificationReporter(String filter, boolean debug, int submission_types,
			String unofficialNames, String dir)
	{
		super(filter, debug, submission_types, unofficialNames, dir);

		new Thread(this).start();
	}

	protected Report createReport(String file)
	{
		IdentificationReport report = new IdentificationReport(file);
		BufferedReader reader;
		String buffer;
		String[] tokens;

		try
		{
			reader = new BufferedReader(new FileReader(file));

			while ((buffer = reader.readLine()) != null)
			{
				tokens = buffer.split(": ");
				report.put(tokens[0], tokens[1]);
			}
			reader.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return report;
	}

	protected void setBestResults(LinkedList reports)
	{
		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.PRECISION));
		addToBestResults(GlobalAlignmentEvaluator.PRECISION, (IdentificationReport) reports.getLast(), reports);

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.RECALL));
		addToBestResults(GlobalAlignmentEvaluator.RECALL, (IdentificationReport) reports.getLast(), reports);

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.F_MEASURE));
		addToBestResults(GlobalAlignmentEvaluator.F_MEASURE, (IdentificationReport) reports.getLast(), reports);

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.COMBINED_ERROR));
		addToBestResults(GlobalAlignmentEvaluator.COMBINED_ERROR, (IdentificationReport) reports.getFirst(),
				reports);

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.OVER_GENERATION));
		addToBestResults(GlobalAlignmentEvaluator.OVER_GENERATION, (IdentificationReport) reports.getFirst(),
				reports);

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.UNDER_GENERATION));
		addToBestResults(GlobalAlignmentEvaluator.UNDER_GENERATION, (IdentificationReport) reports.getFirst(),
				reports);
	}

	protected void addToBestResults(String key, IdentificationReport best, LinkedList reports)
	{
		IdentificationReport current;
		if (key.equals(GlobalAlignmentEvaluator.PRECISION) || key.equals(GlobalAlignmentEvaluator.RECALL)
				|| key.equals(GlobalAlignmentEvaluator.F_MEASURE))
		{
			Collections.reverse(reports);
		}
		for (Iterator i = reports.iterator(); i.hasNext();)
		{
			current = (IdentificationReport) i.next();
			if (!current.getValue(key).equals(best.getValue(key)))
			{
				break;
			}
			_bestResults.put(key, current);
		}
	}

	protected void writeOut(LinkedList reports)
	{
		Iterator i;
		int counter = 0;
		IdentificationReport current;

		LinkedList shuffled = getShuffled(_names);

		LinkedHashMap codeNames = new LinkedHashMap();

		for (i = reports.iterator(); i.hasNext();)
		{
			current = (IdentificationReport) i.next();

			if (_debug)
			{
				codeNames.put(current, current.toString());
			}
			else if (current.isPrintable())
			{
				codeNames.put(current, shuffled.removeFirst());
			}
		}

		if (!_debug)
		{
			System.out.println(codeNames);
		}

		writeHeader();
		System.out.println("<br>");
		System.out.println("<br>");
		System.out.println("<table border>");
		System.out.println("<tr>");
		System.out.println("<td>" + SYSTEM + "</td>");
		System.out.println("<td>" + GlobalAlignmentEvaluator.PRECISION + " (%) </td>");
		System.out.println("<td>" + GlobalAlignmentEvaluator.RECALL + " (%) </td>");
		System.out.println("<td>" + GlobalAlignmentEvaluator.F_MEASURE + "</td>");
		System.out.println("<td>" + GlobalAlignmentEvaluator.COMBINED_ERROR + "</td>");
		System.out.println("<td>" + GlobalAlignmentEvaluator.OVER_GENERATION + "</td>");
		System.out.println("<td>" + GlobalAlignmentEvaluator.UNDER_GENERATION + "</td>");
		if (_debug)
		{
			System.out.println("<td>" + GlobalAlignmentEvaluator.TOTAL_IN_GC + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.TOTAL_IDENTIFIED + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.TOTAL_CORRECT_F + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.SPURIOUS + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.MISSING + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.TOTAL_CORRECT_F + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.SUM_PARTIAL_CORRECT + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.SUM_PARTIAL_INCORRECT + "</td>");
			System.out.println("<td>" + GlobalAlignmentEvaluator.TOTAL_OCCURRENCES_PARTIAL_CORRECT + "</td>");
		}
		System.out.println("</tr>");

		Collections.sort(reports, new ScoreComparator(GlobalAlignmentEvaluator.F_MEASURE));
		Collections.reverse(reports);

		for (i = reports.iterator(); i.hasNext();)
		{
			current = (IdentificationReport) i.next();

			if (!codeNames.containsKey(current))
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
			System.out.print("<td>");
			if (_bestResults.get(GlobalAlignmentEvaluator.PRECISION).contains(current))
			{
				System.out.print("<font color=green><b>");
			}
			System.out.print(getPercentage(current.getValue(GlobalAlignmentEvaluator.PRECISION)));
			if (_bestResults.get(GlobalAlignmentEvaluator.PRECISION).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if (_bestResults.get(GlobalAlignmentEvaluator.RECALL).contains(current))
			{
				System.out.print("<font color=green><b>");
			}
			System.out.print(getPercentage(current.getValue(GlobalAlignmentEvaluator.RECALL)));
			if (_bestResults.get(GlobalAlignmentEvaluator.RECALL).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if (_bestResults.get(GlobalAlignmentEvaluator.F_MEASURE).contains(current))
			{
				System.out.print("<font color=green><b>");
			}
			System.out.print(getZerofiedNaN(current.getValue(GlobalAlignmentEvaluator.F_MEASURE)));
			if (_bestResults.get(GlobalAlignmentEvaluator.F_MEASURE).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if (_bestResults.get(GlobalAlignmentEvaluator.COMBINED_ERROR).contains(current))
			{
				System.out.print("<font color=green><b>");
			}
			System.out.print(current.getValue(GlobalAlignmentEvaluator.COMBINED_ERROR));
			if (_bestResults.get(GlobalAlignmentEvaluator.COMBINED_ERROR).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if (_bestResults.get(GlobalAlignmentEvaluator.OVER_GENERATION).contains(current))
			{
				System.out.print("<font color=green><b>");
			}
			System.out.print(current.getValue(GlobalAlignmentEvaluator.OVER_GENERATION));
			if (_bestResults.get(GlobalAlignmentEvaluator.OVER_GENERATION).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			System.out.print("<td>");
			if (_bestResults.get(GlobalAlignmentEvaluator.UNDER_GENERATION).contains(current))
			{
				System.out.print("<font color=green><b>");
			}
			System.out.print(current.getValue(GlobalAlignmentEvaluator.UNDER_GENERATION));
			if (_bestResults.get(GlobalAlignmentEvaluator.UNDER_GENERATION).contains(current))
			{
				System.out.print("</b></font>");
			}
			System.out.print("</td>");
			if (_debug)
			{
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.TOTAL_IN_GC));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.TOTAL_IDENTIFIED));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.TOTAL_CORRECT_F));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.SPURIOUS));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.MISSING));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.TOTAL_CORRECT_F));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.SUM_PARTIAL_CORRECT));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.SUM_PARTIAL_INCORRECT));

				System.out.print("</td>");
				System.out.print("<td>");
				System.out.print(current.getValue(GlobalAlignmentEvaluator.TOTAL_OCCURRENCES_PARTIAL_CORRECT));

				System.out.print("</td>");
			}
			System.out.print("</tr>");
			System.out.println("");
		}

		System.out.println("</table>");

		writeFooter();
	}

	public static void main(String[] args)
	{
		String filter = null;
		int submissions = 0;
		String unofficialNames = null;
		boolean debug = false;
		String dir = null;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-filtro"))
			{
				i++;
				filter = args[i];
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

			if (args[i].equals("-dir"))
			{
				dir = args[++i];
				continue;
			}
		}

		if (filter == null)
		{
			printSynopsis();
			return;
		}

		new GlobalIdentificationReporter(filter, debug, submissions, unofficialNames, dir);
	}

	private static void printSynopsis()
	{
		System.out.println("Utilização:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -jar <ficheiro_jar> -filtro <filtro>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
				.println("java -Dfile.encoding=ISO-8859-1 -jar Reporter.jar -filtro selectivo.altid.ida2id:absoluto.altid.ida2id -debug");
	}

	class ScoreComparator implements Comparator
	{
		private String _key;

		public ScoreComparator(String key)
		{
			_key = key;
		}

		public int compare(Object o1, Object o2)
		{
			IdentificationReport r1 = (IdentificationReport) o1;
			IdentificationReport r2 = (IdentificationReport) o2;

			double diff;
			double score1 = Double.parseDouble(r1.getValue(_key));
			double score2 = Double.parseDouble(r2.getValue(_key));

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
					|| _key.equals(GlobalAlignmentEvaluator.F_MEASURE))
			{
				return Double.MIN_VALUE;
			}
			return Double.MAX_VALUE;
		}
	}
}
