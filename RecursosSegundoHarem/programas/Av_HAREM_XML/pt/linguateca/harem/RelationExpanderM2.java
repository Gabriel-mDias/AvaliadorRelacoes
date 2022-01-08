package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import pt.linguateca.relations.DerivationMap;
import pt.linguateca.relations.RelatedEntity;
import pt.linguateca.relations.Relation;
import pt.linguateca.relations.RelationsGraphM2;
import pt.linguateca.relations.RelationsList;
import pt.linguateca.relations.TransitiveRules;

/** avaliacao do ReRelEM_M2 - passo 3 **/
public class RelationExpanderM2 extends HaremEvaluator implements Runnable
{
	/** expandir a participacao **/
	private boolean _expandParticipation;
	/** a participacao esta' no formato da CD, em termos de facetas -  
	 * as relações são entre facetas únicas e nunca vagas **/
	private boolean _participationLikeGC;
	/** transformar tipos nao basicos em outra, impedindo-os de ser expandidos **/
	private boolean _normalizeTypes;

	private boolean _debug;
	private boolean _showInconsistences;
	private EvaluatedAlignmentProcessor _processor;

	private static final int MAX_ITERATIONS = 10;

	public RelationExpanderM2(String alignmentFile, boolean useTags, boolean expPart,
			boolean normalizeTypes, boolean partLikeGC, boolean debug, boolean inconsistences){

		super(alignmentFile, useTags);

		_expandParticipation = expPart;
		_normalizeTypes = normalizeTypes;
		_participationLikeGC = partLikeGC;
		_debug = debug;
		_showInconsistences = inconsistences;
		_processor = new IdentificationEvaluatedAlignmentProcessor();

		new Thread(this).start();
	}

	public void run()
	{	
		//para contar o tempo que demora a expandir
		GregorianCalendar inicio = null;
		if(_debug)
		{
			inicio = new GregorianCalendar();
			System.out.println("inicio= "+getTime(inicio));
		}

		BufferedReader reader = null;
		boolean processOk = false;
		String buffer;
		IdentificationEvaluatedAlignment current;
		RelatedEntity entity;
		RelationsGraphM2 goldenGraph = null;
		RelationsGraphM2 partGraph = null;
		EntitiesAttributesFilter categoriesFilter = null;

		LinkedList<IdentificationEvaluatedAlignment> alignments = null;

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
					processOk = true;
					System.out.println("\n"+buffer);
					alignments = new LinkedList<IdentificationEvaluatedAlignment>();
					goldenGraph = new RelationsGraphM2();
					partGraph = new RelationsGraphM2();

					continue;
				}

				else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
				{					
					if(goldenGraph != null){
						if(_debug) System.out.println("GOLDEN:");
						//removeUknownRelations(goldenGraph);
										
						expand(goldenGraph, AlignmentsToTriples.GC);
					}

					if(partGraph != null){
						//removeUknownRelations(partGraph);

						if(_expandParticipation)
						{
							if(_debug) System.out.println("PART:");
							
							expand(partGraph, AlignmentsToTriples.PARTICIPATION);
						}
					}

					if(alignments != null)
					{
						updateRelations(alignments);
						printAlignments(alignments);
					}

					System.out.println(_tagBase.getEndOfDocTag());
					processOk = false;
					continue;
				}

				else if (!isEvaluatable(buffer))
				{
					//System.out.println(buffer);
					continue;
				}

				current = (IdentificationEvaluatedAlignment) _processor.getEvaluatedAlignment(buffer);

