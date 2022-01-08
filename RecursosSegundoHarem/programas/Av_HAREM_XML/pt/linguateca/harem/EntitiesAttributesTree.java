package pt.linguateca.harem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author Besugo
 *
 * Árvore de atributos, utilizada para representar atributos clássicos válidos ou filtros
 */
public class EntitiesAttributesTree {

	protected TreeMap<String, TreeMap<String, Set<String>>> _tree;
	public static final String OTHER = "OUTRO";
	public static final String EVERYTHING = "*";

	private final String SEP_CATS = ":";
	private final String SEP_TYPES = ";";
	private final String SEP_SUBS = ",";
	private final String RIGHT_TYPES = "(";
	private final String LEFT_TYPES = ")";
	private final String RIGHT_SUBS = "{";
	private final String LEFT_SUBS = "}";

	public EntitiesAttributesTree(){

		_tree = new TreeMap<String, TreeMap<String,Set<String>>>();
	}

	public EntitiesAttributesTree(String filter, TagBase tags){

		_tree = new TreeMap<String, TreeMap<String,Set<String>>>();
		fillTree(filter, tags);
	}

	private void fillTree (String filterString, TagBase tags){
		
		//para retirar o # do inicio
		filterString = filterString.replaceAll("#", "");
		
		if(filterString.equals(EVERYTHING)){
			_tree = tags.getEntitiesAttributesTree().getTree();
			return;
		}
		
		String[] categories = filterString.split(SEP_CATS);
		String[] types, subs;
		String currCat, currType, currSub;
		
		for (int i = 0; i < categories.length; i++)
		{			
			types = categories[i].split("["+RIGHT_TYPES+SEP_TYPES+LEFT_TYPES+"]");
			currCat = types[0];
			this.newCategory(currCat);

			if(types.length > 1){

				if(types[1].equals(EVERYTHING)){
					this.addTypes(currCat, tags.getTypes(currCat));
					for(String t : this.getTypes(currCat))
						this.addSubtypes(currCat, t, tags.getSubtypes(currCat, t));
				}
				
				else {
					
					for (int j = 1; j < types.length; j++)
					{
						subs = types[j].split("["+RIGHT_SUBS+SEP_SUBS+LEFT_SUBS+"]");
						currType = subs[0];
						this.newType(currCat, currType);		

						if(subs.length > 1){

							if(subs[1].equals(EVERYTHING))
								this.addSubtypes(currCat, currType, tags.getSubtypes(currCat, currType));

							else {
								
								for (int k = 1; k < subs.length; k++)
								{	
									currSub = subs[k];
									this.newSubtype(currCat, currType, currSub);
								}
							}
						}
					}
				}
			}
		}
	}

	public void newCategory(String category){

		if(!_tree.containsKey(category))
			_tree.put(category, new TreeMap<String, Set<String>>());
	}

	public void newType(String category, String type){

		TreeMap<String, Set<String>> types = _tree.get(category);
		if(types == null){
			newCategory(category);
			types = _tree.get(category);
		}

		if(!types.containsKey(type))
			types.put(type, new HashSet<String>());
	}

	public void newSubtype(String category, String type, String sub){

		TreeMap<String, Set<String>> types = _tree.get(category);
		if(types == null){
			newCategory(category);
			types = _tree.get(category);
		}

		Set<String> subs = types.get(type);
		if(subs == null){
			newType(category, type);
			subs = types.get(type);
		}

		if(!subs.contains(sub))
			subs.add(sub);
	}

	public void addTypes(String cat, Set<String> types){

		for(String t : types){
			newType(cat, t);
		}
	}

	public void addSubtypes(String cat, String type, Set<String> subs){

		for(String s : subs){
			newSubtype(cat, type, s);
		}
	}

	/*public String getCategoryOfType(String type)
	{
		String current;

		for (Iterator<String> i = _tree.keySet().iterator(); i.hasNext();)
		{
			current = (String) i.next();
			if (containsType(current, type))
			{
				return current;
			}
		}

		return null;
	}*/

	public boolean isEmpty()
	{
		return _tree.isEmpty();
	}
	
	public boolean containsCategory(String key)
	{
		return _tree.containsKey(key);
	}
	
	public boolean hasTypes(String key)
	{
		return !_tree.get(key).isEmpty();
	}
	
	public boolean containsType(String key, String type)
	{
		TreeMap<String, Set<String>> map = _tree.get(key);

		if(map == null)
			return false;

		return map.containsKey(type);
	}

	public boolean containsType(String type)
	{
		Set<String> cats = _tree.keySet();
		for(String c : cats){
			if(_tree.get(c).containsKey(type))
				return true;
		}
		return false;
	}

