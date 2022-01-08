package pt.linguateca.relations;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import pt.linguateca.harem.AlignmentSelector;
import pt.linguateca.harem.EntitiesAttributesFilter;
import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.TagBase;


public class RelationM2 extends Relation
{		
	protected String _sourceCat;
	protected String _targetCat;

	public RelationM2(String type, String a, String b, String source, String target){

		super(type, a, b);
		_sourceCat = source;
		_targetCat = target;
	}

	/*public RelationM2(String representation){
		//A source type B target
		//super(representation.split(PARTS_SEP)[2], representation.split(PARTS_SEP)[0], representation.split(PARTS_SEP)[3]);

		String[] split = representation.split(PARTS_SEP);

		super._a = split[0];
		_sourceCat = split[1];
		super._type = split[2];
		super._b = split[3];
		_targetCat = split[4];
	}*/

	public String getSourceNodeId()
	{
		return getSuperId(super._a, _sourceCat);
	}

	public String getTargetNodeId()
	{
		return getSuperId(super._b, _targetCat);
	}

	public static String getSuperId(String id, String facet)
	{
		return id+RelationProcessor.PARTS_SEP+facet;
	}
	
	public static String[] splitSuperId(String id)
	{
		return id.split(RelationProcessor.PARTS_SEP);
	}	
	
	public String getSourceCategory()
	{
		return _sourceCat;
	}

	public String getTargetCategory()
	{
		return _targetCat;
	}

	public Relation getSimpleRelationWithSuperID()
	{
		return new Relation(_type, getSuperId(_a, _sourceCat), getSuperId(_b, _targetCat));
	}

	public RelationM2 filter(EntitiesAttributesFilter categoriesFilter)
	{
		List<String> sourceCats = splitAttributes(_sourceCat);
		List<String> targetCats = splitAttributes(_targetCat);

		Set<String> filteredSourceCats = new LinkedHashSet<String>();
		Set<String> filteredTargetCats = new LinkedHashSet<String>();

		String allCats = TagBase.getInstance().getSimpleEntityTag();

		if(sourceCats.contains(allCats))
			filteredSourceCats.add(allCats);
		else
		{
			for(String cat : sourceCats)
				if(categoriesFilter.containsCategory(cat))
					filteredSourceCats.add(cat);
		}

		if(targetCats.contains(allCats))
			filteredTargetCats.add(allCats);
		else
		{
			for(String cat : targetCats)
				if(categoriesFilter.containsCategory(cat))
					filteredTargetCats.add(cat);
		}

		if(filteredSourceCats.isEmpty() || filteredTargetCats.isEmpty())
			return null;

		else
		{
			return new RelationM2(_type, _a, _b, NamedEntity.vagueValuesToString(filteredSourceCats),
					NamedEntity.vagueValuesToString(filteredTargetCats));
		}
	}

	private static List<String> splitAttributes(String attributes)
	{
		return Arrays.asList(attributes.split(TagBase.getInstance().getVagueSepRegex()));
	}

	public boolean compatibleCategoriesAndArgumentsWith(RelationM2 other)
	{
		return (super.sameArgumentsAs(other) &&
				compatibleCategories(_sourceCat, other._sourceCat)
				&& compatibleCategories(_targetCat, other._targetCat));
	}

	public boolean compatibleCategoriesWith(RelationM2 other)
	{
		return (compatibleCategories(_sourceCat, other._sourceCat)
				&& compatibleCategories(_targetCat, other._targetCat));
	}

	public CorelKey getCorelKey()
	{
		return new CorelKey(_a, _b, _sourceCat, _targetCat);
	}

	public String toString(){
		return _a + RelationProcessor.PARTS_SEP + _sourceCat + RelationProcessor.PARTS_SEP
		+ _type + RelationProcessor.PARTS_SEP + _b + RelationProcessor.PARTS_SEP + _targetCat;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
		+ ((_sourceCat == null) ? 0 : _sourceCat.hashCode());
		result = prime * result
		+ ((_targetCat == null) ? 0 : _targetCat.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RelationM2 other = (RelationM2) obj;
		if (_sourceCat == null) {
			if (other._sourceCat != null)
				return false;
		} else if (!_sourceCat.equals(other._sourceCat))
			return false;
		if (_targetCat == null) {
			if (other._targetCat != null)
				return false;
		}
		return true;
	}

	public boolean compatibleWith(RelationM2 r)
	{
		return compatibleCategoriesAndArgumentsWith(r) && _type.equals(r._type);
	}

	public static boolean compatibleCategories(String cat1, String cat2)
	{		
		if(cat1.trim().equals(cat2.trim()))
			return true;

		/*if(cat1.equals(TagBase.getInstance().getSimpleEntityTag())
				|| cat2.equals(TagBase.getInstance().getSimpleEntityTag()))
			return true;*/

		List<String> values1 = NamedEntity.attributeToList(cat1, TagBase.getInstance().getVagueSepRegex(), true);
		List<String> values2 = NamedEntity.attributeToList(cat2, TagBase.getInstance().getVagueSepRegex(), true);

		return !AlignmentSelector.getIntersection(values1, values2).isEmpty();
	}
	
	/*public static boolean compatibleCategories(String cat1, String cat2)
	{		
		if(cat1.trim().equals(cat2.trim()))
			return true;

		if(cat1.equals(TagBase.getInstance().getSimpleEntityTag())
				|| cat2.equals(TagBase.getInstance().getSimpleEntityTag()))
			return true;

		List<String> values1 = splitAttributes(cat1);
		List<String> values2 = splitAttributes(cat2);

		for(String c : values1)
			if(values2.contains(c))
				return true;

		for(String c : values2)
			if(values1.contains(c))
				return true;

		return false;
	}*/
}
