package pt.linguateca.relations.rules;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationsPair;
import pt.linguateca.relations.TransitiveRule;

public class Rule45 extends TransitiveRule{

	// 4 - ident(A, B) e inclui(B, C) -> inclui(A, C)
	// 5 - ident(A, B) e incluido(B, C) -> incluido(A, C)
	
	public Rule45(TagBase tags) {
		super(tags, Relation.IDENTIDADE, Relation.INCLUI);
	}
	
	/*@Override
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