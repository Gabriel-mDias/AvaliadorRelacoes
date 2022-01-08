package pt.linguateca.relations;

import java.util.Iterator;

public interface Node<E> extends Iterable<E>{

	public String getKey();
	public Iterator<E> iterator();
	public boolean addEdge(E edge);
}
