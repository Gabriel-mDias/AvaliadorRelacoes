/*
 * Created on Apr 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public class IdentificationAltAlignmentSelector extends AltAlignmentSelector
{

	public IdentificationAltAlignmentSelector(String alignmentsFile, boolean useTags, boolean debug)
	{
		super(alignmentsFile, useTags, new IdentificationEvaluatedAlignmentProcessor(), debug);
		new Thread(this).start();
	}

	protected EvaluatedALTAlignment createNewEvaluatedALTAlignment()
	{
		return new IdentificationEvaluatedALTAlignment();
	}

	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;
		boolean debug = false;

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

			if (args[i].equals("-depurar"))
			{
				i++;
				debug = args[i].equalsIgnoreCase("sim");
				continue;
			}
		}

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new IdentificationAltAlignmentSelector(alignments, useTags, debug);
	}

	private static void printSynopsis()
	{
		System.out.println("Utilização:");
		System.out
				.println("java -Dfile.encoding=ISO-8859-1 -jar <ficheiro_jar> -alinhamento <ficheiro_alinhado>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -jar Identificador.jar -alinhamento elle.alinhado");
	}
}
