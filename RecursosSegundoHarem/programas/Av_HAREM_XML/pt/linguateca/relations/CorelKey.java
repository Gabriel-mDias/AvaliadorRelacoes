package pt.linguateca.relations;

public class CorelKey
{
	private String a, b, source, target;

	public CorelKey(String a, String b, String source, String target) {
		super();
		this.a = a;
		this.b = b;
		this.source = source;
		this.target = target;
	}

	public String getA() {
		return a;
	}

	public String getB() {
		return b;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public boolean isCompatible(CorelKey other)
	{
		return a.equals(other.a) && b.equals(other.b)
		&& RelationM2.compatibleCategories(source, other.source)
		&& RelationM2.compatibleCategories(target, other.target);
	}
	
	public String toString()
	{
		return a+"_"+source+" "+b+"_"+target;
	}
}
