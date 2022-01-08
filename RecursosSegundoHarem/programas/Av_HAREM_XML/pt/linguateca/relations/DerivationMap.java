package pt.linguateca.relations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DerivationMap {

	//private String _id;
	private HashMap<Relation, Set<Relation>> _map;

	public DerivationMap()
	{
		//_id = id;
		_map = new HashMap<Relation, Set<Relation>>();
	}

	public void addBaseRelation(Relation r)
	{
		if(!_map.containsKey(r))
			_map.put(r, null);
	}

	public void addSimpleDerivation(Relation r1, Relation result)
	{
		if(!_map.containsKey(result))
		{
			Set<Relation> relations = new HashSet<Relation>();
			relations.add(r1);

			_map.put(result, relations);
		}
	}

	public void addDerivation(Relation r1, Relation r2, Relation result)
	{
		//TODO: se r1 e r2 nao existirem?
		if(!_map.containsKey(result))
		{
			Set<Relation> relations = new HashSet<Relation>();
			relations.add(r1);
			relations.add(r2);

			_map.put(result, relations);
		}
	}

	protected void printDerivation(Relation r, int n)
	{
		
		//int spaces = n;
		Set<Relation> relations = _map.get(r);

		/*while(--spaces > 0)
			System.err.print(" ");*/
		
		if(relations == null)
		{
			System.out.println("an: "+r);
			return;
		}
		else
		{
			System.out.println("d"+n+": "+r);
		}

		/*while(--spaces > 0)
			System.err.print("  ");*/
		
		System.out.println("\t"+relations);
		for(Relation rel : relations)
		{
			printDerivation(rel, n+1);
		}
	}

	//TODO: forma para visualizar melhor isto!
	public void printDerivationPath(Relation r)
	{
		printDerivation(r, 0);
	}

	public void printMap()
	{
		System.out.println(_map);
	}
}
