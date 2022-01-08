package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pt.linguateca.relations.CorelKey;
import pt.linguateca.relations.RelationEvaluation;
import pt.linguateca.relations.RelationM2;
import pt.linguateca.relations.RelationProcessor;
import pt.linguateca.relations.RelationsInDocEvaluation;

/** avaliacao do ReRelEM_M2 - passo 4 - pontuacao de Relacoes **/
public class RelationsEvaluatorM2 extends HaremEvaluator implements Runnable{

	//private boolean IGNORE_SPURIOUS_IDS = true;

	public RelationsEvaluatorM2(String alignmentFile, boolean useTags){

		super(alignmentFile, useTags);
		new Thread(this).start();
	}

	public void run()
	{	
		BufferedReader reader = null;
		String buffer;
		int state = -1;

		RelationProcessor processor = new RelationProcessor();
		RelationM2 current;
		
		LinkedList<RelationM2> gcRelations = null;
		LinkedList<RelationM2> partRelations = null;

		RelationsInDocEvaluation evaluation;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			while ((buffer = reader.readLine()) != null)
			{
				//filtros
				if(buffer.startsWith("#")){
					System.out.println(buffer);
					continue;
				}		

				if(buffer.startsWith(_tagBase.getDocTag())){
					System.out.println("\n"+buffer);
					state = 0;

					gcRelations = new LinkedList<RelationM2>();
					partRelations = new LinkedList<RelationM2>();

					continue;
				}

				else if(buffer.startsWith(_tagBase.getEndOfDocTag()))
				{
					//output
					evaluation = evaluate(gcRelations, partRelations);
					printRelationsEvaluation(evaluation);

					System.out.println(buffer);
					state = -1;

					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.GC))
				{
					state = 1;
					continue;
				}

				else if ((state == 0 || state == 1) && buffer.startsWith(AlignmentsToTriples.PARTICIPATION))
				{
					state = 2;
					continue;
				}

				else if (buffer.trim().equals(""))
				{
					continue;
				}

				current = processor.getRelationM2(buffer);
				
				if(state == 1)
				{
					if(current != null)
						gcRelations.add(current);
				}

				if(state == 2)
				{
					if(current != null)
						partRelations.add(current);
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

	private RelationsInDocEvaluation evaluate(LinkedList<RelationM2> gc, LinkedList<RelationM2> part)
	{
		//Relações certas, CORELs (IDs + facetas)

		//LinkedList<RelationM2> gc = removeDuplicateRelations(gcRelations);
		//LinkedList<RelationM2> part = removeDuplicateRelations(partRelations);

		//Set<String> uniqueCorelsGC = new HashSet<String>(); //sets nao têm duplicados				
		//Set<String> uniqueCorelsPart = new HashSet<String>();

		Set<CorelKey> uniqueCorelsGC = new HashSet<CorelKey>(); //sets nao têm duplicados
		Set<CorelKey> uniqueCorelsPart = new HashSet<CorelKey>(); //sets nao têm duplicados

		//ArrayList<String> correctCorels = new ArrayList<String>();
		Set<CorelKey> correctCorels = new HashSet<CorelKey>();

		RelationsInDocEvaluation evaluation = new RelationsInDocEvaluation();
		evaluation.setTotalRelationsInGC(gc.size());
		evaluation.setTotalRelationsInPart(part.size());
		RelationEvaluation current;

		//String corelKey = "";
		CorelKey corelKey = null;

		//System.out.println("GC: "+gc);
		//System.out.println("Part: "+part);

		//tipos nao basicos para "outra"
		for(RelationM2 r : gc)
		{
			r.normalizeType();
		}
		
		//tipos nao basicos para "outra"
		for(RelationM2 r : part)
		{
			r.normalizeType();
		}

		for(RelationM2 partRel : part)
		{				
			corelKey = partRel.getCorelKey();
			uniqueCorelsPart.add(corelKey);
			
			for(RelationM2 goldenRel : gc)
			{
				if(partRel.compatibleWith(goldenRel))
				{
					current = new RelationEvaluation(partRel);
					current.setEvaluation(RelationEvaluation.RELATION_CORRECT_STRING);

					correctCorels.add(corelKey);

					uniqueCorelsGC.add(corelKey);
					gc.remove(goldenRel);
					evaluation.add(current);
					break;
				}
			}
		}
		
		//criar CORELs e CORELs+FACETAS unicos na CD
		for(RelationM2 r : gc)
		{
			uniqueCorelsGC.add(r.getCorelKey());
		}
		
		//avaliar relacoes que nao estao completamente correctas
		for(RelationM2 r : part)
		{
			if(evaluation.containsRelation(r)) //a relação já foi avaliada
			{
				continue;
			}
			
			//simpleRel = r.getSimpleRelationWithSuperID();
			corelKey = r.getCorelKey();

			current = new RelationEvaluation(r);

			if(containsCompatibleKey(correctCorels, corelKey)) //O COREL já foi avaliado e está correcto
			{
				current.setEvaluation(RelationEvaluation.COREL_CORRECT_STRING);					
			}

			else if (containsCompatibleKey(uniqueCorelsGC, corelKey)) //relação tem COREL correcto
			{
				current.setEvaluation(RelationEvaluation.COREL_CORRECT_STRING);
				correctCorels.add(corelKey);

			} else { //relações completamente espúrias

				current = new RelationEvaluation(r);
				current.setEvaluation(RelationEvaluation.SPURIOUS_RELATION_STRING);
			}

			evaluation.add(current);
		}

		//relações em falta
		for(RelationM2 r : gc)
		{
			current = new RelationEvaluation(r);
			current.setEvaluation(RelationEvaluation.MISSING_RELATION_STRING);
			evaluation.add(current);
		}

		evaluation.setUniqueArgPairsInGC(uniqueCorelsGC.size());
		evaluation.setUniqueArgPairsInPart(uniqueCorelsPart.size());
		evaluation.setCorrectCorels(correctCorels.size());

		return evaluation;
	}

	private LinkedList<RelationM2> removeDuplicateRelations(List<RelationM2> list) {
		Set<RelationM2> set = new LinkedHashSet<RelationM2>();
		set.addAll(list);
		return new LinkedList<RelationM2>(set);
	}

	private LinkedList<String> removeDuplicateCorels(List<String> list) {
		Set<String> set = new LinkedHashSet<String>();
		set.addAll(list);
		return new LinkedList<String>(set);
	}

	/*private LinkedList<Relation> removeRelationsWithSpuriousIds(LinkedList<RelationM2> list)
	{
		LinkedList<Relation> toReturn = new LinkedList<Relation>();

		for(Relation r : list)
		{
			if(!IDNormalizer.isSpuriousId(r.getA()) && !IDNormalizer.isSpuriousId(r.getB()))
			{
				toReturn.add(r);
			}
		}

		return toReturn;
	}*/

	private boolean containsCompatibleKey(Set<CorelKey> list, CorelKey key)
	{
		for(CorelKey tmp : list)
		{
			if(tmp.isCompatible(key))
				return true;
		}
		return false;
	}
	
	private void printRelationsEvaluation(RelationsInDocEvaluation list)
	{
		System.out.println(list.toStringWithoutScore());
	}

	public static void main(String args[]){

		String alignments = null;
		boolean useTags = false;

		try{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-alinhamento"))
				{
					i++;
					alignments = args[i];
					continue;
				}

				if (args[i].equals("-etiquetas"))
				{
					i++;
					useTags = args[i].equalsIgnoreCase("sim");
					continue;
				}
			}

		} catch (Exception e)
		{
			printSynopsis();
			return;
		}
		new RelationsEvaluatorM2(alignments, useTags);
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
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento <ficheiro_relacoes>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 cp .;lib/jdom.jar pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento ficheiro.alinhado.alts.expandido");
	}
}
