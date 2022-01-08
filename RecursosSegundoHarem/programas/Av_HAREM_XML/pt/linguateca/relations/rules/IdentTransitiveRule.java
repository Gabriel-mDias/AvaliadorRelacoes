package pt.linguateca.relations.rules;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.TransitiveRule;

public class IdentTransitiveRule extends TransitiveRule{

	//ident(A, C) e qualquer(C, B) --> qualquer(A, B)
	//ident(A, C) e qualquer(B, C) --> qualquer(B, A)
	public IdentTransitiveRule(TagBase tags, String type) {
		super(tags, Relation.IDENTIDADE, type);
	}
}
