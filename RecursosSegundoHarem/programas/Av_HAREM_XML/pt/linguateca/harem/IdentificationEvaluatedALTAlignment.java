/*
 * Created on Apr 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Iterator;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class IdentificationEvaluatedALTAlignment extends EvaluatedALTAlignment
{

	public int compareTo(Object o)
	{

		IdentificationEvaluatedALTAlignment another = (IdentificationEvaluatedALTAlignment) o;

		double myFMeasure = getMeasureF();
		double anotherFMeasure = another.getMeasureF();

		double myError = getCombinedError();
		double anotherError = another.getCombinedError();

		if (myFMeasure - anotherFMeasure > 0)
			return 1;

		if (myFMeasure - anotherFMeasure < 0)
			return -1;

		if (myError - anotherError < 0)
			return 1;

		if (myError - anotherError > 0)
			return -1;

		if (_alignments.size() > another._alignments.size())
			return 1;

		if (_alignments.size() < another._alignments.size())
			return -1;

		return 0;
	}

	protected double getMeasureF()
	{
		double precision;
		double recall;
		// HashSet alignedSubmissionEntities;
		int alignedSubmissionEntities = 1;
		int alignedGoldenEntities = 1; // dummy
		double score = 1; // dummy
		NamedEntity alignedSubmissionEntity;
		String currentScore;
		String[] tokens;
		IdentificationEvaluatedAlignment alignment;

		IdentificationDuplicateFinder finder = new IdentificationDuplicateFinder();

		// alignedSubmissionEntities = new HashSet();
		// alignedSubmissionEntities.add("dummy");

		for (Iterator i = _alignments.iterator(); i.hasNext();)
		{
			alignment = (IdentificationEvaluatedAlignment) i.next();

			if (!alignment.getGoldenEntity().getCategories().contains(_tagBase.getSpuriousTag()))
			{
				alignedGoldenEntities++;
			}

			for (Iterator j = alignment.getAligned(); j.hasNext();)
			{
				alignedSubmissionEntity = (NamedEntity) j.next();
				currentScore = alignment.getScore(alignedSubmissionEntity);

				if (currentScore.equals(IndividualAlignmentEvaluator.MISSING))
				{
					continue;
				}
				// alignedSubmissionEntities.add(alignedSubmissionEntity);
				// alignedSubmissionEntities++;
				if (finder.isDifferentFromPrevious(alignment))
					alignedSubmissionEntities++;

				if (currentScore.equals(IndividualAlignmentEvaluator.CORRECT))
				{
					score++;
					continue;
				}

				if (currentScore.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_EXCESS)
						|| currentScore.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_LACK))
				{
					tokens = currentScore.split("[(;)]");
					score += Double.parseDouble(tokens[1]);
					continue;
				}
			}

		}

		if (_info == null)
			_info = new IdentificationDebugInfo();

		if (score == 0)
		{
			return 0;
		}

		precision = score / alignedSubmissionEntities;

		recall = score / alignedGoldenEntities;

		((IdentificationDebugInfo) _info)._precision = precision;
		((IdentificationDebugInfo) _info)._recall = recall;
		((IdentificationDebugInfo) _info)._FMeasure = (2 * precision * recall) / (precision + recall);

		return (2 * precision * recall) / (precision + recall);
	}

	private double getCombinedError()
	{
		IdentificationEvaluatedAlignment alignment;
		String[] tokens;
		String currentScore;
		double total = 1;
		double error = 0;

		for (Iterator i = _alignments.iterator(); i.hasNext();)
		{
			alignment = (IdentificationEvaluatedAlignment) i.next();
			for (Iterator j = alignment.getAligned(); j.hasNext();)
			{
				total++;
				currentScore = alignment.getScore((NamedEntity) j.next());
				if (currentScore.equals(IndividualAlignmentEvaluator.MISSING)
						|| currentScore.equals(IndividualAlignmentEvaluator.SPURIOUS))
				{
					error++;
					continue;
				}

				if (currentScore.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_EXCESS)
						|| currentScore.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_LACK))
				{
					tokens = currentScore.split("[(;)]");
					error += Double.parseDouble(tokens[2].trim());
				}
			}
		}

		((IdentificationDebugInfo) _info)._combinedError = error / total;

		return error / total;
	}

	class IdentificationDebugInfo extends DebugInfo
	{
		public double _precision;

		public double _recall;

		public double _FMeasure;

		public double _combinedError;

		public String toString()
		{
			return "\tPrecis\u00e3o=" + _precision + "\tAbrang\u00eancia=" + _recall + "\tMedida_F=" + _FMeasure
					+ "\tErro_Combinado=" + _combinedError;
		}
	}

}
