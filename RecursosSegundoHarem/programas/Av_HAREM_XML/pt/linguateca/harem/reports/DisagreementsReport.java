package pt.linguateca.harem.reports;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import pt.linguateca.harem.ALTEntity;
import pt.linguateca.harem.DocumentReader;
import pt.linguateca.harem.HaremEntity;
import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.NamedEntityTagParser;
import pt.linguateca.harem.TagBase;
import pt.linguateca.harem.TaggedDocument;

/**
 * Relatorio de dúvidas e discordâncias
 * Tem como entrada uma CD
 * @author bESUGO
 *
 */
public class DisagreementsReport {

	private DocumentReader _reader;
	private LinkedHashMap<String, HashMap<String, LinkedList<NamedEntity>>> _comments;
	private LinkedList<String> _omitted;
	private HashMap<String, Integer> _categories;

	private String[] _commentFilter;
	private boolean _showLines;
	private boolean _showHeaders;
	private boolean _showOmitted;

	private static final String SEM_CATEG = "EM";
	private static final String ALL_COMMENTS = "todos";

	public DisagreementsReport(String file, String commentFilter, boolean showLines, boolean showHeaders, boolean showOmitted){

		_reader = new DocumentReader(file);
		_comments = new LinkedHashMap<String, HashMap<String,LinkedList<NamedEntity>>>();
		_omitted = new LinkedList<String>();
		_categories = new HashMap<String, Integer>();

		_commentFilter = (commentFilter != null ? commentFilter.split(";") : null);

		_showLines = showLines;
		_showHeaders = showHeaders;
		_showOmitted = showOmitted;

		report();
	}

	private void report(){

		HaremEntity entity;
		TaggedDocument currentDoc;

		NamedEntityTagParser parser = new NamedEntityTagParser();

		while((currentDoc = _reader.getNextDocument()) != null){

			parser.setDocument(currentDoc.getDocument());	

			if(_showOmitted)
				updateOmitted(parser.publicGetOmitted());

			while(parser.recognize()){

				entity = parser.getEntity();

				if(entity instanceof NamedEntity){
					NamedEntity ne = (NamedEntity)entity;
					//System.out.println(ne);

					updateCategories(ne);
					updateComments(ne);

				} else if(entity instanceof ALTEntity){
					ALTEntity alt = (ALTEntity)entity;
					LinkedList<NamedEntity> entities = alt.getAllEntities();

					Iterator<NamedEntity> it_entities = entities.iterator();
					while(it_entities.hasNext()){

						NamedEntity ne = it_entities.next();
						updateCategories(ne);
						updateComments(ne);
					}					
				}
				//System.out.println(_table);
			}
		}
	}

	private void updateCategories(NamedEntity entity)
	{
		LinkedList<String> categories = entity.getCategories();

		if(categories.isEmpty())
		{
			if(!entity.isOmitted())
			{
				categories.add(SEM_CATEG);
			}
			else return;
		}

		String key = getCompatibleKey(categories, _categories.keySet());
		if(key == null)
		{
			key = NamedEntity.vagueValuesToString(categories);
			_categories.put(key, 0);
			//System.out.println("---> "+key+", "+entity);
		}

		/*if(categories.contains("PESSOA"))
			System.out.println(NamedEntity.vagueValuesToString(categories)+" -- "+key);*/

		_categories.put(key, _categories.get(key)+1);

		//categorias separadas
		/*LinkedList<String> done = new LinkedList<String>();
		for(String c : entity.getCategories())
		{
			if(!_categories.containsKey(c))
				_categories.put(c, 0);

			if(!done.contains(c))
			{
				done.add(c);
				_categories.put(c, _categories.get(c)+1);
			}
		}*/
	}

	private void updateOmitted(Set<Element> omitted)
	{
		NamedEntity entity;
		for(Element el : omitted)
		{
			/*if(el.getQualifiedName().equals(TagBase.getInstance().getAltTag()))
			{
				toAdd = new ALTEntity(el).toString();
			}
			else
			{
				toAdd = new NamedEntity(el).toString2();
			}*/

			Iterator descendants = el.getDescendants(new ElementFilter(TagBase.getInstance().getSimpleEntityTag()));

			while(descendants.hasNext())
			{
				entity = new NamedEntity((Element)descendants.next());

				if(filter(entity.getComments()) != null)
					continue;

				_omitted.add(entity.toString()
						//.replace("<EM>", "").replace("</EM>", "")
						//.substring("<EM>".length(), toAdd.length() - "</EM>".length())
				);
			}
		}
	}

