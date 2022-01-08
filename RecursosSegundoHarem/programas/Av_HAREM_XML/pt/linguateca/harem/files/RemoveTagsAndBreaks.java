package pt.linguateca.harem.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class RemoveTagsAndBreaks extends Thread{

	private BufferedReader _reader;

	public RemoveTagsAndBreaks(String doc){

		_reader = createReader(doc);
	}

	private BufferedReader createReader(String path){

		try {
			return new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void run(){

		String line = null;
		
		try{
			while((line = _reader.readLine()) != null){
				
				int index = firstNoWhitespaceChar(line);
				
				if(index >= 0)
					line = line.substring(index);
				else
					continue;
					
				if(line.startsWith("<P>"))
					System.out.println(line);
				else
					System.out.print(line +" ");
				
				/*if(line.startsWith("<P>"))
					System.out.println(noTags(line));
				else
					System.out.print(noTags(line)+" ");
				*/
			}		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int firstNoWhitespaceChar(String s){
		int i = 0;
		
		for(i = 0; i < s.length(); i++)
			if(!Character.isWhitespace(s.charAt(i)))
				return i;
		
		return -1;
	}
	
	private String noTags(String s){
		return s.replaceAll("\\<.*?\\>", "");
	}
	
	public static void main(String args[]){
		
		try {
			System.setOut(new PrintStream(args[0]+"_clean.txt"));
			new RemoveTagsAndBreaks(args[0]).start();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
