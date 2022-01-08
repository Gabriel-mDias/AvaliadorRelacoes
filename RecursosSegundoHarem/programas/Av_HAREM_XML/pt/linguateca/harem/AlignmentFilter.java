package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * 
 * @author Besugo
 * Véus
 *
 */
public class AlignmentFilter extends HaremEvaluator implements Runnable
{
	private static final String HAREM_STYLE = "harem";

	private static final String RELAXED_STYLE = "relaxado";

	private static final String MUC_STYLE = "muc";

	private EntitiesAttributesFilter _evalFilter;
	private EntitiesAttributesFilter _systemFilter;

	private IdentificationEvaluatedAlignmentProcessor _processor;

	private String _style;
	private Set<String> _spurious;

	//private String _mode;

	public AlignmentFilter(String alignments, boolean useTags, String filter1, String filter2, String style)
	{
		super(alignments, useTags);

		_evalFilter = createFilterObject(filter1);
		_systemFilter = createFilterObject(filter2);

		_processor = new IdentificationEvaluatedAlignmentProcessor();

		_style = style;	
		//_mode = (mode != null ? mode : DEFAULT_MODE);

		new Thread(this).start();
	}

	private EntitiesAttributesFilter createFilterObject(String filter)
	{
		EntitiesAttributesFilter filterObject;
		if (filter == null || filter.equals(EntitiesAttributesTree.EVERYTHING)){
			filterObject = new EntitiesAttributesFilter();
			filterObject.setTree(_tagBase.getEntitiesAttributesTree(), true);
		}
		else
		{
			filterObject = new EntitiesAttributesFilter(filter, _tagBase);
		}

		return filterObject;
	}

