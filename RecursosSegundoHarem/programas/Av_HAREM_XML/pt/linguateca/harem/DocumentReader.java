package pt.linguateca.harem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;


/**
 * 
 * @author nseco, Besugo
 * 
 * Leitor de documentos XML com colecções HAREM
 */
public class DocumentReader
{
	private String _file;

	//private BufferedReader _reader;
	
	private Document _fileDoc;
	private Iterator _docs;
	
	private TagBase _tagBase;

	private static SAXBuilder builder;
	
	/** Creates a new instance of DocumentReader */
	public DocumentReader(String file)
	{	
		System.out.println("# "+file);
		
		if(builder == null)
			builder = new SAXBuilder(false);
		
		try
		{
			_file = file;
			//_reader = new BufferedReader(new FileReader(file));
			_tagBase = TagBase.getInstance();

			_fileDoc = builder.build(new File(file));
			_docs = _fileDoc.getRootElement().getDescendants(
					new ElementFilter(_tagBase.getDocTag()));
			
		} catch (JDOMException ex) {
			ex.printStackTrace();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public Document getFileDocument(){
		return _fileDoc;
	}
	
	public String getFile()
	{
		return _file;
	}

	public Iterator getIterator(){
		return _docs;
	}
	
	public TaggedDocument getNextDocument()
	{
		//System.out.println("-> "+_file+" "+ _docs.hasNext());
		if(_docs.hasNext()){
			
			return new TaggedDocument((Element)_docs.next());
		}
		else return null;
	}

}