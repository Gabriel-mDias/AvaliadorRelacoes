package pt.linguateca.relations;

public class RelationsPair {

	private Relation _first;
	private Relation _second;
	
	public RelationsPair(Relation first, Relation second)
	{
		_first = first;
		_second = second;
	}
	
	public Relation getFirst() {
		return _first;
	}

	public Relation getSecond() {
		return _second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_first == null) ? 0 : _first.hashCode());
		result = prime * result + ((_second == null) ? 0 : _second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RelationsPair other = (RelationsPair) obj;
		if (_first == null) {
			if (other._first != null)
				return false;
		} else if (!_first.equals(other._first))
			return false;
		if (_second == null) {
			if (other._second != null)
				return false;
		} else if (!_second.equals(other._second))
			return false;
		return true;
	}

/*	public RelationsPair getSwitchedPair()
	{
		return new RelationsPair(_second, _first);
	}*/

	public String toString()
	{
		return _first + " e " + _second;
	}
}
