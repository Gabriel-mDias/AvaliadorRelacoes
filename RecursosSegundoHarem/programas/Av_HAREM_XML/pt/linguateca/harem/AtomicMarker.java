package pt.linguateca.harem;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.jdom.output.XMLOutputter;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import pt.linguateca.util.AtomicCounter;

public class AtomicMarker {

	//private String _string;
	private Element _element;
	private HashMap<String, AtomicCounter> _map;
	private TagBase _tagBase;
	
	private int lastIndex = 0;
	
	public AtomicMarker(Element el, HashMap<String, AtomicCounter> counterMap){
		//_string = str;
		_element = el;
		_map = counterMap;
		_tagBase = TagBase.getInstance();
	}
	
	public Element getDocElement(){
		return _element;
	}
	
	public void markTokens(){
		markTokens(_element);
		
		//printElement();
	}
	
	private void markTokens(Element el){

		List content = el.getContent();

		for(Object obj : content){

			if(obj instanceof Element){
				Element element = (Element)obj;
				
				if(element.getQualifiedName().equals(_tagBase.getAltTag()))
					markAlt(element);
				else
					markTokens(element);

			} else if(obj instanceof Text) {

				Text text = (Text) obj;
				String txt = text.getText();
				lastIndex = 0;
				AtomicCounter counter;
				LinkedList<String> tokens = TaggedDocument.tokenize(txt);

				String newText = text.getText();
				//System.out.println("TOKENS= "+tokens);
				for(String token : tokens){

					if ((counter = _map.get(token.toUpperCase())) != null){

						//counter.increment();
						String marked = _tagBase.openTag("" + counter.increment()) + token
						+ _tagBase.closeTag("" + counter.getValue());

						//System.out.println("1:: "+newText+" -> "+token);
						newText = replaceFirstUnmarked(token, marked, newText);
						//System.out.println("2:: "+newText);

					}
				}
				text.setText(newText);
			}
		}
	}

	private String replaceFirstUnmarked(String a, String b, String str){

		String part1 = str.substring(0, lastIndex);
		String part2 = str.substring(lastIndex);
		
		//System.out.println("1 ->"+part1);
		//System.out.println("2 ->"+part2);
		
		int index = part2.indexOf(a);
		if(index < 0)
			return str;
		else
			lastIndex += index+b.length();
		
		return part1 + part2.replaceFirst(a, b);
	}
	
	private void markAlt(Element el){
		
		ALTEntity alt = new ALTEntity(el);
		HaremEntity marked = alt.markTokens(_map);
		
		//passado por referencia
		el.removeContent();
		//Content tmp = marked.getElement().detach();
		
		List content = marked.getElement().getContent();
		for(Object obj : content){
			Content next = (Content)obj;
			el.addContent((Content)next.clone());
		}
		
			
		//el.setContent(tmp);
		//el = marked.getElement();
		
		//System.out.println("~~~~~~ "+el.getContent());
	}
	
	//para debug
	private void printElementEntities(Element el){

		ElementFilter filter = new ElementFilter(_tagBase.getSimpleEntityTag());
		List content = el.getContent(filter);
		for(Object obj : content)
			System.out.println("~~~~~~~~~~~~~~~ "+((Element)obj).getText());
	}
	
	private void printElement(){
		
		System.err.println("Element ---------------------");
		XMLOutputter out = new XMLOutputter();
		ByteOutputStream bos = new ByteOutputStream();
				
		try {
			out.output(_element, bos);
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println(bos.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
	}
}
