package pt.linguateca.relations;


public class RelationProcessor{

	public static final String PARTS_SEP = " ";
	
	public RelationProcessor()
	{
		
	}

	public Relation getRelation(String representation)
	{
		String[] args = representation.split(PARTS_SEP);
		
		if(args.length == 3)
		{
			//a type b
			return new Relation(args[1].trim(), args[0].trim(), args[2].trim());
		}
		
		if(args.length == 5)
		{
			//a source type b target
			return new RelationM2(args[2].trim(), args[0].trim(), args[3].trim(), args[1].trim(), args[4].trim());
		}
				
		return null;
	}
	
	public RelationM2 getRelationM2(String representation)
	{
		String[] args = representation.split(PARTS_SEP);
		
		if(args.length == 5)
		{
			//a source type b target
			return new RelationM2(args[2].trim(), args[0].trim(), args[3].trim(), args[1].trim(), args[4].trim());
		}
				
		return null;
	}
}
