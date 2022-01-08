
package pt.linguateca.harem;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

import pt.linguateca.util.AtomicCounter;

/**
 * 
 * @author nseco, Besugo
 *
 * Entidade mencionada (EM)
 */
public class NamedEntity extends HaremEntity
{	
	protected Element _entityElement;

	private static SAXBuilder builder;

	public NamedEntity()
	{
		_entityElement = new Element(_tagBase.getSimpleEntityTag());
	}

	public NamedEntity(String id, boolean dummy)
	{
		this();
		_entityElement.setAttribute(_tagBase.getIdAt(), id);
	}

	public NamedEntity(Element el)
	{	
		_entityElement = el;
	}

	public NamedEntity(String text){

		//System.out.println(text);

		if(builder == null)
			builder = new SAXBuilder(false);

		//nao esta' a substituir caracteres invalidos, por isso substituo-os eu
		text = replaceInvalidXMLChars1(text);

		StringReader reader = new StringReader(text);

		try {
			Document doc = builder.build(reader);
			_entityElement = doc.getRootElement();

		} catch (JDOMException e) {
			System.err.println("EM mal formada!\n"+text+"\n"+e);
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String replaceInvalidXMLChars1(String text){

		String inside = text.replaceAll("\\<.*?\\>", "");
		//inside = inside.replaceAll("\n", " ");

		int openLength = text.indexOf(inside);
		String open = text.substring(0, openLength);

		int insideLength = inside.length();
		String close = text.substring(openLength+insideLength);

		inside = replaceInvalidXMLChars2(inside);

		return open+inside+close;

	}

	public Element getElement(){
		return _entityElement;
	}

	public void setOmitted(boolean b){
		if(b)
			_entityElement.setAttribute(_tagBase.getOmittedTag(), _tagBase.getAttributeTrue());
		else _entityElement.setAttribute(_tagBase.getOmittedTag(), _tagBase.getAttributeFalse());
	}

	public boolean isOmitted(){

		String value = _entityElement.getAttributeValue(_tagBase.getOmittedTag());
		if(value == null)
			return false;
		else
			return value.equals(_tagBase.getAttributeTrue());
	}

	/** numero de alternativas no ALT em que a EM esta' inserida **/
	public void setNumAltsAttribute(int num)
	{
		_entityElement.setAttribute(_tagBase.getNumAltsAt(), ""+num);
	}

	public int getNumAltsAttribute()
	{
		String value = _entityElement.getAttributeValue(_tagBase.getNumAltsAt());
		return value != null ? Integer.parseInt(value) : 1;
	}

	public void normalizeCorels(HashMap<String, String> conversionMap){

		Attribute corels = _entityElement.getAttribute(_tagBase.getCorelAt());
		if(corels != null){

			String value = corels.getValue();
			String[] parts = value.split(_tagBase.getCorelSep());

			for(int i = 0; i < parts.length; i++){
				parts[i] = conversionMap.get(parts[i]);
			}

			String newValue = parts[0];
			for(int i = 1; i < parts.length; i++){
				newValue += _tagBase.getCorelSep() + parts[i];
			}
		}
	}

	public Object clone()
	{
		NamedEntity clone = new NamedEntity();
		clone._entityElement = (Element)_entityElement.clone();

		return clone;
	}

	public String toString()
	{
		String tag = getOpeningTag();

		//retirar as quebras de linhas
		tag += _entityElement.getText().replaceAll("\n", " ");

		tag += getClosingTag();

		return tag;
	}

	/** Também mostra elementos que estejam dentro da EM - acontece nos omitidos!**/
	public String toString2()
	{
		String tag = getOpeningTag();

		//retirar as quebras de linhas
		for(Object obj : _entityElement.getContent())
		{
			if(obj instanceof Text)
				tag += ((Text)obj).getText().replaceAll("\n", " ");
			else if(obj instanceof Element)
			{
				Element el = (Element)obj;
				if(el.getQualifiedName().equals(_tagBase.getAltTag()))
				{
					tag += new ALTEntity(el).toString();
				}
				else if(el.getQualifiedName().equals(_tagBase.getSimpleEntityTag()))
				{
					tag += new NamedEntity(el).toString();	
				}
			}
		}

		tag += getClosingTag();

		return tag;
	}

	public AttributeTupleSet getAttributeTupleSet(){
		return AttributeTupleSet.getAttributeTupleSet(this);
	}

	public AttributeTuple getAttributeTuple(int index){

		String cat = null;
		String type = null;
		String sub = null;

		if(getCategories().size() > index)
			cat = getCategories().get(index);

		if(getTypes().size() > index)
			type = getTypes().get(index);

		if(getSubtypes().size() > index)
			sub = getSubtypes().get(index);

		return new AttributeTuple(cat, type, sub);
	}

	public void removeCategory(String category)
	{
		int index;
		LinkedList<String> cats = attributeToList(_tagBase.getCategTag(), _tagBase.getVagueSepRegex());

		for(index = 0; index < cats.size(); index++){
			if(cats.get(index).equals(category))
				break;
		}
		removeAttributesAt(index);
	}

	public void removeType(String type)
	{
		int index;
		LinkedList<String> types = attributeToList(_tagBase.getTypeTag(), _tagBase.getVagueSepRegex());

		for(index = 0; index < types.size(); index++){
			if(types.get(index).equals(type))
				break;
		}

		removeAttributesAt(index);
	}

	public void removeAttributesAt(int index){

		LinkedList<String> cats = getCategories();
		LinkedList<String> types = getTypes();
		LinkedList<String> subs = getSubtypes();

		if(cats.size() > index)
			cats.remove(index);

		if(types.size() > index)
			types.remove(index);

		if(subs.size() > index)
			subs.remove(index);

		setClassicAttributes(cats, types, subs);

	}

	public void removeId(){
		_entityElement.removeAttribute(_tagBase.getIdAt());
	}

	//TODO: verificar o funcionamento disto!

	protected void setClassicAttributes(
			LinkedList<String> cats, LinkedList<String> types, LinkedList<String> subs){

		String newCategoryValue = vagueValuesToString(cats);
		String newTypeValue = vagueValuesToString(types);
		String newSubtypeValue = vagueValuesToString(subs);

		if (!newSubtypeValue.equals(""))
			_entityElement.setAttribute(_tagBase.getSubtypeTag(), newSubtypeValue);
		else
			_entityElement.removeAttribute(_tagBase.getSubtypeTag());

		if (!newTypeValue.equals(""))
			_entityElement.setAttribute(_tagBase.getTypeTag(), newTypeValue);
		else
			_entityElement.removeAttribute(_tagBase.getTypeTag());

		if (!newCategoryValue.equals(""))
			_entityElement.setAttribute(_tagBase.getCategTag(), newCategoryValue);
		else
			_entityElement.removeAttribute(_tagBase.getCategTag());
	}

	/*private String vagueValuesToString(LinkedList<String> list){

		String newValue = "";
		if(list != null && !list.isEmpty()){

			newValue = list.removeFirst();
			for(String v : list){
				newValue = newValue.concat(_tagBase.getVagueSep()).concat(v);
			}
		}
		return newValue;
	}*/

	public static String vagueValuesToString(Iterable<String> iterable){

		String newValue = "";
		if(iterable != null)
		{
			for(String v : iterable){
				newValue += TagBase.getInstance().getVagueSep() + v;
			}
		}

		if(newValue.length() == 0)
			return newValue;

		return newValue.substring(1);
	}

	public LinkedList<String> getTypesForCategory(String category)
	{
		LinkedList<String> types = new LinkedList<String>();
		String currentCategory;
		String currentType;
		//int index = 0;
		Iterator<String> i, j;

		if (getTypes() != null)
		{
			for (i = getCategories().iterator(), j = getTypes().iterator(); i.hasNext() && j.hasNext();)
			{
				currentCategory = (String) i.next();
				currentType = (String) j.next();

				if (category.equals(currentCategory))
				{
					types.add(currentType);
				}
			}
		}

		return types;
	}

	public String getOpeningTag()
	{
		String tag = _entityElement.getQualifiedName();

		List<Attribute> attributes = _entityElement.getAttributes();
		for(Attribute at : attributes){
			tag += " "+at.getName()+"=\""+at.getValue()+"\"";
		}

		//tag += " OMITIDO="+isOmitted();

		return _tagBase.openTag(tag);
	}

	public String getClosingTag()
	{
		/*String tag;

		tag = getStringRepresentation(_categories);

		return _tagBase.closeTag(tag);*/

		return _tagBase.closeTag(_entityElement.getName());
	}

	public String getEntity()
	{
		return _entityElement.getText();
	}

	public String getId()
	{
		return _entityElement.getAttributeValue(_tagBase.getIdAt());
	}

	public LinkedList<String> getCategories()
	{
		return attributeToList(_tagBase.getCategTag(), _tagBase.getVagueSepRegex());
	}

	public LinkedList<String> getTypes()
	{
		return attributeToList(_tagBase.getTypeTag(), _tagBase.getVagueSepRegex());
	}

	public LinkedList<String> getSubtypes()
	{
		return attributeToList(_tagBase.getSubtypeTag(), _tagBase.getVagueSepRegex());
	}

	public String getComments()
	{
		return _entityElement.getAttributeValue(_tagBase.getComentAt());
	}

	/**
	 * Attribute value to attribute linked list
	 * @param at
	 * @return list of values
	 */
	protected LinkedList<String> attributeToList(String at, String sep){
		return attributeToList(_entityElement.getAttributeValue(at), sep, true);
	}

	public static LinkedList<String> attributeToList(String value, String sep, boolean staticMethod){

		//System.out.println("string= "+value);

		//se nao tiver o atributo
		if(value == null)
			return new LinkedList<String>();

		String[] cats = value.split(sep);
		List<String> list = Arrays.asList(cats);

		//System.out.println("list= "+list);
		return new LinkedList<String>(list);
	}

	protected LinkedList<String> getCorels(){
		return attributeToList(_tagBase.getCorelAt(), _tagBase.getCorelSep());	
	}

	protected LinkedList<String> getTipoRels(){
		return attributeToList(_tagBase.getTipoRelAt(), _tagBase.getCorelSep());	
	}

	public String getComent(){
		return _entityElement.getAttributeValue(_tagBase.getComentAt());
	}

	protected boolean startsWith(MarkedToken token)
	{
		CounterTagParser parser = new CounterTagParser();
		LinkedList tokens = split(parser);

		return token.equals(tokens.getFirst());
	}

	protected boolean endsWith(MarkedToken token)
	{
		CounterTagParser parser = new CounterTagParser();
		LinkedList<String> tokens = split(parser);

		return token.equals(tokens.getLast());
	}

	public boolean isSpurious()
	{
		return getCategories().contains(_tagBase.getSpuriousTag());
	}

	protected boolean hasCategories()
	{
		return !getCategories().isEmpty();
	}

	protected boolean hasCategory(String category)
	{
		return attributeToList(_tagBase.getCategTag(), _tagBase.getVagueSepRegex()).contains(category);
		//return _categories.contains(category);
	}

	protected boolean hasType(String type)
	{
		return attributeToList(_tagBase.getTypeTag(), _tagBase.getVagueSepRegex()).contains(type);
		//return _types.contains(type);
	}

	protected boolean hasSubType(String sub)
	{
		return attributeToList(_tagBase.getSubtypeTag(), _tagBase.getVagueSepRegex()).contains(sub);
		//return _types.contains(type);
	}

	protected LinkedList split(Parser parser)
	{
		LinkedList tokens = new LinkedList();
		int index;
		parser.setText(getEntity());

		for (int i = 0; i < getEntity().length(); i++)
		{
			index = parser.recognize(i);
			if (index != -1)
			{
				tokens.add(parser.getEntity());
				i = index - 1;
			}
		}

		return tokens;
	}

	protected LinkedList getNormalizedAtoms()
	{
		LinkedList atoms = new LinkedList();
		CounterTagParser parser = new CounterTagParser();
		String current = "";
		int index;
		char currentChar;
		parser.setText(getEntity());

		for (int i = 0; i < getEntity().length(); i++)
		{
			currentChar = getEntity().charAt(i);
			index = parser.recognize(i);
			if (index != -1)
			{
				if (!current.equals(""))
				{
					atoms.add(current.toUpperCase().intern());
					current = "";
				}

				atoms.add(((MarkedToken) parser.getEntity()).getToken().toUpperCase().intern());
				i = index - 1;
				continue;
			}

			if (!Character.isLetterOrDigit(currentChar))
			{
				if (!current.equals(""))
				{
					atoms.add(current.toUpperCase().intern());
					current = "";
				}
				continue;
			}

			current += currentChar;
		}

		if (!current.equals(""))
		{
			atoms.add(current.toUpperCase().intern());
		}

		return atoms;
	}

	protected HaremEntity unmarkTokens()
	{
		String toUnMark = getEntity();

		NamedEntity clone = (NamedEntity) clone();
		String unmarked = "";
		CounterTagParser parser = new CounterTagParser();

		int index;

		parser.setText(toUnMark);

		for (int i = 0; i < toUnMark.length(); i++)
		{
			index = parser.recognize(i);
			if (index != -1)
			{
				unmarked += ((MarkedToken) parser.getEntity()).getToken();
				i = index - 1;
				continue;
			}

			unmarked += toUnMark.charAt(i);
		}

		clone.setEntity(unmarked);
		return clone;
	}

	protected boolean isMarked()
	{
		CounterTagParser parser = new CounterTagParser();
		parser.setText(getEntity());
		int index = parser.recognize(0);
		return index != -1;
	}

	protected HaremEntity markEntity(int tag)
	{
		NamedEntity clone = (NamedEntity) clone();
		clone.setEntity(_tagBase.openTag("" + tag) + getEntity() + _tagBase.closeTag("" + tag));
		return clone;
	}

	protected HaremEntity markTokens(HashMap markable)
	{
		String toMark;
		String current = "";
		String marked = "";
		AtomicCounter counter;
		char currentChar;
		char previousChar = '\n';
		NamedEntity clone = (NamedEntity) clone();

		toMark = getEntity();

		for (int i = 0; i < toMark.length(); i++)
		{
			currentChar = toMark.charAt(i);
			if (TaggedDocument.isTokenDelimiter(previousChar, currentChar))
			{
				if ((counter = (AtomicCounter) markable.get(current.toUpperCase())) != null)// **
				{
					marked += _tagBase.openTag("" + counter.increment()) + current
					+ _tagBase.closeTag("" + counter.getValue());
					// marked += currentChar;
				}
				else
				{
					marked += current; // + currentChar;
				}

				if (!Character.isLetterOrDigit(currentChar))
				{
					current = "";
					marked += currentChar;
				}
				else
				{
					current = "" + currentChar;
				}

				previousChar = currentChar;
				continue;
			}

			current += currentChar;
			previousChar = currentChar;
		}

		if ((counter = (AtomicCounter) markable.get(current.toUpperCase())) != null)// **
		{
			marked += _tagBase.openTag("" + counter.increment()) + current
			+ _tagBase.closeTag("" + counter.getValue());
		}
		else
		{
			marked += current;
		}

		clone.setEntity(marked);

		return clone;
	}

	public Set<String> getMarkableTokens()
	{
		HashSet<String> tokens = new HashSet<String>();
		String toTokenize;
		String current = "";
		char currentChar;
		char previousChar = '\n';

		toTokenize = getEntity();

		for (int i = 0; i < toTokenize.length(); i++)
		{
			currentChar = toTokenize.charAt(i);
			if (TaggedDocument.isTokenDelimiter(previousChar, currentChar))
			{
				if (TaggedDocument.isMarkable(current))
				{
					tokens.add(current.toUpperCase());// ***
				}

				if (!Character.isLetterOrDigit(currentChar))
				{
					current = "";
				}
				else
				{
					current = "" + currentChar;
				}
				previousChar = currentChar;
				continue;
			}

			current += currentChar;
			previousChar = currentChar;
		}

		if (TaggedDocument.isMarkable(current))
		{
			tokens.add(current.toUpperCase());// **
		}

		return tokens;
	}

	protected void addCategory(String category)
	{
		addAttributeValue(_tagBase.getCategTag(), category);
	}

	protected void addCategories(LinkedList<String> categories)
	{
		for (Iterator<String> i = categories.iterator(); i.hasNext();)
		{
			addCategory(i.next());
		}
	}

	protected void addType(String type)
	{
		addAttributeValue(_tagBase.getTypeTag(), type);
	}

	protected void addTypes(LinkedList<String> types)
	{
		for (Iterator<String> i = types.iterator(); i.hasNext();)
		{
			addType(i.next());
		}
	}

	protected void addSubtype(String subtype)
	{
		addAttributeValue(_tagBase.getSubtypeTag(), subtype);
	}

	protected void addSubtypes(LinkedList<String> subtypes)
	{
		for (Iterator<String> i = subtypes.iterator(); i.hasNext();)
		{
			addSubtype(i.next());
		}
	}

	protected void addAttributeValue(String at, String value){

		Attribute attribute = _entityElement.getAttribute(at);

		String sep = (at.equals(_tagBase.getCorelAt()) || at.equals(_tagBase.getTipoRelAt())
				|| at.equals(_tagBase.getSourceFacetAt()) || at.equals(_tagBase.getTargetFacetAt()) ?
				_tagBase.getCorelSep() : _tagBase.getVagueSep());
		
		String attribute_v = null;
		if(attribute != null && !attribute.getValue().equals("")) {
			attribute_v = attribute.getValue()
			.concat(sep)
			.concat(value);
		} else
			attribute_v = value;

		_entityElement.setAttribute(at, attribute_v);
	}

	public void setSpurious(){

		_entityElement.getAttributes().clear();
		_entityElement.setAttribute(_tagBase.getCategTag(), _tagBase.getSpuriousTag());
	}

	protected void setEntity(String entity)
	{	
		_entityElement.setText(entity.trim());
	}

	protected void setId(String id)
	{
		_entityElement.setAttribute(_tagBase.getIdAt(), id);
	}

	/**
	 * EM com atributos filtrados
	 * @param filter O filtro
	 * @return A EM de acordo com o filtro dado
	 */
	public NamedEntity filter(EntitiesAttributesFilter filter)
	{	
		AttributeTupleSet tuples = getAttributeTupleSet();
		AttributeTupleSet valid = new AttributeTupleSet();
		String cat, type, sub;

		for(AttributeTuple at : tuples)
		{	
			cat = at.getCategory();
			type = at.getType();
			sub = at.getSubtype();

			if(filter.containsCategory(cat)){

				if(type == null){
					valid.add(at);
					continue;
				} else if(!filter.hasTypes(cat)){
					valid.add(new AttributeTuple(cat, null, null));
					continue;
				}

				if(filter.containsType(cat, type)){

					if(sub == null){
						valid.add(at);
						continue;
					} else if(!filter.hasSubtypes(cat, type)){
						valid.add(new AttributeTuple(cat, type, null));
						continue;
					}

					if(filter.containsSubtype(cat, type, sub))
						valid.add(at);
				}
			}
		}

		return replaceClassicAttributes(valid);
	}

	public NamedEntity removeClassicAttributes(){

		if(!this.isSpurious()){
			NamedEntity clone = (NamedEntity)this.clone();
			clone.setClassicAttributes(null, null, null);
			return clone;
		} else return this;
	}

	public NamedEntity removeComments(){

		if(!this.isSpurious()){
			NamedEntity clone = (NamedEntity)this.clone();
			clone._entityElement.removeAttribute(_tagBase.getComentAt());
			return clone;
		} else return this;
	}

	public boolean containsAttributeTuple(AttributeTuple at){
		return getAttributeTupleSet().contains(at);
	}

	public void addAttributeTuple(AttributeTuple tuple){
		String cat = tuple.getCategory() != null ? tuple.getCategory() : "";
		String type = tuple.getType() != null ? tuple.getType() : "";
		String sub = tuple.getSubtype() != null ? tuple.getSubtype() : "";

		if( !cat.equals("") || ( cat.equals("") && !getCategories().isEmpty())){
			this.addCategory(cat);

			if( !type.equals("") || ( type.equals("") && !getTypes().isEmpty())){
				this.addType(type);

				if( !sub.equals("") || ( sub.equals("") && !getSubtypes().isEmpty()))
					this.addSubtype(sub);
			}
		}
	}

	private NamedEntity replaceClassicAttributes(AttributeTupleSet tuples){

		NamedEntity clone = (NamedEntity)this.clone();

		LinkedList<String> newCats = new LinkedList<String>();
		LinkedList<String> newTypes = new LinkedList<String>();
		LinkedList<String> newSubs = new LinkedList<String>();
		String cat, type, sub;

		for(AttributeTuple at : tuples){
			cat = (at.getCategory() == null ? "" : at.getCategory());
			type = (at.getType() == null ? "" : at.getType());
			sub = (at.getSubtype() == null ? "" : at.getSubtype());

			newCats.add(cat);
			newTypes.add(type);
			newSubs.add(sub);
		}

		clone.setClassicAttributes(newCats, newTypes, newSubs);
		return clone;		
	}

	/*	public double getMaximumCSC(EntitiesAttributesFilter filter)
	{
		//cria um set de tuplos avaliados a partir da entidade
		//e calcula o valor da CSC se estivesse tudo correcto
		AttributesEvaluation allCorrect = new AttributesEvaluation(filter);
		AttributeTupleSet tuples = getAttributeTupleSet();

		if(!isSpurious())
		{
			for(AttributeTuple tuple : tuples){

				if(tuple.getCategory() != null)
					allCorrect.addCorrect(AttributesEvaluation.CATEGORY, tuple);

				if(tuple.getType() != null)
					allCorrect.addCorrect(AttributesEvaluation.TYPE, tuple);

				if(tuple.getSubtype() != null)
					allCorrect.addCorrect(AttributesEvaluation.SUBTYPE, tuple);
			}
		}
		return allCorrect.getCombinedSemanticClassification();
	}*/

	public boolean equivalent (NamedEntity outra, boolean categ, boolean tipo, boolean subtipo) {

		if(!getEntity().equals(outra.getEntity()))
			return false;

		if(categ && !getCategories().equals(outra.getCategories()))
			return false;

		if(tipo && !getCategories().equals(outra.getTypes()))
			return false;

		if(subtipo && !getSubtypes().equals(outra.getSubtypes()))
			return false;

		return true;
	}

	public boolean equals (NamedEntity outra) {

		return _entityElement.equals(outra.getElement());
	}

	public NamedEntityComparationTable diffs (NamedEntity outra) {

		NamedEntityComparationTable table = new NamedEntityComparationTable();

		if(getEntity() != null)
			table.setEntity(getEntity().equals(outra.getEntity()));

		if(getId() != null)
			table.setId(getId().equals(outra.getId()));

		if(getCategories() != null)
			table.setCategory(getCategories().equals(outra.getCategories()));

		if(getCategories() != null)
			table.setType(getTypes().equals(outra.getTypes()));

		if(getSubtypes() != null)
			table.setSubtype(getSubtypes().equals(outra.getSubtypes()));

		return table;
	}

	/**
	 * Converte uma lista de NEs sob a forma de String para uma LinkedList de NEs
	 * @param nes
	 * @return
	 */
	public static LinkedList<NamedEntity> toNamedEntityList(String nes)
	{		
		final String control = "###";
		final String nullStr = "["+null+"]";
		LinkedList<NamedEntity> ret = new LinkedList<NamedEntity>();

		if(nes.equals(nullStr)) //[null]
			return ret;

		String normalized = nes.replaceAll("\\[", "").replaceAll("\\]", "");
		normalized = normalized.replaceAll(">, <", ">"+control+"<");

		String[] split = normalized.split(control);

		for(String s : split)
		{
			ret.add(new NamedEntity(s));
		}

		return ret;
	}

	private String replaceInvalidXMLChars2(String texto){
		return texto.replaceAll("&", "&amp;")
		.replaceAll("\\''", "&apos;")
		.replaceAll("\"","&quot;")
		.replaceAll("<","&lt;")
		.replaceAll(">","&gt;");
	}

	public static void main(String args[]){

		String str = "<EM CATEG=\"VALOR\" TIPO=\"MOEDA\">2 escudos</EM>";
		NamedEntity ne = new NamedEntity(str);
		System.out.println(ne);
	}
}