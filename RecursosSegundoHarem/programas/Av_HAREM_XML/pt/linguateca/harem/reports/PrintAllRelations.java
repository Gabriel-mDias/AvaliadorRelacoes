package pt.linguateca.harem.reports;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

import pt.linguateca.harem.ALTEntity;
import pt.linguateca.harem.DocumentReader;
import pt.linguateca.harem.EntitiesAttributesFilter;
import pt.linguateca.harem.HaremEntity;
import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.NamedEntityTagParser;
import pt.linguateca.harem.TagBase;
import pt.linguateca.relations.RelatedEntity;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationProcessor;
import pt.linguateca.relations.RelationsList;

//relacoes numa CD para um ficheiro CSV
public class PrintAllRelations extends Thread{

	private static final String SEP = ",";

	private DocumentReader _reader;
	private TagBase _tagBase;
	private Set<String> _filterTypes;
	private boolean _leaveTypesInFilter = false;
	private boolean _gcType;

	public PrintAllRelations(String file, String filter, boolean leave, boolean gctype){

		_tagBase = TagBase.getInstance();
		_reader = new DocumentReader(file);

		if(filter.length() != 0)
			_filterTypes = new HashSet<String>(Arrays.asList(filter.split(SEP)));
		else
			_filterTypes = null;

		_gcType = gctype;
		_leaveTypesInFilter = leave;
	}

	public void run()
	{		
		Element doc;
		NamedEntityTagParser parser = new NamedEntityTagParser();
		Iterator it = _reader.getIterator();
		HaremEntity current;
		RelatedEntity related;
		LinkedHashMap<String, RelatedEntity> map = null;
		//LinkedHashMap<String, RelatedEntity> map = new LinkedHashMap<String, RelatedEntity>();
		EntitiesAttributesFilter filter = new EntitiesAttributesFilter(TagBase.getInstance().getEntitiesAttributesTree(), true);


		while(it.hasNext())
		{
			doc = (Element)it.next();
			parser.setDocument(doc);
			map = new LinkedHashMap<String, RelatedEntity>();
			while(parser.recognize()){

				current = parser.getEntity();

				if(current instanceof NamedEntity){

					related = new RelatedEntity((NamedEntity)current, !_gcType, filter);
					map.put(related.getId(), related);


				} else if(current instanceof ALTEntity){
					ALTEntity alt = (ALTEntity)current;
					LinkedList<NamedEntity> entities = alt.getAllEntities();

					Iterator<NamedEntity> it_entities = entities.iterator();
					while(it_entities.hasNext()){

						related = new RelatedEntity(it_entities.next(), !_gcType, filter);
						map.put(related.getId(), related);

					}					
				}
			}

			printRelations(doc.getAttributeValue(_tagBase.getDocIDTag()), map);
		}
	}

	private void printRelations(String docid, Map<String, RelatedEntity> map)
	{
		System.out.println();
		printLine("DOC", docid, "", "", "", "", "");
		printHeader();

		for(RelatedEntity entity : map.values())
		{
			for(RelationsList list : entity.getRelationsLists().values())
			{
				for(Relation r : list)
				{	
					if(_filterTypes == null ||
							(_leaveTypesInFilter && _filterTypes.contains(r.getType())) ||
							(!_leaveTypesInFilter && !_filterTypes.contains(r.getType())))
					{
						printLine(
								map.get(getOnlyId(r.getA())).getEntity(),
								r.getType(),
								map.get(getOnlyId(r.getB())).getEntity(),
								r.getA(),
								reformatCategories(map.get(getOnlyId(r.getA())).getCategories()),
								r.getB(),
								reformatCategories(map.get(getOnlyId(r.getB())).getCategories())
						);
					}
				}
			}
		}
	}

	private String getOnlyId(String idAndFacet)
	{
		return idAndFacet.split(RelationProcessor.PARTS_SEP)[0];
	}
	
	private String reformatCategories(LinkedList<String> categories)
	{
		return categories.toString().replace(", ", "|");
	}

	/*private String getIdPlusEntity(String id, Map<String, RelatedEntity> map)
	{
		return id+"_"+map.get(id).getEntity();
	}*/

	private void printHeader()
	{
		printLine("EM1", "TIPO", "EM2", "ID1", "CATEG1", "ID2", "CATEG2");
	}

	private void printLine(String col1, String col2, String col3, String col4, String col5, String col6, String col7)
	{
		System.out.println(col1+SEP+col2+SEP+col3+SEP+col4+SEP+col5+SEP+col6+SEP+col7);
	}

	public static void main(String args[]){

		String file = null;
		String filter = "";
		boolean leave = true;
		boolean gctype = true;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-coleccao"))
			{
				file = args[++i];
				continue;
			}

			if (args[i].equals("-deixa"))
			{
				filter = args[++i];
				leave = true;
				continue;
			}

			if (args[i].equals("-retira"))
			{
				filter = args[++i];
				leave = false;
				continue;
			}

			if (args[i].equals("-tipo_part"))
			{
				filter = args[++i];
				gctype = false;
				continue;
			}
		}

		if(file == null)
			System.err.println("Utilizacao:\n" +
			"java PrintAllRelations [-deixa tiporel1;tiporel2] [-retira tiporel3] [-tipo_part] <coleccao.xml>");
		else
			new PrintAllRelations(file, filter, leave, gctype).start();
	}
}
