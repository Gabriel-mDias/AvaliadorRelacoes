package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import pt.linguateca.relations.RelatedEntity;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationsGraphM2;
import pt.linguateca.relations.RelationsList;

/**
 * 
 * @author Besugo
 * ReRelEM - conversor da notação de alinhamentos na notação de triplas
 *
 */
public class AlignmentsToTriples extends HaremEvaluator implements Runnable
{
	public static final String GC = "[CD]";
	public static final String PARTICIPATION = "[Part]";
	
	private EvaluatedAlignmentProcessor _processor;
	private boolean _normalizeTypes;
	private boolean _partLikeGC;

	public AlignmentsToTriples(String alignmentFile, boolean useTags,
			boolean normalizeTypes, boolean participation)
	{
		super(alignmentFile, useTags);
		_processor = new IdentificationEvaluatedAlignmentProcessor();
		_normalizeTypes = normalizeTypes;
		_partLikeGC = participation;
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment current;
		RelatedEntity entity;
		EntitiesAttributesFilter categoriesFilter;
		RelationsGraphM2 goldenGraph = null;
		RelationsGraphM2 partGraph = null;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			buffer = reader.readLine();

			//filtro
			if(buffer.startsWith("#")){
				System.out.println(buffer);
				categoriesFilter = new EntitiesAttributesFilter(buffer, _tagBase);
			} else
			{
				categoriesFilter = new EntitiesAttributesFilter();
				categoriesFilter.setTree(_tagBase.getEntitiesAttributesTree(), true);
			}
			while ((buffer = reader.readLine()) != null)
			{
				if(buffer.startsWith(_tagBase.getDocTag()))
				{
					goldenGraph = new RelationsGraphM2();
					partGraph = new RelationsGraphM2();
					System.out.println("\n"+buffer);
					continue;
				}

				else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
				{
					printRelations(GC, goldenGraph);
					printRelations(PARTICIPATION, partGraph);
					
					System.out.println(_tagBase.getEndOfDocTag());
					continue;
				}

				else if (!isEvaluatable(buffer))
				{
					//System.out.println(buffer);
					continue;
				}

				current = (IdentificationEvaluatedAlignment) _processor.getEvaluatedAlignment(buffer);

				if(!current.getGoldenEntity().isSpurious())
				{
					entity = new RelatedEntity(current.getGoldenEntity(),
							false, categoriesFilter);

					current.setGoldenEntity(entity);
					
					for(RelationsList relations : entity.getRelationsLists().values())
						goldenGraph.addNode(relations);
				}

				if(!current.isNullAligned()){

					entity = new RelatedEntity(current.getFirstAlignment(),
							!_partLikeGC, categoriesFilter);

					current.getAlignedList().set(0, entity);
					
					for(RelationsList relations : entity.getRelationsLists().values())
						partGraph.addNode(relations);
				}
			}
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

	private void printRelations(String title, RelationsGraphM2 graph)
	{
		System.out.println(title);

		for(RelationsList relations : graph)
			for(Relation r : relations)
			{
				if(_normalizeTypes)
					r.normalizeType();
				
				System.out.println(r);
			}
	}
	
	public static void main(String args[]){

		boolean useTags = false;
		String alignments = null;
		boolean normalizeTypes = false;
		boolean participation = false;
		
		try
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-alinhamento"))
				{
					alignments = args[++i];
					continue;
				}
				
				if (args[i].equals("-normaliza_tipos"))
				{
					normalizeTypes = true;
					continue;
				}
				
				if (args[i].equals("-part_tipo_cd"))
				{
					participation = true;
					continue;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println(e);
			//printSynopsis();
			return;
		}
		new AlignmentsToTriples(alignments, useTags, normalizeTypes, participation);
	}
	
	/**
	 * No caso de os argumentos de entrada nao serem correctamente fornecidos.
	 * Este metodo imprime uma mensagem de ajuda para a consola.
	 * 
	 */
	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar " +
				"pt.linguateca.harem.AlignmentsToTriples -alinhamento <ficheiro_alinhamentos>");
	}
}
