package pt.linguateca.relations;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class RelationsInDocEvaluation extends LinkedList<RelationEvaluation>{
	
	public static final String RELATIONS_GC = "Rels_CD";
	public static final String RELATIONS_PART = "Rels_Part";
	public static final String UNIQUE_ARG_PAIRS_GC = "Corels_CD";
	public static final String UNIQUE_ARG_PAIRS_PART = "Corels_Part";
	public static final String CORRECT_CORELS = "CorelsCorrectos";
	
	public static final double COREL_CORRECT = 0.5;
	public static final double TIPOREL_CORRECT = 0.5;
	public static final double COREL_SPURIOUS = 0.2;
	
	private int _totalRelationsInGC;
	private int _uniqueArgPairsInGC;
	private double _maxScoreInGC;
	
	private int _totalRelationsInPart;
	private int _uniqueArgPairsInPart;
	private double _maxScoreInPart;
	
	private int _correctCorels;
	
	private Set<Relation> _relations;
	
	public RelationsInDocEvaluation()
	{
		_relations = new HashSet<Relation>();
	}
	
	public boolean add(RelationEvaluation re)
	{
		_relations.add(re.getRelation());
		return super.add(re);
	}
	
	public int getTotalRelationsInGC() {
		return _totalRelationsInGC;
	}

	public void setTotalRelationsInGC(int relationsInGC) {
		_totalRelationsInGC = relationsInGC;
	}

	public double getMaxScoreInGC() {
		return _maxScoreInGC;
	}

	public void setMaxScoreInGC(double scoreInGC) {
		_maxScoreInGC = scoreInGC;
	}

	public int getTotalRelationsInPart() {
		return _totalRelationsInPart;
	}

	public void setTotalRelationsInPart(int relationsInPart) {
		_totalRelationsInPart = relationsInPart;
	}

	public int getUniqueArgPairsInGC() {
		return _uniqueArgPairsInGC;
	}
	
	public void setUniqueArgPairsInGC(int corelsInGC) {
		_uniqueArgPairsInGC = corelsInGC;
	}
	
	public int getUniqueArgPairsInPart() {
		return _uniqueArgPairsInPart;
	}
	
	public void setUniqueArgPairsInPart(int corelsInPart) {
		_uniqueArgPairsInPart = corelsInPart;
	}
	
	public int getCorrectCorels()
	{
		return _correctCorels;
	}
	
	public void setCorrectCorels(int corels){
		_correctCorels = corels;
	}
	
	public double getMaxScoreInPart() {
		return _maxScoreInPart;
	}

	public void setMaxScoreInPart(double scoreInPart) {
		_maxScoreInPart = scoreInPart;
	}

	public boolean containsRelation(Relation r)
	{
		return _relations.contains(r);
	}
	
	public String superToString()
	{
		return super.toString();
	}
	
	private String getHeader()
	{
		return "["
			+ RELATIONS_GC + "(" + _totalRelationsInGC + ") "
			+ RELATIONS_PART + "(" + _totalRelationsInPart + ") "
			+ UNIQUE_ARG_PAIRS_GC + "(" + _uniqueArgPairsInGC + ") "
			+ UNIQUE_ARG_PAIRS_PART + "(" + _uniqueArgPairsInPart + ") "
			+ CORRECT_CORELS + "(" + _correctCorels + ")"
			+ "]";
	}
	
	public String toString()
	{
		String toReturn = getHeader();
		
		for(RelationEvaluation eval : this)
			toReturn += "\n" + eval.toString();
		
		return toReturn;
	}
	
	public String toStringWithoutScore()
	{
		String toReturn = getHeader();
		
		for(RelationEvaluation eval : this)
			toReturn += "\n" + eval.toStringWithoutScore();
		
		return toReturn;
	}
}
