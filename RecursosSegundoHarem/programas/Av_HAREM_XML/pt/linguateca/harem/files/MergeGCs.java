package pt.linguateca.harem.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import pt.linguateca.harem.TagBase;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
 * 
 * @author Besugo
 *
 * Junta os atributos do TEMPO às EMs temporais da CD ReRelEM
 */
public class MergeGCs extends Thread{

	private final String CHARSET = "ISO-8859-1";

	private Document _toGrow;
	private Document _other;
	
	private TagBase _tagBase;
	
	private HashMap<String, Element> _entitiesToGrow;
	private HashMap<String, Element> _otherEntities;
	
	public MergeGCs(String cd1, String cd2)
	{
		_tagBase = TagBase.getInstance();
		SAXBuilder builder = new SAXBuilder();

		try {
			_toGrow = builder.build(cd1);
			_other = builder.build(cd2);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_entitiesToGrow = new HashMap<String, Element>();
		_otherEntities = new HashMap<String, Element>();
		
		this.start();
	}
	
	public void run()
	{
		fillMap(_toGrow, _entitiesToGrow);
		fillMap(_other, _otherEntities);
		
		for(String key : _otherEntities.keySet())
		{
			if(_entitiesToGrow.containsKey(key))
			{
				merge(_entitiesToGrow.get(key), _otherEntities.get(key));
			}
		}
		
		System.out.println(output(_toGrow));
	}
	
	private void fillMap(Document doc, HashMap<String, Element> map)
	{
		ElementFilter neFilter = new ElementFilter(_tagBase.getSimpleEntityTag());
		
		Element tmp;
		Iterator i = doc.getDescendants(neFilter);		
		while(i.hasNext())
		{
			tmp = (Element)i.next();
			map.put(tmp.getAttributeValue(_tagBase.getIdAt()), tmp);
		}
	}
	
	private void merge(Element toGrow, Element other)
	{
		LinkedList<Attribute> toAdd = new LinkedList<Attribute>();
		Attribute at1, at2;
		boolean containsAttribute;	
		for(Object obj1 : other.getAttributes())
		{
			at1 = (Attribute)obj1;
			containsAttribute = false;
			for(Object obj2 : toGrow.getAttributes())
			{
				at2 = (Attribute)obj2;
				if(at1.getQualifiedName().equals(at2.getQualifiedName()))
				{
					containsAttribute = true;
					break;
				}
			}
			
			if(!containsAttribute)
			{
				toAdd.add(at1);
			}
		}
		
		for(Attribute at : toAdd)
		{
			toGrow.setAttribute(at.detach());
		}
	}
	
	private String output(Document doc){
		Format format = Format.getRawFormat().setEncoding(CHARSET);
		XMLOutputter out = new XMLOutputter(format);
		ByteOutputStream bos = new ByteOutputStream();

		try {
			out.output(doc, bos);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toString();
	}
	
	public static void main(String args[])
	{
		try {
			System.setOut(new PrintStream("saida.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new MergeGCs(args[0], args[1]);
	}
}
