package pt.linguateca.harem.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CompareTokens extends Thread{

	private BufferedReader _reader1;
	private BufferedReader _reader2;
	
	public CompareTokens(String doc1, String doc2){

		_reader1 = createReader(doc1);
		_reader2 = createReader(doc2);
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
	
		int current1 = -1;
		int current2 = -1;
		
		String lastSentence1 = "";
		String lastSentence2 = "";
		String lastWord1 = "";
		String lastWord2 = "";
		
		String line = null;
		int stop = 0;
				
		try {
			
			do line = _reader1.readLine();			
			while(line != null && !line.contains("<DOC"));
			
			do line = _reader2.readLine();			
			while(line != null && !line.contains("<DOC"));
			
			while((current1 = _reader1.read()) != -1 && (current2 = _reader2.read()) != -1){
						
				while(Character.isWhitespace(current1)){
					lastSentence1 += lastWord1;
					
					if(!lastSentence1.endsWith(" "))
						lastSentence1 += " ";
						
					lastWord1 = "";
					current1 = _reader1.read();
				}
				
				while(Character.isWhitespace(current2)){
					lastSentence2 += lastWord2;
					
					if(!lastSentence2.endsWith(" "))
						lastSentence2 += " ";
					
					lastWord2 = "";
					current2 = _reader2.read();
				}
								
				//System.out.println((char)current1);
				//System.out.println((char)current2);
				
				lastWord1 += (char)current1;
				lastWord2 += (char)current2;
								
				if(lastWord1.equals("<P>"))
					lastSentence1 = "";
				if(lastWord2.equals("<P>"))
					lastSentence2 = "";
				
				if(current1 != current2 && Character.toLowerCase(current1) != Character.toLowerCase(current2)){
					System.out.println("-----");
					//System.out.println("current1 = "+(char)current1+" VS current2 = "+(char)current2);
					System.out.println("palavra1 = "+lastWord1+" VS palavra2 = "+lastWord2);
					System.out.println(lastSentence1+"\nVS\n"+lastSentence2);
					stop++;
				}
				
				/*if(stop > 10)
					break;*/
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		new CompareTokens(args[0], args[1]).start();
	}
}
