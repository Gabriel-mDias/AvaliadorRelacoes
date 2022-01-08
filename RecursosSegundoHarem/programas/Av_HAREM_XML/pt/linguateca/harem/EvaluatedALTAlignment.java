/*
 * Created on Jun 21, 2005
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
public abstract class EvaluatedALTAlignment implements Comparable
{
	protected LinkedList<EvaluatedAlignment> _alignments;

	protected TagBase _tagBase;

	protected DebugInfo _info;

	public EvaluatedALTAlignment()
	{
		_alignments = new LinkedList<EvaluatedAlignment>();
		_tagBase = TagBase.getInstance();
	}

	public void addAlignment(EvaluatedAlignment alignment)
	{
		_alignments.add(alignment);
	}
	
	public LinkedList<EvaluatedAlignment> getAlternatives()
	{
		return _alignments;
	}
	
	public Iterator<EvaluatedAlignment> getAlternativesIterator()
	{
		return _alignments.iterator();
	}

	public String toString()
	{
		return _alignments.toString();
	}

	public DebugInfo getDebugInfo()
	{
		return _info;
	}

	abstract class DebugInfo
	{
		public abstract String toString();
	}
}
