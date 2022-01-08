package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author nseco
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class SemanticAlignmentEvaluator extends HaremEvaluator implements Runnable
{
	//private static final double SPURIOUS = 0.0;
	private static final double DEFAULT_ALPHA = 1.0;
	private static final double DEFAULT_BETA = 0.5;
	private static final double DEFAULT_GAMMA = 0.25;
	private static final double DEFAULT_IDENTIFICATION = 1;

	private EntitiesAttributesFilter _filter;

	private boolean _useCorrectlyIdentified;
	private boolean _penalties;
	private double[] _weights;

	public SemanticAlignmentEvaluator(String alignment, boolean useTags,
			boolean useCorrectlyIdentified, double[] weights, boolean penalties)
	{
		super(alignment, useTags);
		_useCorrectlyIdentified = useCorrectlyIdentified;
		_penalties = penalties;

		_weights = weights;
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment current;
		ArrayList<AttributesEvaluation> evaluations;
		IdentificationEvaluatedAlignmentProcessor processor = new IdentificationEvaluatedAlignmentProcessor();

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			buffer = reader.readLine();

			if(buffer.startsWith("#")){
				System.out.println(buffer);
				_filter = new EntitiesAttributesFilter(buffer, _tagBase);
			}

			while ((buffer = reader.readLine()) != null)
			{
				if (!isEvaluatable(buffer))
				{
					System.out.println(buffer);
					continue;
				}

				current = (IdentificationEvaluatedAlignment) processor.getEvaluatedAlignment(buffer);

				if (_useCorrectlyIdentified && (current.isNullAligned() || current.isSpurious()))
					continue;

				evaluations = evaluate(current);

				System.out.println(current.getGoldenEntity().toString() + Aligner.ALIGNMENT_CONNECTOR
						+ current.getAlignedList().toString() + IndividualAlignmentEvaluator.EVALUATION_MARKER
						+ evaluations);
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			reader.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private ArrayList<AttributesEvaluation> evaluate(IdentificationEvaluatedAlignment alignment)
	{
		ArrayList<AttributesEvaluation> results = new ArrayList<AttributesEvaluation>(alignment.getAlignmentCount());
		AttributesEvaluation semanticEvaluation;
		NamedEntity currentEntity;
		Iterator<NamedEntity> i;

		//System.out.println(categWeight + " - " + typeWeight + " - " + subtypeWeight);

		//System.out.println("-- to be evaluated: "+alignment);
		for (i = alignment.getAligned(); i.hasNext();)
		{
			currentEntity = i.next();

			if(_weights != null && _weights.length >= 4)
			{
				semanticEvaluation = new AttributesEvaluation(_filter, alignment.getGoldenEntity(), currentEntity,
						_penalties,
						getWeight(alignment.getScore(currentEntity)), getAltWeight(alignment),
						_weights[0], _weights[1], _weights[2], _weights[3]);
			}
			else
			{
				semanticEvaluation = new AttributesEvaluation(_filter, alignment.getGoldenEntity(), currentEntity,
						_penalties,
						getWeight(alignment.getScore(currentEntity)), getAltWeight(alignment),
						DEFAULT_IDENTIFICATION, DEFAULT_ALPHA, DEFAULT_BETA, DEFAULT_GAMMA);
			}

			//semanticEvaluation.evaluate();
			results.add(semanticEvaluation);
		}
		return results;
	}

	/*private boolean hasCommonCategory(LinkedList<String> categories, LinkedList<String> types)
	{
		for(Iterator<String> i = categories.iterator(); i.hasNext();)
		{
			for (Iterator<String> j = types.iterator(); j.hasNext();)
			{
				if (_tagBase.getPossibleCategoriesOfType(j.next()).contains(i.next())){
					//System.out.println("true");
					return true;
				}
			}
		}
		//System.out.println("false");
		return false;
	}

	private boolean hasCommonType(LinkedList<String> types, LinkedList<String> subs)
	{
		for (Iterator<String> j = types.iterator(); j.hasNext();)
		{
			for (Iterator<String> k = subs.iterator(); k.hasNext();)
			{
				if (_tagBase.getPossibleTypesOfSubtype(k.next()).contains(j.next()))
					return true;
			}
		}
		return false;
	}*/

	private double getWeight(String score)
	{
		if (score.equals(IndividualAlignmentEvaluator.CORRECT))
		{
			return 1;
		}

		if (score.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_EXCESS)
				|| score.startsWith(IndividualAlignmentEvaluator.PARTIAL_CORRECT_LACK))
		{
			return Double.parseDouble(score.split("[(;)]")[1]);
		}

		return 0;
	}

	private double getAltWeight(EvaluatedAlignment alignment)
	{
		return alignment.getAltWeight();
	}

	/**
	 * Retorna as entradas que list1 contem e list2 nao
	 * @param list1
	 * @param list2
	 * @return
	 */
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

		return difference;
	}

	private LinkedList<String> getIntersection(LinkedList<String> list1, LinkedList<String> list2)
	{

		/*System.out.println("1:"+list1);
		System.out.println("2:"+list2);*/

		LinkedList<String> intersection = new LinkedList<String>();
		//Set validCategories;
		String current;

		if (list1 == null || list2 == null)
		{
			return intersection;
		}

		//validCategories = _filter.getKeySet();
		for (Iterator<String> i = list1.iterator(); i.hasNext();)
		{
			current = i.next();
			if (list2.contains(current))
			{
				intersection.add(current);
			}
		}

		return intersection;
	}

	private LinkedList<AttributeTuple> getTuplesIntersection(LinkedList<AttributeTuple> list1, LinkedList<AttributeTuple> list2)
	{
		LinkedList<AttributeTuple> intersection = new LinkedList<AttributeTuple>();
		AttributeTuple shared = null;

		for(AttributeTuple at1 : list1){
			for(AttributeTuple at2 : list2){

				shared = at1.getIntersection(at2);
				if(!shared.isEmpty() && !intersection.contains(shared))
					intersection.add(shared);
			}
		}

		return intersection;
	}

	/**
	 * list1 - list2
	 * @param list1
	 * @param list2
	 * @return
	 */
	private LinkedList<AttributeTuple> getTuplesDifference(LinkedList<AttributeTuple> list1, LinkedList<AttributeTuple> list2)
	{
		LinkedList<AttributeTuple> difference = new LinkedList<AttributeTuple>();
		AttributeTuple diff = null;

		if ((list1 == null || list1.isEmpty())&& (list2 == null || list2.isEmpty())){
			return difference;
		}

		if (list1 == null || list1.isEmpty()){
			return difference;
		}

		if (list2 == null || list2.isEmpty()){
			difference.addAll(list1);
			return difference;
		}

		int highestLevel = -1;
		AttributeTuple tuple = null;
		for(AttributeTuple at1 : list1){

			if(!at1.isEmpty() && !list2.contains(at1)){

				for(AttributeTuple at2 : list2){
					diff = at1.getDifference(at2);
					if(diff.getLevel() > highestLevel)
						tuple = diff;
				}

				if(tuple != null && !difference.contains(tuple))
					difference.add(tuple);
			}
		}
		return difference;
	}

	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;
		boolean useCorrectlyIdentifed = false;
		boolean penalties = true;
		double[] weights = null;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-alinhamento"))
			{
				i++;
				alignments = args[i];
				continue;
			}

			if (args[i].equals("-penalizacoes") || args[i].equals("-penalties"))
			{
				i++;
				penalties = !args[i].equals("nao");
				continue;
			}

			if (args[i].equals("-pesos"))
			{
				i++;
				weights = getDoubleArray(args[i].split(";"));
				continue;
			}

			if (args[i].equals("-etiquetas"))
			{
				i++;
				useTags = args[i].equalsIgnoreCase("sim");
				continue;
			}

			if (args[i].equals("-relativo"))
			{
				i++;
				useCorrectlyIdentifed = args[i].equalsIgnoreCase("sim");
				continue;
			}
		}

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new SemanticAlignmentEvaluator(alignments, useTags, useCorrectlyIdentifed, weights, penalties);
	}

	private static double[] getDoubleArray(String[] array)
	{
		double[] toReturn = new double[array.length];

		try{
			
			for(int i = 0; i < array.length; i++)
				toReturn[i] = Double.parseDouble(array[i]);
		
		} catch (NumberFormatException e)
		{
			System.err.println("Formato invalido na indicacao dos pesos!\n" +
					"A avaliacao sera' feita com os valores por omissao.");
			return null;
		}
		
		return toReturn;
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.SemanticAlignmentEvaluator " +
		"-alinhamento <ficheiro_alinhamentos_avaliados> [-penalizacoes <sim|nao>] [-pesos identificacao;alfa;beta;gama]");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.SemanticAlignmentEvaluator -alinhamento participacao.alinhado.avalida.veu.alts");
	}
}
