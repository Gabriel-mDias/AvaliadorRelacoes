/*
 * Created on Jun 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author nseco, Besugo
 * Selector de ALTs para a avaliação relaxada de ALTs
 * 
 */
public abstract class AltAlignmentSelector extends HaremEvaluator implements Runnable
{
	private boolean _debug;

	private EvaluatedAlignmentProcessor _processor;

	public AltAlignmentSelector(String alignmentsFile, boolean useTags, EvaluatedAlignmentProcessor processor,
			boolean debug)
	{
		super(alignmentsFile, useTags);
		_processor = processor;
		_debug = debug;
	}

	protected abstract EvaluatedALTAlignment createNewEvaluatedALTAlignment();

	public void run()
	{
		BufferedReader reader = null;
		String buffer;
		int state = 0;
		LinkedList<EvaluatedALTAlignment> alternatives = null;
		EvaluatedALTAlignment currentALT = null;

		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			if (this instanceof SemanticAltAlignmentSelector)
			{
				buffer = reader.readLine();
				((SemanticAltAlignmentSelector) this)._filter = new EntitiesAttributesFilter(buffer, _tagBase);
				System.out.println(buffer);
			}

			while ((buffer = reader.readLine()) != null)
			{				
				if (state == 0 && isALT(buffer))
				{
					alternatives = new LinkedList<EvaluatedALTAlignment>();
					state = 1;
					continue;
				}

				if (state == 1 && isALTn(buffer))
				{
					currentALT = createNewEvaluatedALTAlignment();
					state = 2;
					continue;
				}

				if (state == 2 && isClosingALTn(buffer))
				{
					alternatives.add(currentALT);
					state = 3;
					continue;
				}

				if (state == 3 && isALTn(buffer))
				{
					currentALT = createNewEvaluatedALTAlignment();
					state = 2;
					continue;
				}

				if (state == 3 && isClosingALT(buffer))
				{
					Collections.sort(alternatives); //a melhor fica no fim!
					if (!_debug)
					{
						updateWeights(alternatives.getLast(), alternatives.size());
						printAlternative(alternatives.getLast());
					}
					else
					{
						printAlternatives(alternatives);
					}
					currentALT = null;
					alternatives = null;
					state = 0;
					continue;
				}

				if (state == 2)
				{
					currentALT.addAlignment(_processor.getEvaluatedAlignment(buffer));
					continue;
				}

				//espurios do ALT - entre </ALTN> e </ALT>
				if (state == 3)
				{
					continue;
				}

				System.out.println(buffer);

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

	private void updateWeights(EvaluatedALTAlignment altAlignment, int numAlts){

		for(EvaluatedAlignment alignment : altAlignment.getAlternatives()){
			for(int i = 0; i < alignment.getAlignedList().size(); i++){
				((SemanticEvaluatedAlignment)alignment).resetAltWeight(i, numAlts);	
			}
		}
	}

	private void printAlternatives(LinkedList alternatives)
	{
		EvaluatedALTAlignment current;
		int counter = 0;

		System.out.println(_tagBase.openTag(_tagBase.getAltTag()));

		for (Iterator i = alternatives.iterator(); i.hasNext();)
		{
			counter++;
			current = (EvaluatedALTAlignment) i.next();

			System.out.print(_tagBase.openTag(_tagBase.getAltTag() + counter));
			System.out.println(current.getDebugInfo().toString());
			printAlternative(current);

			System.out.println(_tagBase.closeTag(_tagBase.getAltTag() + counter));
		}
		System.out.println(_tagBase.closeTag(_tagBase.getAltTag()));
	}

	private void printAlternative(EvaluatedALTAlignment alt)
	{
		for (Iterator<EvaluatedAlignment> i = alt.getAlternativesIterator(); i.hasNext();)
		{
			System.out.println(i.next());
		}
	}

	private boolean isALT(String buffer)
	{
		if (buffer.equals(_tagBase.openTag(_tagBase.getAltTag())))
		{
			return true;
		}

		return false;
	}

	private boolean isALTn(String buffer)
	{
		if (buffer.matches(_tagBase.openTag(_tagBase.getAltTag() + "[123456789]")))
		{
			return true;
		}

		return false;
	}

	private boolean isClosingALT(String buffer)
	{
		if (buffer.equals(_tagBase.closeTag(_tagBase.getAltTag())))
		{
			return true;
		}

		return false;
	}

	private boolean isClosingALTn(String buffer)
	{
		if (buffer.matches(_tagBase.closeTag(_tagBase.getAltTag() + "[123456789]")))
		{
			return true;
		}
		return false;
	}

}
