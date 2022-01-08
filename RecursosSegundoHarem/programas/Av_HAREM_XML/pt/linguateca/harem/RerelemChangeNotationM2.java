package pt.linguateca.harem;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import pt.linguateca.relations.Relation;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
 * 
 * @author Besugo
 *
 * Conversor de notações ReRelEM
 */
public class RerelemChangeNotationM2 extends Thread{

	private static final String CHARSET = "ISO-8859-1";
	private static final String T1TOT2 = "-t1t2";
	private static final String FAC_SOURCE = TagBase.getInstance().getSourceFacetAt();
	private static final String FAC_TARGET = TagBase.getInstance().getTargetFacetAt();
	public static final String UNVAGUE_ENTITY = "NV";
	private static final String T2_SEP = "\\*\\*";
	private static final String T2_SEP_ = "**";
	private static final int THINGS_IN_TIPOREL = 4;

	private Document _doc;
	private boolean _t1ToT2 = false;
	private boolean _debug;

	private TagBase _tagBase;
	private HashMap<String, String> _idToCategory;

	public RerelemChangeNotationM2(String file, String tipo, boolean debug){

		_tagBase = TagBase.getInstance();
		SAXBuilder builder = new SAXBuilder();

		_t1ToT2 = tipo.equals(T1TOT2);
		_idToCategory = new HashMap<String, String>();

		_debug = debug;

		try {
			_doc = builder.build(file);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(){

		ElementFilter docFilter = new ElementFilter(_tagBase.getSimpleEntityTag());
		Iterator it = _doc.getDescendants(docFilter);

		if(_t1ToT2)
			allType1To2(it);
		else
		{
			Element tmp;
			while(it.hasNext())
			{
				tmp = (Element)it.next();
				_idToCategory.put(getId(tmp), getCategory(tmp));
			}
			
			it = _doc.getDescendants(docFilter);
			allType2To1(it);
		}

		if(!_debug)
			System.out.println(output(_doc));
	}

	private String getId(Element el)
	{
		return el.getAttributeValue(_tagBase.getIdAt());
	}

	private String getCategory(Element el)
	{
		String value = el.getAttributeValue(_tagBase.getCategTag());
		//remover duplicados LOCAL|LOCAL -> LOCAL
		if(value == null)
			return _tagBase.getSimpleEntityTag();

		List<String> list = Arrays.asList(value.split(_tagBase.getVagueSepRegex()));
		LinkedHashSet<String> set =	new LinkedHashSet<String>(list);

		value = "";
		for(String s : set)
			value += s + _tagBase.getVagueSep();

		return value.substring(0, value.trim().length() - 1);
	}

	private void allType1To2(Iterator it)
	{
		Element tmp;
		while(it.hasNext()){
			tmp = (Element)it.next();
			type1To2(tmp);
		}
	}

	private void type1To2(Element el)
	{
		Attribute corel, tiporel, facRight, facLeft;

		if((corel = el.getAttribute(_tagBase.getCorelAt())) == null)
			return;

		if((tiporel = el.getAttribute(_tagBase.getTipoRelAt())) == null)
		{
			String t = "";
			for(String s : corel.getValue().split(_tagBase.getCorelSep()))
				t += Relation.IDENTIDADE+_tagBase.getCorelSep();

			el.setAttribute(_tagBase.getTipoRelAt(), t);
			tiporel = el.getAttribute(_tagBase.getTipoRelAt());
		}

		//detecta quando há número diferente de COREls e TIPORELs
		if(corel.getValue().split(_tagBase.getCorelSep()).length != tiporel.getValue().split(_tagBase.getCorelSep()).length)
			System.err.println("COREL=\""+corel.getValue()+"\" TIPOREL=\""+tiporel.getValue()+"\"");

		if((facRight = el.getAttribute(FAC_TARGET)) == null)
		{
			System.err.println("EM "+ el.getAttributeValue(_tagBase.getIdAt()) +"nao tem atributo "+FAC_TARGET);
			return;
		}

		if((facLeft = el.getAttribute(FAC_SOURCE)) == null)
		{
			System.err.println("EM "+ el.getAttributeValue(_tagBase.getIdAt()) +"nao tem atributo "+FAC_TARGET);
			return;
		}

		String tiporelT2 = getTiporelT2(corel, tiporel, facRight, facLeft);
		//el.removeAttribute(_tagBase.getCorelAt());
		//COREL fica igual!
		el.removeAttribute(FAC_TARGET);
		el.removeAttribute(FAC_SOURCE);
		tiporel.setValue(tiporelT2);
	}

	private String getTiporelT2(Attribute corel, Attribute tiporel, Attribute facright, Attribute facleft)
	{
		String[] corels = corel.getValue().split(_tagBase.getCorelSep());
		String[] tiporels = tiporel.getValue().split(_tagBase.getCorelSep());
		String[] facsright = facright.getValue().split(_tagBase.getCorelSep());
		String[] facsleft = facleft.getValue().split(_tagBase.getCorelSep());

		String toReturn = "";
		for(int i = 0; i < corels.length; i++)
		{
			toReturn += facsright[i]+T2_SEP+tiporels[i]+T2_SEP+corels[i]+T2_SEP+facsleft[i];
			if(i > 0)
				toReturn += " ";
		}

		return toReturn;
	}

	private void allType2To1(Iterator it)
	{
		Element tmp;
		while(it.hasNext()){
			tmp = (Element)it.next();
			type2To1(tmp);
		}
	}

	private void type2To1(Element el)
	{		
		Attribute tiporel = el.getAttribute(_tagBase.getTipoRelAt());
		if(tiporel == null)
			return;

		//TODO: Pode ser preciso alterar algo aqui por causa dos CORELs agora estarem certos
		Attribute corel = el.getAttribute(_tagBase.getCorelAt());
		if(corel == null)
			return;

		String[] tiporels = tiporel.getValue().split(_tagBase.getCorelSep());
		String[] corels = corel.getValue().split(_tagBase.getCorelSep());

		if(_debug && corels.length != tiporels.length)
			System.err.println(elementToString(el));
		/*else
			System.out.println(corel.getValue()+" -- "+tiporel.getValue());*/

		String [] tiporelValues;
		String newCorelValue = "";
		String newTiporelValue = "";
		String facSource = "";
		String facTarget = "";
		String t;

		for(int i = 0; i < tiporels.length; i++)
		{
			t = tiporels[i];

			if(!newTiporelValue.equals(""))
			{
				newCorelValue += _tagBase.getCorelSep();
				newTiporelValue += _tagBase.getCorelSep();
				facSource += _tagBase.getCorelSep();
				facTarget += _tagBase.getCorelSep();
			}

			//System.out.println("TIPOREL="+t);
			if((tiporelValues = t.split(T2_SEP)).length >= THINGS_IN_TIPOREL)
			{
				/*if(t1)
				{
					System.err.println(tiporel.getValue());
					t1 = false;
				}
				t2 = true;*/


				//FACDIR**COREL_ID**TIPOREL**FACESQ
				facSource += tiporelValues[0];
				newCorelValue += tiporelValues[2]; 
				newTiporelValue += tiporelValues[1];
				facTarget += tiporelValues[3];

				/*for(String tv : tiporelValues)
						System.out.print(tv+", ");
					System.out.println();*/	

			} else 
			{
				/*if(t2)
				{
					System.err.println(tiporel.getValue());
					t2 = false;
				}
				t1 = true;*/

				facSource += _idToCategory.get(getId(el));
				newCorelValue += corels[i]; 
				newTiporelValue += t;
				facTarget += _idToCategory.get(corels[i]);
			}
		}

		el.getAttribute(_tagBase.getCorelAt()).setValue(newCorelValue);
		tiporel.setValue(newTiporelValue);

		el.setAttribute(FAC_SOURCE, facSource);
		el.setAttribute(FAC_TARGET, facTarget);

		//System.out.println(elementToString(el));
	}

	/*private LinkedList<String> getAttributes(Document doc, ElementFilter filter, String attribute){

		LinkedList<String> toReturn = new LinkedList<String>();
		Iterator i = doc.getDescendants(filter);

		while(i.hasNext()){
			toReturn.add(((Element)i.next()).getAttributeValue(attribute));
		}

		return toReturn;
	}*/

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

	private String elementToString(Element el)
	{
		String toReturn = el.getName();
		Attribute at;
		for(Object obj : el.getAttributes())
		{
			at = (Attribute)obj;
			toReturn += " " + at.getName()+"="+_tagBase.asQuotedType(at.getValue());
		}

		return _tagBase.openTag(toReturn);
	}

	public static void main(String args[]){

		if(args.length < 2)
			System.err.println("Utilizacao:\n" +
					"java RerelemChangeNotation <de-para> <coleccao_original.xml> [-debug]" +
					"\n\tde-para: -t1t2 ou -t2t1" +
					"\n\ttipo t1: COREL=\"X\" TIPOREL=\"rel\" "+FAC_SOURCE+"=\"CATEG1\" "+FAC_TARGET+"=\"CATEG2\"" +
					"\n\ttipo t2: TIPOREL=\"CATEG1"+T2_SEP_+"rel"+T2_SEP_+"id"+T2_SEP_+"CATEG2\"");
		else if(args.length == 3 && (args[2].equals("-debug") || args[2].equals("-depurar")))
			new RerelemChangeNotationM2(args[1], args[0], true).start();
		else
			new RerelemChangeNotationM2(args[1], args[0], false).start();
	}
}
