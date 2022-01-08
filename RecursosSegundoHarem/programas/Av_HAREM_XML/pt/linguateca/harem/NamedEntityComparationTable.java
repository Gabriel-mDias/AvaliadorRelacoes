package pt.linguateca.harem;

/**
 * 
 * @author Besugo
 *
 * Tabela de comparação de EMs
 */
public class NamedEntityComparationTable {

	private enum atribs {
		EM, ID, CATEG, TYPE, SUBTYPE, COREL, TIPO_REL, TEMPO_REF, SENTIDO
	}
	
/*	private int EM = 0;
	private int ID = 1;
	private int CATEG = 2;
	private int TIPO = 3;
	private int SUBTIPO = 4;*/
	
	private boolean[] comparation;
	
	public NamedEntityComparationTable(){
		comparation = new boolean[atribs.values().length];
		initialize();
	}
	
	private void initialize(){
		for(boolean b : comparation)
			b = false;
	}
	
	public void setEntity(boolean b){
		comparation[atribs.EM.ordinal()] = b;
	}
	
	public void setId(boolean b){
		comparation[atribs.ID.ordinal()] = b;
	}
	
	public void setCategory(boolean b){
		comparation[atribs.CATEG.ordinal()] = b;
	}
	
	public void setType(boolean b){
		comparation[atribs.TYPE.ordinal()] = b;
	}
	
	public void setSubtype(boolean b){
		comparation[atribs.SUBTYPE.ordinal()] = b;
	}

	public void setCorel(boolean b){
		comparation[atribs.COREL.ordinal()] = b;
	}
	
	public void setTipoRel(boolean b){
		comparation[atribs.TIPO_REL.ordinal()] = b;
	}
	
	public void setTempoRef(boolean b){
		comparation[atribs.TEMPO_REF.ordinal()] = b;
	}
	
	public void setSentido(boolean b){
		comparation[atribs.SENTIDO.ordinal()] = b;
	}
	
	public boolean getEntity(){
		return comparation[atribs.EM.ordinal()];
	}
	
	public boolean getId(){
		return comparation[atribs.ID.ordinal()];
	}
	
	public boolean getCategory(){
		return comparation[atribs.CATEG.ordinal()];
	}
	
	public boolean getType(){
		return comparation[atribs.TYPE.ordinal()];
	}
	
	public boolean getSubtype(){
		return comparation[atribs.SUBTYPE.ordinal()];
	}
	
	public boolean getCorel(boolean b){
		return comparation[atribs.COREL.ordinal()];
	}
	
	public boolean getTipoRel(boolean b){
		return comparation[atribs.TIPO_REL.ordinal()];
	}
	
	public boolean getTempoRef(boolean b){
		return comparation[atribs.TEMPO_REF.ordinal()];
	}
	
	public boolean getSentido(boolean b){
		return comparation[atribs.SENTIDO.ordinal()];
	}
}
