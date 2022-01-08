package pt.linguateca.harem.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import pt.linguateca.harem.TagBase;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class RemoveComments extends Thread{

	//private final String FILE_OUT = "CDSegundoHAREM_TEMPO_COMENT.xml";
	private static final String FILE_OUT = "CDSegundoHAREM_ReRelEM.v11.COMENT.xml";
	private static final String CHARSET = "ISO-8859-1";

	private Document _cd;

	private TagBase _tagBase;
	
	private Set<String> _toLeave;

	public RemoveComments(String cd){
		
		_tagBase = TagBase.getInstance();
		_toLeave = new HashSet<String>();
		
		setCommentsToLeave();
		
		SAXBuilder builder = new SAXBuilder();

		try {
			_cd = builder.build(cd);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.start();
	}

	private void setCommentsToLeave()
	{
		_toLeave.add("2/3");
		_toLeave.add("DUVIDATEMPO");
		
		_toLeave.add("INDEP");
		_toLeave.add("futuro");
	}
	
	public void run()
	{
		ElementFilter neFilter = new ElementFilter(_tagBase.getSimpleEntityTag());
		Iterator i = _cd.getDescendants(neFilter);
		
		Element tmp;
		boolean done;
		
		while(i.hasNext()){

			tmp = (Element)i.next();
			done = false;
			
			if(tmp.getAttribute(_tagBase.getComentAt()) != null)
			{
				for(String comment : _toLeave)
				{
					//System.err.println(tmp.getAttributeValue(_tagBase.getComentAt()));
					if(tmp.getAttributeValue(_tagBase.getComentAt()).contains(comment))
					{
						tmp.setAttribute(_tagBase.getComentAt(), comment);
						done = true;
						break;
					}
				}
				
				if(!done)
					tmp.removeAttribute(_tagBase.getComentAt());
			}
		}

		System.out.print(output(_cd));
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
		
		if(args.length == 0)
			System.err.println("Utilizacao:\n" +
					"java RemoveComments <coleccao_original.xml>");
		else
		{
			try {
				System.setOut(new PrintStream(FILE_OUT));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new RemoveComments(args[0]);
		}
	}
}
