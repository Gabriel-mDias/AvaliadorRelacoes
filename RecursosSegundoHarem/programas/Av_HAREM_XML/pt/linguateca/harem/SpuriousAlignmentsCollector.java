package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Filtra todos os alinhamentos espúrios para serem analisados
 * @author Besugo
 */
public class SpuriousAlignmentsCollector extends HaremEvaluator implements Runnable
{
	public SpuriousAlignmentsCollector(String alignmentFile, boolean useTags)
	{
		super(alignmentFile, useTags);
		new Thread(this).start();
	}

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment current;
		IdentificationEvaluatedAlignmentProcessor processor = new IdentificationEvaluatedAlignmentProcessor();

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			buffer = reader.readLine();

			if(buffer.startsWith("#")){
				System.out.println(buffer);
			}

			while ((buffer = reader.readLine()) != null)
			{
				if(buffer.startsWith(_tagBase.getDocTag())){
					System.out.println("\n"+buffer);
					continue;
				}
					
				else if (!isEvaluatable(buffer))
					continue;

				current = (IdentificationEvaluatedAlignment) processor.getEvaluatedAlignment(buffer);
				if(current.isSpurious())
					System.out.println(current.getAlignedList().toString());
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

		new SpuriousAlignmentsCollector(alignments, useTags);
	}
}
