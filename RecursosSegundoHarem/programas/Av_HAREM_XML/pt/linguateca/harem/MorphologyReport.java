/*
 * Created on Jun 10, 2005
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
public class MorphologyReport extends Report
{
	private LinkedHashMap _numberMap;

	private LinkedHashMap _genreMap;

	private LinkedHashMap _combinedMap;

	public MorphologyReport(String system)
	{
		super(system);

		_numberMap = new LinkedHashMap();
		_genreMap = new LinkedHashMap();
		_combinedMap = new LinkedHashMap();
	}

	public boolean isPrintable()
	{
		return true;
	}

	public void putInNumberMap(String key, String value)
	{
		_numberMap.put(key, value);
	}

	public String getValueInNumberMap(String key)
	{
		return (String) _numberMap.get(key);
	}

	public Set getNumberKeys()
	{
		return _numberMap.keySet();
	}

	public void putInGenreMap(String key, String value)
	{
		_genreMap.put(key, value);
	}

	public String getValueInGenreMap(String key)
	{
		return (String) _genreMap.get(key);
	}

	public Set getGenreKeys()
	{
		return _genreMap.keySet();
	}

	public void putInCombinedMap(String key, String value)
	{
		_combinedMap.put(key, value);
	}

	public String getValueInCombinedMap(String key)
	{
		return (String) _combinedMap.get(key);
	}

	public Set getCombinedKeys()
	{
		return _combinedMap.keySet();
	}
}
