package pt.linguateca.relations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pt.linguateca.harem.AlignmentSelector;
import pt.linguateca.harem.EntitiesAttributesFilter;
import pt.linguateca.harem.NamedEntity;

public class RelatedEntity extends NamedEntity{

	private HashMap<String, RelationsList> _relations;
	private EntitiesAttributesFilter _categoriesFilter;

	/*public RelatedEntity(NamedEntity entity, boolean participation){
		this(entity, true);
	}*/

	public RelatedEntity(NamedEntity entity, boolean participation, EntitiesAttributesFilter filter){
		super(entity.getElement());

		_categoriesFilter = filter;

		LinkedList<String> facets = new LinkedList<String>();
		LinkedList<String> categories = getCategories();

		if(categories.isEmpty())
		{
			facets.add(_tagBase.getSimpleEntityTag());
		}
		else if (participation)
		{
			facets.add(removeDuplicatesInVagueString(
					_entityElement.getAttributeValue(_tagBase.getCategTag())));
		}
		else
		{
			facets = (LinkedList<String>)categories.clone();
		}

		_relations = new HashMap<String, RelationsList>(facets.size());
		for(String f : facets)
			_relations.put(getFacetId(f), new RelationsList(getFacetId(f), this));

		createRelations();
	}

	private String removeDuplicatesInVagueString(String vague)
	{
		LinkedList<String> list = attributeToList(vague, _tagBase.getVagueSepRegex(), true);
		//System.out.println("list -> "+list);
		LinkedList<String> noDuplicates = removeDuplicatesInList(list);
		//System.out.println("clean -> "+noDuplicates);
		return vagueValuesToString(noDuplicates);
	}

	private LinkedList<String> removeDuplicatesInList(LinkedList<String> list)
	{
		LinkedList<String> toReturn = new LinkedList<String>();
		for(String s : list)
		{
			if(!toReturn.contains(s))
				toReturn.add(s);
		}
		return toReturn;
	}

	private String getFacetId(String facet)
	{
		return RelationM2.getSuperId(getId(), facet);
	}

	private void createRelations()
	{
		LinkedList<String> corels = getCorels();
		LinkedList<String> tiporels = getTipoRels();
		List<String> source = getSourceFacets();
		List<String> target = getTargetFacets();

		if(corels.size() != tiporels.size())
		{
			System.err.println("COREL com tamanho diferente de TIPOREL! : "+getId());
			return;
		}

		if(!corels.isEmpty()
				&& (source.isEmpty() || target.isEmpty()))
		{
			System.err.println("Atributos "+_tagBase.getSourceFacetAt()+
					" ou "+_tagBase.getTargetFacetAt()+" por preencher : " +getId());
			//System.err.println("#"+this);
			return;
		}
		
		if(!source.isEmpty() && !target.isEmpty()
				&& source.size() != target.size() && corels.size() != source.size())
		{
			System.err.println("COREL com tamanho diferente de "+_tagBase.getSourceFacetAt()+" ou "+_tagBase.getTargetFacetAt()+"! "+getId());
			return;
		}

		//colocar as relacoes existentes no mapa
		RelationM2 rel;
		Relation simpleRel;

		//System.err.println("#"+this+" "+source+" "+target);
		for(int i = 0; i < corels.size(); i++)
		{
			rel = new RelationM2(tiporels.get(i), getId(), corels.get(i),
					source.get(i), target.get(i));

			//System.err.println("rel= "+rel);
			//System.err.println("filtrada= "+rel.filter(_categoriesFilter));

			//filtrar as facetas da relacao, porque pode ter deixado de existir depois dos filtros HAREM
			if((rel = rel.filter(_categoriesFilter)) != null)
			{
				simpleRel = rel.getSimpleRelationWithSuperID();

				if(_relations.containsKey(simpleRel.getA()))
					_relations.get(simpleRel.getA()).addRelation(simpleRel);
				else
					System.err.println("Problema: "+simpleRel.getA()+": "+_relations.keySet());
			}
			else
			{
				continue;
			}
		}
	}

	public HashMap<String, RelationsList> getRelationsLists()
	{
		return _relations;
	}

