package pt.linguateca.harem;

import java.util.HashMap;
import java.util.Set;


public class RelationsReport extends Report
{
	private HashMap<String, String> _corelsMap;

	private HashMap<String, String> _relationsMap;

	private HashMap<String, String> _scoreMap;

	public RelationsReport(String system)
	{
		super(system);
	
		_corelsMap = new HashMap<String, String>();
		_relationsMap = new HashMap<String, String>();
		_scoreMap = new HashMap<String, String>();
	}

	public boolean isPrintable()
	{
		return !Double.isNaN(Double.parseDouble(_corelsMap.get(GlobalAlignmentEvaluator.PRECISION)));
	}

	public void putInCorelsMap(String key, String value)
	{
		_corelsMap.put(key, value);
	}

	public String getValueInCorelsMap(String key)
	{
		return _corelsMap.get(key);
	}

	public Set<String> getCorelsKeys()
	{
		return _corelsMap.keySet();
	}	

	public void putInRelationsMap(String key, String value)
	{
		_relationsMap.put(key, value);
	}

	public String getValueInRelationsMap(String key)
	{
		return _relationsMap.get(key);
	}

	public Set<String> getRelationsKeys()
	{
		return _relationsMap.keySet();
	}

	public void putInScoreMap(String key, String value)
	{
		_scoreMap.put(key, value);
	}

	public String getValueInScoreMap(String key)
	{
		return _scoreMap.get(key);
	}

	public Set<String> getScoreKeys()
	{
		return _scoreMap.keySet();
	}
}
