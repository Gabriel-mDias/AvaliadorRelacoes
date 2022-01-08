/*
 * Created on Jun 10, 2005
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
public abstract class Report implements Comparable
{
	protected String _system;

	public Report(String system)
	{
		_system = system;
	}

	public abstract boolean isPrintable();

	public void setSystemName(String systemName)
	{
		_system = systemName;
	}

	public String getSystemName()
	{
		return _system;
	}

	public String toString()
	{
		return _system;
	}

	public int compareTo(Object o)
	{
		return _system.compareTo(((Report) o)._system);
	}
}
