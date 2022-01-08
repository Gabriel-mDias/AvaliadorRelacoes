/*
 * Created on Jun 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class IdentificationReport extends Report
{
	private LinkedHashMap _values;

	public IdentificationReport(String system)
	{
		super(system);
		_values = new LinkedHashMap();
	}

	public boolean isPrintable()
	{
		return !Double.isNaN(Double.parseDouble((String) _values.get(GlobalAlignmentEvaluator.PRECISION)));
	}

	public void put(String key, String value)
	{
		_values.put(key, value);
	}

	public Set getKeys()
	{
		return _values.keySet();
	}

	public String getValue(String key)
	{
		return (String) _values.get(key);
	}

}
