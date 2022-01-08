/*
 * Created on Mar 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import org.jdom.Element;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public abstract class HaremEntity
{

	protected TagBase _tagBase;

	/**
	 * 
	 */
	public HaremEntity()
	{
		_tagBase = TagBase.getInstance();
	}

	public int hashCode()
	{
		return toString().hashCode();
	}

	public boolean equals(Object o)
	{
		HaremEntity another;
		if (o instanceof HaremEntity)
		{
			another = (HaremEntity) o;
			return toString().equals(another.toString());
		}

		return false;
	}

	public abstract String getOpeningTag();

	public abstract String getClosingTag();

	public abstract String toString();

	public abstract Object clone();

	public abstract Element getElement();
	
	protected abstract LinkedList split(Parser parser);

	protected abstract Set<String> getMarkableTokens();

	protected abstract HaremEntity markTokens(HashMap markable);

	protected abstract HaremEntity unmarkTokens();
	
	protected abstract HaremEntity markEntity(int tag);

	protected abstract boolean isOmitted();
	
	protected abstract void setOmitted(boolean b);
}