	public boolean hasSubtypes(String cat, String type)
	{
		return !_tree.get(cat).get(type).isEmpty();
	}
	
	public boolean containsSubtype(String key, String type, String sub)
	{
		TreeMap<String, Set<String>> map = _tree.get(key);

		if(map == null)
			return false;

		Set<String> set = map.get(type);

		if(set == null)
			return false;

		return set.contains(sub);
	}

	public boolean containsSubtype(String sub)
	{
		Set<String> cats = _tree.keySet();
		for(String c : cats){
			Set<String> types = _tree.get(c).keySet();
			for(String t : types)
				if(_tree.get(c).get(t).contains(sub))
					return true;
		}
		return false;
	}

	public Set<String> getCategories()
	{
		return _tree.keySet();
	}

	public Set<String> getTypes(String category)
	{
		if(_tree.get(category) != null)
			return _tree.get(category).keySet();
		else
			return new HashSet<String>();
	}

	public Set<String> getSubtypes(String category, String type)
	{
		TreeMap<String, Set<String>> types = _tree.get(category);
		if(types != null && types.get(type) != null)
			return _tree.get(category).get(type);
		else
			return new HashSet<String>();

	}

	public TreeMap<String, TreeMap<String, Set<String>>> getTree(){
		return _tree;
	}

	public void fillAttributesWithOutro(){

		if(!containsCategory(OTHER))
			newCategory(OTHER);

		Set<String> categories = getCategories();
		for(String c : categories){

			if(!c.equals(OTHER) && !containsType(c, OTHER))
				newType(c, OTHER);

			Set<String> types = getTypes(c);
			for(String t : types){

				if(!t.equals(OTHER) && !containsSubtype(c, t, OTHER))
					newSubtype(c, t, OTHER);			
			}
		}
	}

	public AttributeTupleSet getValidTuples(NamedEntity ne){
		return getValidTuples(ne.getCategories(), ne.getTypes(), ne.getSubtypes());
	}

	/**
	 * Cria set de atributos coloca a null os atributos invalidos
	 * @param cats
	 * @param types
	 * @param subs
	 * @return
	 */
	public AttributeTupleSet getValidTuples(
			LinkedList<String> cats, LinkedList<String> types, LinkedList<String> subs)
			{

		AttributeTupleSet valid = new AttributeTupleSet();
		AttributeTuple tuple;

		for (int i = 0; i < cats.size(); i++)
		{
			String category = null;
			String type = null;
			String subtype = null;
			tuple = new AttributeTuple(category, type, subtype); //tudo a null

			category = cats.get(i);
			if(category != null && containsCategory(category))
			{	
				tuple.setCategory(category);
				if(types.size() > i)
					type = types.get(i);

				if(type != null && containsType(category, type))
				{	
					tuple.setType(type);
					if(subs.size() > i)
						subtype = subs.get(i);

					if(subtype != null && containsSubtype(category, type, subtype)){
						tuple.setSubtype(subtype);
					}
				}

				if(!valid.contains(tuple)){
					valid.add(tuple);
				}

			}
		}
		return valid;
			}


	public LinkedList<String> getValidCategories(LinkedList<String> list)
	{
		LinkedList<String> valid = new LinkedList<String>();
		String current;

		for (Iterator<String> i = list.iterator(); i.hasNext();)
		{
			current = i.next();
			//System.out.println("current= "+current+"; containsCurrent= "+containsCategory(current));
			if (containsCategory(current) && !valid.contains(current))
			{
				valid.add(current);
			}
		}

		return valid;
	}

	public LinkedList<String> getValidTypes(LinkedList<String> cats, LinkedList<String> types)
	{
		LinkedList<String> valid = new LinkedList<String>();
		String category, type;

		for (int i = 0; i < cats.size(); i++)
		{
			category = cats.get(i);
			if(types.size() > i)
				type = types.get(i);
			else break;

			if (containsType(category, type) && !valid.contains(type))
			{
				valid.add(type);
			}
		}
		return valid;
	}

	public LinkedList<String> getValidTypes(LinkedList<String> list)
	{
		LinkedList<String> valid = new LinkedList<String>();
		String current;

		for (Iterator<String> i = list.iterator(); i.hasNext();)
		{
			current = i.next();
			if (containsType(current) && !valid.contains(current))
			{
				valid.add(current);
			}
		}

		return valid;
	}

