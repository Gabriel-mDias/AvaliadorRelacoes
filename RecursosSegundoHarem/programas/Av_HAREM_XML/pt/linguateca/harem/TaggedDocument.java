package pt.linguateca.harem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import pt.linguateca.util.AtomicCounter;
import pt.linguateca.util.LinkedBucketHashMap;

public class TaggedDocument
{
	//private StopWordList _stopWords;

	private TagBase _tagBase;

	private Element _doc;

	//public boolean SUB_ALT = false;

	private static XMLOutputter out;

	public static final String SUBMISSION_ALT = "ALT na \"submissao\" (DOC seguinte);";

	public static LinkedList<String> tokenize(String string)
	{
		LinkedList<String> tokens = new LinkedList<String>();
		String current = "";
		char currentChar;
		char previousChar = '\0';

		for (int i = 0; i < string.length(); i++)
		{
			currentChar = string.charAt(i);
			if (isTokenDelimiter(previousChar, currentChar))
			{
				tokens.add(current);
				if (!Character.isLetterOrDigit(currentChar))
				{
					tokens.add(currentChar + "");
					current = "";
				}
				else
				{
					current = "" + currentChar;
				}
				previousChar = currentChar;
				continue;
			}

			current += currentChar;
			previousChar = currentChar;
		}

		if (!current.equals(""))
		{
			tokens.add(current);
		}

		return tokens;
	}

	public static boolean isMarkable(String token)
	{
		if (token.equals("") || StopWordList.getInstance().contains(token))
		{
			return false;
		}

		return true;
	}

	public static boolean isTokenDelimiter(char previousChar, char currentChar)// 
	{
		return !Character.isLetterOrDigit(currentChar)
		|| (Character.isLetter(currentChar) && Character.isDigit(previousChar))
		|| (Character.isDigit(currentChar) && Character.isLetter(previousChar))
		|| (Character.isDigit(currentChar) && Character.isDigit(previousChar));
	}

	/** Creates a new instance of Document */
	public TaggedDocument()
	{
		_tagBase = TagBase.getInstance();
	}

	public TaggedDocument(Element el)
	{
		_tagBase = TagBase.getInstance();
		_doc = el;

		if(out == null){
			out = new XMLOutputter();
			out.setFormat(Format.getRawFormat().setEncoding("ISO-8859-1"));
		}
	}

	public String getID()
	{
		return _doc.getAttributeValue(_tagBase.getDocIDTag());
	}

	public String getText()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			out.outputElementContent(_doc, bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toString();
	}

	public Element getDocument(){
		return _doc;
	}

	/*	public void appendDocText(String text)
	{
		if (getText() == null)
		{
			getText() = text;
			return;
		}

		getText() += text;
	}*/

	public AlignmentMap alignDocument(TaggedDocument document)
	{		
		Set<String> set = getMarkableTokens();
		//System.out.println("-- "+set);

		HashMap<String, AtomicCounter> myCounterMap = getCounterMap(set);
		
		//System.out.println("CD:");
		LinkedList<HaremEntity> myMarkedEntities = getMarkedEntities(myCounterMap);

		HashMap<String, AtomicCounter> otherCounterMap = getCounterMap(set);
		//System.out.println("SUB:");
		LinkedList<HaremEntity> otherMarkedEntities = document.getMarkedEntities(otherCounterMap);

		/*System.out.println("myCounterMap= "+myCounterMap);
		System.out.println("otherCounterMap= "+otherCounterMap);*/
		
		LinkedHashMap<HaremEntity, LinkedList<String>> entityToTokensMap =
			getEntityToTokensMap(myMarkedEntities);
		LinkedBucketHashMap tokensToEntityMap = getTokensToEntityMap(otherMarkedEntities);

/*		System.out.println("entityToTokens ---------" +
				"\n"+entityToTokensMap.toString().replaceAll(", ", "\n"));
		System.out.println("tokensToEntity ---------" +
				"\n"+tokensToEntityMap.toString().replaceAll(", ", "\n"));*/

		LinkedList<String> potentialFaults = areAlignable(myCounterMap, otherCounterMap);
		//System.out.println("POTENCIAL FAULTS= "+potentialFaults);
		AlignmentMap alignments = new AlignmentMap();

		alignments.setDocID(document.getID());

		Parser parser = new CounterTagParser();

		//NamedEntity spuriousEntity;
		MarkedToken spuriousKey;

		LinkedList<String> tokens;
		Iterator i, j, k;

		HaremEntity entityToMap;
		//HaremEntity mappedEntity;
		MarkedToken token;

		String problematicToken;
		boolean aligned;

		for (i = entityToTokensMap.keySet().iterator(); i.hasNext();)
		{
			entityToMap = (HaremEntity) i.next();

			tokens = entityToTokensMap.get(entityToMap);
			aligned = false;
			for (j = tokens.iterator(); j.hasNext();)
			{
				token = (MarkedToken) j.next();				
				//mappedEntity = (HaremEntity) tokensToEntityMap.remove(token);
				
				LinkedList<HaremEntity> list = (LinkedList<HaremEntity>)tokensToEntityMap.get(token);
				if(list != null){
					for(HaremEntity mappedEntity : list){
						alignments.putAlignment(entityToMap, mappedEntity);
						aligned = true;
					}
				}
			}

			if (!aligned)
			{
				alignments.putAlignment(entityToMap, null);
				for (j = potentialFaults.iterator(); j.hasNext();)
				{
					problematicToken = (String) j.next();
					
					for (k = entityToMap.split(parser).iterator(); k.hasNext();)
					{											
						token = (MarkedToken) k.next();
						if (token.getToken().equals(problematicToken))
						{
							alignments.putFaults(entityToMap, problematicToken);
						}
					}
				}
			}
		}
		
		//verificar quais sao espurios
		for (i = tokensToEntityMap.keySet().iterator(); i.hasNext();)
		{		
			spuriousKey = (MarkedToken) i.next();
			entityToMap = new NamedEntity();// Dummy
			//spuriousEntity = (NamedEntity) tokensToEntityMap.get(spuriousKey);
			LinkedList<NamedEntity> list = (LinkedList)tokensToEntityMap.get(spuriousKey);

			for(NamedEntity spuriousTest : list){

				if(entityToTokensMap.get(spuriousTest) == null){

					if (!alignments.hasAlignment(spuriousTest))
					{
						((NamedEntity) entityToMap).setEntity(spuriousTest.getEntity());
						((NamedEntity) entityToMap).setSpurious();
						
						alignments.putAlignment(entityToMap, spuriousTest);
						for (j = potentialFaults.iterator(); j.hasNext();)
						{
							problematicToken = (String) j.next();
							for (k = entityToMap.split(parser).iterator(); k.hasNext();)
							{
								token = (MarkedToken) k.next();
								if (token.equals(problematicToken))
								{
									alignments.putFaults(entityToMap, problematicToken);
								}
							}
						}
					}
				}
			}
		}
		return alignments;
	}

