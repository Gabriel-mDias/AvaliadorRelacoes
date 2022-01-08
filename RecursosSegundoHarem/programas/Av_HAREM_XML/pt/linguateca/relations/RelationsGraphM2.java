package pt.linguateca.relations;

import java.util.HashMap;
import java.util.Map;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphOrganicLayout;

public class RelationsGraphM2 extends Graph<RelationsList>{
	
	public RelationsGraphM2(){
		
	}
	
	public Object clone()
	{
		RelationsGraphM2 clone = new RelationsGraphM2();
		clone._map = (HashMap<String, RelationsList>)_map.clone();
		
		return clone;
	}
	
	public boolean containsSuitableBNodeFor(Relation r)
	{
		if(containsNode(r.getB()))
			return true;
		
		for(RelationsList node : this.getAllNodes())
		{
			if(r.hasCompatibleSuperIdB(node._nodeId))
				return true;
		}
		
		return false;
	}
	
	public boolean containsSuitableANodeFor(Relation r)
	{
		if(containsNode(r.getA()))
			return true;
		
		for(RelationsList node : this.getAllNodes())
		{
			if(r.hasCompatibleSuperIdA(node._nodeId))
				return true;
		}
		
		return false;
	}
	
	public boolean addRelation(Relation r)
	{
		//TODO: os nos ja tem de la estar
		
		/*
		if(!containsNode(r.getA()))
		{
			addNode(new RelationsList(r.getA()));
		}
		
		if(!containsNode(r.getB()))
			addNode(new RelationsList(r.getB()));*/
		
		//e preciso a faceta?
		return get(r.getA()).addEdge(r);
	}
	
	/*public void draw(String title)
	{
		draw(title, RelationsGraphDrawer.nodeLabel.ID.ordinal());
	}*/
	
	public void draw(String title, HashMap<String, String> map, int labelType)
	{		
		RelationsGraphM2Drawer drawer = new RelationsGraphM2Drawer(this);
		drawer.drawGraph(map, labelType);
		drawer.show(title);
	}

	public String toString()
	{
		String toReturn = "{";
				
		for(RelationsList list : this)
		{
			toReturn += list +"\n";
		}
		
		return toReturn +"}";
	}
}
