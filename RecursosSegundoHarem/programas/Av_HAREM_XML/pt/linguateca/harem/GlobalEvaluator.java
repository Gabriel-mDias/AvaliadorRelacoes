/*
 * Created on Jul 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public abstract class GlobalEvaluator extends HaremEvaluator implements Runnable
{
	public static final String TOTAL_IN_GC = "Total na CD";

	public static final String TOTAL_IDENTIFIED = "Total identificadas";

	public static final String TOTAL_CORRECT_F = "Total correctamente identificadas";
	
	public static final String TOTAL_CORRECT_M = "Total correctamente identificados";

	public static final String TOTAL_OCCURRENCES_PARTIAL_CORRECT = "Total Ocorr\u00eancias Parcialmente Correctos";

	public static final String SUM_PARTIAL_CORRECT = "Soma Parcialmente Correctos";

	public static final String SUM_PARTIAL_INCORRECT = "Soma Parcialmente Incorrectos";

	public static final String SPURIOUS = "Esp\u00farios";

	public static final String MISSING = "Em Falta";

	public static final String PRECISION = "Precis\u00e3o";

	public static final String RECALL = "Abrang\u00eancia";

	public static final String F_MEASURE = "Medida F";

	public static final String COMBINED_ERROR = "Erro Combinado";

	public static final String OVER_GENERATION = "Sobre-gera\u00e7\u00e3o";

	public static final String UNDER_GENERATION = "Sub-gera\u00e7\u00e3o";

	protected boolean _debug;

	public GlobalEvaluator(String alignments, boolean useTags, boolean debug)
	{
		super(alignments, useTags);
		_debug = debug;
	}

	public abstract void run();

	private String getFirstValid(LinkedList list)
	{
		String current;
		for (Iterator i = list.iterator(); i.hasNext();)
		{
			current = (String) i.next();
			if (current.trim().equals(""))
				continue;

			if (!Character.isLetterOrDigit(current.charAt(0)))
				continue;

			return current;
		}

		return null;
	}

	private String getLastValid(LinkedList list)
	{
		LinkedList clone = (LinkedList) list.clone();
		String current;

		while (!clone.isEmpty())
		{
			current = (String) clone.removeLast();
			if (current.trim().equals(""))
				continue;

			if (!Character.isLetterOrDigit(current.charAt(0)))
				continue;

			return current;
		}

		return null;
	}

	private int getPosition(int start, String token, NamedEntity entity)
	{
		String current;
		String normalizedToken = normalize(token);
		String normalizedEntity = normalize(entity.getEntity());

		return normalizedEntity.indexOf(normalizedToken, start);
	}

	private String normalize(String toNormalize)
	{
		String normalized = "";
		String current;
		LinkedList tokens = TaggedDocument.tokenize(toNormalize);

		for (Iterator i = tokens.iterator(); i.hasNext();)
		{
			current = (String) i.next();
			if (current.trim().equals(""))
				continue;

			normalized += current;
		}

		return normalized.toUpperCase();
	}

}
