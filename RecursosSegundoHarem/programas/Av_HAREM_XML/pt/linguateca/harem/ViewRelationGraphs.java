package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pt.linguateca.relations.RelatedEntity;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationM2;
import pt.linguateca.relations.RelationProcessor;
import pt.linguateca.relations.RelationsGraphM2;
import pt.linguateca.relations.RelationsGraphM2Drawer;
import pt.linguateca.relations.RelationsList;

/**
 * 
 * @author Besugo
 *
 * Visualizador de grafos representados em documentos ReRelEM
 */
public class ViewRelationGraphs extends HaremEvaluator implements Runnable{

	private static final String SEP = ";";
	
	private EvaluatedAlignmentProcessor _processor;
	private boolean _showGC;
	private boolean _showPart;
	
	/** documento a mostrar **/
	private String _docid;
	
	/** tipo de relacoes a mostrar **/
	private List<String> _validRelationsType;

	private HashMap<String, String> _idToEntityMap;
	
	private int _labelType;
	private boolean _alignmentsInput = false;

	public ViewRelationGraphs(String alignmentFile, boolean useTags, boolean gc, boolean part,
			boolean input, String doc, String[] relations, String mapFile, int label){
		
		super(alignmentFile, useTags);

		_processor = new IdentificationEvaluatedAlignmentProcessor();
		_showGC = gc;
		_showPart = part;
		_alignmentsInput = input;

		if(doc == null)
		{
			System.out.println("Nenhum documento especificado!");
			return;
		}
		
		_docid = doc;
		
		_validRelationsType = (relations != null ? Arrays.asList(relations) : null);
		
		_idToEntityMap = (mapFile != null ? createIdToEntityMap(mapFile) : null);
		
		_labelType = label;

		new Thread(this).start();
	}

	private HashMap<String, String> createIdToEntityMap(String mapFile)
	{
		HashMap<String, String> idToEntityMap = new HashMap<String, String>();
		
		DocumentReader reader = new DocumentReader(mapFile);
		HaremEntity entity;
		TaggedDocument currentDoc;

		NamedEntityTagParser parser = new NamedEntityTagParser();

		while((currentDoc = reader.getNextDocument()) != null){

			parser.setDocument(currentDoc.getDocument());	

			while(parser.recognize()){

				entity = parser.getEntity();

				if(entity instanceof NamedEntity){
					NamedEntity ne = (NamedEntity)entity;

					if(ne.isOmitted())
						continue;
					
					idToEntityMap.put(ne.getId(), ne.getEntity());

				} else if(entity instanceof ALTEntity){
					ALTEntity alt = (ALTEntity)entity;
					LinkedList<NamedEntity> entities = alt.getAllEntities();

					Iterator<NamedEntity> it_entities = entities.iterator();
					while(it_entities.hasNext()){

						NamedEntity ne = it_entities.next();
						if(ne.isOmitted())
							continue;
						idToEntityMap.put(ne.getId(), ne.getEntity());
					}					
				}
			}
		}
		return idToEntityMap;
	}
	