	/*private List<String> filterCategories(LinkedList<String> categories)
	{
		LinkedList<String> filteredCategories = new LinkedList<String>();

		if(categories == null || categories.isEmpty() ||
				categories.contains(_tagBase.getSimpleEntityTag()))
		{
			filteredCategories.add(_tagBase.getSimpleEntityTag());
		}
		else
		{
			for(String c : categories)
			{
				if(_categoriesFilter.containsCategory(c))
					filteredCategories.add(c);
			}
		}

		return filteredCategories;
	}

	public void setCorels(LinkedList<String> corels)
	{	
		setRelationAttributes(corels, null);
	}

	private void setRelationAttributes(LinkedList<String> corels, LinkedList<String> tiporels)
	{	
		setRelationAttributes(corels, tiporels, null, null);
	}*/

	public void updateRelationAttributes()
	{
		LinkedList<String> corels = new LinkedList<String>();
		LinkedList<String> tiporels = new LinkedList<String>();
		LinkedList<String> source = new LinkedList<String>();
		LinkedList<String> target = new LinkedList<String>();

		RelationM2 rm2;
		for(RelationsList list : _relations.values())
		{
			for(Relation r : list)
			{
				rm2 = r.getRelationM2();
				if(rm2 != null)
				{
					corels.add(rm2.getB());
					tiporels.add(rm2.getType());
					source.add(rm2.getSourceCategory());
					target.add(rm2.getTargetCategory());
				}

			}
		}

		setRelationAttributes(corels, tiporels, source, target);
	}

	private void setRelationAttributes(List<String> corels, List<String> tiporels, List<String> source, List<String> target)
	{	
		if((tiporels == null && (corels.size() != super.getTipoRels().size()))
				|| (tiporels != null && (corels.size() != tiporels.size())))
		{
			System.err.println("COREL com tamanho diferente de TIPOREL!\n"+this);
			return;
		}

		if(corels != null)
		{
			if(!corels.isEmpty())
			{
				_entityElement.setAttribute(_tagBase.getCorelAt(), asRelationAttributeValue(corels));

				if(tiporels != null)
				{
					_entityElement.setAttribute(_tagBase.getTipoRelAt(), asRelationAttributeValue(tiporels));
				}

				if(source != null)
				{
					_entityElement.setAttribute(_tagBase.getSourceFacetAt(), asRelationAttributeValue(source));
				}

				if(target != null)
				{
					_entityElement.setAttribute(_tagBase.getTargetFacetAt(), asRelationAttributeValue(target));
				}
			}
			else
			{
				_entityElement.removeAttribute(_tagBase.getCorelAt());
				_entityElement.removeAttribute(_tagBase.getTipoRelAt());
				_entityElement.removeAttribute(_tagBase.getSourceFacetAt());
				_entityElement.removeAttribute(_tagBase.getTargetFacetAt());
			}
		}
	}

	private String asRelationAttributeValue(Iterable<String> iterable)
	{
		String newValue = "";
		for(String s : iterable)
			newValue += s + _tagBase.getCorelSep();

		return newValue.trim();
	}

	public RelationsList getRelationsList(String facet)
	{
		return _relations.get(facet);
	}

	public void addRelation(Relation relation)
	{
		RelationM2 rm2;

		if((rm2 = relation.getRelationM2()) != null)
		{			
			super.addAttributeValue(_tagBase.getCorelAt(), rm2.getB());
			super.addAttributeValue(_tagBase.getTipoRelAt(), rm2.getType());
			super.addAttributeValue(_tagBase.getSourceFacetAt(), rm2.getSourceCategory());
			super.addAttributeValue(_tagBase.getTargetFacetAt(), rm2.getTargetCategory());
		}
		else
		{
			super.addAttributeValue(_tagBase.getCorelAt(), relation.getB());
			super.addAttributeValue(_tagBase.getTipoRelAt(), relation.getType());
		}
	}

	/*public boolean removeEdge(Relation edge) {

		if(_relations.get(edge.getA()).removeRelation(edge))
		{
			LinkedList<String> corels = super.getCorels();
			LinkedList<String> tiporels = super.getTipoRels();

			int index = corels.indexOf(edge.getB());
			if(index < 0)
				return false;

			corels.remove(index);
			tiporels.remove(index);			
			setRelationAttributes(corels, tiporels);

			return true;
		}

		else
			return false;
	}*/

	public boolean containsRelation(Relation r){
		return _relations.get(r.getA()).containsRelation(r);
	}

	public double getMaximumRelationsEvaluation()
	{
		return _relations.size() * (RelationsInDocEvaluation.COREL_CORRECT + RelationsInDocEvaluation.TIPOREL_CORRECT);
	}

