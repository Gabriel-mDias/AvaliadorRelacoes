/*
 * Created on Mar 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class ALTEntityTagParser extends HaremTagParser
{
	private LinkedList<LinkedList<Object>> _alternatives;
	
	private Iterator _alts;

	public ALTEntityTagParser(Element d)
	{
		_doc = d;
		_alts = _doc.getDescendants(new ElementFilter(_tagBase.getAltTag()));
	}

	protected boolean doRecognition()
	{	
		if(!_alts.hasNext())
			return false;
		
		_alternatives = new LinkedList<LinkedList<Object>>();
		LinkedList<Object> currAlternative = new LinkedList<Object>();
		Element alt = (Element)_alts.next();
		
		List content = alt.getContent();
		for(Object obj : content){
			if(obj instanceof Text){

				String text = ((Text)obj).getTextTrim();			
				int index = text.indexOf(_tagBase.getAltSep());

				if(index < 0) {
					currAlternative.add(text);				

				} else {

					String[] partes = text.split(_tagBase.getAltSepRegex());

					//se a String for s� o separador
					if(partes.length == 0){
						
						_alternatives.add(currAlternative);
						currAlternative = new LinkedList<Object>();
					
					} else {
						
						currAlternative.add(partes[0].trim());
						for(int i = 1; i < partes.length; i++){
							_alternatives.add(currAlternative);
							currAlternative = new LinkedList<Object>();
							currAlternative.add(partes[i].trim());
						}
					}
				}

				//EM
			} else if(obj instanceof Element &&
					((Element)obj).getQualifiedName().equals(_tagBase.getSimpleEntityTag())){

				currAlternative.add(new NamedEntity((Element)obj));

			} else {
				System.out.println("Erro: Outro tipo! "+obj.getClass());
				System.exit(0);
			}			
		}

		_alternatives.add(currAlternative);
		return true;
	}

	protected void initialize()
	{
		super.initialize();
		_alternatives = null;
	}

	protected void createEntity()
	{
		ALTEntity entity = new ALTEntity();
		entity.setAlternatives(_alternatives);
		_entity = entity;
	}

/*	public static void main(String[] args)
	{
		HaremTagParser rec = new ALTEntityTagParser();
		ALTEntity ent;
		Iterator it;

		System.out.println("True positives");

		rec.setText("<ALT><OBRA TIPO=\"REPRODUZIDA\" MORF=\"F,S\">Fonte</OBRA>|Fonte</ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}

		rec
				.setText("<ALT> <ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO>|"
						+ "<ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO> </ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}

		rec
				.setText("<ALT>*** *** <ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx  x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO>|"
						+ "*** *** <ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO>** *** </ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}

		rec
				.setText("<ALT> <ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO>jhs kdjh skjh|"
						+ "aaaaaaa <ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO> llll l l l|"
						+ " <ABSTRACCAO|ORGANIZACAO TIPO=\"MARCA|EMPRESA\" MORF=\"F,S\">xxxxx x xxxxx xxxxx x </ABSTRACCAO|ORGANIZACAO>+++ ++oo o o </ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}

		rec
				.setText("<ALT><VARIADO TIPO=\"OUTRO\" MORF=\"F,P\">Honras</VARIADO> de <LOCAL TIPO=\"ADMINISTRATIVO\" MORF=\"?,S\">Cardoso</LOCAL>, de"
						+ "<LOCAL TIPO=\"ADMINISTRATIVO\" MORF=\"?,S\">Cantim</LOCAL>, de <LOCAL TIPO=\"ADMINISTRATIVO\" MORF=\"?,S\">Fonseca</LOCAL>, de"
						+ "<LOCAL TIPO=\"ADMINISTRATIVO\" MORF=\"?,S\">Paredes</LOCAL> e de <LOCAL TIPO=\"ADMINISTRATIVO\" MORF=\"?,S\">Temonde</LOCAL> ontem|<VARIADO TIPO=\"OUTRO\" MORF=\"F,P\">"
						+ "Honras de Cardoso, de Cantim, de Fonseca, de Paredes e de Temonde</VARIADO> ontem</ALT>");

		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}

		rec
				.setText("<ALT> Instituição | <ORGANIZACAO TIPO=\"ADMINISTRACAO\" MORF=\"F,S\">Instituição</ORGANIZACAO></ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}

		rec
				.setText("<ALT><OBRA TIPO=\"PUBLICACAO\" MORF=\"F,S\">Constituição de 22</OBRA>|<OBRA TIPO=\"PUBLICACAO\" MORF=\"F,S\">Constituição</OBRA> de <TEMPO TIPO=\"DATA\">22</TEMPO></ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}
		rec
				.setText("<ALT>Agrupamentos de Escuteiros|<ORGANIZACAO|PESSOA TIPO=\"SUB|GRUPOMEMBRO\" MORF=\"M,P\">Agrupamentos de Escuteiros</ORGANIZACAO|PESSOA>|Agrupamentos de <ORGANIZACAO TIPO=\"INSTITUICAO\" MORF=\"M,P\">Escuteiros</ORGANIZACAO></ALT>");
		System.out.println(rec.recognize(0));
		System.out.println(rec.getEntity());
		ent = (ALTEntity) rec.getEntity();
		it = ent.getAlternatives();
		while (it.hasNext())
		{
			System.out.println("*** ALTERNATIVE ***");
			LinkedList list = (LinkedList) it.next();
			while (!list.isEmpty())
			{
				Object o = list.removeFirst();
				System.out.println(o);
			}
		}
	}*/
}