/*
 * Created on May 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class IdentificationEvaluatedAlignmentProcessor extends EvaluatedAlignmentProcessor
{

	public EvaluatedAlignment getEvaluatedAlignment(String buffer)
	{		
		IdentificationEvaluatedAlignment alignment = new IdentificationEvaluatedAlignment();
		LinkedList<NamedEntity> alignments = new LinkedList<NamedEntity>();
		LinkedList<String> scores;
		Double weight = null;
		Iterator<String> k;
		Iterator<NamedEntity> j;

		String[] lados = buffer.split(Aligner.ALIGNMENT_CONNECTOR);
		
		//TODO: confirmar isto
		if(lados.length != 2)
		{
			System.err.println(buffer);
			return null;
		}

		alignment.setGoldenEntity(new NamedEntity(lados[0]));

		String[] emScore = lados[1].split(IndividualAlignmentEvaluator.EVALUATION_MARKER);
		alignments.addAll(NamedEntity.toNamedEntityList(emScore[0]));
		
		if (alignments.size() == 0)
		{
			alignments.add(null);
		}
		
		String[] scoreWeight = emScore[1].split("\\"+IndividualAlignmentEvaluator.ALT_WEIGHT_MARKER);
		
		scores = getScores(scoreWeight[0]);		
		if(scoreWeight.length > 1)
			weight = Double.parseDouble(scoreWeight[1]);
		else
			weight = 1.0;
		
		for (j = alignments.iterator(), k = scores.iterator(); j.hasNext();)
		{
			alignment.addAlignment(j.next(), k.next());
			alignment.setAltWeigth(weight);
		}

		//System.out.println("ALINHAMENTO -----> "+alignment);
		return alignment;
	}

	protected LinkedList<String> getScores(String buffer)
	{
		LinkedList<String> scores = new LinkedList<String>();
		int state = 0;
		String current = "";
		char currentChar;

		for (int i = 0; i < buffer.length(); i++)
		{
			currentChar = buffer.charAt(i);

			if (state == 0 && currentChar == '[')
			{
				state = 1;
				continue;
			}

			if (state == 1 && Character.isLetter(currentChar))
			{
				current = "";
				current += currentChar;
				state = 2;
				continue;
			}

			if (state == 2 && currentChar == '(')
			{
				current += currentChar;
				state = 3;
				continue;
			}

			if (state == 2 && currentChar == ',')
			{
				scores.add(current);
				state = 1;
				continue;
			}

			if (state == 2 && currentChar == ']')
			{
				scores.add(current);
				state = 5;
				continue;
			}

			if (state == 2)
			{
				current += currentChar;
				continue;
			}

			if (state == 3 && currentChar == ')')
			{
				current += currentChar;
				state = 4;
				continue;
			}

			if (state == 3)
			{
				current += currentChar;
				continue;
			}

			if (state == 4 && currentChar == ',')
			{
				scores.add(current);
				current = "";
				state = 1;
				continue;
			}

			if (state == 4 && currentChar == ']')
			{
				scores.add(current);
				current = "";
				state = 5;
				continue;
			}
		}

		return scores;
	}
}