	/** Normalizar os atributos COREL, TIPOREL, FAC_ORIGEM e FAC_ALVO
	 * de acordo com o mapa recebido
	 * @param map Mapeia ID de um lado em {ID do outro, categ do outro}
	 */
	public void normalizeRelationAttributes(HashMap<String, String[]> map, boolean changeCorels)
	{
		LinkedList<String> corels = getCorels();
		LinkedList<String> tiporels = getTipoRels();
		List<String> source = getSourceFacets();
		List<String> target = getTargetFacets();

		Set<Integer> indexesToRemove = new HashSet<Integer>();

		List<String> intersection;
		String[] idCat;
		String newValue;

		for(int i = 0; i < corels.size(); i++)
		{			
			if(!map.containsKey(corels.get(i)))
			{
				indexesToRemove.add(i);
				continue;
			}
			else
			{	
				idCat = map.get(corels.get(i));

				if(!source.isEmpty() && !target.isEmpty())
				{
					if(!source.get(i).equals(vagueValuesToString(getCategories())))
					{
						intersection = AlignmentSelector.getIntersection(
								attributeToList(source.get(i), _tagBase.getVagueSepRegex(), true),
								getCategories());

						/*if(changeCorels)
						{
						System.out.println(source.get(i)+" ... "+vagueValuesToString(getCategories()));
						System.out.println("interseccao = "+intersection);
						}*/

						if(!intersection.isEmpty())
						{
							newValue = vagueValuesToString(intersection);
							source.set(i, newValue);
						}

						else
						{
							//System.err.println("interseccao= "+source.get(i)+" VS "+getCategories());						
							//System.err.println(corels.get(i));
							indexesToRemove.add(i);
							continue;
						}
					}
					if(!target.get(i).equals(idCat[1].trim()))
					{
						intersection = AlignmentSelector.getIntersection(
								attributeToList(target.get(i),_tagBase.getVagueSepRegex(), true),
								attributeToList(idCat[1], _tagBase.getVagueSepRegex(), true));

						if(!intersection.isEmpty())
						{
							newValue = vagueValuesToString(intersection);
							target.set(i, newValue);
						}
						else
						{
							indexesToRemove.add(i);
							continue;
						}
					}
				}

				if(changeCorels)
					corels.set(i, idCat[0]);
			}
		}

		LinkedList<String> normCorels = new LinkedList<String>();
		LinkedList<String> normTiporels = new LinkedList<String>();
		LinkedList<String> normSource = new LinkedList<String>();
		LinkedList<String> normTarget = new LinkedList<String>();

		//seleccionar apenas os indices que nao sao para remover
		for(int i = 0; i < corels.size(); i++)
		{
			if(!indexesToRemove.contains(i))
			{
				normCorels.add(corels.get(i));
				normTiporels.add(tiporels.get(i));

				if(source != null && !source.isEmpty()
						&& target != null && !target.isEmpty())
				{
					normSource.add(source.get(i));
					normTarget.add(target.get(i));
				}
			}
		}

		setRelationAttributes(normCorels, normTiporels, normSource, normTarget);
	}

	private List<String> getSourceFacets()
	{
		String value = _entityElement.getAttributeValue(_tagBase.getSourceFacetAt());	
		return value != null ? Arrays.asList(value.split(_tagBase.getCorelSep())) : new LinkedList<String>();
	}

	private List<String> getTargetFacets()
	{
		String value = _entityElement.getAttributeValue(_tagBase.getTargetFacetAt());
		return value != null ? Arrays.asList(value.split(_tagBase.getCorelSep())) : new LinkedList<String>();
	}

	/** categorias separadas por | e sem duplicados **/ 
	private String getCategoryKey()
	{
		String value = _entityElement.getAttributeValue(_tagBase.getCategTag());

		//remover duplicados LOCAL|LOCAL -> LOCAL
		if(value == null)
			return _tagBase.getSimpleEntityTag();

		List<String> list = Arrays.asList(value.split(_tagBase.getVagueSepRegex()));
		LinkedHashSet<String> set =	new LinkedHashSet<String>(list);

		return NamedEntity.vagueValuesToString(set);
	}

	private String getCompatibleKey(HashMap<String, RelationsList> map, String test)
	{	
		String[] splitTest = RelationM2.splitSuperId(test);
		String[] splitKey;

		for(String key : map.keySet())
		{
			splitKey = RelationM2.splitSuperId(key);
			if(splitTest[0].equals(splitKey[0]) &&
					RelationM2.compatibleCategories(splitKey[1], splitTest[1]))
			{

				return key;
			}
		}
		return null;
	}
}
