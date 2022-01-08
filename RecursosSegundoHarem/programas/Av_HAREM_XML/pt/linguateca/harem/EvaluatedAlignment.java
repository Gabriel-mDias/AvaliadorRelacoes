package pt.linguateca.harem;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author nseco, Besugo
 * 
 */
public abstract class EvaluatedAlignment
{
	protected NamedEntity _goldenEntity;

	// These two should be a HashMap, but because numerica tags are not used
	// two lists are used to avoid hash colisions
	protected LinkedList<NamedEntity> _alignments;

	protected LinkedList<String> _scores;

	protected double _altWeight;

	public EvaluatedAlignment()
	{
		_alignments = new LinkedList<NamedEntity>();
		_scores = new LinkedList<String>();
		_altWeight = 1.0;
	}

	public EvaluatedAlignment(NamedEntity goldenEntity)
	{
		this();
		_goldenEntity = goldenEntity;
	}

	public void setGoldenEntity(NamedEntity goldenEntity)
	{
		_goldenEntity = goldenEntity;
	}

	protected void removeAlignment(NamedEntity entity)
	{
		int index = _alignments.indexOf(entity);
		_alignments.remove(index);
		_scores.remove(index);
	}

	public void addAlignment(NamedEntity entity, String score)
	{
		_alignments.add(entity);
		_scores.add(score);
	}

	/*public void setAlignments(LinkedList<NamedEntity> entities, LinkedList<String> scores)
	{
		_alignments = entities;
		_scores = scores;
	}*/

	public Iterator<NamedEntity> getAligned()
	{
		return _alignments.iterator();
	}

	public LinkedList<NamedEntity> getAlignedList()
	{
		return _alignments;
	}

	public NamedEntity getLastAlignment()
	{
		return _alignments.getLast();
	}

	public NamedEntity getFirstAlignment()
	{
		return _alignments.getFirst();
	}

	public int getAlignmentCount()
	{
		return _alignments.size();
	}

	public boolean isNullAligned()
	{
		return _alignments.contains(null);
	}

	public boolean isSpurious()
	{
		return _goldenEntity.isSpurious();
	}

	public String getScore(NamedEntity entity)
	{
		int index = _alignments.indexOf(entity);
		return _scores.get(index);
	}

	public double getAltWeight()
	{
		return _altWeight;
	}

	public NamedEntity getGoldenEntity()
	{
		return _goldenEntity;
	}

	public void setSpurious(){

		if(_goldenEntity != null)
			_goldenEntity.setSpurious();

		for(int i = 0; i < _scores.size(); i++)
			_scores.set(i, IndividualAlignmentEvaluator.SPURIOUS);
	}

	public boolean hasCorrectAlignment(){

		for(String s : _scores)
			if(s.equalsIgnoreCase(IndividualAlignmentEvaluator.CORRECT))
				return true;

		return false;
	}

	public NamedEntity getCorrectAlignment(){

		LinkedList<NamedEntity> correct = new LinkedList<NamedEntity>();
		for(int i = 0; i < _scores.size(); i++){
			if(_scores.get(i).equalsIgnoreCase(IndividualAlignmentEvaluator.CORRECT)){
				correct.add(_alignments.get(i));
			}
		}

		if(correct.isEmpty())
			return null;
		else if(correct.size() == 1)
			return correct.get(0);

		else {

			NamedEntity entity = correct.get(0);
			AttributeTupleSet tuples = entity.getAttributeTupleSet();
			AttributeTupleSet otherTuples;

			for(NamedEntity ne : correct.subList(1, correct.size())){
				otherTuples = ne.getAttributeTupleSet();

				for(AttributeTuple tuple : otherTuples){
					if(!tuples.contains(tuple)){
						entity.addAttributeTuple(tuple);
					}
				}
			}
			return entity;
		}
	}
	
	public LinkedList<NamedEntity> getIncorrectAlignments(){
		
		LinkedList<NamedEntity> alignmentsNotCorrect =
			new LinkedList<NamedEntity>();
		
		for(int i = 0; i < _scores.size(); i++){
			if(!_scores.get(i).equalsIgnoreCase(IndividualAlignmentEvaluator.CORRECT)){
				alignmentsNotCorrect.add(_alignments.get(i));
			}
		}
		
		return alignmentsNotCorrect;
	}

	/*public void removeCorrectAlignments(){
		
		LinkedList<NamedEntity> alignmentsNotCorrect =
			new LinkedList<NamedEntity>();
		LinkedList<String> scoresNotCorrect =
			new LinkedList<String>();
		
		for(int i = 0; i < _scores.size(); i++){
			if(!_scores.get(i).equalsIgnoreCase(IndividualAlignmentEvaluator.CORRECT)){
				alignmentsNotCorrect.add(_alignments.get(i));
				scoresNotCorrect.add(_scores.get(i));
			}
		}
		
		_alignments = alignmentsNotCorrect;
		_scores = scoresNotCorrect;
	}*/

	public void setAltWeigth(double weight){		
		_altWeight = weight;
	}

	public void removeClassicAttributes(){
		_goldenEntity = _goldenEntity.removeClassicAttributes();

		if(_alignments != null && _alignments.get(0) != null)
		{
			LinkedList<NamedEntity> newAlignments = new LinkedList<NamedEntity>();
			for(int i = 0; i < _alignments.size(); i++)
				newAlignments.add(_alignments.get(i).removeClassicAttributes());

			if(!newAlignments.isEmpty())
				_alignments = newAlignments;
		}
	}

	public void removeComments(){
		_goldenEntity = _goldenEntity.removeComments();

		if(_alignments != null && _alignments.get(0) != null)
		{
			LinkedList<NamedEntity> newAlignments = new LinkedList<NamedEntity>();
			for(int i = 0; i < _alignments.size(); i++)
				newAlignments.add(_alignments.get(i).removeComments());

			if(!newAlignments.isEmpty())
				_alignments = newAlignments;
		}
	}

	public void leaveOnlyCategories(LinkedList<String> categories)
	{
		LinkedList<String> toRemove = new LinkedList<String>();

		if(!this.isSpurious())
		{
			for(String s : _goldenEntity.getCategories())
			{
				if(!categories.contains(s))
					toRemove.add(s);
			}
			
			//System.out.println("toRemove: "+toRemove+"\nGE antes = "+_goldenEntity);
			for(String s : toRemove)
				_goldenEntity.removeCategory(s);
			//System.out.println("GE depois = "+_goldenEntity);
		}

		if(_alignments != null && _alignments.get(0) != null)
		{
			//LinkedList<NamedEntity> newAlignments = new LinkedList<NamedEntity>();
			for(int i = 0; i < _alignments.size(); i++)
			{
				toRemove = new LinkedList<String>();
				for(String s : _alignments.get(i).getCategories())
				{
					if(!categories.contains(s))
						toRemove.add(s);
				}
				
				for(String s : toRemove)
					_alignments.get(i).removeCategory(s);		
			}
		}
	}
}
