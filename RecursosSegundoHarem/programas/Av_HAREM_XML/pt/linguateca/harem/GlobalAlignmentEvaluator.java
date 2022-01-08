/*
 * Created on May 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class GlobalAlignmentEvaluator extends GlobalEvaluator
{

	public GlobalAlignmentEvaluator(String alignments, boolean useTags, boolean debug)
	{
		super(alignments, useTags, debug);
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment alignment;
		Iterator i;
		NamedEntity current;
		String score;
		String[] tokens;
		IdentificationEvaluatedAlignmentProcessor processor = new IdentificationEvaluatedAlignmentProcessor();

		IdentificationDuplicateFinder finder = new IdentificationDuplicateFinder();

		double totalIdentified = 0;
		double totalInGoldenSet = 0;
		double totalMissing = 0;
		double totalSpurious = 0;
		double totalCorrect = 0;
		double totalPartiallyCorrect = 0;
		double totalPartiallyIncorrect = 0;
		double totalOcurrencesPartial = 0; // I need this variable to calculate combined error; the sum
		// of the
		// correctness of an identified entity when aligned with one golden entity
		// would be greater than should
		double precision;
		double recall;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			while ((buffer = reader.readLine()) != null)
			{

				if (!isEvaluatable(buffer))
					continue;

				alignment = (IdentificationEvaluatedAlignment) processor.getEvaluatedAlignment(buffer);

				if (!alignment.getGoldenEntity().isSpurious())
					totalInGoldenSet++;

				for (i = alignment.getAligned(); i.hasNext();)
				{

					current = (NamedEntity) i.next();
					score = alignment.getScore(current);

					// System.out.println("\n\n");
					// System.out.println(alignment);

					if (score.equals(IndividualAlignmentEvaluator.MISSING))
					{
						totalMissing++;
						continue;
					}

					if (finder.isDifferentFromPrevious(alignment))
					{
						totalIdentified++;
					}

					if (score.equals(IndividualAlignmentEvaluator.CORRECT))
					{
						totalCorrect++;
						continue;
					}

					if (score.equals(IndividualAlignmentEvaluator.SPURIOUS))
					{
						totalSpurious++;
						continue;
					}

					if (score.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_EXCESS)
							|| score.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_LACK))
					{
						tokens = score.split("[(;)]");
						totalPartiallyCorrect += Double.parseDouble(tokens[1]);
						totalPartiallyIncorrect += Double.parseDouble(tokens[2].trim());
						totalOcurrencesPartial++;
						continue;
					}

				}

				if (_debug)
				{
					System.out.println(alignment);
					System.out.println("");
					System.out.println(TOTAL_IN_GC + ": " + (int) totalInGoldenSet);
					System.out.println(TOTAL_IDENTIFIED + ": " + (int) totalIdentified);
					System.out.println(TOTAL_CORRECT_F + ": " + (int) totalCorrect);
					System.out.println(TOTAL_OCCURRENCES_PARTIAL_CORRECT + ": " + (int) totalOcurrencesPartial);
					System.out.println(SUM_PARTIAL_CORRECT + ": " + totalPartiallyCorrect);
					System.out.println(SUM_PARTIAL_INCORRECT + ": " + totalPartiallyIncorrect);
					System.out.println(SPURIOUS + ": " + (int) totalSpurious);
					System.out.println(MISSING + ": " + (int) totalMissing);
					System.out.println("");
					System.out.println("");
					System.out.println("");
					System.out.println("");
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println(TOTAL_IN_GC + ": " + (int) totalInGoldenSet);
		System.out.println(TOTAL_IDENTIFIED + ": " + (int) totalIdentified);
		System.out.println(TOTAL_CORRECT_F + ": " + (int) totalCorrect);
		System.out.println(TOTAL_OCCURRENCES_PARTIAL_CORRECT + ": " + (int) totalOcurrencesPartial);
		System.out.println(SUM_PARTIAL_CORRECT + ": " + totalPartiallyCorrect);
		System.out.println(SUM_PARTIAL_INCORRECT + ": " + totalPartiallyIncorrect);
		System.out.println(SPURIOUS + ": " + (int) totalSpurious);
		System.out.println(MISSING + ": " + (int) totalMissing);

		precision = (totalCorrect + totalPartiallyCorrect) / totalIdentified;
		recall = (totalCorrect + totalPartiallyCorrect) / totalInGoldenSet;

		System.out.println(PRECISION + ": " + precision);
		System.out.println(RECALL + ": " + recall);
		System.out.println(F_MEASURE + ": " + (2 * precision * recall) / (precision + recall));
		System.out.println(OVER_GENERATION + ": " + totalSpurious / totalIdentified);
		System.out.println(UNDER_GENERATION + ": " + totalMissing / totalInGoldenSet);
		System.out.println(COMBINED_ERROR + ": " + (totalPartiallyIncorrect + totalMissing + totalSpurious)
				/ (totalOcurrencesPartial + totalCorrect + totalSpurious + totalMissing));

		try
		{
			reader.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	protected boolean canBeDuplicate(EvaluatedAlignment previous, EvaluatedAlignment current)
	{
		NamedEntity last = previous.getLastAlignment();
		NamedEntity first = current.getFirstAlignment();

		return !(previous.getScore(last).equals(IndividualAlignmentEvaluator.CORRECT)
				|| current.getScore(first).equals(IndividualAlignmentEvaluator.CORRECT)
				|| current.getScore(first).equals(IndividualAlignmentEvaluator.SPURIOUS) || previous.getScore(last)
				.equals(IndividualAlignmentEvaluator.SPURIOUS));
	}

	private void printDebug(double totalIdentified, double totalInGoldenSet, double totalMissing,
			double totalSpurious, double totalCorrect, double totalPartiallyCorrect,
			double totalPartiallyIncorrect, double totalOcurrencesPartial)
	{

	}

	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;
		boolean debug = false;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-alinhamento"))
			{
				i++;
				alignments = args[i];
				continue;
			}

			if (args[i].equals("-etiquetas"))
			{
				i++;
				useTags = args[i].equalsIgnoreCase("sim");
				continue;
			}

			if (args[i].equals("-depurar"))
			{
				i++;
				debug = args[i].equalsIgnoreCase("sim");
				continue;
			}
		}

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new GlobalAlignmentEvaluator(alignments, useTags, debug);
	}

	private static void printSynopsis()
	{
		System.out.println("Utilização:");
		System.out
				.println("java -Dfile.encoding=ISO-8859-1 -jar <ficheiro_jar> -alinhamento <ficheiro_alinhado>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
				.println("java -Dfile.encoding=ISO-8859-1 -jar Ida.jar -alinhamento elle.alinhado.etq.verificado.avalida");
	}
}
