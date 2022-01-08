package pt.linguateca.harem;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

public class AttributesEvaluation {

	public static final String WEIGHT = "Peso";

	public static final String ALT_WEIGHT = "PALT";

	public static final String MAX_CSC_GC = "MaxCSC_CD";

	public static final String MAX_CSC_SYSTEM = "MaxCSC_S";

	public static final String COMBINED_SEMANTIC_CLASSIFCATION = "CSC";

	public static final String CATEGORY = "Categoria";

	public static final String TYPE = "Tipo";

	public static final String SUBTYPE = "Subtipo";

	private static double _identification;
	private static double _alpha;
	private static double _beta;
	private static double _gamma;

	private static boolean _penalties;

	private HashMap<String, AttributeEvaluation> _evaluations;
	private EntitiesAttributesFilter _filter;
	//private AttributeTupleSet _golden;
	//private AttributeTupleSet _part;

	private NamedEntity _goldenEntity;
	private NamedEntity _toEvaluate;

	private double _weight;
	private double _altWeight;

	public AttributesEvaluation(EntitiesAttributesFilter filter, NamedEntity golden,
			NamedEntity part, boolean penalties, double weight, double alt,
			double idWeight, double catWeight, double typeWeight, double subtypeWeight){

		_filter = filter;
		_goldenEntity = golden;
		_toEvaluate = part;

		_weight = weight;
		_altWeight = alt;
		_evaluations = new HashMap<String, AttributeEvaluation>(3);

		_evaluations.put(CATEGORY, new AttributeEvaluation(CATEGORY));
		_evaluations.put(TYPE, new AttributeEvaluation(TYPE));
		_evaluations.put(SUBTYPE, new AttributeEvaluation(SUBTYPE));

		_identification = idWeight;
		_alpha = catWeight;
		_beta = typeWeight;
		_gamma = subtypeWeight;

		evaluate();
	}

	private String evaluatedAttributeToString(String key)
	{
		return _evaluations.get(key).toString();
	}

