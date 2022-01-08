package pt.linguateca.harem;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * 
 * @author Besugo
 *
 * Conjunto de tuplos clássicos (CATEG, TIPO, SUBTIPO)
 */
public class AttributeTupleSet extends HashSet<AttributeTuple>
{	
	public AttributeTupleSet()
	{
		super();
	}
	
	public LinkedList<String> getCategories()
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(AttributeTuple tuple : this)
		{
			if(!toReturn.contains(tuple.getCategory()))
				toReturn.add(tuple.getCategory());
		}
		
		return toReturn;
	}
	
	public LinkedList<String> getTypes()
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(AttributeTuple tuple : this)
		{
			if(!toReturn.contains(tuple.getType()))
				toReturn.add(tuple.getType());
		}
		
		return toReturn;
	}
	
	public LinkedList<String> getSubtypes()
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(AttributeTuple tuple : this)
		{
			if(!toReturn.contains(tuple.getSubtype()))
				toReturn.add(tuple.getSubtype());
		}
		
		return toReturn;
	}
	
	public LinkedList<String> getAttribute(String key)
	{
		if(key.equals(AttributesEvaluation.CATEGORY))
			return getCategories();
		if(key.equals(AttributesEvaluation.TYPE))
			return getTypes();
		if(key.equals(AttributesEvaluation.SUBTYPE))
			return getSubtypes();
		
		return null;
	}
	
	public LinkedList<String> getCategoriesOfType(String type)
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(AttributeTuple tuple : this)
		{
			if(tuple.getType().equals(type) && !toReturn.contains(tuple.getCategory()))
				toReturn.add(tuple.getCategory());
		}
		
		return toReturn;
	}
	
	public LinkedList<String> getTypesOfSubtype(String sub)
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(AttributeTuple tuple : this)
		{
			if(tuple.getSubtype().equals(sub) && !toReturn.contains(tuple.getType()))
				toReturn.add(tuple.getType());
		}
		
		return toReturn;
	}
	
	public static AttributeTupleSet getAttributeTupleSet(
			LinkedList<String> cats, LinkedList<String> types, LinkedList<String> subs){

		AttributeTupleSet tuples = new AttributeTupleSet();
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
	
	public static AttributeTupleSet getAttributeTupleSet(NamedEntity ne)
	{
		return getAttributeTupleSet(emptyEntriesToNull(ne.getCategories()),
				emptyEntriesToNull(ne.getTypes()),
				emptyEntriesToNull(ne.getSubtypes()));
	}
	
	private static LinkedList<String> emptyEntriesToNull(LinkedList<String> list)
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(String s : list)
			if(s.equals(""))
				toReturn.add(null);
			else
				toReturn.add(s);
		
		return toReturn;
	}

}
