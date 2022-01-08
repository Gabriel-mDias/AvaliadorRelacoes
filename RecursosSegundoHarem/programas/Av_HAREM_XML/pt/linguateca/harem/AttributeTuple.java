package pt.linguateca.harem;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * 
 * @author Besugo
 * Tuplo de atributos do HAREM clássico (CATEG, TIPO, SUBTIPO)
 */
public class AttributeTuple {

	protected String _category, _type, _subtype;

	public AttributeTuple() {
		this._category = null;
		this._type = null;
		this._subtype = null;
	}	

	public AttributeTuple(String category, String type, String subtype) {
		this._category = category;
		this._type = type;
		this._subtype = subtype;
	}

	public boolean isSpurious(){
		return _category.equals(TagBase.getInstance().getSpuriousTag());
	}

	public String getCategory() {
		return _category;
	}

	public void setCategory(String category) {
		this._category = category;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		this._type = type;
	}

	public String getSubtype() {		
		return _subtype;
	}

	public void setSubtype(String subtype) {
		this._subtype = subtype;
	}

	public int commonAttributes(AttributeTuple other)
	{
		int common = 0;
		if(_category != null && _category.equals(other.getCategory())){
			common++;
			if(_type != null && _type.equals(other.getType())){
				common++;
				if(_subtype != null &&_subtype.equals(other.getSubtype()))
					common++;
			}
		}
		return common;
	}

	public AttributeTuple getIntersection(AttributeTuple other)
	{		
		AttributeTuple intersection = new AttributeTuple();

		if(!isEmpty() && !other.isEmpty()){

			if(_category != null && other.getCategory() != null && _category.equals(other.getCategory())){
				intersection.setCategory(_category);

				if(_type != null && other.getType() != null && _type.equals(other.getType())){
					intersection.setType(_type);

					if(_subtype != null && other.getSubtype() != null && _subtype.equals(other.getSubtype()))
						intersection.setSubtype(_subtype);
				}
			}
		}		
		return intersection;
	}

	public AttributeTuple getDifference(AttributeTuple other)
	{		
		AttributeTuple difference = new AttributeTuple();
		//TODO: confirmar
		if(!isEmpty() && !other.isEmpty()){

			if(_category != null && !_category.equals(other.getCategory()))
				difference.setCategory(_category);
			else if(_type != null && !_type.equals(other.getType()))
				difference.setType(_type);
			else if(_subtype != null && !_subtype.equals(other.getSubtype()))
				difference.setSubtype(_subtype);
		}
		return difference;
	}

	public AttributeTuple onlyMostSignificant()
	{
		if(_category != null)
			return new AttributeTuple(_category, null, null);
		else if(_type != null)
			return new AttributeTuple(null, _type, null);
		else if(_subtype != null)
			return new AttributeTuple(null, null, _subtype);

		return null;
	}

	public boolean isEmpty()
	{
		return _category == null && _type == null && _subtype == null;
	}

	protected int getLevel()
	{
		if(_category != null) return 1;
		if(_type != null) return 2;
		if(_subtype != null) return 3;

		else return -1;
	}

	public String toString()
	{
		return "CATEG="+_category+" TIPO="+_type+" SUBTIPO="+_subtype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		+ ((_category == null) ? 0 : _category.hashCode());
		result = prime * result
		+ ((_subtype == null) ? 0 : _subtype.hashCode());
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object other){

		AttributeTuple otherTuple = (AttributeTuple) other;
		
		boolean c = false;
		boolean t = false;
		boolean s = false;

		if(isEmpty() && otherTuple.isEmpty())
			return true;

		if(_category == null)
			if(otherTuple.getCategory() != null)
				return false;
			else c = true;
		
		if(_type == null)
			if(otherTuple.getType() != null)
				return false;
			else t = true;

		if(_subtype == null)
			if(otherTuple.getSubtype() != null)
				return false;
			else s = true;
		
		if(!c) c = _category.equals(otherTuple.getCategory());

		if(!t) t = _type.equals(otherTuple.getType());

		if(!s) s = _subtype.equals(otherTuple.getSubtype());

		return c && t && s;
	}

	public boolean hasSameUpperAttributes(AttributeTuple other, String key)
	{
		if(key.equals(AttributesEvaluation.CATEGORY))
			return getCategory().equals(other.getCategory());
		
		if(key.equals(AttributesEvaluation.TYPE))
			return getCategory().equals(other.getCategory())
			&& getType().equals(other.getType());
		
		if(key.equals(AttributesEvaluation.SUBTYPE))
			return getCategory().equals(other.getCategory())
			&& getType().equals(other.getType())
			&& getSubtype().equals(other.getSubtype());
		
		return false;
	}
	
	public static Set<AttributeTuple> getListOfAttributes(
			LinkedList<String> cats, LinkedList<String> types, LinkedList<String> subs){

		Set<AttributeTuple> tuples = new HashSet<AttributeTuple>();
		/*if(subs.size() > 0)
			System.out.println("--- "+types);*/

		String cat = null;
		String type = null;
		String sub = null;
		for (int i = 0; i < cats.size(); i++)
		{
			cat = cats.get(i);

			type = (types.size() > i) ? types.get(i) : null;
			sub = (subs.size() > i) ? subs.get(i) : null;

			tuples.add(new AttributeTuple(cat, type, sub));
		}

		return tuples;
	}

	public static Set<AttributeTuple> getListOfAttributes(NamedEntity ne){

		return getListOfAttributes(ne.getCategories(), ne.getTypes(), ne.getSubtypes());
	}
	
	public static LinkedList<String> getCategories(LinkedList<AttributeTuple> list){

		LinkedList<String> ret = new LinkedList<String>();
		for(AttributeTuple at : list){
			ret.add(at.getCategory());
		}
		return ret;
	}

	public static LinkedList<String> getTypes(LinkedList<AttributeTuple> list){

		LinkedList<String> ret = new LinkedList<String>();
		for(AttributeTuple at : list){
			ret.add(at.getType());
		}
		return ret;
	}

	public static LinkedList<String> getSubtypes(LinkedList<AttributeTuple> list){

		LinkedList<String> ret = new LinkedList<String>();
		for(AttributeTuple at : list){
			ret.add(at.getSubtype());
		}
		return ret;
	}
}
