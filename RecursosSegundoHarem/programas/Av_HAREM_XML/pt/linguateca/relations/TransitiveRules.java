package pt.linguateca.relations;

import java.util.LinkedList;

import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.rules.Rule1;
import pt.linguateca.relations.rules.Rule1011;
import pt.linguateca.relations.rules.Rule23;
import pt.linguateca.relations.rules.Rule45;
import pt.linguateca.relations.rules.Rule67;
import pt.linguateca.relations.rules.Rule89;

public class TransitiveRules {

	private static TagBase _tagBase;
	private static TransitiveRules _rules;	
	private LinkedList<TransitiveRule> _list;
		
	protected TransitiveRules(TagBase tags)
	{
		_tagBase = tags;
		_list = new LinkedList<TransitiveRule>();
		
		//regras
		_list.add(new Rule1(tags));
		_list.add(new Rule23(tags));
		//_list.add(new Rule45(tags));
		//_list.add(new Rule67(tags));
		_list.add(new Rule89(tags));
		_list.add(new Rule1011(tags));
		
		/*for(String[] rel : _tagBase.getPossibleTiposRel())
			_list.add(new IdentTransitiveRule(_tagBase, rel[0]));*/
	}
		
	public static TransitiveRules getInstance(TagBase tags)
	{
		if (_rules == null)
		{					
			_rules = new TransitiveRules(tags);
		}

		return _rules;
	}
	
	public LinkedList<TransitiveRule> getRules()
	{
		return _list;
	}
	
	public Relation getTransitiveRelation(Relation r1, Relation r2)
	{
		Relation transitive = null;
		
		for(TransitiveRule rule : _list)
		{
			//System.out.println(rule);
			transitive = rule.getTransitiveRelation(r1, r2);
			if(transitive != null)
			{
				//System.out.println(rule + ": "+ r1 + " e " + r2 + " -----> " + transitive);
				break;
			}
		}
		
		return transitive;
	}
}
