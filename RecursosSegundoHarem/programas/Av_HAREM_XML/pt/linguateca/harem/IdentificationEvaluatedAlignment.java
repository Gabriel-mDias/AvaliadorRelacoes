package pt.linguateca.harem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author nseco, Besugo
 * 
 * Alinhamento avaliado de acordo com a identificação
 */
public class IdentificationEvaluatedAlignment extends EvaluatedAlignment
{
	public String toString()
	{
		String out = _goldenEntity.toString() + Aligner.ALIGNMENT_CONNECTOR + _alignments.toString();
		out += IndividualAlignmentEvaluator.EVALUATION_MARKER + _scores.toString();

		if(_altWeight != 1.0)
			out += IndividualAlignmentEvaluator.ALT_WEIGHT_MARKER + _altWeight;

		return out;
	}

	public Object clone()
	{
		IdentificationEvaluatedAlignment clone = new IdentificationEvaluatedAlignment();
		NamedEntity current;
		clone.setGoldenEntity((NamedEntity) _goldenEntity.clone());

		for (Iterator i = _alignments.iterator(); i.hasNext();)
		{
			current = (NamedEntity) i.next();
			if (current != null)
			{
				clone.addAlignment((NamedEntity) current.clone(), getScore(current));
			}
			else
			{
				clone.addAlignment(current, getScore(current));
			}
		}

		return clone;
	}

	public IdentificationEvaluatedAlignment getFilteredAlignment(
			EntitiesAttributesFilter evalFilter, EntitiesAttributesFilter systemFilter)
	{
		//System.out.println("A: "+this);

		IdentificationEvaluatedAlignment toReturn = new IdentificationEvaluatedAlignment();
		toReturn.setAltWeigth(_altWeight);

		NamedEntity filteredGolden = null;
		NamedEntity filtered = null;
		boolean alignedHadCategories;

		LinkedList<NamedEntity> filteredAlignments = new LinkedList<NamedEntity>();
		LinkedList<String> newScores = new LinkedList<String>();
		
		//remover alinhamentos com EMs não classificadas quando o cenário do participante
		//nao tem nada em comum com o cenário de avaliação
		if(!isNullAligned())
		{
			Set<NamedEntity> toRemove = new HashSet<NamedEntity>();
			for(NamedEntity entity : _alignments){

				alignedHadCategories = entity.hasCategories();

				if((!alignedHadCategories && !evalFilter.hasIntersectionWith(systemFilter))
						/*|| (hasCategories && systemFilter.getValidTuples(entity).isEmpty())*/)
					toRemove.add(entity);
			}
			
			for(NamedEntity entity : toRemove)
				removeAlignment(entity);
		}
		
		if(!isSpurious())
		{
			filteredGolden = _goldenEntity.filter(evalFilter);			
		}
		else if(isSpurious() && _alignments.isEmpty())
		{
			return null;
		}
		else //se for espurio
		{
			filteredGolden = (NamedEntity)_goldenEntity.clone();
		}

		if(!isNullAligned())
		{
			for(int i = 0; i < _alignments.size(); i++){

				alignedHadCategories = _alignments.get(i).hasCategories();
				filtered = _alignments.get(i).filter(evalFilter);
				
				//se tiver categorias ou se não tinha originalmente
				if(filtered.hasCategories() ||
						(!filtered.hasCategories() && !alignedHadCategories))
				{
					filteredAlignments.add(filtered);
					newScores.add(_scores.get(i));
				}		
			}
		}	

		//se a cd filtrada tiver ficado sem categorias, fica espuria
		if(!filteredGolden.hasCategories() && _goldenEntity.hasCategories())
		{
			filteredGolden.setSpurious();
		}

		//se a cd filtrada for espuria e nao tiver nada alinhado, o alinhamento desaparece
		if(filteredGolden.isSpurious() && filteredAlignments.isEmpty())
		{
			return null;
		}

		toReturn.setGoldenEntity(filteredGolden);

		if(filteredAlignments.isEmpty())
		{
			toReturn.addAlignment(null, IndividualAlignmentEvaluator.MISSING);
		}
		else
		{
			for(int i = 0; i < filteredAlignments.size(); i++)
			{
				if(filteredGolden.isSpurious())
					toReturn.addAlignment(filteredAlignments.get(i), IndividualAlignmentEvaluator.SPURIOUS);
				else
					toReturn.addAlignment(filteredAlignments.get(i), newScores.get(i));
			}
		}

		return toReturn;
	}

