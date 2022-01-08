package pt.linguateca.relations.rules;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationsPair;
import pt.linguateca.relations.TransitiveRule;

public class Rule23 extends TransitiveRule{

	//2 - inclui(A, B) e inclui(B, C) -> inclui(A, C)
	//3 - incluido(A, B) e incluido(B, C) -> incluido(A, C)

	public Rule23(TagBase tags) {
		super(tags, Relation.INCLUI, Relation.INCLUI);
	}

	@Override
	public RelationsPair getNormalizedPair(Relation r1, Relation r2)
	{
		Relation n1 = null;
		Relation n2 = null;

		if((n1 = getNormalizedRelation(r1)) == null)
			return null;

		if((n2 = getNormalizedRelation(r2)) == null)
			return null;

		//inclui(A, B) e inclui(B, C) fica igual
		if(n1.getB().equals(n2.getA()) && !n1.getA().equals(n2.getB()))
			return new RelationsPair(r1, r2);

		//inclui(A, B) e inclui(C, A) troca a ordem
		else if(n1.getA().equals(n2.getB()) && !n1.getB().equals(n2.getA()))
			return new RelationsPair(r2, r1);
		
		return null;		
	}
}
