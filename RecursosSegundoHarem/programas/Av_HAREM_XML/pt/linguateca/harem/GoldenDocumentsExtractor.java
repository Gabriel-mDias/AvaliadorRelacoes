package pt.linguateca.harem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class GoldenDocumentsExtractor extends Thread{

	//private String _cdPath;
	//private String _subPath;

	private final String CHARSET = "ISO-8859-1";
	
	private TagBase _tagBase;

	private DocumentReader _cdReader;
	private DocumentReader _subReader;
	
	private Document _extracted;

	public GoldenDocumentsExtractor(String cd, String sub){

		_tagBase = TagBase.getInstance();

		_cdReader = new DocumentReader(cd);
		_subReader = new DocumentReader(sub);

		
		_extracted = (Document)_subReader.getFileDocument().clone();
		_extracted.getRootElement().removeContent();
	}

	public void run(){

		TaggedDocument submissionDoc;

		LinkedHashMap<String, TaggedDocument> goldenDocs = getNormalizedSet(_cdReader);
		LinkedHashMap<String, TaggedDocument> subDocs = getNormalizedSet(_subReader);
		
		for(TaggedDocument goldenSetDoc : goldenDocs.values())
		{
			//System.out.println(submissionDoc.getID());
			submissionDoc = subDocs.get(goldenSetDoc.getID());
			_extracted.getRootElement().addContent((Element)submissionDoc.getDocument().clone());
		}
		
		/*while ((submissionDoc = _subReader.getNextDocument()) != null)
		{
			//System.out.println(submissionDoc.getID());
			goldenSetDoc = goldenDocs.get(submissionDoc.getID());

			if (goldenSetDoc != null)
			{
				//System.out.println("---");
				_extracted.getRootElement().addContent((Element)submissionDoc.getDocument().clone());
				continue;
			}
		}*/
		
		System.out.println(output(_extracted));
	}

	private LinkedHashMap<String, TaggedDocument> getNormalizedSet(DocumentReader reader)
	{
		LinkedHashMap<String, TaggedDocument> golden = new LinkedHashMap<String, TaggedDocument>();
		TaggedDocument goldenSetDoc;

		while ((goldenSetDoc = reader.getNextDocument()) != null)
		{
			//goldenSetDoc.normalize();
			golden.put(goldenSetDoc.getID(), goldenSetDoc);
		}

		return golden;
	}

	private Document builDocument(String path){

		SAXBuilder builder = new SAXBuilder();
		
		try {
			InputStream reader = new FileInputStream(path);
			
			return builder.build(reader);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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

		/*try {
			System.setOut(new PrintStream("colHAREM-TextosCD.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		new GoldenDocumentsExtractor(args[0], args[1]).start();
	}
}
