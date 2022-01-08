package pt.linguateca.harem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationM2;

/**
 * 
 * @author nseco, Besugo
 * 
 * Etiquetas do Segundo HAREM
 */
public class TagBase
{
	private static TagBase _tagBase;

	private final String DOC_TAG = "DOC";
	
	private final String END_OF_DOC_TAG = "EOD";

	private final String DOC_ID_TAG = "DOCID";

	private final String GENRE_TAG = "GENERO";

	private final String ORIGIN_TAG = "ORIGEM";

	private final String TEXT_TAG = "TEXTO";

	private final String ALT_TAG = "ALT";

	private final String CATEG_AT = "CATEG";

	private final String TYPE_AT = "TIPO";

	private final String SUBTYPE_AT = "SUBTIPO";
	
	private final String OTHER_AT = "OUTRO";

	private final String ID_AT = "ID";

	private final String VAGUE_SEP = "|";

	private final String ALT_SEP = "|";

	private final String MORPH_TAG = "MORF";

	private final String SPURIOUS_TAG = "ESPURIO";

	private final String OMITTED_TAG = "OMITIDO";
	
	private final String AT_TRUE = "1";
	
	private final String AT_FALSE = "0";

	private final String MANUAL_INTERVENTION_TAG = "VERIFICACAO_MANUAL";

	private final String HAREM_TAG = "HAREM";

	private final String SIMPLE_ENTITY_TAG = "EM";

	private final String COREL_AT = "COREL";

	private final String COREL_SEP = " ";

	private final String TIPOREL_AT = "TIPOREL";

	private final String COMENT_AT = "COMENT";
	
	private final String NUM_ALTS = "ALTS";
	
	private final String FAC_TARGET = "FACS_ALVO";
	
	private final String FAC_SOURCE = "FACS_ORIGEM";

	private TagBaseReader _tagBaseReader;

	//private HashSet<String> _documentTags;

	public static TagBase getInstance()
	{
		if (_tagBase == null)
		{					
			_tagBase = new TagBase();
		}

		return _tagBase;
	}

	public String openTag(String tag)
	{
		return "<" + tag + ">";
	}

	public String protectedOpenTag(String tag)
	{
		return "&lt" + tag + "&gt";
	}

	public String closeTag(String tag)
	{
		return "</" + tag + ">";
	}

	public String protectedCloseTag(String tag)
	{
		return "&lt/" + tag + "&gt";
	}

	public String asQuotedType(String type)
	{
		return "\"" + type + "\"";
	}

	public String deQuote(String type)
	{
		return type.replaceAll("\"", "");
	}

	public String getHaremTag()
	{
		return HAREM_TAG;
	}

	public String getManualVerificationTag()
	{
		return MANUAL_INTERVENTION_TAG;
	}

	public String getSimpleEntityTag()
	{
		return SIMPLE_ENTITY_TAG;
	}

	public String getSpuriousTag()
	{
		return SPURIOUS_TAG;
	}

	public String getOmittedTag()
	{
		return OMITTED_TAG;
	}

	public String getAttributeTrue()
	{
		return AT_TRUE;
	}
	
	public String getAttributeFalse()
	{
		return AT_FALSE;
	}
	
	public String getDocTag()
	{
		return DOC_TAG;
	}

	public String getDocIDTag()
	{
		return DOC_ID_TAG;
	}

	public String getEndOfDocTag()
	{
		return END_OF_DOC_TAG;
	}
	
	public String getGenreTag()
	{
		return GENRE_TAG;
	}
	
	public String getTextTag()
	{
		return TEXT_TAG;
	}

	public String getOriginTag()
	{
		return ORIGIN_TAG;
	}

	public String getAltTag()
	{
		return ALT_TAG;
	}

	public String getCategTag()
	{
		return CATEG_AT;
	}

	public String getIdAt()
	{
		return ID_AT;
	}

	public String getTypeTag()
	{
		return TYPE_AT;
	}

	public String getSubtypeTag()
	{
		return SUBTYPE_AT;
	}

	public String getOtherAt()
	{
		return OTHER_AT;
	}
	
	public String getComentAt()
	{
		return COMENT_AT;
	}

	public String getVagueSep()
	{
		return VAGUE_SEP;
	}

	public String getVagueSepRegex()
	{
		return "\\"+VAGUE_SEP;
	}

	public String getAltSep()
	{
		return ALT_SEP;
	}

	public String getAltSepRegex()
	{
		return "\\"+ALT_SEP;
	}

	public String getNumAltsAt()
	{
		return NUM_ALTS;
	}
	