	public LinkedList<String> getValidSubtypes(
			LinkedList<String> cats, LinkedList<String> types, LinkedList<String> subs)
			{
		LinkedList<String> valid = new LinkedList<String>();
		String category = null;
		String type = null;
		String subtype = null;

		for (int i = 0; i < cats.size(); i++)
		{
			category = cats.get(i);

			if(types.size() > i)
				type = types.get(i);
			else break;

			if(subs.size() > i)
				subtype = subs.get(i);
			else break;

			if (containsSubtype(category, type, subtype) && !valid.contains(subtype))
			{
				valid.add(subtype);
			}
		}
		return valid;
			}

	public LinkedList<String> getValidSubtypes(LinkedList<String> list)
	{
		LinkedList<String> valid = new LinkedList<String>();
		String current;

		for (Iterator<String> i = list.iterator(); i.hasNext();)
		{
			current = i.next();
			if (containsSubtype(current) && !valid.contains(current))
			{
				valid.add(current);
			}
		}

		return valid;
	}

	/**
	 * Tipos de types que pertencem 'a categoria category
	 * @param category
	 * @param types
	 * @return
	 */
	public LinkedList<String> getTypesOfCategory(String category, Collection<String> types)
	{
		//System.out.println("category= "+category);
		//System.out.println("types= "+types);
		LinkedList<String> correct = new LinkedList<String>();
		Set<String> allTypes = getTypes(category);

		String current;

		if (allTypes == null || types == null)
			return correct;

		for (Iterator<String> i = types.iterator(); i.hasNext();)
		{
			current = i.next();
			if (allTypes.contains(current))
				correct.add(current);
		}
		//System.out.println("correct= "+correct);
		return correct;
	}

	public LinkedList<String> getPossibleCategoriesOfType(String type)
	{
		LinkedList<String> possibilities = new LinkedList<String>();

		for(String c : getCategories()){
			for(String t : getTypes(c))
				if(t.equals(type))
					possibilities.add(c);
		}

		return possibilities;
	}

	public LinkedList<String> getPossibleTypesOfSubtype(String sub)
	{
		LinkedList<String> possibilities = new LinkedList<String>();

		for(String c : getCategories())
			for(String t : getTypes(c))
				for(String s : getSubtypes(c, t))
					if(s.equals(sub))
						possibilities.add(t);

		return possibilities;
	}

	public LinkedList<String> getSubtypesOfType(String category, String type, Collection<String> subs){

		LinkedList<String> correct = new LinkedList<String>();
		Set<String> allSubs = getSubtypes(category, type);

		String current;

		if (allSubs == null || subs == null)
			return correct;

		for (Iterator<String> i = subs.iterator(); i.hasNext();)
		{
			current = i.next();
			if (allSubs.contains(current))
				correct.add(current);
		}
		return correct;
	}
	
	public boolean hasIntersectionWith(EntitiesAttributesTree other)
	{	
		//só funciona até ao nível TIPO
		Set<String> catIntersection = getIntersection(getCategories(), other.getCategories());		
		if(catIntersection.isEmpty())
			return false;

		Set<String> typeIntersection, types;
		for(String c : catIntersection)
		{
			types = getTypes(c);
			typeIntersection = getIntersection(types, other.getTypes(c));
			if(!typeIntersection.isEmpty() ||
					(typeIntersection.isEmpty() && types.isEmpty()))
				return true;
		}
		
		return false;
	}
	
	private Set<String> getIntersection(Set<String> set1, Set<String> set2)
	{	
		//System.out.println("set1= "+set1);
		//System.out.println("set2= "+set2);
		
		Set<String> intersection = new HashSet<String>();
		String current;

		if (set1 == null || set2 == null)
		{
			return intersection;
		}

		for (Iterator<String> i = set1.iterator(); i.hasNext();)
		{
			current = i.next();
			if (set2.contains(current))
			{
				intersection.add(current);
			}
		}

		return intersection;
	}
	
	public String toString(){
		
		String ret = new String();
		int i = 0, j, k;
		
		Set<String> categories = _tree.keySet();
		for(String c : categories){

			j = 0;
			i++;

			ret += c;

			Set<String> types = _tree.get(c).keySet();
			if(!types.isEmpty())
				ret += RIGHT_TYPES;

			for(String t : types){

				k = 0;
				j++;

				ret += t;

				Set<String> subs = _tree.get(c).get(t);
				if(!subs.isEmpty())
					ret += RIGHT_SUBS;

				for(String s : subs){
					k++;

					ret += s;

					if(k <= subs.size() - 1)
						ret += SEP_SUBS;
				}

				if(!subs.isEmpty())
					ret += LEFT_SUBS;

				if(j <= types.size() - 1)
					ret += SEP_TYPES;
			}

			if(!types.isEmpty())
				ret += LEFT_TYPES;

			if(i <= categories.size() - 1)
				ret += SEP_CATS;
		}

		return ret;
		//return _tree.toString();
	}
}
