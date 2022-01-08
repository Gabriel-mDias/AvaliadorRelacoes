/*
 * Created on Apr 22, 2005
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
public class HaremEvaluator extends HaremConsoleWriter
{
	protected String _alignmentsFile;

	public HaremEvaluator(String alignments, boolean useTags)
	{
		super(useTags);
		_alignmentsFile = alignments;
	}

	protected boolean isEvaluatable(String buffer)
	{
		return !(buffer.startsWith("#")
				|| buffer.equals("")
				|| buffer.startsWith(_tagBase.getHaremTag())
				|| buffer.startsWith("<" + _tagBase.getAltTag())
				|| buffer.startsWith("</" + _tagBase.getAltTag())
				|| buffer.startsWith(_tagBase.openTag(_tagBase.getOmittedTag()))
				|| buffer.startsWith(_tagBase.openTag(_tagBase.getManualVerificationTag()))
				|| buffer.startsWith(_tagBase.getDocTag())
				|| buffer.startsWith(_tagBase.getEndOfDocTag()));
	}
}
