package pt.linguateca.relations;

import java.util.Arrays;

public class Relation
{
	public static final String IDENTIDADE = "ident";
	public static final String INCLUI = "inclui";
	public static final String INCLUIDO = "incluido";
	public static final String OCORRE = "ocorre_em";
	public static final String SEDE = "sede_de";
	public static final String OUTRA = "outra";

	public static final String[] BASIC_TYPES = {IDENTIDADE, INCLUI, INCLUIDO, OCORRE, SEDE};

	private static final boolean NEW_TYPE_EQUALS_OUTRA = true;

	protected String _type;
	protected String _a, _b;

	public Relation(){}

	public Relation(String type, String a, String b){

		_type = type;

		_a = a;
		_b = b;
	}

	/*public Relation(String representation){
		//type(A B)

		representation = representation.replace("(", " ").replace(")", "");
		String[] split = representation.split(" ");		

		_type = split[0];
		_a = split[1];
		_b = split[2];
	}*/

	/*public Relation(String representation){
		//A type B

		String[] split = representation.split(PARTS_SEP);		

		_type = split[1].trim();
		_a = split[0].trim();
		_b = split[2].trim();
	}*/

	public void normalizeType()
	{
		if(NEW_TYPE_EQUALS_OUTRA && !isBasicType())
			_type = OUTRA;
	}

	public boolean isBasicType()
	{
		return Arrays.asList(BASIC_TYPES).contains(_type);
	}

	public String getType() {
		return _type;
	}

	public String getA() {
		return _a;
	}

	public String getB() {
		return _b;
	}

	public boolean sameArgumentsAs(Relation other)
	{
		return (_a.equals(other._a) && _b.equals(_b));
	}
	
	/*	public void setA(String str) {
		_a = str;
	}

	public void setB(String str) {
		_b = str;
	}*/

	/*public String getCorelKey()
	{
		return _a+PARTS_SEP+_b;
	}*/

	public String toString2(){
		return _type+"("+_a+" "+_b+")";
	}

	public String toString(){
		return _a + RelationProcessor.PARTS_SEP +_type + RelationProcessor.PARTS_SEP + _b;
	}

	public boolean hasCompatibleSuperIdA(String id)
	{
		return areCompatibleSuperIds(id, _a);
	}
	
	public boolean hasCompatibleSuperIdB(String id)
	{
		return areCompatibleSuperIds(id, _b);
	}
	
	private boolean areCompatibleSuperIds(String id1, String id2)
	{
		String[] splitId1 = RelationM2.splitSuperId(id1);
		if(splitId1.length == 1)
			return id1.equals(id2);
		
		String[] splitId2 = RelationM2.splitSuperId(id2);
		if(splitId2.length == 1)
			return false;
		
		return splitId1[0].equals(splitId2[0]) && RelationM2.compatibleCategories(splitId1[1], splitId2[1]);
	}
	
	public boolean hasOnlyOneCommonArgument(Relation other)
	{
		return 
		(_a.equals(other.getA()) && !_b.equals(other.getB())) || 
		(_a.equals(other.getB()) && !_b.equals(other.getA())) ||
		(_b.equals(other.getA()) && !_a.equals(other.getB())) || 
		(_b.equals(other.getB()) && !_a.equals(other.getA()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_a == null) ? 0 : _a.hashCode());
		result = prime * result + ((_b == null) ? 0 : _b.hashCode());

		//if(!NEW_TYPE_EQUALS_OUTRA) //se tiver isto, as relações são consideradas exactamente iguais, cópia uma da outra
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		/*		else
		{		
			if(!isBasicType())
				result = prime * result + ((_type == null) ? 0 : OUTRA.hashCode());
			else
				result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		}*/

		return result;
	}

	public RelationM2 getRelationM2()
	{
		String[] splittedA, splittedB;
		if(_a.contains(RelationProcessor.PARTS_SEP) && _b.contains(RelationProcessor.PARTS_SEP))
		{
			splittedA = _a.split(RelationProcessor.PARTS_SEP);
			splittedB = _b.split(RelationProcessor.PARTS_SEP);
			
			return new RelationM2(_type, splittedA[0], splittedB[0], splittedA[1], splittedB[1]);
		}
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Relation other = (Relation) obj;
		if (_a == null) {
			if (other._a != null)
				return false;
		} else if (!_a.equals(other._a))
			return false;		
		if (_b == null) {
			if (other._b != null)
				return false;
		} else if (!_b.equals(other._b))
			return false;

		if (_type == null) {
			if (other._type != null)
				return false;

		}

		//aqui garante-se que a comparacao entre um tipo nao basico e outra dá true
		else if (NEW_TYPE_EQUALS_OUTRA && _type.equals(OUTRA) && !other.isBasicType())
			return true;

		else if (NEW_TYPE_EQUALS_OUTRA && !isBasicType() && other._type.equals(OUTRA))
			return true;

		else if (!_type.equals(other._type))
			return false;

		//System.out.println("IGUAIS: "+this +" e "+other);
		return true;
	}
}
