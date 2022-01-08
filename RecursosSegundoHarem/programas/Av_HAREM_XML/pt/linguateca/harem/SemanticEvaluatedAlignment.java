package pt.linguateca.harem;

import java.util.Iterator;

public class SemanticEvaluatedAlignment extends EvaluatedAlignment
{

	public String toString()
	{
		String out = _goldenEntity.toString() + Aligner.ALIGNMENT_CONNECTOR + _alignments.toString();
		out += IndividualAlignmentEvaluator.EVALUATION_MARKER + scoresToString();
		return out;
	}

	public boolean hasCorrectCategory()
	{
		NamedEntity entity;
		SemanticScoreTuple score;

		for (Iterator<NamedEntity> i = getAligned(); i.hasNext();)
		{
			entity = i.next();
			score = new SemanticScoreParser(getScore(entity)).getScoreTuple();

			if (score.getCorrectCategories() > 0)
				return true;
		}

		return false;
	}

	public boolean hasCorrectType()
	{
		NamedEntity entity;
		SemanticScoreTuple score;

		for (Iterator<NamedEntity> i = getAligned(); i.hasNext();)
		{
			entity = i.next();
			score = new SemanticScoreParser(getScore(entity)).getScoreTuple();

			if (score.getCorrectTypes() > 0)
				return true;
		}

		return false;
	}

	public boolean hasCorrectSubtype()
	{
		NamedEntity entity;
		SemanticScoreTuple score;

		for (Iterator<NamedEntity> i = getAligned(); i.hasNext();)
		{
			entity = i.next();
			score = new SemanticScoreParser(getScore(entity)).getScoreTuple();

			if (score.getCorrectSubtypes() > 0)
				return true;
		}

		return false;
	}
	
	private String scoresToString()
	{
		String string = "";

		for (Iterator<String> i = _scores.iterator(); i.hasNext();)
		{
			string += "{" + i.next() + "}";
			if (i.hasNext())
			{
				string += ", ";
			}
		}

		return "[" + string + "]";
	}
	
	/**
	 * Usado para quando se usa a avaliacao relaxada de ALTs, as EMs de cada alternativa terem o mesmo peso que as outras
	 * @param i index do alinhamento
	 * @param n numero de ALTs
	 */
	public void resetAltWeight(int i, int n)
	{		
		String score = _scores.get(i);
		
		//Se nao tem peso de ALT é porque já vai contar como 1 e não é necessário reset
		if(score.indexOf(AttributesEvaluation.ALT_WEIGHT) < 0)
			return;
				
		int beginIndex = score.indexOf(AttributesEvaluation.ALT_WEIGHT) + AttributesEvaluation.ALT_WEIGHT.length();
		
		int state = 0;
		String number = "";
		char current;
		int endIndex = beginIndex;

		for (; endIndex < score.length(); endIndex++)
		{
			current = score.charAt(endIndex);

			if (state == 0 && current == '(')
			{
				state = 1;
				continue;
			}

			if (state == 1 && current == ')')
				break;

			number += current;
		}

		double weight = Double.parseDouble(number);
		double newWeight = n * weight;
		
		String newScore = score.substring(0, beginIndex);
		newScore += "("+newWeight+")";
		newScore += score.substring(endIndex, score.length());
		_scores.set(i, newScore);
	}
}