	public void run()
	{		
		BufferedReader reader = null;
		String buffer;
		IdentificationEvaluatedAlignment alignment;
		LinkedList<IdentificationEvaluatedAlignment> alignments;
		boolean processOK = false;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));
			printFilter();

			while ((buffer = reader.readLine()) != null)
			{				
				//se começar com a a tag DOC
				if (buffer.startsWith(_tagBase.getDocTag()))
				{
					System.out.println(buffer);
					processOK = true;
					_spurious = new HashSet<String>();
					continue;
				}

				if(!processOK)
				{
					continue;
				}

				if (!isEvaluatable(buffer))
				{
					System.out.println(buffer);
					continue;
				}
			
				alignment = (IdentificationEvaluatedAlignment) _processor.getEvaluatedAlignment(buffer);
				
				if(!_evalFilter.hasEverything())
					alignment = filterAlignment(alignment);

				if(alignment != null && !_style.equals(HAREM_STYLE))
					alignments = adjustAlignment(alignment);
				else if(alignment == null)
					alignments = null;
				else
				{
					alignments = new LinkedList<IdentificationEvaluatedAlignment>();
					alignments.add(alignment);
				}

				while (alignments != null && !alignments.isEmpty())
					System.out.println(alignments.removeFirst());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			;
		}
	}

	private IdentificationEvaluatedAlignment filterAlignment(IdentificationEvaluatedAlignment alignment)
	{
		return alignment.getFilteredAlignment(_evalFilter, _systemFilter);
	}

	/*private IdentificationEvaluatedAlignment filterGoldenEntity(IdentificationEvaluatedAlignment alignment){

		IdentificationEvaluatedAlignment clone = (IdentificationEvaluatedAlignment) alignment.clone();
		if(clone.isSpurious())
			return clone;

		NamedEntity golden = clone.getGoldenEntity();
		NamedEntity filteredGolden = null;

		//System.out.println("GOLDEN = "+golden);

		filteredGolden = (_identificationOnly ?
				golden.removeClassicAttributes() : golden.filter(_filter));

		//System.out.println("FILTERED = "+filteredGolden);
		clone.setGoldenEntity(filteredGolden);

		if(!_identificationOnly && filteredGolden.getCategories().isEmpty()){
			if(!clone.isNullAligned()){
				clone.setSpurious();
			} else return null;
		}

		return clone;
	}*/

	private LinkedList<IdentificationEvaluatedAlignment> adjustAlignment(IdentificationEvaluatedAlignment alignment){

		//System.out.println(alignment.getAlignmentCount()+" : "+_style);
		//System.out.println("alinhamento: "+alignment);

		LinkedList<IdentificationEvaluatedAlignment> adjusted = new LinkedList<IdentificationEvaluatedAlignment>();
		IdentificationEvaluatedAlignment newAlignment;
		IdentificationEvaluatedAlignment currentClone;
		NamedEntity golden, aligned;
		String alignedId;

		if (alignment.getAlignmentCount() == 1 && _style.equalsIgnoreCase(RELAXED_STYLE))
		{
			adjusted.add(alignment);
		}

		else if (alignment.getAlignmentCount() == 1 && _style.equalsIgnoreCase(MUC_STYLE))
		{	
			if (alignment.getScore(alignment.getFirstAlignment()).equals(IndividualAlignmentEvaluator.CORRECT)
					|| alignment.getScore(alignment.getFirstAlignment()).equals(IndividualAlignmentEvaluator.MISSING))
			{
				adjusted.add(alignment);
			}

			else //parcialmente correcto ou espurio
			{
				//System.out.println("firstAlignment= "+alignment.getFirstAlignment());
				//System.out.println("firstAlignmentScore= "+alignment.getScore(alignment.getFirstAlignment()));

				if(!alignment.isSpurious())
				{
					newAlignment = new IdentificationEvaluatedAlignment();
					golden = (NamedEntity)alignment.getGoldenEntity().clone();
					newAlignment.setGoldenEntity(golden);
					newAlignment.addAlignment(null, IndividualAlignmentEvaluator.MISSING);
					adjusted.add(newAlignment);
				}

				alignedId = alignment.getFirstAlignment().getId();

				if(!_spurious.contains(alignedId)) //para evitar espúrias repetidas!
				{
					newAlignment = new IdentificationEvaluatedAlignment();
					golden = (NamedEntity)alignment.getFirstAlignment().clone();
					golden.setSpurious();
					newAlignment.setGoldenEntity(golden);
					newAlignment.addAlignment(alignment.getFirstAlignment(), IndividualAlignmentEvaluator.SPURIOUS);
					adjusted.add(newAlignment);
					_spurious.add(alignedId);
				}
			}
		}

		else
		{
			currentClone = (IdentificationEvaluatedAlignment) alignment.clone();

			newAlignment = new IdentificationEvaluatedAlignment();
			newAlignment.setGoldenEntity(currentClone.getGoldenEntity());
			aligned = currentClone.getFirstAlignment();

			if (_style.equalsIgnoreCase(RELAXED_STYLE))
			{
				newAlignment.addAlignment(aligned, currentClone.getScore(aligned));
				currentClone.removeAlignment(aligned);
			}
			else {

				aligned = currentClone.getCorrectAlignment();

				//se for espuria e alinhada a null, nao e' nada!
				if(aligned == null && !newAlignment.isSpurious()){

					newAlignment.addAlignment(null, IndividualAlignmentEvaluator.MISSING);
					adjusted.add(newAlignment);

				//aqui só chegam alinhamentos com duas EMs correctas - estranho, mas acontece :)
				//se for espuria só alinha com uma EM e já foi tratada antes
				} else if(aligned != null){

					newAlignment.addAlignment(aligned, IndividualAlignmentEvaluator.CORRECT);
					//currentClone.removeCorrectAlignments();
					adjusted.add(newAlignment);
				}
			}

			for (Iterator<NamedEntity> j = currentClone.getIncorrectAlignments().iterator(); j.hasNext();)
			{
				aligned = j.next();
				alignedId = aligned.getId();

				if(!_spurious.contains(alignedId)) //para evitar espúrias repetidas!
				{
					newAlignment = new IdentificationEvaluatedAlignment();
					newAlignment.setGoldenEntity((NamedEntity) aligned.clone());
					newAlignment.getGoldenEntity().setSpurious();
					newAlignment.addAlignment(aligned, IndividualAlignmentEvaluator.SPURIOUS);
					adjusted.add(newAlignment);
					_spurious.add(alignedId);
				}
			}
		}

		return adjusted;
	}

	//private NamedEntity merge(Identifi)
	
	private void printFilter()
	{
		System.out.print("#");
		System.out.println(_evalFilter);
	}

	public static void main(String[] args)
	{
		boolean useTags = false;
		String style = MUC_STYLE;
		String file = null;
		String systemFilter = null;
		String evalFilter = null;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-alinhamento"))
			{
				i++;
				file = args[i];
				continue;
			}

			if (args[i].equals("-etiquetas"))
			{
				i++;
				useTags = args[i].equalsIgnoreCase("sim");
				continue;
			}

			if (args[i].equals("-avaliacao"))
			{
				i++;
				evalFilter = args[i].toUpperCase();				
				continue;
			}

			if (args[i].equals("-sistema"))
			{
				i++;
				systemFilter = args[i].toUpperCase();				
				continue;
			}

			/*if (args[i].equals("-cenario"))
			{
				i++;
				filter = args[i].toUpperCase();				
				continue;
			}*/

			/*if (args[i].equals("-genero"))
			{
				i++;
				genre = args[i];
				continue;
			}

			if (args[i].equals("-origem"))
			{
				i++;
				origin = args[i];
				continue;
			}*/

			if (args[i].equals("-estilo"))
			{
				i++;
				style = args[i];
				continue;
			}
		}

		if (file == null)
		{
			printSynopsis();
			return;
		}

		new AlignmentFilter(file, useTags, evalFilter, systemFilter, style);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentFilter -alinhamento <ficheiro_alinhamentos_avaliados> -estilo muc [-sistema filtro1] [-avaliacao filtro2]");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AlignmentFilter -alinhamento participacao.alinhado.avalida -estilo muc" +
				"-sistema \"PESSOA(*):LOCAL(*):ORGANIZACAO(*)\"" +
		"-avaliacao \"*\"");
	}
}
