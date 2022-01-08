/*
 * Created on Aug 17, 2005
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
public class EvaluatedEntityScoreList
{
	private NamedEntity _entity;

	private LinkedList _scores;

	public EvaluatedEntityScoreList(NamedEntity entity)
	{
		_scores = new LinkedList();
		_entity = entity;
	}

	public String toString()
	{
		return _entity.toString() + "\t " + _scores.toString();
	}

	public void addScoreTuple(SemanticScoreTuple tuple)
	{
		_scores.add(tuple);
	}

	public int size()
	{
		return _scores.size();
	}

	public int getCorrectCategoryFrequency()
	{
		SemanticScoreTuple current;
		int frequency = 0;
		for (Iterator i = _scores.iterator(); i.hasNext();)
		{
			current = (SemanticScoreTuple) i.next();
			frequency += current.getCorrectCategories();
		}

		return frequency;
	}

	public int getMissingCategoryFrequency()
	{
		SemanticScoreTuple current;
		int frequency = 0;
		for (Iterator i = _scores.iterator(); i.hasNext();)
		{
			current = (SemanticScoreTuple) i.next();
			frequency += current.getMissingCategories();
		}

		return frequency;
	}

	public int getSpuriousCategoryFrequency()
	{
		SemanticScoreTuple current;
		int frequency = 0;
		for (Iterator i = _scores.iterator(); i.hasNext();)
		{
			current = (SemanticScoreTuple) i.next();
			frequency += current.getSpuriousCategories();
		}

		return frequency;
	}

	public int getCorrectTypeFrequency()
	{
		SemanticScoreTuple current;
		int frequency = 0;
		for (Iterator i = _scores.iterator(); i.hasNext();)
		{
			current = (SemanticScoreTuple) i.next();
			frequency += current.getCorrectTypes();
		}

		return frequency;
	}

	public int getMissingTypeFrequency()
	{
		SemanticScoreTuple current;
		int frequency = 0;
		for (Iterator i = _scores.iterator(); i.hasNext();)
		{
			current = (SemanticScoreTuple) i.next();
			frequency += current.getMissingTypes();
		}

		return frequency;
	}

	public int getSpuriousTypeFrequency()
	{
		SemanticScoreTuple current;
		int frequency = 0;
		for (Iterator i = _scores.iterator(); i.hasNext();)
		{
			current = (SemanticScoreTuple) i.next();
			frequency += current.getSpuriousTypes();
		}

		return frequency;
	}
}
