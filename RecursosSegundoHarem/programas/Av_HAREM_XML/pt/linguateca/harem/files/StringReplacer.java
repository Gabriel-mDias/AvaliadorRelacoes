package pt.linguateca.harem.files;

import java.util.HashMap;

public class StringReplacer {

	private String _string;
	private HashMap<String, Integer> _map;
	
	
	public StringReplacer(String str){
		_string = str;
		_map = new HashMap<String, Integer>();
	}
	
	public String getString(){
		return _string;
	}
	
	public boolean replace(String a, String b){
		
		Integer pos;
		if((pos = _map.get(a)) != null){
			
			String sub = _string.substring(pos);
			int index = sub.indexOf(a) + a.length() + pos;
			if(index < 0)
				return false;
			
			String subReplaced = sub.replaceFirst(a, b);
			_string = _string.replaceFirst(sub, subReplaced);
			_map.put(a, index);
			return true;
			
		} else {
			
			int index = _string.indexOf(a) + a.length();
			if(index < 0)
				return false;
			
			_string = _string.replaceFirst(a, b);
			_map.put(a, index);
			return true;
		}
	}
}
