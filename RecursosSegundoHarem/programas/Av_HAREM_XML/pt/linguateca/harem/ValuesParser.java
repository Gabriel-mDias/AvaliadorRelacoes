package pt.linguateca.harem;

/**
 * 
 * @author Besugo
 * 
 * Parser para valores representados na forma atributo(valor)
 */
public class ValuesParser {

	private String _score;
	
	public ValuesParser()
	{
		_score = null;
	}
	
	public void setBuffer(String buffer)
	{
		_score = buffer;
		//System.out.println(_score);
	}
	
	public double getDouble(String str)
	{
		int index = _score.lastIndexOf(str);
		return getDouble(index + str.length());
	}
	
	public int getInt(String str)
	{
		int index = _score.lastIndexOf(str);
		return (int)getDouble(index + str.length());
	}
	
	public String getString(String str)
	{
		int index = _score.lastIndexOf(str) + str.length();
		
		int state = 0;
		String toReturn = "";
		char current;

		for (; index < _score.length(); index++)
		{
			current = _score.charAt(index);

			if (state == 0 && current == '(')
			{
				state = 1;
				continue;
			}

			if (state == 1 && current == ')')
				break;

			toReturn += current;
		}

		return toReturn;
	}
	
	private double getDouble(int index)
	{
		int state = 0;
		String number = "";
		char current;

		for (; index < _score.length(); index++)
		{
			current = _score.charAt(index);

			if (state == 0 && current == '(')
			{
				state = 1;
				continue;
			}

			if (state == 1 && current == ')')
				break;

			number += current;
		}

		return Double.parseDouble(number);
	}
}
