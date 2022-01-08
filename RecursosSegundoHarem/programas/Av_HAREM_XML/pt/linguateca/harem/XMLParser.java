package pt.linguateca.harem;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public abstract class XMLParser
{
	protected String _text;
	protected Element _doc;

	protected static SAXBuilder builder;
	protected static XMLOutputter out;
	
	public XMLParser() {
		super();
		if(builder == null)
			builder = new SAXBuilder();
		
		if(out == null)
			out = new XMLOutputter();
	}

	public boolean recognize()
	{
		initialize();
		boolean hasMore = doRecognition();
		if (hasMore)
		{
			createEntity();
		}
		return hasMore;
	}
	
	public void setDocument(Element d)
	{
		_doc = d;			
		initialize();
	}

	protected abstract boolean doRecognition();

	protected abstract void initialize();

	protected abstract Object getEntity();

	protected abstract void createEntity();
}