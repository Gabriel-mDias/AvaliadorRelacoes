package pt.linguateca.harem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * @author nseco, Besugo
 * 
 * Parser para entidades mencionadas, anotadas pela etiqueta EM.
 */
public class NamedEntityTagParser extends HaremTagParser
{

	private Element _entityElement;
	private Iterator _entities;

	//private HashMap<Element, LinkedList<LinkedList<Object>>> _alts;
	private ArrayList<Element> _alts;
	
	//fragmentos OMITIDOs
	private Set<Element> _omitted;

	public NamedEntityTagParser()
	{
		;
	}

	public NamedEntityTagParser(Element d)
	{
		setDocument(d);
	}

	public void setDocument(Element d){
		_doc = d;
		_entities = _doc.getDescendants(new ElementFilter(_tagBase.getSimpleEntityTag()));
		_alts = new ArrayList<Element>();
		_omitted = getOmitted();
		_text = textFromDoc();
	}

/*	private Element docFromText(){

		Document d = null;

		try {

			d =  builder.build(_text);

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}*/

	private String textFromDoc(){

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			out.output(_doc, os);
			return os.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected void initialize()
	{
		super.initialize();
		_entityElement = null;
		/*		_entityText = null;
		_openingCategories = null;
		_closingCategories = null;
		_types = null;
		_subtypes = null;
		_genre = null;
		_number = null;*/

		//System.out.println(textFromDoc());
		//_entities = _doc.getContent(new ElementFilter(_tagBase.getSimpleEntityTag()));
		//System.out.println(_entities);
	}

	protected boolean doRecognition()
	{
		if(!_entities.hasNext())
			return false;

		boolean ok = false;

		do{
			_entityElement = (Element)_entities.next();

			if(isOmitted(_entityElement)){
				setOmitted(_entityElement);
			}
			
			Element parent = _entityElement.getParentElement();
			if(isAlt(parent)) {

				if(isOmitted(parent)){
					setOmitted(parent);
				}
				
				_entityElement = parent;

				if(!_alts.contains(_entityElement)){			
					_alts.add(_entityElement);
					ok = true;
					break;
					
				} else continue;
				
/*				if(_alts.get(_entityElement) == null){
					//_alts.put(_entityElement, altEntity(_entityElement));
					altEntity(_entityElement);
					ok = true;
					break;

				} else continue;*/

			} else {
				ok = true;
				break;
			}

		} while(_entities.hasNext());

		return ok;
	}

	protected void createEntity()
	{
		if(isNE(_entityElement))
			_entity = new NamedEntity(_entityElement);

		else if(isAlt(_entityElement)){
			/*ALTEntity entity = new ALTEntity();
			entity.setAlternatives(_alts.get(_entityElement));
			_entity = entity;*/
			
			_entity = new ALTEntity(_entityElement);
		}
	}
	
	private Set<Element> getOmitted(){
		Iterator it = _doc.getDescendants(new ElementFilter(_tagBase.getOmittedTag()));
		Set<Element> omitted = new HashSet<Element>();
		
		//altera OMITIDO para EM, e coloca-a na lista de omitidos
		Element el;
		while(it.hasNext())
		{
			el = (Element)it.next();
			el.setName(_tagBase.getSimpleEntityTag());			
			omitted.add(el);
		}
		
		/*for(Element o : omitted)
			System.out.println("OMITTED: "+o.getText());*/
		
		return omitted;
	}
	
	public Set<Element> publicGetOmitted()
	{
		if(_omitted != null)
			return _omitted;
		else return null;
	}
	
	private boolean isOmitted(Element el){
	
		for(Element o : _omitted)
		{
			if(o.equals(el))
				return true;
			
			if(o.isAncestor(el))
				return true;
		}
		return false;
	}
	
	private void setOmitted(Element el){
		el.setAttribute(_tagBase.getOmittedTag(), _tagBase.getAttributeTrue());
	}
	
	private boolean isAlt(Element el){
		return el.getQualifiedName().equals(_tagBase.getAltTag());
	}

	private boolean isNE(Element el){
		return el.getQualifiedName().equals(_tagBase.getSimpleEntityTag());
	}
	
	/*	public static void main(String[] args)
	{
		HaremTagParser rec = new NamedEntityTagParser();

		System.out.println("True positives");

		rec.setText("<LOCAL TIPO=\"ADMINISTRATIVO\" MORF=\"M,S\"><1>Portugal</1></LOCAL>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec
		.setText("<ABSTRACCAO|ORGANIZACAO TIPO=\"marca|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x</ABSTRACCAO|ORGANIZACAO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec.setText("<ABSTRACCAO MORF=\"F,S\">xxxxx x xxxxx xxxxx x  </ABSTRACCAO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec
		.setText("<ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\">  xxxxx x xxxxx xxxxx x</ABSTRACCAO|ORGANIZACAO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec.setText("<ABSTRACCAO TIPO=\"MARCA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x</ABSTRACCAO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec
		.setText("<ABSTRACCAO|ORGANIZACAO MORF=\"F,S\" TIPO=\"MARCA|EMPRESA\">xxxxx x xx\nxxx xxxxx x</ABSTRACCAO|ORGANIZACAO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec.setText("<ABSTRACCAO>xxxxx x xxxxx xxxxx x</ABSTRACCAO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());

		rec.setText("<OMITIDO>></OMITIDO>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		System.out.println(((NamedEntity) rec.getEntity()).getMarkableTokens());

	}*/
}