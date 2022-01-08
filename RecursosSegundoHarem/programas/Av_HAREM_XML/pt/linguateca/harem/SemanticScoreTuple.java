/*
 * Created on Aug 17, 2005
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
public class SemanticScoreTuple
{
	private int _missingCategories;

	private int _missingTypes;
	
	private int _missingSubtypes;

	private int _spuriousCategories;

	private int _spuriousTypes;
	
	private int _spuriousSubtypes;

	private int _correctCategories;

	private int _correctTypes;
	
	private int _correctSubtypes;

	private double _weight;
	
	private double _altWeight;

	private double _csc;
	
	private double _maxCSCsys;
	
	private double _maxCSCgc;

	/**
	 * 
	 */
	public SemanticScoreTuple()
	{
		;
	}

	public void setWeight(double weight)
	{
		_weight = weight;
	}

	public double getWeight()
	{
		return _weight;
	}

	public void setAltWeight(double weight)
	{
		_altWeight = weight;
	}
	
	public double getAltWeight()
	{
		return _altWeight;
	}
	
	public void setMaximumCSCinGC(double csc)
	{
		_maxCSCgc = csc;
	}
	
	public double getMaximumCSCinGC()
	{
		return _maxCSCgc;
	}
	
	public void setMaximumCSCinSystem(double csc)
	{
		_maxCSCsys = csc;
	}
	
	public double getMaximumCSCinSystem()
	{
		return _maxCSCsys;
	}
	
	public void setCombinedSemanticClassification(double csc)
	{
		_csc = csc;
	}

	public double getCombinedSemanticClassification()
	{
		return _csc;
	}

	public void setMissingCategories(int missingCategories)
	{
		_missingCategories = getBinaryValue(missingCategories);
	}

	public int getMissingCategories()
	{
		return _missingCategories;
	}

	public void setSpuriousCategories(int spuriousCategories)
	{
		_spuriousCategories = getBinaryValue(spuriousCategories);
	}

	public int getSpuriousCategories()
	{
		return _spuriousCategories;
	}

	public void setCorrectCategories(int correctCategories)
	{
		_correctCategories = getBinaryValue(correctCategories);
	}

	public int getCorrectCategories()
	{
		return _correctCategories;
	}

	public void setMissingTypes(int missingTypes)
	{
		_missingTypes = getBinaryValue(missingTypes);
	}

	public int getMissingTypes()
	{
		return _missingTypes;
	}

	public void setSpuriousTypes(int spuriousTypes)
	{
		_spuriousTypes = getBinaryValue(spuriousTypes);
	}

	public int getSpuriousTypes()
	{
		return _spuriousTypes;
	}

	public void setCorrectTypes(int correctTypes)
	{
		_correctTypes = getBinaryValue(correctTypes);
	}

	public int getCorrectTypes()
	{
		return _correctTypes;
	}

	public void setMissingSubtypes(int missing)
	{
		_missingSubtypes = getBinaryValue(missing);
	}

	public int getMissingSubtypes()
	{
		return _missingSubtypes;
	}

	public void setSpuriousSubtypes(int spurious)
	{
		_spuriousSubtypes = getBinaryValue(spurious);
	}

	public int getSpuriousSubtypes()
	{
		return _spuriousSubtypes;
	}

	public void setCorrectSubtypes(int correct)
	{
		_correctSubtypes = getBinaryValue(correct);
	}

	public int getCorrectSubtypes()
	{
		return _correctSubtypes;
	}
	
	private int getBinaryValue(int number)
	{
		if (number > 0)
			return 1;

		return 0;
	}

}
