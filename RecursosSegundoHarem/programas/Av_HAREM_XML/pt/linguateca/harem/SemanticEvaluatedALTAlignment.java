/*
 * Created on Jun 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Iterator;
import java.util.Set;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class SemanticEvaluatedALTAlignment extends EvaluatedALTAlignment
{
	private EntitiesAttributesFilter _filter;

	public SemanticEvaluatedALTAlignment(EntitiesAttributesFilter filter)
	{
		super();
		_filter = filter;
	}

	public int compareTo(Object o)
	{
		SemanticEvaluatedALTAlignment another = (SemanticEvaluatedALTAlignment) o;

		double myCategoryFMeasure = getMeasureF();
		double anotherCategoryFMeasure = another.getMeasureF();

		if (myCategoryFMeasure > anotherCategoryFMeasure)
			return 1;

		if (myCategoryFMeasure < anotherCategoryFMeasure)
			return -1;

		double anotherSumOfCSC = another.getSumOfCSC();
		double mySumOfCSC = getSumOfCSC();

		if (mySumOfCSC > anotherSumOfCSC)
			return 1;

		if (mySumOfCSC < anotherSumOfCSC)
			return -1;

		if (_alignments.size() > another._alignments.size())
			return 1;

		if (_alignments.size() < another._alignments.size())
			return -1;

		return 0;
	}

	private double getSumOfCSC()
	{
		SemanticEvaluatedAlignment current;
		NamedEntity aligned;
		String score;
		SemanticScoreTuple scoreTuple;
		double sum = 0;

		for (Iterator<EvaluatedAlignment> i = _alignments.iterator(); i.hasNext();)
		{
			current = (SemanticEvaluatedAlignment) i.next();
			for (Iterator<NamedEntity> k = current.getAligned(); k.hasNext();)
			{
				aligned = (NamedEntity) k.next();

				score = current.getScore(aligned);
				scoreTuple = new SemanticScoreParser(score).getScoreTuple();
				sum += scoreTuple.getCombinedSemanticClassification() * scoreTuple.getWeight();
			}
		}

		return sum;
	}
	
	private double getMeasureF(){
		
		double currentMaximumCSC;
		double maximumCscInGc = 1;
		double totalSystemCsc = 1;
		double maximumSystemCsc = 1;
		
		double precision;
		double recall;
		
		SemanticEvaluatedAlignment alignment = null;
		NamedEntity current = null;
		SemanticScoreTuple scoreTuple = null;
		
		Iterator<EvaluatedAlignment> i;
		Iterator<NamedEntity> j;
		
		for (i = _alignments.iterator(); i.hasNext();)
		{
			alignment = (SemanticEvaluatedAlignment)i.next();
			
			if (!alignment.getGoldenEntity().isSpurious())
			{
				currentMaximumCSC = getMaximumCSC(alignment.getGoldenEntity());
				maximumCscInGc += currentMaximumCSC;

			}

			for (j = alignment.getAligned(); j.hasNext();)
			{
				current = j.next();
				scoreTuple = new SemanticScoreParser(alignment.getScore(current)).getScoreTuple();

				totalSystemCsc += scoreTuple.getCombinedSemanticClassification() * scoreTuple.getWeight();

				if (current != null)
				{
					maximumSystemCsc += getMaximumCSC(current);
				}
			}			
		}
		
		precision = totalSystemCsc / maximumSystemCsc;
		recall = totalSystemCsc / maximumCscInGc;
		
		return (2 * precision * recall) / (recall + precision);
	}
	
	private double getMaximumCSC(NamedEntity entity)
	{
		//return AttributesEvaluation.getMaximumCSC(entity.getAttributeTupleSet(), _filter);
		return AttributesEvaluation.getMaximumCSC(entity, _filter);
	}

	class SemanticDebugInfo extends DebugInfo
	{
		public double _categoryPrecision;

		public double _categoryRecall;

		public double _categoryFMeasure;

		public double _typePrecision;

		public double _typeRecall;

		public double _typeFMeasure;

		public double _sumOfCSC;

		public String toString()
		{
			return "\nCATEGORIA(Precisao=" + _categoryPrecision + "\tAbrangencia=" + _categoryRecall
					+ "\tMedida_F=" + _categoryFMeasure + ")\n" + "Soma_CSC=" + _sumOfCSC;
		}
	}
}
