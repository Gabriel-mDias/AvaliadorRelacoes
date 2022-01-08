/*
 * Created on Jun 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class SemanticEvaluatedAlignmentProcessor extends EvaluatedAlignmentProcessor
{

	public EvaluatedAlignment getEvaluatedAlignment(String buffer)
	{		
		SemanticEvaluatedAlignment alignment = new SemanticEvaluatedAlignment();
		LinkedList<NamedEntity> alignments = new LinkedList<NamedEntity>();
		LinkedList<String> scores;
		Iterator<String> k;
		Iterator<NamedEntity> j;

		String[] sides = buffer.split(Aligner.ALIGNMENT_CONNECTOR);
		if(sides.length != 2)
		{
			System.out.println(buffer);
		}

		NamedEntity golden = new NamedEntity(sides[0]);
		alignment.setGoldenEntity(golden);
				
		int index = sides[1].indexOf(IndividualAlignmentEvaluator.EVALUATION_MARKER);
		String systemSide = sides[1].substring(0, index);
				
		alignments.addAll(NamedEntity.toNamedEntityList(systemSide));

		if (alignments.size() == 0)
		{
			alignments.add(null);
		}
		
		scores = getScores(index+sides[0].length(), buffer);
		for (j = alignments.iterator(), k = scores.iterator(); j.hasNext();)
		{
			alignment.addAlignment(j.next(), k.next());
		}

		return alignment;
	}

	protected LinkedList<String> getScores(int index, String buffer)
	{
		LinkedList<String> scores = new LinkedList<String>();
	
		buffer = buffer.substring(index + IndividualAlignmentEvaluator.EVALUATION_MARKER.length());
		String[] tokens = buffer.split("[{}]");
	
		for (int i = 0; i < tokens.length; i++)
		{
			if (tokens[i].startsWith(AttributesEvaluation.CATEGORY))
			{
				scores.add(tokens[i]);
			}
		}

		return scores;
	}
}
