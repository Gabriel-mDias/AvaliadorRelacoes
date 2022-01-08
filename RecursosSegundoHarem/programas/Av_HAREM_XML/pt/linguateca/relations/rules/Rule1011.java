package pt.linguateca.relations.rules;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.TransitiveRule;

public class Rule1011 extends TransitiveRule{

	// 10 - ident(A,B) e outra (B,C) -> outra (A,C)
	// 11 - ident(A, B) e outra (C,B) -> outra (C,A)
	
	//TODO: esta n�o se devia fazer quando uma das rela��es � outra. Devia ser sempre!
	
	public Rule1011(TagBase tags) {
		super(tags, Relation.IDENTIDADE, Relation.OUTRA);
	}

}
