/*
 * Created on Jul 15, 2005
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
public class IdentificationDuplicateFinder extends DuplicateFinder
{

	protected boolean canBeDuplicate(EvaluatedAlignment current)
	{
		NamedEntity last = _previous.getLastAlignment();
		NamedEntity first = current.getFirstAlignment();

		return !(_previous.getScore(last).equals(IndividualAlignmentEvaluator.CORRECT)
				|| current.getScore(first).equals(IndividualAlignmentEvaluator.CORRECT)
				|| current.getScore(first).equals(IndividualAlignmentEvaluator.SPURIOUS) || _previous.getScore(last)
				.equals(IndividualAlignmentEvaluator.SPURIOUS));
	}

}
