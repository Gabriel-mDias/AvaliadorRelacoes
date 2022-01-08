/*
 * Created on Jul 15, 2005
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
public abstract class DuplicateFinder
{
	protected int _lastPos;

	protected EvaluatedAlignment _previous;

	/**
	 * 
	 */
	public DuplicateFinder()
	{
		_lastPos = 0;
		_previous = null;
	}

	protected abstract boolean canBeDuplicate(EvaluatedAlignment current);

	protected boolean isDifferentFromPrevious(EvaluatedAlignment current)
	{
		if (current == null)
		{
			_lastPos = 0;
			_previous = null;
			return true;
		}

		if (_previous == null)
		{
			_previous = current;
			return true;
		}

		NamedEntity last = _previous.getLastAlignment();
		NamedEntity first = current.getFirstAlignment();

		if (last == null || first == null)
		{
			_previous = current;
			return true;
		}

		NamedEntity goldenPrevious = _previous.getGoldenEntity();
		NamedEntity goldenCurrent = current.getGoldenEntity();

		if (!canBeDuplicate(current))
		{
			_lastPos = 0;
			_previous = current;
			return true;
		}

		if (!last.equals(first))
		{
			_lastPos = 0;
			_previous = current;
			return true;
		}

		LinkedList<String> tokens1 = TaggedDocument.tokenize(goldenPrevious.getEntity());
		LinkedList<String> tokens2 = TaggedDocument.tokenize(goldenCurrent.getEntity());
		
		if (_lastPos != 0)
			_lastPos++;

		_lastPos = getPosition(_lastPos, getLastValid(tokens1), last);

		if (_lastPos == -1 || _lastPos > getPosition(_lastPos + 1, getFirstValid(tokens2), last))
		{
			_lastPos = 0;
			_previous = current;
			return true;
		}

		// System.err.println(first);
		_previous = current;
		return false;
	}

	private String getFirstValid(LinkedList<String> list)
	{
		String current;
		for (Iterator<String> i = list.iterator(); i.hasNext();)
		{
			current = i.next();
			if (current.trim().equals(""))
				continue;

			if (!Character.isLetterOrDigit(current.charAt(0)))
				continue;

			return current;
		}

		return null;
	}

	private String getLastValid(LinkedList<String> list)
	{
		LinkedList<String> clone = (LinkedList<String>) list.clone();
		String current;

		while (!clone.isEmpty())
		{
			current = (String) clone.removeLast();
			if (current.trim().equals(""))
				continue;

			if (!Character.isLetterOrDigit(current.charAt(0)))
				continue;

			return current;
		}

		return null;
	}

	private int getPosition(int start, String token, NamedEntity entity)
	{
		String normalizedToken = normalize(token);
		String normalizedEntity = normalize(entity.getEntity());

		return normalizedEntity.indexOf(normalizedToken, start);
	}

	private String normalize(String toNormalize)
	{
		String normalized = "";
		String current;
		LinkedList<String> tokens = TaggedDocument.tokenize(toNormalize);

		for (Iterator<String> i = tokens.iterator(); i.hasNext();)
		{
			current = i.next();
			if (current.trim().equals(""))
				continue;

			normalized += current;
		}

		return normalized.toUpperCase();
	}

}
