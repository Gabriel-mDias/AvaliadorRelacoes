/*
 * Created on Jul 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.LinkedList;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class SemanticScoreParser
{
	private String _score;

	public SemanticScoreParser(String score)
	{
		_score = score;
	}

	public SemanticScoreTuple getScoreTuple()
	{
		SemanticScoreTuple tuple = new SemanticScoreTuple();

		tuple.setMissingCategories(getMissing(AttributesEvaluation.CATEGORY).size());
		tuple.setMissingTypes(getMissing(AttributesEvaluation.TYPE).size());
		tuple.setMissingSubtypes(getMissing(AttributesEvaluation.SUBTYPE).size());

		tuple.setCorrectCategories(getCorrect(AttributesEvaluation.CATEGORY).size());
		tuple.setCorrectTypes(getCorrect(AttributesEvaluation.TYPE).size());
		tuple.setCorrectSubtypes(getMissing(AttributesEvaluation.SUBTYPE).size());

		tuple.setSpuriousCategories(getSpurious(AttributesEvaluation.CATEGORY).size());
		tuple.setSpuriousTypes(getSpurious(AttributesEvaluation.TYPE).size());
		tuple.setSpuriousSubtypes(getMissing(AttributesEvaluation.SUBTYPE).size());

		tuple.setMaximumCSCinGC(getMaximumCSCinGC());
		tuple.setMaximumCSCinSystem(getMaximumCSCinSystem());
		tuple.setCombinedSemanticClassification(getCombinedSemanticClassification());
		tuple.setWeight(getWeight());
		tuple.setAltWeight(getAltWeight());

		return tuple;
	}
	
	private double getWeight()
	{
		int index = _score.lastIndexOf(AttributesEvaluation.WEIGHT);
		return getDouble(index + AttributesEvaluation.WEIGHT.length());
	}
	
	private double getAltWeight()
	{
		int index = _score.lastIndexOf(AttributesEvaluation.ALT_WEIGHT);
		
		if(index >= 0)
			return getDouble(index + AttributesEvaluation.ALT_WEIGHT.length());
		else
			return 1;
	}

	private double getMaximumCSCinGC()
	{
		int index = _score.lastIndexOf(AttributesEvaluation.MAX_CSC_GC);
		return getDouble(index + AttributesEvaluation.MAX_CSC_GC.length());
	}
	
	private double getMaximumCSCinSystem()
	{
		int index = _score.lastIndexOf(AttributesEvaluation.MAX_CSC_SYSTEM);
		return getDouble(index + AttributesEvaluation.MAX_CSC_SYSTEM.length());
	}
	
	private double getCombinedSemanticClassification()
	{
		int index = _score.lastIndexOf(AttributesEvaluation.COMBINED_SEMANTIC_CLASSIFCATION);
		return getDouble(index + AttributesEvaluation.COMBINED_SEMANTIC_CLASSIFCATION.length());
	}

	private LinkedList getCorrect(String token)
	{
		int index = _score.lastIndexOf(token);
		index = _score.indexOf(IndividualAlignmentEvaluator.CORRECT, index);
		return getList(index + IndividualAlignmentEvaluator.CORRECT.length() + 1);
		// return listCount(index + IndividualAlignmentEvaluator.CORRECT.length() + 1);
	}

	private LinkedList getSpurious(String token)
	{
		int index = _score.lastIndexOf(token);
		index = _score.indexOf(IndividualAlignmentEvaluator.SPURIOUS, index);
		return getList(index + IndividualAlignmentEvaluator.SPURIOUS.length() + 1);
		// return listCount(index + IndividualAlignmentEvaluator.SPURIOUS.length() + 1);
	}

	private LinkedList getMissing(String token)
	{
		//System.out.println("score= "+_score);
		int index = _score.lastIndexOf(token);
		index = _score.indexOf(IndividualAlignmentEvaluator.MISSING, index);
		return getList(index + IndividualAlignmentEvaluator.MISSING.length() + 1);
		// return listCount(index + IndividualAlignmentEvaluator.MISSING.length() + 1);
	}
	
	private double getDouble(int index)
	{
		int state = 0;
		String number = "";
		char current;

		for (; index < _score.length(); index++)
		{
			current = _score.charAt(index);

			if (state == 0 && current == '(')
			{
				state = 1;
				continue;
			}

			if (state == 1 && current == ')')
				break;

			number += current;
		}

		return Double.parseDouble(number);
	}

	/**
	 * Lista com categorias ou tipos
	 * @param index
	 * @return
	 */
	private LinkedList<String> getList(int index)
	{
		LinkedList<String> list = new LinkedList<String>();
		int state = 0;
		String buffer = null;
		char currentChar;

		for (; index < _score.length(); index++)
		{
			currentChar = _score.charAt(index);

			if (state == 0 && currentChar == '[')
			{
				buffer = "";
				state = 1;
				continue;
			}

			if (state == 1 && Character.isLetter(currentChar))
			{
				buffer += currentChar;
				state = 2;
				continue;
			}

			// if (state == 2 && currentChar == ',')
			if (state == 2 && currentChar == '|')
			{
				list.add(buffer);
				buffer = "";
				continue;
			}

			if (currentChar == ']')
			{
				if (!buffer.equals(""))
					list.add(buffer);
				break;
			}

			if (state == 2)
			{
				buffer += currentChar;
			}

		}
		return list;
	}

}