	private void updateComments(NamedEntity entity)
	{
		String comments;

		if((comments = filter(entity.getComments())) == null)
			return;		

		if(!_comments.containsKey(comments))
			_comments.put(comments, new HashMap<String, LinkedList<NamedEntity>>());

		if(entity.getCategories().isEmpty())
			entity.getCategories().add(SEM_CATEG);

		//String key = getCompatibleKey(entity.getCategories(), _comments.get(comments).keySet());
		for(String c : entity.getCategories())
		{
			if(!_comments.get(comments).containsKey(c))
				_comments.get(comments).put(c, new LinkedList<NamedEntity>());

			_comments.get(comments).get(c).add(entity);
		}
	}

	/** para LOCAL|COISA ser igual a COISA|LOCAL **/
	private String getCompatibleKey(LinkedList<String> categories, Set<String> keys)
	{
		LinkedList<String> tmp;	
		for(String key : keys)
		{
			tmp = NamedEntity.attributeToList(key, TagBase.getInstance().getVagueSepRegex(), true);
			if(getDifference(categories, tmp).isEmpty())
				return key;
		}
		return null;
	}

	/** diferenca entre duas listas **/
	private LinkedList<String> getDifference(LinkedList<String> list1, LinkedList<String> list2)
	{		
		LinkedList<String> difference = new LinkedList<String>();
		String current;

		if (list1 == null && list2 == null)
		{
			return difference;
		}

		if (list1 == null)
		{
			return difference;
		}

		if (list2 == null)
		{
			difference.addAll(list1);
			return difference;
		}

		for (Iterator<String> i = list1.iterator(); i.hasNext();)
		{
			current = i.next();
			if (!list2.contains(current))
			{
				difference.add(current);
			}
		}

		for (Iterator<String> i = list2.iterator(); i.hasNext();)
		{
			current = i.next();
			if (!list1.contains(current) && !difference.contains(current))
			{
				difference.add(current);
			}
		}

		return difference;
	}

	private String filter(String comments)
	{
		if(comments == null || _commentFilter == null)
			return null;

		for(String s : _commentFilter)
		{
			if(s.equals(ALL_COMMENTS))
				return s;

			if(comments.contains(s))
				return s;
		}

		return null;
	}

	public String toString(){

		//List<String> orderedKeys;
		//int size;

		String toReturn = "Comentarios:";

		for(String cat : sortStringIntMap(_categories))
		{
			toReturn += "\n\t"+cat+"&"+_categories.get(cat);

			for(String com : _comments.keySet())
			{
				if(!_comments.get(com).containsKey(cat))
				{
					toReturn += cat.contains(TagBase.getInstance().getVagueSep()) ?
							"&-" : "&0";
				}
				else
					toReturn += "&"+_comments.get(com).get(cat).size();
			}		
			toReturn += "\\tabularnewline";
		}


		/*for(String comment : _comments.keySet())
		{
			if(_showHeaders)
				toReturn += "\n"+comment+" = "+getCommentedNEs(comment);

			orderedKeys = sortStringNEMap(_comments.get(comment));

			//for(String cat : _comments.get(comment).keySet())
			for(String cat : orderedKeys)
			{
				if(_showHeaders)
				{
					toReturn += "\n\t"+comment+"&";
					toReturn += cat.replace("|", "$|$")+"&";

					size = _comments.get(comment).get(cat).size();
					toReturn += size+"&";

					for(String ic : NamedEntity.attributeToList(cat, TagBase.getInstance().getVagueSepRegex(), true))
					{
						toReturn += ic+"("+((float)size/(float)_categories.get(ic))*1000+");";
					}

					toReturn += "\\tabularnewline";
				}

				if(_showLines)
				{
					for(NamedEntity ne : _comments.get(comment).get(cat))
					{
						toReturn += "\n\t\t"+comment+"&";
						toReturn += cat.replace("|", "$|$");
						toReturn += "&"+ne.getEntity()+"\\tabularnewline";
					}
				}
			}
		}*/

		if(_showOmitted)
		{
			toReturn += "\nOmitidos: ";
			toReturn += "\nTotal = "+_omitted.size();
			for(String o : _omitted)
			{
				toReturn += "\n\t"+o;
			}
		}

		return toReturn;
	}

	public int getCommentedNEs(String comment)
	{
		int count = 0;
		LinkedList<NamedEntity> done = new LinkedList<NamedEntity>();
		
		for(String cat : _comments.keySet())
		{
			for(String com : _comments.get(cat).keySet())
			{
				for(NamedEntity ne : _comments.get(cat).get(com))
				{
					if(ne.getComments().contains(comment) && !done.contains(ne))
					{
						count++;
						done.add(ne);
					}
					
				}
			}
		}
		return count;
	}



