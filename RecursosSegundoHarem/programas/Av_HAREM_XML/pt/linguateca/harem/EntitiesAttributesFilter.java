package pt.linguateca.harem;

public class EntitiesAttributesFilter extends EntitiesAttributesTree{

	private boolean everything;
	
	public EntitiesAttributesFilter(){
		super();
		everything = false;
	}
	
	public EntitiesAttributesFilter(String filter, TagBase tags){
		super(filter, tags);
		everything = false;
	}
	
	public EntitiesAttributesFilter(EntitiesAttributesTree tree, boolean b){
		super();
		super._tree = tree.getTree();
		everything = b;
	}
		
	public void setTree(EntitiesAttributesTree tree, boolean b){
		super._tree = tree.getTree();
		everything = b;
	}
	
	public void setEverything(boolean b){
		everything = b;
	}
	
	public boolean hasEverything(){
		return everything;
	}
	
	public String toString(){
		return super.toString();
	}
}
