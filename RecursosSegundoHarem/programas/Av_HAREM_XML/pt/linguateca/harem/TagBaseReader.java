package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

public class TagBaseReader
{
	private final static String DEFAULT_CONF_FILE = "harem.conf";

	private final static String COMMENT = "#";
	private final static String CAT = "C";
	private final static String TIPO = "T";
	private final static String SUB = "S";
	private final static String REF = "R";
	private final static String TEMPO_REF = "X";
	private final static String SENTIDO = "Y";
	private final static String SEP = ":";
	private final static String SEP_INVERSAS = ";";

	private String currCat;
	private String currType;
	private String currSub;

	//private HashMap<String, HashMap<String, Set<String>>> _categories;
	private EntitiesAttributesTree _categories;
	private HashMap<String, Set<String>> _tempoRefs;
	private HashMap<String, Set<String>> _tempoSentidos;
	private LinkedList<String[]> _tiposRel; //a primeira e' a identidade!!!

	private String _file;

	public TagBaseReader()
	{
		this(DEFAULT_CONF_FILE, null);
	}

	public TagBaseReader(HashMap<String, Double> weights)
	{
		this(DEFAULT_CONF_FILE, weights);
	}

	public TagBaseReader(String file, HashMap<String, Double> weights)
	{
		_file = file;		
		//_categories = new HashMap<String, HashMap<String, Set<String>>>();
		_categories = new EntitiesAttributesTree();
		_tempoRefs = new HashMap<String, Set<String>>();
		_tempoSentidos = new HashMap<String, Set<String>>();
		_tiposRel = new LinkedList<String[]>();
	}

	public boolean load()
	{
		try
		{		
			BufferedReader reader = new BufferedReader(new FileReader(_file));

			String linha = null;
			while((linha = reader.readLine()) != null){
				if(linha.equals(""))
					break;
				if(linha.startsWith(COMMENT))
					continue;

				parse(linha);
			}

		} catch (FileNotFoundException e) {
			System.out.println("Ficheiro de configuracao nao encontrado! " + _file);
			return false;
			//System.exit(-1);
		} catch (IOException e) {
			System.out.println("Excepcao na leitura da configuracao!");
			return false;
			//System.exit(-1);
		} catch (FormatoInvalidoException e) {
			System.out.println("O ficheiro com as categorias nao tem o formato correcto!\n"/* +
			"CATEGORIA:TIPO1,TIPO2..."*/);
			return false;
			//System.exit(-1);
		}

		_categories.fillAttributesWithOutro();
		return true;
	}

	private void parse(String linha) throws FormatoInvalidoException{

		String[] lados =  linha.split(SEP);

		if(lados.length < 2)
			throw new FormatoInvalidoException();

		if(lados[0].equals(CAT)){

			//HashMap<String, Set<String>> tipos = new HashMap<String, Set<String>>();

			//para haver o tipo vazio
			//tipos.put("", null);
			//_categories.put(lados[1], tipos);
			currCat = lados[1].trim();
			_categories.newCategory(currCat);

		} else if(lados[0].equals(TIPO)){

			if(currCat == null)
				throw new FormatoInvalidoException();

			//HashMap<String, Set<String>> cat = _categories.get(catActual);
			//Set<String> subs = new HashSet<String>();

			//para haver o subtipo vazio
			//subs.add("");
			currType = lados[1].trim();
			_categories.newType(currCat, currType);

		} else if(lados[0].equals(SUB)) {

			if(currType == null)
				throw new FormatoInvalidoException();

			//Set<String> tipo = _categories.get(catActual).get(tipoActual);

			currSub = lados[1].trim();
			_categories.newSubtype(currCat, currType, currSub);

		} else if(lados[0].equals(REF)) {

			String[] rels = lados[1].trim().split(SEP_INVERSAS);
			if(rels.length == 2){
				_tiposRel.add(rels);

			} else { //quando nao ha inversa, como em outra
				String[] iguais = {rels[0].trim(), null};				
				_tiposRel.add(iguais);
			}

		} else if(lados[0].equals(TEMPO_REF)) {

			String chave = chave(currCat, currType, currSub);

			Set<String> lista = _tempoRefs.get(chave);
			if(lista == null) {
				_tempoRefs.put(chave, new HashSet<String>());
				lista = _tempoRefs.get(chave);
			}
			lista.add(lados[1].trim());

		} else if(lados[0].equals(SENTIDO)) {

			String chave = chave(currCat, currType, currSub);

			Set<String> lista = _tempoSentidos.get(chave);
			if(lista == null) {
				_tempoSentidos.put(chave, new HashSet<String>());
				lista = _tempoSentidos.get(chave);
			}
			lista.add(lados[1].trim());

		} else throw new FormatoInvalidoException();
	}

	private String chave(String catActual, String tipoActual, String subActual) throws FormatoInvalidoException{

		if(catActual == null)
			throw new FormatoInvalidoException();

		String chave = catActual;
		chave += (tipoActual != null ? SEP+tipoActual : "");
		chave += (subActual != null ? SEP+subActual : "");

		return chave;
	}

	public TreeMap<String, TreeMap<String, Set<String>>> getEntityTags()
	{
		return _categories.getTree();
	}

	public EntitiesAttributesTree getEntityAttributes()
	{
		return _categories;
	}

	public static void main(String[] args)
	{
		TagBaseReader reader = new TagBaseReader();
		reader.load();

		System.out.println(reader.getEntityAttributes());
	}

	public LinkedList<String[]> getTiposRef() {
		return _tiposRel;
	}

	public String getInverse(String relation) {

		for(String[] rels : _tiposRel){

			//System.out.println(rels[0]+" - "+rels[1]);

			if(relation.equals(rels[0]))
				return rels[1];
			else if(relation.equals(rels[1]))
				return rels[0];
		}
		return null;
	}

	/** indica se um tipo de relação está definido como directo: rel1;rel2 -> rel1 é directo **/
	public boolean isDirectRelationType(String type)
	{
		for(String[] rels : _tiposRel){

			//System.out.println(rels[0]+" - "+rels[1]);

			if(type.equals(rels[0]))
				return true;
			else if(type.equals(rels[1]))
				return false;
		}
		
		return false;
	}
}

class FormatoInvalidoException extends Exception{

	public FormatoInvalidoException(){
		super("Ficheiro de configuracao apresenta um formato invalido!");
	}
}