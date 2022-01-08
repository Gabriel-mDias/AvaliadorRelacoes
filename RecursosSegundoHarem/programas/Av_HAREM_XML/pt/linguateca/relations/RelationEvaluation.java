package pt.linguateca.relations;

import pt.linguateca.harem.IndividualAlignmentEvaluator;

public class RelationEvaluation {

	public static final double DEFAULT_COREL_CORRECT = 0.5;
	public static final double DEFAULT_TIPOREL_CORRECT = 0.5;
	public static final double DEFAULT_COREL_SPURIOUS = 0.2;
	
	public static final String EVALUATION_STRING = "Aval";
	public static final String SCORE_STRING = "Pont";
	public static final String EVALUATION_MARKER = IndividualAlignmentEvaluator.EVALUATION_MARKER;
	
	public static final String RELATION_CORRECT_STRING = "Rela\u00e7\u00e3o Correcta";
	public static final String COREL_CORRECT_STRING = "COREL e categorias Correctos";
	public static final String SPURIOUS_RELATION_STRING = "Espuria";
	public static final String MISSING_RELATION_STRING = "Em Falta";
	
	private Relation _relation;
	private String _evaluation;
	
	public RelationEvaluation(Relation rel)
	{
		_relation = rel;
	}
	
	public Relation getRelation()
	{
		return _relation;
	}
	
	public void setEvaluation(String evaluation)
	{
		_evaluation = evaluation;
	}
	
	public double getScore()
	{
		if(_evaluation == null)
			return 0;
		
		if(_evaluation.equals(RELATION_CORRECT_STRING))
			return DEFAULT_COREL_CORRECT + DEFAULT_TIPOREL_CORRECT;
		else if(_evaluation.equals(COREL_CORRECT_STRING))
			return DEFAULT_COREL_CORRECT;
		else if(_evaluation.equals(SPURIOUS_RELATION_STRING))
			return -DEFAULT_COREL_SPURIOUS;
		else return 0;
	}
	
	public static double getScoreForCorrectRelation()
	{
		return getScoreForCorrectCorel() + DEFAULT_TIPOREL_CORRECT;
	}
	
	public static double getScoreForCorrectCorel()
	{
		return DEFAULT_COREL_CORRECT;
	}
	
	public String toString()
	{
		return _relation + " " + EVALUATION_MARKER + " "
		+ EVALUATION_STRING + "(" + _evaluation + ") "
		+ SCORE_STRING + "(" + getScore() + ")";
	}
	
	public String toStringWithoutScore()
	{
		return _relation + " " + EVALUATION_MARKER + " "
		+ EVALUATION_STRING + "(" + _evaluation + ")";
	}
}