	/*private HashMap<String, Integer> getCommentedCategoriesDistribution(LinkedList<NamedEntity> entities)
	{
		HashMap<String, Integer> categories = new HashMap<String, Integer>();
		int count;
		String catValue;

		for(NamedEntity ne : entities)
		{
			if(ne.getCategories() == null || ne.getCategories().isEmpty())
			{
				if(!categories.containsKey(SEM_CATEG))
					categories.put(SEM_CATEG, 0);

				count = categories.get(SEM_CATEG);
				categories.put(SEM_CATEG, ++count);
			}
			else
			{
				catValue = ne.getCategories().get(0);

				for(int i = 1; i < ne.getCategories().size(); i++)
					catValue += "|"+ne.getCategories().get(i);

				if(!categories.containsKey(catValue))
					categories.put(catValue, 0);

				count = categories.get(catValue);
				categories.put(catValue, ++count);

			}
		}
		return categories;
	}*/


	public static void main(String args[]){

		String file = null;
		String commentContains = null;
		boolean showLines = false;
		boolean showHeaders = false;
		boolean showOmitted = false;

		try{

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-coleccao"))
				{
					file = args[++i];
					continue;
				}

				if (args[i].equals("-comentarios"))
				{
					commentContains = args[++i];
					continue;
				}

				if (args[i].equals("-linhas"))
				{
					showLines = true;
					continue;
				}

				if (args[i].equals("-cabecalhos"))
				{
					showHeaders = true;
					continue;
				}

				if (args[i].equals("-omitidos"))
				{
					showOmitted = true;
					continue;
				}
			}

		}
		catch (Exception e)
		{
			System.err.println(e);
			return;
		}

		DisagreementsReport report = new DisagreementsReport(file, commentContains, showLines, showHeaders, showOmitted);
		System.out.println(report.getCommentsToString());
		System.out.println("Total 2/3 = " + report.getCommentedNEs("2/3"));
	}

	public List<String> sortStringNEMap(HashMap<String, LinkedList<NamedEntity>> map) {

		List<Map.Entry<String, LinkedList<NamedEntity>>> entries = new LinkedList<Map.Entry<String,LinkedList<NamedEntity>>>();
		for(Map.Entry<String, LinkedList<NamedEntity>> entry : map.entrySet())
			entries.add(entry);


		// Sort the entries with your own comparator for the values:
		Collections.sort(entries, new CompareEntries());

		List<String> indexes = new LinkedList<String>();

		for(Map.Entry<String, LinkedList<NamedEntity>> e : entries)
			indexes.add(e.getKey());

		return indexes;
	}

	public List<String> sortStringIntMap(HashMap<String, Integer> map) {

		List<Map.Entry<String, Integer>> entries = new LinkedList<Map.Entry<String, Integer>>();
		for(Map.Entry<String, Integer> entry : map.entrySet())
			entries.add(entry);

		// Sort the entries with your own comparator for the values:
		Collections.sort(entries, new CompareEntriesInteger());

		List<String> indexes = new LinkedList<String>();

		for(Map.Entry<String, Integer> e : entries)
			indexes.add(e.getKey());

		return indexes;
	}

	public LinkedHashMap<String, HashMap<String, LinkedList<NamedEntity>>> getComments() {
		return _comments;
	}

	public String getCommentsToString()
	{
		String toReturn = "";

		for(String co : _comments.keySet())
		{
			for(String ca : _comments.get(co).keySet())
			{
				toReturn += "Categoria: "+ca+"\n";

				for(NamedEntity ne : _comments.get(co).get(ca))
					toReturn += "\t"+ne+"\n";
			}
		}

		return toReturn;
	}
}

class CompareEntries implements Comparator
{
	public int compare(Object lhs, Object rhs) {
		Map.Entry<String, LinkedList<NamedEntity>> a = (Map.Entry<String, LinkedList<NamedEntity>>)lhs;
		Map.Entry<String, LinkedList<NamedEntity>> b = (Map.Entry<String, LinkedList<NamedEntity>>)rhs;

		return b.getValue().size() - a.getValue().size();
	}
}

class CompareEntriesInteger implements Comparator
{
	public int compare(Object lhs, Object rhs) {
		Map.Entry<String, Integer> a = (Map.Entry<String, Integer>)lhs;
		Map.Entry<String, Integer> b = (Map.Entry<String, Integer>)rhs;

		return b.getValue() - a.getValue();
	}
}
