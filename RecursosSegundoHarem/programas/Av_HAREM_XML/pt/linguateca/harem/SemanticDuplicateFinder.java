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
public class SemanticDuplicateFinder extends DuplicateFinder
{
	protected boolean canBeDuplicate(EvaluatedAlignment current)
	{
		NamedEntity last = _previous.getLastAlignment();
		NamedEntity first = current.getFirstAlignment();
		SemanticScoreParser parser;

		parser = new SemanticScoreParser(_previous.getScore(last));
		double lastWeight = parser.getScoreTuple().getWeight();

		parser = new SemanticScoreParser(current.getScore(first));
		double firstWeight = parser.getScoreTuple().getWeight();

		return (lastWeight > 0 && lastWeight < 1 && firstWeight > 0 && firstWeight < 1);
	}
}