	public String getMorphTag()
	{
		return MORPH_TAG;
	}

	public Set<String> getCategories()
	{
		//return _entityTags.keySet();
		return _tagBaseReader.getEntityAttributes().getCategories();
	}

	public Set<String> getTypes(String category)
	{
		//return _entityTags.get(category).keySet();
		return _tagBaseReader.getEntityAttributes().getTypes(category);
	}
	
	public Set<String> getSubtypes(String category, String type)
	{
		//return _entityTags.get(category).get(type);
		return _tagBaseReader.getEntityAttributes().getSubtypes(category, type);
	}

	public LinkedList<String> getPossibleCategoriesOfType(String type)
	{
		return _tagBaseReader.getEntityAttributes().getPossibleCategoriesOfType(type);
	}
	
	public LinkedList<String> getPossibleTypesOfSubtype(String sub)
	{
		return _tagBaseReader.getEntityAttributes().getPossibleTypesOfSubtype(sub);
	}
	
	public LinkedList<String[]> getPossibleTiposRel(){
		return _tagBaseReader.getTiposRef();
	}
	
	/*public RelationM2 getInverse(RelationM2 relation){
		
		String inverse = _tagBaseReader.getInverse(relation.getType());
		if(inverse != null)
			return new RelationM2(inverse, relation.getB(), relation.getA(), relation.getTargetCategory(), relation.getSourceCategory());
		else return null;
	}*/
	
	public String getIdentity()
	{
		return _tagBaseReader.getTiposRef().get(0)[0];
	}

	public boolean isDirectRelationType(String type)
	{
		return _tagBaseReader.isDirectRelationType(type);
	}
	
	public Relation getInverse(Relation relation){
		
		String inverse = _tagBaseReader.getInverse(relation.getType());
		if(inverse != null)
		{
			if(relation instanceof RelationM2)
			{
				RelationM2 rel2 = (RelationM2)relation;
				return new RelationM2(inverse, relation.getB(), relation.getA(), rel2.getTargetCategory(), rel2.getSourceCategory());
			}
			else
				return new Relation(inverse, relation.getB(), relation.getA());
		}
		else return null;
	}
	
	public String getInverseType(String relation){
		
		return _tagBaseReader.getInverse(relation);
	}
		
	/*public Set getAllTypes()
	{
		HashSet types = new HashSet();
		Collection col = _entityTags.values();
		
		for (Iterator i = col.iterator(); i.hasNext();)
		{
			types.addAll((Set) i.next());
		}

		return types;
	}*/

	/*public boolean containsDocumentTag(String tag)
	{
		return _documentTags.contains(tag);
	}*/

	public boolean containsEntityCategory(String tag)
	{
		return _tagBaseReader.getEntityAttributes().containsCategory(tag);
	}

	public boolean containsEntityType(String cat, String type)
	{
		return _tagBaseReader.getEntityAttributes().containsType(cat, type);
	}
	
	/** Creates a new instance of TagBase */
	private TagBase()
	{
		
		HashMap<String, Double> weights = new HashMap<String, Double>(5);
		weights = new HashMap<String, Double>();
		weights.put(CATEG_AT, null);
		weights.put(TYPE_AT, null);
		weights.put(SUBTYPE_AT, null);
		weights.put(COREL_AT, null);
		weights.put(TIPOREL_AT, null);
		
		_tagBaseReader = new TagBaseReader(weights);
		_tagBaseReader.load();

		//_documentTags = new HashSet<String>();

		//loadDocumentTags();
		//loadEntityTags();

	}
	
	/*private void loadDocumentTags()
	{
		_documentTags.add("DOC");
		_documentTags.add("DOCID");
	}

	private void loadEntityTags()
	{
		//e' preciso alguma coisa?
		//_tagBaseReader.getEntityAttributes().newCategory(SIMPLE_ENTITY_TAG);
		//_tagBaseReader.getEntityAttributes().newCategory(OMITTED_TAG);
		//_tagBaseReader.getEntityAttributes().newCategory(SPURIOUS_TAG);
	}*/

	public String getCorelAt() {
		return COREL_AT;
	}

	public String getCorelSep() {
		return COREL_SEP;
	}

	public String getTipoRelAt() {
		return TIPOREL_AT;
	}
	
	public EntitiesAttributesTree getEntitiesAttributesTree(){
		return _tagBaseReader.getEntityAttributes();
		//return _tagBaseReader.getEntityAttributes();
	}

	public String getTargetFacetAt() {
		return FAC_TARGET;
	}

	public String getSourceFacetAt() {
		return FAC_SOURCE;
	}
}