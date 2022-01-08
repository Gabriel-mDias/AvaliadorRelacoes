package pt.linguateca.relations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Graph<E extends Node<?>> implements Iterable<E>{

	protected HashMap<String, E> _map;
	
	public Graph (){
		_map = new HashMap<String, E>();
	}
	
	public void addNode(E node){
		_map.put(node.getKey(), node);		
	}
	
/*	public E removeNode(String key){
		return _map.remove(key);
	}*/
		
	public Iterator<E> iterator(){
		return _map.values().iterator();
	}
	
	public E get(String s){
		return _map.get(s);
	}
	
	public Set<String> getKeys()
	{
		return _map.keySet();
	}
	
	public boolean containsNode(String id)
	{
		return _map.containsKey(id);
	}
	
	public Set<String> getAllKeys()
	{
		return _map.keySet();
	}
	
	public Collection<E> getAllNodes()
	{
		return _map.values();
	}
	
	public String toString()
	{
		return _map.toString().replace(", ", "\n");
	}
}
