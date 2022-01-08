/*
 * Created on Jun 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.HashMap;
import java.util.Set;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class SemanticReport extends Report
{
	private HashMap<String, String> _idMap;

	private HashMap<String, String> _combinedMap;

	public SemanticReport(String system)
	{
		super(system);
	
		_idMap = new HashMap<String, String>();
		_combinedMap = new HashMap<String, String>();
	}

	public boolean isPrintable()
	{
		return !Double.isNaN(Double.parseDouble(_idMap.get(GlobalAlignmentEvaluator.PRECISION)));
	}

	public void putInIdMap(String key, String value)
	{
		_idMap.put(key, value);
	}

	public String getValueInIdMap(String key)
	{
		return _idMap.get(key);
	}

	public Set<String> getIdKeys()
	{
		return _idMap.keySet();
	}	

	public void putInCombinedMap(String key, String value)
	{
		_combinedMap.put(key, value);
	}

	public String getValueInCombinedMap(String key)
	{
		return _combinedMap.get(key);
	}

	public Set<String> getCombinedKeys()
	{
		return _combinedMap.keySet();
	}
}
