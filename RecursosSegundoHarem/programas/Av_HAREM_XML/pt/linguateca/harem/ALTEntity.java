package pt.linguateca.harem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;

import pt.linguateca.util.AtomicCounter;

/**
 * @author nseco, Besugo
 * 
 * Entidade ALT
 */
public class ALTEntity extends HaremEntity
{
	private LinkedList<LinkedList<Object>> _alternatives;
	private Element _altElement;
	private TagBase _tagBase;

	/**
	 * 
	 */
	public ALTEntity()
	{
		_tagBase = TagBase.getInstance();
		_altElement = new Element(_tagBase.getAltTag());
		_alternatives = new LinkedList<LinkedList<Object>>();
	}

	public ALTEntity(Element alt)
	{
		_tagBase = TagBase.getInstance();
		_altElement = alt;
		_alternatives = buildAlternatives();
		/*_alternatives = new LinkedList();
		altFromElement();*/
		//System.out.println("----> "+this);
	}

	public String getOpeningTag()
	{
		return _tagBase.openTag(_tagBase.getAltTag());
	}

	public String getClosingTag()
	{
		return _tagBase.closeTag(_tagBase.getAltTag());
	}

	public void addAlternative(LinkedList alternative)
	{
		_alternatives.add(alternative);
		addAlternativeToElement(alternative);
	}

	private void addAlternativeToElement(LinkedList alternative){
		LinkedList<LinkedList<Object>> alternatives = new LinkedList<LinkedList<Object>>();
		alternatives.add(alternative);
		updateElement(alternatives);
	}
	
	public void setAlternatives(LinkedList alternatives)
	{
		_alternatives = alternatives;
		_altElement.removeContent();
		updateElement(alternatives);
	}
	
	private void updateElement(LinkedList<LinkedList<Object>> alternatives){
		
		for(LinkedList<Object> alternative : alternatives){
			
			if(!_altElement.getContent().isEmpty())
				_altElement.addContent(_tagBase.getAltSep());
			
			for(Object obj : alternative){
				
				if(obj instanceof NamedEntity)
					_altElement.addContent(((NamedEntity)obj).getElement());
				else
					_altElement.addContent((String)obj);
			}
		}	
	}

	public Iterator getAlternativesIterator()
	{
		return _alternatives.iterator();
	}

	public LinkedList getAlternatives()
	{
		return _alternatives;
	}
	
	public LinkedList<NamedEntity> getAllEntities(){
		
		LinkedList<NamedEntity> entities = new LinkedList<NamedEntity>();
		List elements = _altElement.getContent(new ElementFilter(_tagBase.getSimpleEntityTag()));
		Iterator it = elements.iterator();
		while(it.hasNext()){
			entities.add(new NamedEntity((Element)it.next()));
		}
		return entities;	
	}
	
	private LinkedList<LinkedList<Object>> buildAlternatives(){
		return buildAlternatives(_altElement);
	}

	private LinkedList<LinkedList<Object>> buildAlternatives(Element alt){

		LinkedList<LinkedList<Object>> alternatives = new LinkedList<LinkedList<Object>>();
		LinkedList<Object> currAlternative = new LinkedList<Object>();

		boolean terminaEmSep = false;
		boolean comecaEmSep = false;

		List content = alt.getContent();
		for(Object obj : content){
			
			//System.out.println(obj);
			
			if(obj instanceof Text){

				String text = ((Text)obj).getTextTrim();
				int index = text.indexOf(_tagBase.getAltSep());

				if(index < 0) {
					currAlternative.add(text);

				} else {

					String[] partes = text.split(_tagBase.getAltSepRegex());
					
					terminaEmSep = text.trim().endsWith(_tagBase.getAltSep());
					comecaEmSep = text.trim().startsWith(_tagBase.getAltSep());
										
					if(comecaEmSep){
						alternatives.add(currAlternative);
						currAlternative = new LinkedList<Object>();
						if(partes.length > 1) //TODO: confirmar isto e ver se funciona para a String "|"
							currAlternative.add(partes[1].trim());
					
					} else if(terminaEmSep){
						currAlternative.add(partes[0].trim());
						alternatives.add(currAlternative);
						currAlternative = new LinkedList<Object>();
						
					} else {
						
						int i;
						for(i = 0; i < partes.length - 1; i++){
							currAlternative.add(partes[0].trim());
							alternatives.add(currAlternative);
							currAlternative = new LinkedList<Object>();
						}
						currAlternative.add(partes[i].trim());	
					}
				}

				//EM
			} else if(obj instanceof Element &&
					((Element)obj).getQualifiedName().equals(_tagBase.getSimpleEntityTag())){

				currAlternative.add(new NamedEntity((Element)obj));
			
				//TODO: E' possivel ALTs encaixados?? - NAO!!
			} else if (obj instanceof Element) {
			
				Element el = (Element)obj;
				
				if(el.getQualifiedName().equals(_tagBase.getAltTag())){
					buildAlternatives(el);
				} else {
					System.out.println("Erro: Outro tipo de elemento! "
						+el.getQualifiedName());
					System.exit(0);
				}
				
			} else {
				System.out.println("Erro: Outro tipo! "+obj.getClass());
				System.exit(1);
			}			
		}

		alternatives.add(currAlternative);
		//System.out.println("alternatives -> "+alternatives);
		return alternatives;
	}

	public Element getElement(){
		return _altElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String string = getOpeningTag();
		Iterator i = _alternatives.iterator();
		Iterator j;

		string += getAltItemString((LinkedList) i.next());
		while (i.hasNext())
		{
			string += "|" + getAltItemString((LinkedList) i.next());
		}

		string += getClosingTag();

		return string;
	}