	public LinkedList<HaremEntity> getEntities()
	{		
		NamedEntityTagParser nameParser = new NamedEntityTagParser(_doc);
		//ALTEntityTagParser altParser = new ALTEntityTagParser(_doc);
		LinkedList<HaremEntity> entities = new LinkedList<HaremEntity>();

		/*		while (altParser.recognize())
		{
			entities.add(altParser.getEntity());
		}*/

		while (nameParser.recognize())
		{
			entities.add(nameParser.getEntity());
		}

		return entities;
	}

	public LinkedList<HaremEntity> getMarkedEntities(HashMap<String, AtomicCounter> counterMap)
	{		
		//markTokens(_doc, counterMap);
		AtomicMarker marker = new AtomicMarker(_doc, counterMap);
		marker.markTokens();
		_doc = marker.getDocElement();

		NamedEntityTagParser nameParser = new NamedEntityTagParser(_doc);
		//ALTEntityTagParser altParser = new ALTEntityTagParser(_doc);
		LinkedList<HaremEntity> marked = new LinkedList<HaremEntity>();

		/*		while (altParser.recognize())
		{
			HaremEntity alt = altParser.getEntity();
			//marked.add(alt.markTokens(counterMap));
			marked.add(alt);
			//System.out.println("ALT: "+alt);
		}*/

		//System.out.println("nameParser.recognize()");
		while (nameParser.recognize())
		{
			//NamedEntity ne = (NamedEntity)nameParser.getEntity();
			HaremEntity ne = nameParser.getEntity();
			//marked.add(ne.markTokens(counterMap));
			marked.add(ne);
		}

		return marked;
	}

	/*	private void markTokens(Element el, HashMap counterMap){

		List content = el.getContent();

		for(Object obj : content){

			if(obj instanceof Element){
				markTokens((Element)obj, counterMap);
				//System.out.println("CONTENT = " + content);

			} else if(obj instanceof Text) {

				Text text = (Text) obj;
				AtomicCounter counter;
				LinkedList<String> tokens = tokenize(text.getText());
				String newText = text.getText();

				//System.out.println(tokens);
				for(String token : tokens){

					if ((counter = (AtomicCounter) counterMap.get(token.toUpperCase()))
							!= null){

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

		int index = str.indexOf(a);
		if(index < 0)
			return str;

		int begin = index - 1;
		int end = index + a.length();

		if(betweenTags(str, begin, end) || isTag(str, begin, end)){
			String part = str.substring(0, end);
			return part.concat(replaceFirstUnmarked(a, b, str.substring(end)));

		} else {
			return str.replaceFirst(a, b);
		}
	}

	private boolean betweenTags(String str, int begin, int end){

		if(begin < 0 || end >= str.length()){
			return false;
		}

		return ( (str.charAt(begin) == '>' && str.charAt(end) == '<'));	
	}

	private boolean isTag(String str, int begin, int end){

		if(begin < 0 || end >= str.length()){
			return false;
		}

		return ( ((str.charAt(begin) == '<' || str.charAt(begin) == '/')
				&& str.charAt(end) == '>'));	
	}*/

