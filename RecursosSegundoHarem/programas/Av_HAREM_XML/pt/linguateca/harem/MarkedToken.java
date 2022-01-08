/*
 * Created on Mar 25, 2005
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
public class MarkedToken
{
	private TagBase _tagBase;

	private int _count;

	private String _token;

	public MarkedToken(String token)
	{
		this();
		_token = token;
		_count = 0;
	}

	public MarkedToken()
	{
		_tagBase = TagBase.getInstance();
	}

	public int hashCode()
	{
		return toString().toUpperCase().hashCode();
	}

	public boolean equals(Object o)
	{
		MarkedToken another;
		if (o instanceof MarkedToken)
		{
			another = (MarkedToken) o;
			return toString().equalsIgnoreCase(another.toString());
		}

		return false;
	}

	public String toString()
	{
		return _tagBase.openTag("" + _count) + _token + _tagBase.closeTag("" + _count);
	}

	protected void setToken(String token)
	{
		_token = token;
	}

	public String getToken()
	{
		return _token;
	}

	protected void setCount(int count)
	{
		_count = count;
	}

	public int getCount()
	{
		return _count;
	}
}