	/*public IdentificationEvaluatedAlignment getFilteredAlignment(EntitiesAttributesFilter filter, EntitiesAttributesFilter filter, String mode)
	{
		IdentificationEvaluatedAlignment toReturn = new IdentificationEvaluatedAlignment();
		toReturn.setAltWeigth(_altWeight);

		NamedEntity filteredGolden = null;
		NamedEntity filtered = null;
		boolean hasCategories = true;
		LinkedList<NamedEntity> filteredAlignments = new LinkedList<NamedEntity>();
		LinkedList<String> newScores = new LinkedList<String>();

		if(!isSpurious())
		{
			filteredGolden = _goldenEntity.filter(filter);
		}
		else
		{
			filteredGolden = (NamedEntity)_goldenEntity.clone();
		}

		if(!isNullAligned())
		{
			for(int i = 0; i < _alignments.size(); i++){

				hasCategories = _alignments.get(i).hasCategories();
				filtered = _alignments.get(i).filter(filter);

				if(filtered.hasCategories() ||
						(!filtered.hasCategories() && !hasCategories))
				{
					filteredAlignments.add(filtered);
					newScores.add(_scores.get(i));
				}		
			}
		}	

		//se a cd filtrada tiver ficado sem categorias, fica espuria
		if(!filteredGolden.hasCategories())
		{
			//filteredGolden = ((NamedEntity)_goldenEntity.clone());
			filteredGolden = new NamedEntity();
			filteredGolden.setEntity(_goldenEntity.getEntity());
			filteredGolden.setSpurious();
		}

		//se a cd filtrada for espuria...
		if(filteredGolden.isSpurious())
		{
			//se nao tiver nada alinhado, o alinhamento desaparece
			if(filteredAlignments.isEmpty())
				return null;

			if(mode == null || (mode.equals(AlignmentFilter.MODE_A) && !someAlignmentHasCategories()))
			{
				return null;
			}
		}

		toReturn.setGoldenEntity(filteredGolden);

		if(filteredAlignments.isEmpty())
		{
			toReturn.addAlignment(null, IndividualAlignmentEvaluator.MISSING);
		}
		else
		{
			for(int i = 0; i < filteredAlignments.size(); i++)
			{
				if(filteredGolden.isSpurious())
					toReturn.addAlignment(filteredAlignments.get(i), IndividualAlignmentEvaluator.SPURIOUS);
				else
					toReturn.addAlignment(filteredAlignments.get(i), newScores.get(i));
			}
		}

		return toReturn;
	}*/

	private boolean someAlignmentHasCategories()
	{
		for(Iterator<NamedEntity> i = getAligned(); i.hasNext(); )
			if(i.next().hasCategories())
				return true;

		return false;		
	}

	public IdentificationEvaluatedAlignment getIdentificationOnlyAlignment(){
		IdentificationEvaluatedAlignment toReturn = new IdentificationEvaluatedAlignment();
		toReturn.setAltWeigth(_altWeight);

		if(!isSpurious())
			toReturn.setGoldenEntity(_goldenEntity.removeClassicAttributes());
		else
			toReturn.setGoldenEntity(_goldenEntity);

		if(!isNullAligned()){

			LinkedList<NamedEntity> entities = new LinkedList<NamedEntity>();
			LinkedList<String> scores = new LinkedList<String>();
			for(int i = 0; i < _alignments.size(); i++){
				entities.add(_alignments.get(i).removeClassicAttributes());
				scores.add(_scores.get(i));
			}

			for(int i = 0; i < entities.size(); i++)
				toReturn.addAlignment(entities.get(i), scores.get(i));
		} else
			toReturn.addAlignment(null, IndividualAlignmentEvaluator.MISSING);

		return toReturn;
	}
}