				//eliminar os atributos que nao interessam...
				current.removeComments();
				alignments.add(current);

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
							!_participationLikeGC, categoriesFilter);

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

		if(_debug)
		{
			GregorianCalendar fim = new GregorianCalendar();
			System.out.println("fim= "+getTime(fim));
			System.out.println("tempo= "+getIntervalInSeconds(inicio, fim));
		}
	}

	private void normalizeTypes(RelationsGraphM2 graph)
	{
		if(_normalizeTypes)
			for(RelationsList list : graph)
				for(Relation relation : list)
					relation.normalizeType();
	}
	
	private void expand(RelationsGraphM2 graph, String graphId)
	{		
		normalizeTypes(graph);
		
		DerivationMap derivationMap = new DerivationMap();

		//passado por referencia
		inverseRelations(graph, derivationMap);
		if(_debug || _showInconsistences)
			detectInconsistences(graph, graphId, derivationMap);

		int i = 0;
		while(transitiveRelations(graph, derivationMap) && ++i < MAX_ITERATIONS)
		{
			//System.out.println("graph "+(i+1)+":\n"+graph);
			inverseRelations(graph, derivationMap);
			if(_debug || _showInconsistences)
			{
				System.out.println("[----- Fim da iteracao n."+i+" -----]");
				detectInconsistences(graph, graphId, derivationMap);
			}
		}
	}

	@Deprecated
	//isto agora é feito no normalizador de IDs
	/** elimina CORELs com IDs inexistentes (que estavam OMITIDOs ou foram filtrados..) **/
	private void removeUknownRelations(RelationsGraphM2 graph)
	{
		Set<Relation> toRemove = new HashSet<Relation>();

		for(RelationsList node : graph){
			for(Relation r : node){

				if(!graph.containsNode(r.getB()))
					toRemove.add(r);
			}

			for(Relation r : toRemove)
			{
				//System.out.println("(-) "+r);
				node.removeRelation(r);
			}
		}			
	}

	private void inverseRelations(RelationsGraphM2 graph, DerivationMap derivation)
	{
		//System.out.println("INVERSE RELATIONS");

		Relation inverse = null;
		RelationsList otherArg = null;
		boolean addedRelation = false;
		
		//inverse relations;
		for(RelationsList node : graph){
			for(Relation r : node){

				if(_debug || _showInconsistences)
					derivation.addBaseRelation(r);

				if((inverse = _tagBase.getInverse(r)) != null)
				{
					otherArg = graph.get(inverse.getA());
					if(otherArg != null && !otherArg.containsRelation(inverse))
					{
						addedRelation = otherArg.addEdge(inverse);
						if(_debug && addedRelation)
						{
							System.out.println("Adicionada inversa: "+inverse);
							derivation.addSimpleDerivation(r, inverse);
						}
						else if(_showInconsistences && addedRelation)
						{
							derivation.addSimpleDerivation(r, inverse);
						}
					}
					/*else
					{
						System.out.println("---> "+inverse.getA()+"...\n"+graph.getAllKeys());
					}*/
				}					
			}
		}
	}

	private boolean transitiveRelations(RelationsGraphM2 graph, DerivationMap derivation)
	{
		//System.out.println("TRANSITIVE RELATIONS");

		Relation transitive = null;
		boolean addedRelation = false;

		Iterator<RelationsList> i1 = graph.iterator(); //nós 1 
		Iterator<RelationsList> i2 = null; //nós 2
		RelationsList left = null;
		RelationsList right = null;

		ArrayList<RelationsList> nodes = new ArrayList<RelationsList>(graph.getAllNodes());
		Set<Relation> toAdd = new HashSet<Relation>();

		TransitiveRules rules = TransitiveRules.getInstance(_tagBase);

		HashMap<Relation, String> _debugMap = new HashMap<Relation, String>();

		while(i1.hasNext())
		{
			left = i1.next();
			nodes.remove(left);

			i2 = nodes.iterator();
			//System.out.println("left = "+left);

			while(i2.hasNext())
			{
				right = i2.next();
				//System.out.println("right = "+right);

				for(Relation r1 : left)
				{
					for(Relation r2 : right)
					{
						//System.out.print("\n$");
						transitive = rules.getTransitiveRelation(r1, r2);
						if(transitive != null){

							if(_debug || _showInconsistences)
							{
								if(_debug)
									_debugMap.put(transitive, r1 + " e " + r2 + " --> " + transitive);

								derivation.addDerivation(r1, r2, transitive);
							}

							toAdd.add(transitive);
						} 
					}
				}
			}
		}

		boolean test = false;
		//System.out.println("A adicionar: "+toAdd);

		//se a relação já existir, addEdge retorna false
		for(Relation r : toAdd)
		{		
			/*System.out.println("graph= "+graph);
			System.out.println("r.getA()= "+r.getA());
			System.out.println("graph.get(r.getA())= "+graph.get(r.getA()));*/

			if(!graph.containsNode(r.getA()))
			{
				System.err.println("COREL com ID inexistente! "+r.getA()+" - Omitido na CD?");
				//System.err.println("KEYS= "+graph.getKeys());
				continue;
			}

			test = (graph.get(r.getA())).addEdge(r);
			addedRelation = test || addedRelation;
			if(_debug && test)
			{
				System.out.println("Adicionada por transitividade:");
				System.out.println("\t"+_debugMap.get(r));
			}
		}	

		return addedRelation;
	}

	private void detectInconsistences(RelationsGraphM2 graph, String where, DerivationMap derivation)
	{
		for(RelationsList node : graph.getAllNodes())
			detectInconsistence(node, where, derivation);
	}

	private void detectInconsistence(RelationsList relations, String where, DerivationMap derivation)
	{
		String inverseType;

		for(Relation r1 : relations)
		{	
			inverseType = _tagBase.getInverseType(r1.getType());

			for(Relation r2 : relations)
			{	
				if(r1.getA().equals(r2.getA()) && r1.getB().equals(r2.getB()))
				{	
					//1 - rel(A,B) e inv(A,B)
					if(inverseType != null && !inverseType.equals(r1.getType()) && r2.getType().equals(inverseType))
					{
						System.out.println("INCONSISTENCIA na "+where+": "+relations.getKey() + " --> " +r1 + " e " + r2);
						derivation.printDerivationPath(r1);
						derivation.printDerivationPath(r2);
						continue;
					}

					//2 - inclui(A,B) e sede_de(A,B)
					if(r1.getType().equals(Relation.INCLUI) && r2.getType().equals(Relation.SEDE) ||
							r1.getType().equals(Relation.SEDE) && r2.getType().equals(Relation.INCLUI))
					{
						System.out.println("INCONSIST "+where+": "+relations.getKey() + " --> " +r1 + " e " + r2);
						continue;
					}
				}

				//3 - ident(A,B) e relacao(A,B)
				if(r1.getType().equals(Relation.IDENTIDADE) &&
						!r2.getType().equals(Relation.IDENTIDADE) &&
						r2.getA().equals(r1.getA()) && r2.getB().equals(r1.getB()))
				{
					System.out.println("INCONSISTENCIA na "+where+": "+relations.getKey() + " --> " +r1 + " e " + r2);
					continue;
				}
			}
		}
	}

	/*private void detectInconsistence(RelationsList relations, String where)
	{
		String inverse;
		Relation test;
		for(Relation r : relations)
		{				
			//* - rel(A,B) e inv(A,B)
			inverse = _tagBase.getInverseType(r.getType());

			if(inverse == null)
				continue;

			test = new Relation(inverse, r.getA(), r.getB());

			//test pode ser igual a r, se a inversa for igual à directa: ident, vinculo_inst...
			if(!test.equals(r) && relations.containsRelation(test))
			{
				//System.out.println(relations);
				System.out.println("INCONSISTENCIA na "+where+": "+relations.getKey() + " --> " +r + " e " + test);
				//break;
			}
		}
	}*/

	/*private void printAlignments(LinkedList<IdentificationEvaluatedAlignment> alignments)
	{
		for(IdentificationEvaluatedAlignment al : alignments)
			System.out.println(al);
	}*/

	/*private void printRelations(String title, RelationsGraph graph)
	{
		System.out.println(title);

		for(RelatedEntity entity : graph)
			for(Relation r : entity)
				System.out.println(r);
	}*/

	private void updateRelations(LinkedList<IdentificationEvaluatedAlignment> alignments)
	{
		NamedEntity golden, part;
		for(IdentificationEvaluatedAlignment al : alignments)
		{
			golden = al.getGoldenEntity();
			part = al.getFirstAlignment();
			if(!golden.isSpurious())
			{
				if(golden instanceof RelatedEntity)
				{
					((RelatedEntity)golden).updateRelationAttributes();
				}
			}
			if(!al.isNullAligned())
			{
				if(part instanceof RelatedEntity)
				{
					((RelatedEntity)part).updateRelationAttributes();
				}
			}
		}
	}

	private void printAlignments(LinkedList<IdentificationEvaluatedAlignment> alignments)
	{
		for(IdentificationEvaluatedAlignment al : alignments)
			System.out.println(al);
	}

	/*private void printRelations(String title, RelationsGraphM2 graph)
	{
		System.out.println(title);

		for(RelationsList relations : graph)
			for(Relation r : relations)
				System.out.println(r);
	}*/

	public static void main(String args[]){
		String alignments = null;
		boolean useTags = false;
		boolean debug = false;
		boolean showInconsistences = false;
		boolean participationLikeGC = false;
		boolean expandParticipation = true;
		boolean normalizeTypes = false;


		try{

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-alinhamento"))
				{
					alignments = args[++i];
					continue;
				}

				if (args[i].equals("-exptudo"))
				{
					//default é expandir tudo
					expandParticipation = args[++i].equalsIgnoreCase("sim");
					continue;
				}

				if (args[i].equals("-etiquetas"))
				{
					useTags = args[++i].equalsIgnoreCase("sim");
					continue;
				}

				if (args[i].equals("-normaliza_tipos"))
				{
					normalizeTypes = true;
					continue;
				}

				if (args[i].equals("-part_tipo_cd"))
				{
					participationLikeGC = true;
					continue;
				}

				if (args[i].equals("-ver_inconsistencias"))
				{
					showInconsistences = true;
					continue;
				}

				if (args[i].equals("-depurar") || args[i].equals("-debug"))
				{
					debug = true;
					continue;
				}
			}

		} catch (Exception e)
		{
			printSynopsis();
			return;
		}

		/*try {
			System.setOut(new PrintStream("saidaExpandidor"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		new RelationExpanderM2(alignments, useTags, expandParticipation, normalizeTypes,
				participationLikeGC, debug, showInconsistences);
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
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar [-Xmx512M] pt.linguateca.harem.RelationsExpander " +
		"-alinhamento <ficheiro_alinhamentos_alts> [-exptudo <sim|nao>] [-normaliza_tipos] [-part_tipo_cd]");
		System.out.println("Opcoes:\n" +
				"\t-exptudo nao: expande apenas relacoes na CD\n" +
				"\t-normaliza_tipos: transforma tipos nao basicos em outra, impedindo a sua eventual expansao." +
				"\t-part_tipo_cd: a participacao está na forma da CD, ou seja, todas as relacoes são entre facetas unicas, " +
		"nao havendo relacoes entre facetas vagas.");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsExpander -alinhamento ficheiro.alinhado.alts -exptudo sim");
	}


	private String getTime(GregorianCalendar calendar)
	{
		return calendar.get(Calendar.HOUR_OF_DAY)
		+":"+calendar.get(Calendar.MINUTE)
		+":"+calendar.get(Calendar.SECOND);
	}

	private String getIntervalInSeconds(GregorianCalendar c1, GregorianCalendar c2)
	{
		long ms1 = c1.getTimeInMillis();
		long ms2 = c2.getTimeInMillis();
		long interval = Math.abs(ms1 - ms2);
		return Math.abs(interval/1000f)+"";
	}
}