	public void run()
	{
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			if(_alignmentsInput)
				drawFromAlignmentsInput(reader);
			else
				drawFromRelationsInput(reader);

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void drawFromAlignmentsInput(BufferedReader reader) throws IOException
	{
		String buffer = null;
		IdentificationEvaluatedAlignment current;
		RelationsGraphM2 goldenGraph = null;
		RelationsGraphM2 subGraph = null;
		String title = "";
		LinkedList<IdentificationEvaluatedAlignment> alignments = null;
		EntitiesAttributesFilter filter = null;

		RelatedEntity entity;
		
		while ((buffer = reader.readLine()) != null)
		{
			if(buffer.startsWith("#"))
			{
				filter = new EntitiesAttributesFilter(buffer, _tagBase);
				System.out.println(buffer);
				continue;
			}				

			if(buffer.startsWith(_tagBase.getDocTag()))
			{
				System.out.println("\n"+buffer);
				title = buffer;
				alignments = new LinkedList<IdentificationEvaluatedAlignment>();
				goldenGraph = new RelationsGraphM2();
				subGraph = new RelationsGraphM2();

				continue;
			}

			else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
			{
				if(goldenGraph != null && _showGC){

					if(_docid.equals(title.split(" ")[1]))
						goldenGraph.draw(title+" - CD", _idToEntityMap, _labelType);
				}

				if(subGraph != null && _showPart){

					if(_docid.equals(title.split(" ")[1]))
						subGraph.draw(title+" - Part", _idToEntityMap, _labelType);
				}
				
				continue;
			}

			else if (!isEvaluatable(buffer))
			{
				continue;
			}

			current = (IdentificationEvaluatedAlignment) _processor.getEvaluatedAlignment(buffer);

			//eliminar os atributos que nao interessam...
			current.removeComments();
			alignments.add(current);
						
			if(!current.getGoldenEntity().isSpurious() && _showGC)
			{
				//System.err.println("# "+current.getGoldenEntity());
				entity = new RelatedEntity(current.getGoldenEntity(), false, filter);
				for(RelationsList relations : entity.getRelationsLists().values())
				{
					goldenGraph.addNode(relations);
					for(Relation rel : relations)
					{
						if(isValidRelationType(rel.getType()))
							goldenGraph.addRelation(rel);
					}
				}
			}

			if(!current.isNullAligned() && _showPart)
			{
				entity = new RelatedEntity(current.getFirstAlignment(), true, filter);
				for(RelationsList relations : entity.getRelationsLists().values())
				{
					subGraph.addNode(relations);
					for(Relation rel : relations)
					{
						if(isValidRelationType(rel.getType()))
							subGraph.addRelation(rel);
					}
				}
			}
			
			/*if(!current.getGoldenEntity().isSpurious())
				goldenGraph.addNode(new RelatedEntity(current.getGoldenEntity()));

			if(!current.isNullAligned()){
				Iterator<NamedEntity> i = current.getAligned();
				while(i.hasNext())
					subGraph.addNode(new RelatedEntity(i.next()));
			}*/
		}
	}

	private void drawFromRelationsInput(BufferedReader reader) throws IOException
	{
		String buffer = null;
		Relation current;
		RelationsGraphM2 goldenGraph = null;
		RelationsGraphM2 partGraph = null;
		String title = "";
		int state = -1;
		RelationProcessor processor = new RelationProcessor();

		while ((buffer = reader.readLine()) != null)
		{
			//filtros
			if(buffer.startsWith("#")){
				System.out.println(buffer);
				continue;
			}		

			if(buffer.startsWith(_tagBase.getDocTag())){
				System.out.println("\n"+buffer);
				title = buffer;
				goldenGraph = null;
				partGraph = null;
				state = 0;

				continue;
			}

			else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
			{
				if(goldenGraph != null && _showGC){
					
					if(_docid.equals(title.split(" ")[1]))
						goldenGraph.draw(title+" - CD", _idToEntityMap, _labelType);
				}

				if(partGraph != null && _showPart){

					if(_docid.equals(title.split(" ")[1]))
						partGraph.draw(title+" - Part", _idToEntityMap, _labelType);
				}
			}

			else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.GC))
			{
				goldenGraph = new RelationsGraphM2();
				state = 1;
				continue;
			}

			else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.PARTICIPATION))
			{
				partGraph = new RelationsGraphM2();
				state = 2;
				continue;
			}

			else if (buffer.trim().equals(""))
			{
				continue;
			}

			current = processor.getRelation(buffer);
			if(current instanceof RelationM2)
				current = ((RelationM2)current).getSimpleRelationWithSuperID();
			
			if(state == 1)
			{
				if(current != null && isValidRelationType(current.getType()))
					goldenGraph.addRelation(current);
			}

			if(state == 2)
			{
				if(current != null && isValidRelationType(current.getType()))
					partGraph.addRelation(current);
			}
		}
	}

	private boolean isValidRelationType(String type)
	{
		return _validRelationsType == null || _validRelationsType.contains(type);
	}
	
	public static void main(String args[]){

		String alignments = null;
		//boolean debug = false;
		boolean showGC = true;
		boolean showPart = true;
		String doc = null;
		String[] relations = null;
		int label = -1;
		boolean alignmentsInput = true;
		String mapFile = null;

		try{

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-alinhamento"))
				{
					alignments = args[++i];
					continue;
				}

				if (args[i].equals("-doc"))
				{
					doc = args[++i];
					continue;
				}

				if (args[i].equals("-ver"))
				{
					i++;
					if(args[i].equalsIgnoreCase("cd"))
					{
						showGC = true;
						showPart = false;
					}
					else if(args[i].equalsIgnoreCase("part"))
					{
						showPart = true;
						showGC = false;
					}

					continue;
				}

				if (args[i].equals("-entrada"))
				{
					i++;
					if(args[i].equalsIgnoreCase("alinhamentos"))
					{
						alignmentsInput = true;
					}
					else if(args[i].equalsIgnoreCase("triplas"))
					{
						alignmentsInput = false;
					}

					continue;
				}
				
				if (args[i].equals("-relacoes"))
				{
					relations = args[++i].split(SEP);
					continue;
				}
				
				if (args[i].equals("-mapa"))
				{
					i++;
					mapFile = args[i];
					continue;
				}
				
				if (args[i].equals("-nos"))
				{
					i++;
					if(args[i].equalsIgnoreCase("id"))
					{
						label = RelationsGraphM2Drawer.nodeLabel.ID.ordinal();
					}
					else if(args[i].equalsIgnoreCase("em"))
					{
						label = RelationsGraphM2Drawer.nodeLabel.CONTENT.ordinal();
					}
					else if(args[i].equalsIgnoreCase("ambos"))
					{
						label = RelationsGraphM2Drawer.nodeLabel.BOTH.ordinal();
					}

					continue;
				}

				/*if (args[i].equals("-depurar") || args[i].equals("-debug"))
				{
					debug = true;
					continue;
				}*/
			}
		} catch (Exception e)
		{
			System.out.println(e);
			printSynopsis();
			return;
		}

		if(alignments == null)
		{
			printSynopsis();
			return;
		}
		
			
		new ViewRelationGraphs(alignments, false, showGC, showPart, alignmentsInput, doc, relations, mapFile, label);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:\n");
		System.out.println(
				"java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar;lib/jgraph.jar pt.linguateca.harem.ViewRelationsGraphs " +
				"-alinhamento <ficheiro_alinhamentos> [-ver <cd|part>] -doc docid" +
				"[-relacoes tipo1;tipo2...] [-entrada alinhamentos] [-nos <em|id|ambos>] [-mapa CD.xml]");
		System.out.println("Info adicional:");
		System.out.println("\t<ficheiro_alinhamentos> deve ser um ficheiro de saida do organizador de ALTs, Expandidor de relacoes ou Normalizador de IDs");
		System.out.println("\tSe a opcao -ver nao for especificada, sao mostrados os grafos dois dois lados");
		System.out.println("\tA opcao -entrada alinhamentos deve ser utilizada quando a entrada e' um ficheiro de alinhamentos, caso contrario" +
				" deve ser dado um ficheiro de relacoes");
		System.out.println("\tA opcao -relacoes tem de ser seguida de um filtro com o tipo das relacoes" +
				"que se pretendem ver, separadas por ;");
		System.out.println("\tSe a opcao -nos nao for especificada, os nos mostrarao apenas o ID");
		System.out.println("\tA opcao -mapa deve ser seguida de um ficheiro XML com documentos onde " +
				"seja possivel associar IDs a EMs, como por exemplo a CD do ReRelEM.");
	}
}
