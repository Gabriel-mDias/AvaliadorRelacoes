package pt.linguateca.relations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import pt.linguateca.harem.AlignmentSelector;

public class RelationsList implements Iterable<Relation>, Cloneable, Node<Relation>{

	protected String _nodeId;
	//protected String _entity;
	protected RelatedEntity _entity;
	protected HashSet<Relation> _list;

	public RelationsList(String node, RelatedEntity entity){
		_nodeId = node;
		_entity = entity;
		_list = new HashSet<Relation>();
	}

	public String getEntity()
	{
		return _entity.getEntity();
	}

	public void addRelations(Collection<Relation> list){
		for(Relation r : list)
			addRelation(r);
	}

	public boolean addRelation(Relation r)
	{
		if(!r.getA().equals(_nodeId))
			return false;

		//System.out.println("list= "+_list+" "+"r = "+r);

		if(!r.getB().equals(_nodeId) && !_list.contains(r))
		{
			return _list.add(r);
		}
		return false;
	}

	/** Adiciona uma relação à lista */
	/*public boolean createRelation(Relation r)
	{
		return addRelation(r, false);
	}*/

	/** Adiciona uma relação à lista e actualiza os atributos da EM */
	/*public boolean addRelation(Relation r)
	{
		return addRelation(r, true);
	}*/

	public void addRelation(String type, String id)
	{
		addRelation(new Relation(type, _nodeId, id));
	}

	public Set<Relation> getList(){
		return _list;
	}

	public int size(){
		return _list.size();
	}

	public boolean containsRelationWith(String id)
	{	
		for(Relation r : _list)
			if(r.getB().equals(id))
				return true;

		return false;
	}

	public Set<Relation> relationsWithoutSpuriousArguments()
	{
		Set<Relation> toReturn = new HashSet<Relation>();
		for(Relation r : _list)
			if(!AlignmentSelector.isSpuriousId(r.getA()) && !AlignmentSelector.isSpuriousId(r.getB()))
				toReturn.add(r);

		return toReturn;
	}

	@Override
	public Iterator<Relation> iterator()
	{
		return _list.iterator();
	}

	public boolean containsRelation(Relation r)
	{
		/*		boolean toReturn = _list.contains(r);

		if(toReturn) {
			System.out.println("Lista: "+_list);
			System.out.println("Procurar: "+r);
		}

		return toReturn;*/
		return _list.contains(r);
	}

	public boolean removeRelation(Relation r)
	{		
		return _list.remove(r);
	}

	public Set<Relation> getAllRelationsWith(String id)
	{
		Set<Relation> toReturn = new HashSet<Relation>();
		for(Relation r : _list)
			if(r.getB().equals(id))
				toReturn.add(r);

		return toReturn;
	}

	public void removeAllRelationsWith(String id)
	{
		Set<Relation> toRemove = new HashSet<Relation>();
		for(Relation r : _list)
			if(r.getB().equals(id))
				toRemove.add(r);

		for(Relation r : toRemove)
			_list.remove(r);
	}

	private Set<String> getRelatedIds(boolean repetition, boolean spurious)
	{
		Set<String> idsList = new HashSet<String>();

		for(Relation r : _list)
		{
			if(repetition || !idsList.contains(r.getB()) &&
					(spurious || !AlignmentSelector.isSpuriousId(r.getB())))
			{
				idsList.add(r.getB());
			}	
		}
		return idsList;
	}

	public Set<String> getAllRelatedIds()
	{
		return getRelatedIds(true, true);
	}

	public Set<String> getAllRelatedIdsNoSpurious()
	{
		return getRelatedIds(true, false);
	}

	/**
	 * Ids relacionados, sem repeticao
	 * @return
	 */
	public Set<String> getRelatedIdsNoRepetitionNoSpurious()
	{
		return getRelatedIds(false, false);
	}

	public Set<String> getRelatedIdsNoRepetition()
	{
		return getRelatedIds(false, true);
	}

	@Override
	public Object clone()
	{
		RelationsList clone = null;
		try {
			clone = (RelationsList)super.clone();
			clone._nodeId = _nodeId;
			clone._list = (HashSet<Relation>)_list.clone();

		} catch (CloneNotSupportedException e) {
			System.out.println(e+" in RelationsList");
		}
		return clone;
	}

	public String toString()
	{
		String toString = "";
		for(Relation r : _list)
			toString += r+" ";

		return "[ "+toString+"]";
	}

	@Override
	public boolean addEdge(Relation edge) {
		return addRelation(edge);
	}

	@Override
	public String getKey() {
		return _nodeId;
	}
}