	//TODO: alterar
	public Object clone()
	{
		ALTEntity clone = new ALTEntity();
		LinkedList cloneAlternative;
		LinkedList alternative;
		Object next;
		Iterator i, j;

		for (i = _alternatives.iterator(); i.hasNext();)
		{
			alternative = (LinkedList) i.next();
			cloneAlternative = new LinkedList();

			for (j = alternative.iterator(); j.hasNext();)
			{
				next = j.next();
				if (next instanceof String)
				{
					cloneAlternative.add(next.toString());
				}
				else
				{
					cloneAlternative.add(((NamedEntity) next).clone());
				}
			}
			clone._alternatives.add(cloneAlternative);
			cloneAlternative = null;
		}

		return clone;
	}

	protected LinkedList split(Parser parser)
	{
		LinkedList toReturn = new LinkedList();
		LinkedHashSet tokens = new LinkedHashSet();
		LinkedList alternative;
		Iterator i, j;
		Object next;

		for (i = getAlternativesIterator(); i.hasNext();)
		{
			alternative = (LinkedList) i.next();
			for (j = alternative.iterator(); j.hasNext();)
			{
				next = j.next();
				if (next instanceof NamedEntity)
				{
					tokens.addAll(((NamedEntity) next).split(parser));
				}
			}
		}

		toReturn.addAll(tokens);
		return toReturn;
	}

	public Set getMarkableTokens()
	{
		HashSet tokens = new HashSet();
		LinkedList alternative;
		Iterator i, j;
		Object o;

		for (j = getAlternativesIterator(); j.hasNext();)
		{
			alternative = (LinkedList) j.next();

			for (i = alternative.iterator(); i.hasNext();)
			{
				o = i.next();
				if (o instanceof NamedEntity)
				{
					tokens.addAll(((NamedEntity) o).getMarkableTokens());
				}
			}
		}

		return tokens;
	}

	protected HaremEntity unmarkTokens()
	{
		ALTEntity clone = new ALTEntity();
		LinkedList currentAlternative;
		LinkedList alternativeClone;
		Object current;
		Iterator i, j;

		for (i = _alternatives.iterator(); i.hasNext();)
		{
			currentAlternative = (LinkedList) i.next();
			alternativeClone = new LinkedList();
			for (j = currentAlternative.iterator(); j.hasNext();)
			{
				current = j.next();
				if (current instanceof NamedEntity)
				{
					alternativeClone.add(((NamedEntity) current).unmarkTokens());
					continue;
				}

				alternativeClone.add(current);
			}
			clone.addAlternative(alternativeClone);
		}

		return clone;
	}

	protected HaremEntity markTokens(HashMap markable)
	{
		ALTEntity clone = new ALTEntity();
		LinkedList cloneAlternative;
		LinkedList alternative;
		LinkedList tokens;
		HashMap mapCounterClone = null;
		AtomicCounter counter;
		Iterator i, j;
		Object next;

		for (i = getAlternativesIterator(); i.hasNext();)
		{			
			alternative = (LinkedList) i.next();
			cloneAlternative = new LinkedList();
			mapCounterClone = cloneCounterMap(markable);
			for (j = alternative.iterator(); j.hasNext();)
			{
				next = j.next();
				if (next instanceof String)
				{
					tokens = TaggedDocument.tokenize((String) next);
					while (!tokens.isEmpty())
					{
						next = (String) tokens.removeFirst();
						if ((counter = (AtomicCounter) mapCounterClone.get(((String) next).toUpperCase())) != null)// **
						{
							next = _tagBase.openTag("" + counter.increment()) + next
							+ _tagBase.closeTag("" + counter.getValue());
							//counter.increment();
						}
						cloneAlternative.add(next);
					}
				}
				else
				{
					cloneAlternative.add(((NamedEntity) next).markTokens(mapCounterClone));
				}
			}
			clone.addAlternative(cloneAlternative);
		}

		// parameter was passed by reference, update changes
		markable.putAll(mapCounterClone);
		return clone;
	}

	private HashMap cloneCounterMap(HashMap markable)
	{
		HashMap clone = new HashMap();
		AtomicCounter counter;
		String key;

		for (Iterator i = markable.keySet().iterator(); i.hasNext();)
		{
			key = (String) i.next();
			counter = (AtomicCounter) markable.get(key);
			clone.put(key, counter.clone());
		}

		return clone;
	}

	private String getAltItemString(LinkedList item)
	{
		String string = "";

		for (Iterator i = item.iterator(); i.hasNext();)
		{
			string += i.next().toString();
		}

		return string;
	}

	@Override
	protected HaremEntity markEntity(int tag) {
		
		Element element = getElement();
		List content = element.getContent(new ElementFilter(_tagBase.getSimpleEntityTag()));
		
		for(int i = 0; i < content.size(); i++){
			Object obj = content.get(i);
			NamedEntity ne = new NamedEntity((Element)obj);
			NamedEntity marked = (NamedEntity)ne.markEntity(tag);
			element.setContent(i, marked.getElement());
		}
		
		return new ALTEntity(element);
	}

	public void setOmitted(boolean b){
		if(b)
			_altElement.setAttribute(_tagBase.getOmittedTag(), _tagBase.getAttributeTrue());
		else _altElement.setAttribute(_tagBase.getOmittedTag(), _tagBase.getAttributeFalse());
	}
	
	public boolean isOmitted(){
		
		String value = _altElement.getAttributeValue(_tagBase.getOmittedTag());
		if(value == null)
			return false;
		else
			return value.equals(_tagBase.getAttributeTrue());
	}

}