	public Set<String> getMarkableTokens()
	{	
		NamedEntityTagParser nameParser = new NamedEntityTagParser(_doc);
		HashSet<String> tokens = new HashSet<String>();

		while(nameParser.recognize()){
			//System.out.println(nameParser.getEntity());
			tokens.addAll((nameParser.getEntity()).getMarkableTokens());
		}

		return tokens;
	}

	public String toString()
	{
		String toString;
		toString = _tagBase.openTag(_tagBase.getDocTag()) + "\n";
		toString += _tagBase.openTag(_tagBase.getDocIDTag()) + getID() + _tagBase.closeTag(_tagBase.getDocIDTag())
		+ "\n";
		toString += _tagBase.openTag(_tagBase.getTextTag()) + "\n" + getText().trim() + "\n"
		+ _tagBase.openTag(_tagBase.getTextTag()) + "\n";
		toString += _tagBase.closeTag(_tagBase.getDocTag());
		return toString;
	}

	private HashMap<String, AtomicCounter> getCounterMap(Set<String> markable)
	{
		HashMap<String, AtomicCounter> counterMap =	new HashMap<String, AtomicCounter>();
		Iterator<String> i = markable.iterator();

		while (i.hasNext())
		{
			counterMap.put(i.next(), new AtomicCounter());
		}

		return counterMap;
	}

	private LinkedHashMap<HaremEntity, LinkedList<String>> getEntityToTokensMap(LinkedList<HaremEntity> markedEntities)
	{
		LinkedHashMap<HaremEntity, LinkedList<String>> map =
			new LinkedHashMap<HaremEntity, LinkedList<String>>();
		HaremEntity entity;
		LinkedList<String> tokens;
		CounterTagParser parser = new CounterTagParser();

		for (Iterator<HaremEntity> i = markedEntities.iterator(); i.hasNext();)
		{
			entity = i.next();

			/*if(entity instanceof ALTEntity)
				System.out.println(".... "+entity);*/

			tokens = entity.split(parser);
			map.put(entity, tokens);
		}
		return map;
	}

	private LinkedBucketHashMap getTokensToEntityMap(LinkedList<HaremEntity> markedEntities)
	{
		LinkedBucketHashMap map = new LinkedBucketHashMap(new LinkedList());
		NamedEntity entity;
		//ALTEntity alt;
		LinkedList tokens;
		MarkedToken spurious;
		Iterator<HaremEntity> i;
		Iterator j;
		int count = 0;
		CounterTagParser parser = new CounterTagParser();

		//System.out.println("MARKED = "+markedEntities);

		for (i = markedEntities.iterator(); i.hasNext();)
		{
			HaremEntity he = i.next();

			//EM simples
			if(he instanceof NamedEntity){
				entity = (NamedEntity) he;
				tokens = entity.split(parser);

				//System.out.println("TOKENS = "+tokens);

				if (tokens.isEmpty())
				{
					count++;
					spurious = new MarkedToken();
					spurious.setCount(count);
					spurious.setToken(_tagBase.getSpuriousTag());
					/*LinkedList<HaremEntity> list = new LinkedList<HaremEntity>();
					list.add(entity.markEntity(count));
					map.put(spurious, list);*/
					map.put(spurious, entity.markEntity(count));
				}
				else
				{
					for (j = tokens.iterator(); j.hasNext();)
					{
						map.put(j.next(), entity);
					}
				}

				//ALT
			} else if(he instanceof ALTEntity){
				//System.out.println(SUBMISSION_ALT);
				//alt = (ALTEntity)he;
				//System.out.println(".... "+alt);
				ALTEntity alt = ((ALTEntity)he);
				LinkedList<NamedEntity> entities = alt.getAllEntities();
				//int numAlts = alt.getAlternatives().size();
				
				for(NamedEntity ne : entities){
					tokens = ne.split(parser);

					if (tokens.isEmpty())
					{
						count++;
						spurious = new MarkedToken();
						spurious.setCount(count);
						spurious.setToken(_tagBase.getSpuriousTag());
												
						map.put(spurious, ne.markEntity(count));
					}
					else
					{
						for (j = tokens.iterator(); j.hasNext();)
						{
							//ne.setNumAltsAttribute(numAlts);
							map.put(j.next(), ne);
						}
					}
				}
			}
		}
		//System.out.println("---MAP =\n"+map);
		return map;
	}

	private LinkedList<String> areAlignable(HashMap<String, AtomicCounter> entities1, HashMap<String, AtomicCounter> entities2)
	{
		Iterator<String> i;
		String key;
		AtomicCounter counter1;
		AtomicCounter counter2;
		LinkedList<String> faults = new LinkedList<String>();

		//System.out.println("ENTITIES1 = "+entities1);
		//System.out.println("ENTITIES2 = "+entities2);
		
		for (i = entities1.keySet().iterator(); i.hasNext();)
		{
			key = i.next();
					
			counter1 = (AtomicCounter) entities1.get(key);
			counter2 = (AtomicCounter) entities2.get(key);

			if (!counter1.equals(counter2))
			{
				//System.out.println("KEY= "+key);
				//System.out.println("COUNTER1= "+counter1);
				//System.out.println("COUNTER2= "+counter2);
				faults.add(key);
			}
		}

		return faults;
	}

}