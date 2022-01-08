package pt.linguateca.harem.files;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import pt.linguateca.harem.TagBase;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class NormalizeDocIds extends Thread{

	private final String CHARSET = "ISO-8859-1";

	private Document _ch;
	private Document _sub;

	private TagBase _tagBase;

	public NormalizeDocIds(String ch, String sub){

		_tagBase = TagBase.getInstance();
		SAXBuilder builder = new SAXBuilder();

		try {
			_ch = builder.build(ch);
			_sub = builder.build(sub);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(){

		ElementFilter docFilter = new ElementFilter(_tagBase.getDocTag());

		LinkedList<String> docids = getAttributes(_ch, docFilter, _tagBase.getDocIDTag());
		Iterator i = _sub.getDescendants(docFilter);
		Iterator<String> j = docids.iterator();
		Element tmp;

		while(i.hasNext()){

			tmp = (Element)i.next();
			tmp.setAttribute(_tagBase.getDocIDTag(), j.next());
		}

		System.out.println(output(_sub));
	}

	private LinkedList<String> getAttributes(Document doc, ElementFilter filter, String attribute){

		LinkedList<String> toReturn = new LinkedList<String>();
		Iterator i = doc.getDescendants(filter);
		
		while(i.hasNext()){
			toReturn.add(((Element)i.next()).getAttributeValue(attribute));
		}
		
		return toReturn;
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

	public static void main(String args[]){
		
		if(args.length != 2)
			System.err.println("Utilizacao:\n" +
					"java NormalizeDocIds <coleccao_original.xml> <coleccao_eviada.xml");
		else
			new NormalizeDocIds(args[0], args[1]).start();
	}
}
