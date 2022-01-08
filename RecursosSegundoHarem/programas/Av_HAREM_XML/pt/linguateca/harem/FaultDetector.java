/*
 * Created on Apr 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class FaultDetector extends HaremEvaluator implements Runnable
{
	private static final String HAS_FAULTS = "ALINHAMENTOS_EXCESSIVOS";

	private static final String EVALUATION_MARKER = " : ";

	public FaultDetector(String alignments, boolean useTags)
	{
		super(alignments, useTags);
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		LinkedHashSet<NamedEntity> alignments;
		NamedEntity current;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			while ((buffer = reader.readLine()) != null)
			{
				if (!isEvaluatable(buffer))
				{
					System.out.println(buffer);
					continue;
				}

				alignments = new LinkedHashSet<NamedEntity>();

				String[] lados = buffer.split(Aligner.ALIGNMENT_CONNECTOR);
				if(lados.length != 2)
				{
					System.out.println(buffer);
					continue;
				}
				
				current = new NamedEntity(lados[0]);
				alignments.addAll(NamedEntity.toNamedEntityList(lados[1]));
				
				if (alignments.size() <= 1)
				{
					if (_useTags)
					{
						System.out.println(current.toString() + Aligner.ALIGNMENT_CONNECTOR + alignments.toString());
					}
					else
					{
						System.out.println(current.unmarkTokens().toString() + Aligner.ALIGNMENT_CONNECTOR
								+ clean(alignments).toString());
					}

					continue;
				}

				if (isFaulty(current, alignments))
				{
					if (_useTags)
					{
						System.out.println(current.toString() + Aligner.ALIGNMENT_CONNECTOR + alignments.toString()
								+ EVALUATION_MARKER + HAS_FAULTS);
					}
					else
					{
						System.out.println(current.unmarkTokens().toString() + Aligner.ALIGNMENT_CONNECTOR
								+ clean(alignments).toString() + EVALUATION_MARKER + HAS_FAULTS);
					}
					continue;
				}

				if (_useTags)
				{
					System.out.println(current.toString() + Aligner.ALIGNMENT_CONNECTOR + alignments.toString());
				}
				else
				{
					System.out.println(current.unmarkTokens().toString() + Aligner.ALIGNMENT_CONNECTOR
							+ clean(alignments).toString());
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

	private boolean isFaulty(NamedEntity golden, LinkedHashSet alignments)
	{
		NamedEntity current;
		NamedEntity another;
		LinkedList goldenAtoms;
		boolean process;

		goldenAtoms = ((NamedEntity) golden.unmarkTokens()).getNormalizedAtoms();

		for (Iterator i = alignments.iterator(); i.hasNext();)
		{
			current = (NamedEntity) i.next();
			process = false;
			for (Iterator j = alignments.iterator(); j.hasNext();)
			{
				another = (NamedEntity) j.next();
				if (!process)
				{
					if (another == current)
					{
						process = true;
					}
					continue;
				}

				if (isFaulty(goldenAtoms, ((NamedEntity) current.unmarkTokens()).getNormalizedAtoms(),
						((NamedEntity) another.unmarkTokens()).getNormalizedAtoms()))
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean isFaulty(LinkedList goldenAtoms, LinkedList atoms1, LinkedList atoms2)
	{
		atoms1.retainAll(goldenAtoms);
		atoms2.retainAll(goldenAtoms);
		String atom;

		for (Iterator i = atoms1.iterator(); i.hasNext();)
		{
			atom = (String) i.next();
			if (atoms2.contains(atom) && getCount(goldenAtoms, atom) < 2)
				return true;
		}

		return false;
	}

	private int getCount(LinkedList atoms, String atom)
	{
		int counter = 0;
		for (Iterator i = atoms.iterator(); i.hasNext();)
		{
			if (atom.equals(i.next()))
				counter++;
		}

		return counter;
	}

	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;

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

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new FaultDetector(alignments, useTags);
	}

	private static void printSynopsis()
	{
		System.out.println("Utilização:");
		System.out
				.println("java -Dfile.encoding=ISO-8859-1 -jar <ficheiro_jar> -alinhamento <ficheiro_alinhado>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -jar Falhas.jar -alinhamento elle.alinhado");
	}
}
