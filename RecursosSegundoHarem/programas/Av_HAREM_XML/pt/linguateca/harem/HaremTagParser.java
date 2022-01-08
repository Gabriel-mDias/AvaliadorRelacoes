/*
 * Created on Mar 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public abstract class HaremTagParser extends XMLParser
{
	private static final char[] IGNORABLE_TEXT_CHARCTERS =
	{ '\n', '\t', '\r', '\f' };

	protected HaremEntity _entity;

	protected TagBase _tagBase;

	public HaremTagParser()
	{
		_tagBase = TagBase.getInstance();
	}

	public HaremEntity getEntity()
	{
		return _entity;
	}

	protected void initialize()
	{
		_entity = null;
	}

	protected boolean isControlCharacter(char current)
	{
		for (int i = 0; i < IGNORABLE_TEXT_CHARCTERS.length; i++)
		{
			if (current == IGNORABLE_TEXT_CHARCTERS[i])
				return true;
		}
		return false;
	}

}