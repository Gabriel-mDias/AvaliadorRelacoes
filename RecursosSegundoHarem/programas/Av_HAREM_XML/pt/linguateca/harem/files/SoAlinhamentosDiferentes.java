package pt.linguateca.harem.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import pt.linguateca.harem.NamedEntity;
import pt.linguateca.harem.NamedEntityComparationTable;
import pt.linguateca.harem.TagBase;
import pt.linguateca.harem.TaggedDocument;

public class SoAlinhamentosDiferentes {

	private BufferedReader reader;
	private TagBase _tagBase;

	private final boolean COMPARA_CAT = true;
	private final boolean COMPARA_TIPO = true;
	private final boolean COMPARA_SUBTIPO = false;

	private final String ALIGNMENT_SEPARATOR = " ---> ";
	private final String ATRIBUTE_SEPARATOR = ";";

	public SoAlinhamentosDiferentes(String file){

		try {
			reader = new BufferedReader(new FileReader(file));

		} catch (FileNotFoundException e) {
			System.out.println("Ficheiro nao encontrado! "+file);
			e.printStackTrace();
		}

		_tagBase = TagBase.getInstance();
	}

	public void filtra(){

		String linha = null;

		try {
			
			String um = reader.readLine();
			String dois = reader.readLine();
			
			print("LADO UM", dois, "", "", "", "");
			print("LADO DOIS", um, "", "", "", "");
						
			//print("ENTIDADE", "PROBLEMA", "LADO UM", "LADO DOIS");
			
			boolean alts = false;
			while((linha = reader.readLine()) != null){
				
				if(!linha.startsWith("<")){
					
					if(linha.startsWith(TaggedDocument.SUBMISSION_ALT)){
						if(!alts)
							System.out.println(linha);
						
						alts = true;
					}
										
					else
						System.out.println(linha);
				}

				//<VERIFICACAO_MANUAL>
				else if (linha.startsWith(_tagBase.openTag(_tagBase.getManualVerificationTag())))
					System.out.println("O alinhador teve um problema na identificacao dos atomos");

				else if (linha.startsWith(_tagBase.openTag(_tagBase.getAltTag()))) {
					
					mostraAlts();
					
				} else {
					comparaAlinhamentos(linha);
				}	
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void mostraAlts() throws IOException{
		
		String linha = null;
		//int i = 1;
		
		for(int i = 1; !(linha = reader.readLine()).equals(_tagBase.closeTag(_tagBase.getAltTag())); i++){
			
			String alternativa = _tagBase.getAltTag().concat(i+"");
			if(linha.equals(_tagBase.openTag(alternativa))){
				
				print(alternativa, "", "", "", "", "");
				System.out.println();
				
				for(; !(linha = reader.readLine()).equals(_tagBase.closeTag(alternativa)); )
					comparaAlinhamentos(linha);
			}
		}
	}
	
	private void comparaAlinhamentos(String linha){

		//System.out.println(linha);
		String[] alinhamentos = linha.split(ALIGNMENT_SEPARATOR);
		NamedEntity um = new NamedEntity(alinhamentos[0]);
				
		LinkedList<NamedEntity> lista = NamedEntity.toNamedEntityList(alinhamentos[1]);

		//uma EM de um lado corresponde a mais do outro
		if(lista.size() != 1){

			print(um.getEntity(), "CORRESP", alinhamentos[0], alinhamentos[1], um.getComent(), "");

		} else {

			NamedEntity dois = lista.get(0);
			NamedEntityComparationTable table = um.diffs(dois);
			
				//EM espuria
			if(um.getCategories().size() > 0
					&& um.getCategories().get(0).equals(_tagBase.getSpuriousTag())) {

				print(
						dois.getEntity(),
						_tagBase.getSpuriousTag(),
						"",
						dois.toString(),
						"",
						dois.getComent()
				);

				//EM diferente
			} else if(!table.getEntity()) {

				print(
						"",
						_tagBase.getSimpleEntityTag(),
						um.getEntity(),
						dois.getEntity(),
						um.getComent(),
						dois.getComent());

				//CATEG diferente
			} else if(COMPARA_CAT && !table.getCategory()) {

				print(
						um.getEntity(),
						_tagBase.getCategTag(),
						um.getCategories().toString(),
						dois.getCategories().toString(),
						um.getComent(),
						dois.getComent());
				
				//TIPO diferente			
			} else if(COMPARA_TIPO && !table.getType()) {

				String types = (um.getTypes() != null ? um.getTypes().toString() : ""); 

				print(
						um.getEntity(),
						_tagBase.getTypeTag(),
						types,
						dois.getTypes().toString(),
						um.getComent(),
						dois.getComent());
				
				//SUBTIPO diferente
			} else if(COMPARA_SUBTIPO && !table.getSubtype()) {

				String subtypes = (um.getSubtypes() != null ? um.getSubtypes().toString() : ""); 

				print(
						um.getEntity(),
						_tagBase.getSubtypeTag(),
						subtypes,
						dois.getSubtypes().toString(),
						um.getComent(),
						dois.getComent());
			}	
		}
	}

	private void print(String col1, String col2, String col3, String col4, String col5, String col6){

		String str = col1 + ATRIBUTE_SEPARATOR;
		str += col2 + ATRIBUTE_SEPARATOR;
		str += col3 + ATRIBUTE_SEPARATOR;
		str += col4 + ATRIBUTE_SEPARATOR;;
		
		if(col5 != null)
			str += col5;		
		
		if(col6 != null)
			str += ATRIBUTE_SEPARATOR + col6;
		
		System.out.println(str);
	}

	private String removeId(String em){

		//System.out.println(em.replaceAll(" ID=\".*?\"", ""));
		return em.replaceAll(" ID=\".*?\"", "");
	}

	public static void main(String args[]){
		new SoAlinhamentosDiferentes(args[0]).filtra();
	}
}
