package pt.linguateca.relations.rules;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationsPair;
import pt.linguateca.relations.TransitiveRule;

public class Rule67 extends TransitiveRule{

	// 6 - ident(A, B) e sede_de(B, C) -> sede_de(A, C)
	// 7 - ident(A, B) e ocorre_em(B, C) -> ocorre_em(A, C)
	
	public Rule67(TagBase tags) {
		super(tags, Relation.IDENTIDADE, Relation.SEDE);
	}

	/*@Override
	public Relation getTransitiveRelationOld(Relation r1, Relation r2) {

		RelationsPair normalized = getNormalizedPair(r1, r2);

		if(normalized != null)
		{
			Relation n1 = normalizeToDirect(normalized.getFirst());
			Relation n2 = normalizeToDirect(normalized.getSecond());
			
			if(n1 != null && n2 != null && n1.getType().equals(n2.getType()))
			{
				if(n1.getB().equals(n2.getA()))
					return new Relation(_rtype2, n1.getA(), n2.getB());
			}

			n1 = normalizeToInverse(normalized.getFirst());
			n2 = normalizeToInverse(normalized.getSecond());
			
			if(n1 != null && n2 != null)
			{
				if(n1.getB().equals(n2.getA()))
					return new Relation(_tagBase.getInverseType(_rtype2), n1.getA(), n2.getB());
			}
		}
		
		return null;
	}

	
	@Override
	protected Relation normalizeToDirect(Relation r) {

		if(r.getType().equals(_rtype1))
			return r;
		else if(r.getType().equals(_rtype2))
			return r;
		else if(r.getType().equals(_tagBase.getInverseType(_rtype2)))
			return _tagBase.getInverse(r);

		return null;
	}

	@Override
	protected Relation normalizeToInverse(Relation r) {

		if(r.getType().equals(_rtype1))
			return _tagBase.getInverse(r);
		else if(r.getType().equals(_rtype2))
			return _tagBase.getInverse(r);
		else if(r.getType().equals(_tagBase.getInverseType(_rtype2)))
			return r;

		return null;
	}*/

}
