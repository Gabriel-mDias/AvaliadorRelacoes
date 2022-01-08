/*
 * Created on Mar 25, 2005
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
public class CounterTagParser extends Parser
{
	private MarkedToken _marked;

	private String _recognized;

	private String _count;

	protected void initialize()
	{
		_recognized = null;
		_marked = null;
		_count = null;
	}

	protected void createEntity()
	{
		_marked = new MarkedToken();
		_marked.setToken(_recognized);
		_marked.setCount(Integer.parseInt(_count));
	}

	public Object getEntity()
	{
		return _marked;
	}

	public int doRecognition(int index)
	{
		int terminal = 5;
		int state = 0;
		int i;
		char current;

		_count = "";
		_recognized = "";

		for (i = index; i < _text.length(); i++)
		{
			current = _text.charAt(i);

			if (state == 0 && current == '<')
			{
				state = 1;
				continue;
			}

			if (state == 1 && Character.isDigit(current))
			{
				_count += current;
				continue;
			}

			if (state == 1 && current == '>')
			{
				state = 2;
				continue;
			}

			if (state == 2 && current == '<')
			{
				state = 3;
				continue;
			}

			if (state == 2)
			{
				_recognized += current;
				continue;
			}

			if (state == 3 && current == '/')
			{
				state = 4;
				continue;
			}

			if (state == 4 && Character.isDigit(current))
			{
				continue;
			}

			if (state == 4 && current == '>')
			{
				state = 5;
				continue;
			}

			break;
		}

		if (state == terminal)
		{
			return i;
		}

		_recognized = null;
		return -1;
	}

	public static void main(String[] args)
	{
		CounterTagParser parser = new CounterTagParser();
		int index;

		parser.setText("<1>njnjnjnn</1>");
		index = parser.recognize(0);
		System.out.println(index);
		System.out.println(parser.getEntity().toString());

		parser.setText("<3>njn jnjnn</3>");
		index = parser.recognize(0);
		System.out.println(index);
		System.out.println(parser.getEntity().toString());

	}
}