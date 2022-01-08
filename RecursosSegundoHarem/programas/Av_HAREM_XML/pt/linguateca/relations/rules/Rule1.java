package pt.linguateca.relations.rules;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.TransitiveRule;

public class Rule1 extends TransitiveRule{

	//1 - ident(A, B) e ident(B, C)-> ident(A, C)
		
	public Rule1(TagBase tags) {
		super(tags, Relation.IDENTIDADE, Relation.IDENTIDADE);
	}
	
	@Override
	public Relation getTransitiveRelation(Relation r1, Relation r2)
	{
		if(!(r1.getType().equals(_rtype1) && r2.getType().equals(_rtype2)))
		{
			return null;
		}
				
		if(r1.getA().equals(r2.getA()) && !r1.getB().equals(r2.getB()))
			return new Relation(_rtype1, r1.getB(), r2.getB());
		else if(r1.getA().equals(r2.getB()) && !r1.getB().equals(r2.getA()))
			return new Relation(_rtype1, r1.getB(), r2.getA());
		else if(r1.getB().equals(r2.getA()) && !r1.getA().equals(r2.getB()))
			return new Relation(_rtype1, r1.getA(), r2.getB());
		else if(r1.getB().equals(r2.getB()) && !r1.getA().equals(r2.getA()))
			return new Relation(_rtype1, r1.getA(), r2.getA());
		
		return null;
	}
}