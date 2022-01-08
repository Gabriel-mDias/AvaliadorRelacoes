package pt.linguateca.relations;

public class RelationComparation {

	private Relation _r1;
	private Relation _r2;
	
	private boolean _type;
	private boolean _a1a2;
	private boolean _a1b2;
	private boolean _b1a2;
	private boolean _b1b2;
	
	public RelationComparation(Relation r1, Relation r2)
	{
		_r1 = r1;
		_r2 = r2;
		
		compare();
	}
	
	private void compare()
	{
		_type = _r1.getType().equals(_r2.getType());
		_a1a2 = _r1.getA().equals(_r2.getA());
		_a1b2 = _r1.getA().equals(_r2.getB());
		_b1a2 = _r1.getB().equals(_r2.getA());
		_b1b2 = _r1.getB().equals(_r2.getB());
	}
	
	public boolean sameType()
	{
		return _type;
	}
	
	
}