	private void evaluate()
	{
		if(_toEvaluate == null || _goldenEntity.isSpurious())
			return;

		AttributeTupleSet goldenAttributes = _filter.getValidTuples(_goldenEntity);
		AttributeTupleSet attributesToEvaluate = _filter.getValidTuples(_toEvaluate);

		//categories
		for(AttributeTuple part : attributesToEvaluate)
		{
			for(AttributeTuple golden : goldenAttributes)
			{
				if(golden.getCategory() != null && golden.getCategory().equals(part.getCategory()))
				{
					addCorrect(CATEGORY, part);
					break;
				}
			}
		}

		AttributeTupleSet correct = getCorrect(CATEGORY);
		AttributeTupleSet spurious = null;
		AttributeTupleSet missing = null;

		try {
			spurious = getTuplesWithDifferentAttribute(attributesToEvaluate, correct, CATEGORY, 
					AttributeTuple.class.getDeclaredMethod("getCategory", null));
			missing = getTuplesWithDifferentAttribute(goldenAttributes, correct, CATEGORY, 
					AttributeTuple.class.getDeclaredMethod("getCategory", null));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//System.out.println("missing: "+missing);
		//System.out.println("spurious: "+spurious);

		for(AttributeTuple tuple : spurious)
		{
			addSpurious(CATEGORY, tuple);			
			attributesToEvaluate.remove(tuple);
		}

		for(AttributeTuple tuple : missing)
		{
			if(!tuple.isSpurious())
				addMissing(CATEGORY, tuple);

			goldenAttributes.remove(tuple);
		}

		//types
		for(AttributeTuple part : attributesToEvaluate)
		{
			for(AttributeTuple golden : goldenAttributes)
			{
				if(golden.getType() != null && golden.getType().equals(part.getType()))
				{
					addCorrect(TYPE, part);
					break;
				}
			}
		}

		correct = getCorrect(TYPE);	

		try {
			spurious = getTuplesWithDifferentAttribute(attributesToEvaluate, correct, TYPE,
					AttributeTuple.class.getDeclaredMethod("getType", null));
			missing = getTuplesWithDifferentAttribute(goldenAttributes, correct, TYPE,
					AttributeTuple.class.getDeclaredMethod("getType", null));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(AttributeTuple tuple : spurious)
		{
			addSpurious(TYPE, tuple);
			attributesToEvaluate.remove(tuple);
		}

		for(AttributeTuple tuple : missing)
		{
			addMissing(TYPE, tuple);
			goldenAttributes.remove(tuple);
		}

		//subtypes
		for(AttributeTuple part : attributesToEvaluate)
		{
			for(AttributeTuple golden : goldenAttributes)
			{
				if(golden.getSubtype() != null && golden.getSubtype().equals(part.getSubtype()))
				{
					addCorrect(SUBTYPE, part);
					break;
				}
			}
		}

		correct = getCorrect(SUBTYPE);	

		try {
			spurious = getTuplesWithDifferentAttribute(attributesToEvaluate, correct, SUBTYPE, 
					AttributeTuple.class.getDeclaredMethod("getSubtype", null));
			missing = getTuplesWithDifferentAttribute(goldenAttributes, correct, SUBTYPE, 
					AttributeTuple.class.getDeclaredMethod("getSubtype", null));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(AttributeTuple tuple : spurious)
		{
			addSpurious(SUBTYPE, tuple);
			attributesToEvaluate.remove(tuple);
		}

		for(AttributeTuple tuple : missing)
		{
			addMissing(SUBTYPE, tuple);
			goldenAttributes.remove(tuple);
		}
	}

	private AttributeTupleSet getTuplesWithDifferentAttribute(AttributeTupleSet set, AttributeTupleSet correct, String key, Method method)
	throws Exception{

		AttributeTupleSet tuples = new AttributeTupleSet();
		String toCompare = null;
		for(AttributeTuple tuple : set)
		{
			toCompare = (String)method.invoke(tuple, null);
			if(toCompare != null && !correct.getAttribute(key).contains(toCompare))
				tuples.add(tuple);
		}
		return tuples;
	}

	private double getCombinedSemanticClassification()
	{
		//CSC = 1 + S(1, N) {(1 - 1/num-cats)*cat-certa +
		//(1 - 1/num-tipos)*tipo-certo + (1 - 1/num-subtipos)*subtipo-certo}
		//- S(1, M) {(1/num-cats)*cat-espuria + (1/num-tipos)*tipo-espurio
		//+ (1/num-subtipos)*subtipo-espurio}

		if(_goldenEntity.isSpurious() || _toEvaluate == null)
			return 0;

		double sumScore = _identification;
		double penalties = 0;

		double numCats = _filter.getCategories().size();
		numCats = (numCats == 0) ? 1.0 : numCats;

		//System.out.println("numCats= "+numCats);

		AttributeEvaluation catEvaluation = _evaluations.get(CATEGORY);
		AttributeEvaluation typeEvaluation = _evaluations.get(TYPE);
		AttributeEvaluation subEvaluation = _evaluations.get(SUBTYPE);

		for(AttributeTuple tuple : catEvaluation.getCorrect())
			sumScore += ( 1 - (1 / numCats) ) * _alpha;
		for(AttributeTuple tuple : catEvaluation.getSpurious())
			penalties += (1 / numCats) * _alpha;

		/*		System.out.println("sumScore cats = "+sumScore);
		System.out.println("penalties cats = "+penalties);*/

		double numTypes, numSubtypes;

		for(AttributeTuple tuple : typeEvaluation.getCorrect())
		{
			numTypes = _filter.getTypes(tuple.getCategory()).size();
			numTypes = (numTypes == 0) ? 1.0 : numTypes;
			sumScore += ( 1 - (1 / numTypes) ) * _beta;
		}

		for(AttributeTuple tuple : typeEvaluation.getSpurious())
		{
			numTypes = _filter.getTypes(tuple.getCategory()).size();
			numTypes = (numTypes == 0) ? 1.0 : numTypes;
			penalties += (1 / numTypes) * _beta;
		}

		/*		System.out.println("sumScore tipos = "+sumScore);
		System.out.println("penalties tipos = "+penalties);*/

		for(AttributeTuple tuple : subEvaluation.getCorrect())
		{
			numSubtypes = _filter.getSubtypes(tuple.getCategory(), tuple.getType()).size();
			numSubtypes = (numSubtypes == 0) ? 1.0 : numSubtypes;
			sumScore += ( 1 - (1 / numSubtypes) ) * _gamma;
		}

		for(AttributeTuple tuple : subEvaluation.getSpurious())
		{
			numSubtypes = _filter.getSubtypes(tuple.getCategory(), tuple.getType()).size();
			numSubtypes = (numSubtypes == 0) ? 1.0 : numSubtypes;
			penalties += (1 / numSubtypes) * _gamma;
		}

		if(_penalties)
			return sumScore - penalties;
		else return sumScore;
	}

	private double getMaximumCSCinGC()
	{
		return getMaximumCSC(_goldenEntity, _filter);
	}

	private double getMaximumCSCinSystem()
	{
		return getMaximumCSC(_toEvaluate, _filter);
	}

	public static double getMaximumCSC(NamedEntity entity, EntitiesAttributesFilter filter)
	{
		if(entity == null || entity.isSpurious())
			return 0;

		//cria uma avaliacao da entidade contra ela propria
		//para calcular o valor da CSC se estivesse tudo correcto
		AttributesEvaluation allCorrect = new AttributesEvaluation(filter, entity, entity,
				_penalties, 1, 1, _identification, _alpha, _beta, _gamma);
		return allCorrect.getCombinedSemanticClassification();
	}

	protected void addCorrect(String key, AttributeTupleSet values)
	{
		for(AttributeTuple at : values)
			addCorrect(key, at);
	}

	protected void addCorrect(String key, AttributeTuple value)
	{
		_evaluations.get(key).addCorrect(value);
	}

	protected void addSpurious(String key, AttributeTupleSet values)
	{
		for(AttributeTuple at : values)
			addSpurious(key, at);
	}

	protected void addSpurious(String key, AttributeTuple value)
	{
		_evaluations.get(key).addSpurious(value);
	}

	protected void addMissing(String key, AttributeTupleSet values)
	{
		for(AttributeTuple at : values)
			addMissing(key, at);
	}

	protected void addMissing(String key, AttributeTuple value)
	{
		_evaluations.get(key).addMissing(value);
	}

	private AttributeTupleSet getCorrect(String key)
	{
		return _evaluations.get(key).getCorrect();
	}

	public String toString(){

		String toReturn = "{" + CATEGORY + "(" +
		evaluatedAttributeToString(CATEGORY) + ") "
		+ TYPE + "(" + 
		evaluatedAttributeToString(TYPE) + ") "
		+ SUBTYPE + "(" + 
		evaluatedAttributeToString(SUBTYPE) + ") "
		+ MAX_CSC_GC + "(" + getMaximumCSCinGC() + ") "
		+ MAX_CSC_SYSTEM + "(" + getMaximumCSCinSystem() + ") "
		+ COMBINED_SEMANTIC_CLASSIFCATION + "("	+ getCombinedSemanticClassification() + ") "
		+ WEIGHT + "(" + _weight + ")";

		if(_altWeight != 1)
			toReturn += " "+ALT_WEIGHT + "(" + _altWeight + ")";

		toReturn += "}";

		return toReturn;
	}
}

class AttributeEvaluation
{
	private String _key;
	private AttributeTupleSet _correct, _spurious, _missing;

	public AttributeEvaluation(String key)
	{
		_key = key;
		_correct = new AttributeTupleSet();
		_spurious = new AttributeTupleSet();
		_missing = new AttributeTupleSet();
	}

	public void addCorrect(AttributeTuple tuple)
	{
		for(AttributeTuple correct : _correct)
		{
			if(tuple.hasSameUpperAttributes(correct, _key))
				return;
		}

		_correct.add(tuple);
	}

	public void addSpurious(AttributeTuple tuple)
	{
		for(AttributeTuple spurious : _spurious)
		{
			if(!tuple.hasSameUpperAttributes(spurious, _key))
				return;
		}

		_spurious.add(tuple);
	}

	public void addMissing(AttributeTuple tuple)
	{
		for(AttributeTuple missing : _missing)
		{
			if(!tuple.hasSameUpperAttributes(missing, _key))
				return;
		}

		_missing.add(tuple);
	}

	public String getKey()
	{
		return _key;
	}

	public AttributeTupleSet getCorrect()
	{
		return _correct;
	}

	public AttributeTupleSet getSpurious()
	{
		return _spurious;
	}

	private String getFormatedString(LinkedList<String> list)
	{
		return list.toString().replaceAll(", ", "|");
	}

	public String toString()
	{
		return IndividualAlignmentEvaluator.CORRECT + ":"+getFormatedString(_correct.getAttribute(_key))+" "
		+ IndividualAlignmentEvaluator.SPURIOUS + ":"+getFormatedString(_spurious.getAttribute(_key))+" "
		+ IndividualAlignmentEvaluator.MISSING + ":"+getFormatedString(_missing.getAttribute(_key));
	}
}
