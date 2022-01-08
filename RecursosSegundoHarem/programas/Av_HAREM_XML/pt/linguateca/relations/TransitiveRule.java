package pt.linguateca.relations;

import pt.linguateca.harem.TagBase;

public abstract class TransitiveRule {

	protected TagBase _tagBase;
	protected String _rtype1;
	protected String _rtype2;

	/** aplica a todas as relacoes o que for para fazer às "outra" **/
	protected boolean OUTRA_EQUALS_ANY = true;
	
	public TransitiveRule(TagBase tags, String t1, String t2)
	{
		_tagBase = tags;
		_rtype1 = t1;
		_rtype2 = t2;
	}

	/*public abstract Relation getTransitiveRelationOld(Relation r1, Relation r2);

	protected abstract Relation normalizeToDirect(Relation r);
	protected abstract Relation normalizeToInverse(Relation r);*/

	protected RelationsPair getNormalizedPair(Relation r1, Relation r2)
	{
		//1 - normalizar as relacoes: colocar as relacoes iguais a _rtype
		Relation n1 = null;
		Relation n2 = null;

		//System.out.println(r1+" e "+r2);
		
		if((n1 = getNormalizedRelation(r1)) == null)
			return null;

		//System.out.println("--- Normalizacao 1 OK");
		
		if((n2 = getNormalizedRelation(r2)) == null)
			return null;
		
		//System.out.println("--- Normalizacao 2 OK");

		if(n1.getType().equals(Relation.IDENTIDADE))
			n1 = getNormalizedIdentity(n1, n2);		

		//2 - colocar o par na ordem certa
		if(r1.getType().equals(_rtype1) &&
				(r2.getType().equals(_rtype2) || (OUTRA_EQUALS_ANY && _rtype2.equals(Relation.OUTRA))))
		{
			//System.out.println(r1+" e "+r2);
			return new RelationsPair(r1, r2);
		}
		else if((r1.getType().equals(_rtype2) || (OUTRA_EQUALS_ANY && _rtype2.equals(Relation.OUTRA)))
				&& r2.getType().equals(_rtype1))
		{	
			//System.out.println(r2+" e "+r1);
			return new RelationsPair(r2, r1);
		}

		//System.out.println("Nada");
		return null;
	}

	/**
	 * Coloca a relacao no tipo directo
	 * @param r
	 * @return
	 */
	protected Relation getNormalizedRelation(Relation r)
	{		
		if(r.getType().equals(_rtype1) || r.getType().equals(_rtype2) || _rtype2.equals(Relation.OUTRA))
			return r;
		else if(r.getType().equals(_tagBase.getInverseType(_rtype1)))
			return getInverse(r);
		else if(r.getType().equals(_tagBase.getInverseType(_rtype2)))
			return getInverse(r);

		return null;
	}

	protected Relation getInverse(Relation r)
	{
		return new Relation(_tagBase.getInverseType(r.getType()), r.getB(), r.getA());
	}

	/**
	 * Coloca o atributo comum no B da identidade
	 * @param r1
	 * @param r2
	 * @return
	 */
	protected Relation getNormalizedIdentity(Relation r1, Relation r2)
	{
		//ident(A,B) e rel(A,C) --> ident(B,A) e rel(A,C)
		//ident(A,B) e rel(C,A) --> ident(B,A) e rel(C,A) - se rel estiver normalizada esta nao devera' implicar nada...
		if(r1.getA().equals(r2.getA()) || r1.getA().equals(r2.getB()))
			return getInverse(r1);
		else
			return r1;
	}

	public Relation getTransitiveRelation(Relation r1, Relation r2)
	{	
		Relation n1, n2;
		RelationsPair normalized;

		if((normalized = getNormalizedPair(r1, r2)) != null)
		{
			if((n1 = normalized.getFirst()) == null)
				return null;

			if((n2 = normalized.getSecond()) == null)
				return null;

			if(n1.getA().equals(n2.getB()) && !n1.getB().equals(n2.getA()))
				return new Relation(n2.getType(), n2.getA(), n1.getB());
			else if(n2.getA().equals(n1.getB()) && !n2.getB().equals(n1.getA()))
				return new Relation(n2.getType(), n1.getA(), n2.getB());
		}

		return null;
	}
}
