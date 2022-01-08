/*
 * Created on Mar 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.util;


/**
 * @author nseco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AtomicCounter
{

  private int _value;
  /**
   * 
   */
  public AtomicCounter()
  {
    _value = 0;
  }

  public int hashCode()
  {
    return toString().hashCode();
  }
  
  public boolean equals(Object o)
  {    
    if (o instanceof AtomicCounter)
    {
      return ((AtomicCounter)o)._value == _value;
    }    
    return false;
  }
  
  public String toString()
  {
    return Integer.toString(_value);
  }
  
  public Object clone()
  {
    AtomicCounter clone = new AtomicCounter();    
    clone.setValue(_value);    
    return clone;
  }
  
  public int getValue()
  {
    return _value;
  }
  
  public void setValue(int value)
  {
    _value = value;
  }
  
  public int increment()
  {
    _value++;
    return _value;
  }
  
  public int decrement()
  {
    _value--;
    return _value;
  }
}
