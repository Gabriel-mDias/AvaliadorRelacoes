package pt.linguateca.harem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * 
 * @author Besugo
 * Organizador de ALTs
 *
 */
public class AltAlignmentOrganizer extends HaremEvaluator implements Runnable
{
	private boolean _debug;

	private EvaluatedAlignmentProcessor _processor;

	public AltAlignmentOrganizer(String alignmentsFile, boolean useTags, boolean debug)
	{
		super(alignmentsFile, useTags);
		_processor = new IdentificationEvaluatedAlignmentProcessor();
		_debug = debug;
		new Thread(this).start();
	}

	public void run()
	{		
		BufferedReader reader = null;
		String buffer;
		int state = 0;

		EvaluatedAlignment currentAlignment = null;
		LinkedList<LinkedList<EvaluatedAlignment>> alternatives = null;
		LinkedList<EvaluatedAlignment> alignments = null;
		HashMap<AltSegmentation, LinkedList<EvaluatedAlignment>> segmentationsMap = null;
		LinkedList<NamedEntity> spuriousInAlt = null;
		Set<NamedEntity> spuriousLeftInAlt = new HashSet<NamedEntity>();

		//int participationEntityNumAlternatives = 0;
		
		try
		{
			reader = new BufferedReader(new FileReader(_alignmentsFile));

			while ((buffer = reader.readLine()) != null)
			{				
				if (state == 0 && isALT(buffer))
				{
					alternatives = new LinkedList<LinkedList<EvaluatedAlignment>>();
					segmentationsMap = new HashMap<AltSegmentation, LinkedList<EvaluatedAlignment>>();
					spuriousInAlt = new LinkedList<NamedEntity>();
					state = 1;
					continue;
				}

				if (state == 1 && isALTn(buffer))
				{
					alignments = new LinkedList<EvaluatedAlignment>();
					state = 2;
					continue;
				}

				if (state == 2 && isClosingALTn(buffer))
				{					
					AltSegmentation newSegmentation = getGoldenSegmentation(alignments);
					LinkedList<EvaluatedAlignment> existing = segmentationsMap.get(newSegmentation);

					if(existing == null){

						alternatives.add(alignments);
						segmentationsMap.put(newSegmentation, alignments);

					} else {

						merge(existing, alignments);

						//System.out.println("ALIGNMENTS= "+alignments);
						//System.out.println("ALTERNATIVES= "+alternatives);
					}

					state = 3;
					continue;
				}

				if (state == 3 && isALTn(buffer))
				{
					alignments = new LinkedList<EvaluatedAlignment>();
					state = 2;
					continue;
				}

				if (state == 3 && isClosingALT(buffer))
				{
					printAlternatives(alternatives, spuriousInAlt);
					spuriousLeftInAlt.addAll(spuriousInAlt);
					alignments = null;
					alternatives = null;
					state = 0;
					continue;
				}

				if (state == 2)
				{					
					currentAlignment = _processor.getEvaluatedAlignment(buffer);

					if(!currentAlignment.isSpurious()) {

						alignments.add(currentAlignment);
						removeEntityFromList(currentAlignment.getGoldenEntity(), spuriousInAlt);

					} else {

						//System.out.println(currentAlignment);
						//System.out.println(currentAlignment.getAlignedList());

						for(NamedEntity entity : currentAlignment.getAlignedList()){

							//se nao estiver como espuria, nao tiver sido ja' usada noutro ALT
							//e nao houver outra correcta com o mesmo texto 
							if(!spuriousInAlt.contains(entity) &&
									!containsEntity(entity, getGoldenSegmentation(alternatives)) &&
									!containsEntityText(entity, alignments))
							{
								spuriousInAlt.add(entity);
							}
						}
					}
					continue;
				}
				
				if(!isEvaluatable(buffer))
				{
					System.out.println(buffer);
				}
				else
				{
					currentAlignment = _processor.getEvaluatedAlignment(buffer);		
					
					if(!currentAlignment.isSpurious())
						System.out.println(buffer);
					else
					{
						if(!containsEntityId(spuriousLeftInAlt, currentAlignment.getFirstAlignment().getId()))
							System.out.println(buffer);
					}
					
					/*if(currentAlignment.isNullAligned())
						System.out.println(buffer);
					
					else if(!currentAlignment.isSpurious() ||
							(currentAlignment.isSpurious() &&
							!containsEntityId(spuriousLeftInAlt, currentAlignment.getFirstAlignment().getId())))
					{	
						participationEntityNumAlternatives = currentAlignment.getFirstAlignment().getNumAltsAttribute();
						printAlignment(currentAlignment, 1.0/participationEntityNumAlternatives);
					}*/
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean containsEntity(NamedEntity entity, LinkedList<AltSegmentation> alternatives){

		for(AltSegmentation seg : alternatives)
			if(seg.containsEntity(entity))
				return true;

		return false;
	}

	private void removeEntityFromList(NamedEntity entity, LinkedList<NamedEntity> list){
		String entityString = entity.getEntity();
		for(NamedEntity tmp : list){
			if(entityString.equals(tmp.getEntity())){
				list.remove(tmp);
				break;
			}
		}
	}

	private boolean containsEntityText(NamedEntity entity, LinkedList<EvaluatedAlignment> alignments){

		NamedEntity golden = null;
		for(EvaluatedAlignment alignment : alignments){
			golden = alignment.getGoldenEntity();
			if(golden.getEntity().equals(entity.getEntity()))
				return true;
		}

		return false;
	}

	private LinkedList<AltSegmentation> getGoldenSegmentation(LinkedList<LinkedList<EvaluatedAlignment>> alternatives){

		LinkedList<AltSegmentation> toReturn = new LinkedList<AltSegmentation>();

		for(LinkedList<EvaluatedAlignment> alignments : alternatives){
			toReturn.add(getGoldenSegmentation(alignments));
		}

		return toReturn;
	}

	private AltSegmentation getGoldenSegmentation(LinkedList<EvaluatedAlignment> alignments){

		AltSegmentation toReturn = new AltSegmentation();

		for(EvaluatedAlignment  alignment : alignments){
			toReturn.addEntity(alignment.getGoldenEntity().getEntity());
		}

		return toReturn;
	}

	private void merge(LinkedList<EvaluatedAlignment> alignments1, LinkedList<EvaluatedAlignment> alignments2){

		NamedEntity current, otherCurrent;

		Iterator<EvaluatedAlignment> i = alignments1.iterator();
		Iterator<EvaluatedAlignment> j = alignments2.iterator();

		while(i.hasNext() && j.hasNext()){

			current = i.next().getGoldenEntity();
			otherCurrent = j.next().getGoldenEntity();

			for(AttributeTuple at : otherCurrent.getAttributeTupleSet())
				if(!current.containsAttributeTuple(at))
					current.addAttributeTuple(at);
		}
	}
	
	private boolean containsEntityId(Set<NamedEntity> entities, String id)
	{
		for(NamedEntity entity : entities)
		{
			if(entity.getId().equals(id))
				return true;
		}
		
		return false;
	}

	private boolean containsSegmentation(LinkedList<AltSegmentation> list, AltSegmentation segmentation){
		for(AltSegmentation seg : list)
			if(segmentation.sameSegmentation(seg))
				return true;

		return false;
	}

	private AltSegmentation getExistingSameSegmentation(LinkedList<AltSegmentation> list, AltSegmentation segmentation){
		for(AltSegmentation seg : list)
			if(segmentation.sameSegmentation(seg))
				return seg;

		return null;
	}

	/*private String alignmentKey(LinkedList<EvaluatedAlignment> alignments){

		final String ne = "[NE]";
		String key = "";
		for(EvaluatedAlignment alignment : alignments){
			key += ne+alignment.getGoldenEntity();
		}

		return key;
	}*/

	private void printAlternatives(LinkedList<LinkedList<EvaluatedAlignment>> alternatives,
			LinkedList<NamedEntity> spurious)
	{		
		LinkedList<EvaluatedAlignment> currentAlt;
		LinkedList<LinkedList<EvaluatedAlignment>> noEmpty = removeEmpty(alternatives);
		int counter = 0;
		//se nao ha' alternativas na CD, o peso da penalizacao por EM espuria e' 1!
		double weight = noEmpty.isEmpty() ? 1 : 1.0 / noEmpty.size();

		System.out.println(_tagBase.openTag(_tagBase.getAltTag()));

		for (Iterator<LinkedList<EvaluatedAlignment>> i = noEmpty.iterator(); i.hasNext();)
		{			
			currentAlt = i.next();
			//if(!currentAlt.isEmpty()){
			counter++;
			System.out.println(_tagBase.openTag(_tagBase.getAltTag() + counter));
			printAlignments(currentAlt, weight);
			System.out.println(_tagBase.closeTag(_tagBase.getAltTag() + counter));
			//}
		}

		for(NamedEntity entity : spurious){
			IdentificationEvaluatedAlignment spuriousAlignment = new IdentificationEvaluatedAlignment();
			spuriousAlignment.setGoldenEntity((NamedEntity)entity.clone());
			spuriousAlignment.addAlignment(entity, IndividualAlignmentEvaluator.SPURIOUS);
			spuriousAlignment.setSpurious();
			//printAlignment(spuriousAlignment, weight);
			printAlignment(spuriousAlignment, 1);
		}

		System.out.println(_tagBase.closeTag(_tagBase.getAltTag()));
	}

	private LinkedList<LinkedList<EvaluatedAlignment>> removeEmpty(LinkedList<LinkedList<EvaluatedAlignment>> list){
		LinkedList<LinkedList<EvaluatedAlignment>> toReturn = new LinkedList<LinkedList<EvaluatedAlignment>>();

		for(LinkedList<EvaluatedAlignment> alignments : list)
			if(!alignments.isEmpty())
				toReturn.add(alignments);

		return toReturn;
	}

	private void printAlignments(LinkedList<EvaluatedAlignment> alignments, double weight)
	{
		for(EvaluatedAlignment alignment : alignments)
			printAlignment(alignment, weight);
	}

	private void printAlignment(EvaluatedAlignment alignment, double weight)
	{
		alignment.setAltWeigth(weight);
		System.out.println(alignment);
	}

	private boolean isALT(String buffer)
	{
		if (buffer.equals(_tagBase.openTag(_tagBase.getAltTag())))
		{
			return true;
		}

		return false;
	}

	private boolean isALTn(String buffer)
	{
		if (buffer.matches(_tagBase.openTag(_tagBase.getAltTag() + "[123456789]")))
		{
			return true;
		}

		return false;
	}

	private boolean isClosingALT(String buffer)
	{
		if (buffer.equals(_tagBase.closeTag(_tagBase.getAltTag())))
		{
			return true;
		}

		return false;
	}

	private boolean isClosingALTn(String buffer)
	{
		if (buffer.matches(_tagBase.closeTag(_tagBase.getAltTag() + "[123456789]")))
		{
			return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		String alignments = null;
		boolean useTags = false;
		boolean debug = false;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-alinhamento"))
			{
				i++;
				alignments = args[i];
				continue;
			}

			if (args[i].equals("-etiquetas"))
			{
				i++;
				useTags = args[i].equalsIgnoreCase("sim");
				continue;
			}

			if (args[i].equals("-depurar"))
			{
				i++;
				debug = args[i].equalsIgnoreCase("sim");
				continue;
			}
		}

		if (alignments == null)
		{
			printSynopsis();
			return;
		}

		new AltAlignmentOrganizer(alignments, useTags, debug);
	}

	private static void printSynopsis()
	{
		System.out.println("Utiliza\u00e7\u00e3o:");
		System.out
		.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AltAlignmentOrganizer -alinhamento <ficheiro_alinhamentos_avaliados>");
		System.out.println("\n");
		System.out.println("Exemplo:");
		System.out.println("java -Dfile.encoding=ISO-8859-1 -cp .;lib/jdom.jar pt.linguateca.harem.AltAlignmentOrganizer -alinhamento participacao.alinhado.avalida.veu");
	}

	class AltSegmentation{

		protected LinkedList<String> _entities;

		public AltSegmentation(){
			_entities = new LinkedList<String>();
		}

		public void addEntity(String entity){
			_entities.add(entity);
		}

		public boolean sameSegmentation(AltSegmentation other){

			LinkedList<String> otherEntities = other._entities;

			if(_entities.size() != otherEntities.size())
				return false;

			for(int i = 0; i < _entities.size(); i++)
				if(!_entities.get(i).equals(otherEntities.get(i)))
					return false;

			return true;
		}

		public boolean containsEntity(NamedEntity entity){

			for(String ne : _entities){
				if(ne.equals(entity.getEntity()))
					return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
			+ ((_entities == null) ? 0 : _entities.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final AltSegmentation other = (AltSegmentation) obj;
			if (_entities == null) {
				if (other._entities != null)
					return false;
			} else if (!_entities.equals(other._entities))
				return false;
			return true;
		}

		public String toString(){
			String toString = "";

			for(String s : _entities)
				toString += "<" + s + ">";

			return toString;
		}

		/*public void merge(AltSegmentation other){

			NamedEntity current, otherCurrent;
			for(int i = 0; i < _entities.size(); i++){
				current = _entities.get(i);
				otherCurrent = other._entities.get(i);

				if(!current.getAttributeTuples().equals(otherCurrent.getAttributeTuples()))
					System.out.println("PIM!");
				else
					System.out.println("PUM!");
			}	
		}*/
	}

	/*class AltSegmentation{

		protected LinkedList<NamedEntity> _entities;

		public AltSegmentation(){
			_entities = new LinkedList<NamedEntity>();
		}

		public void addEntity(NamedEntity entity){
			_entities.add(entity);
		}

		public boolean sameSegmentation(AltSegmentation other){

			LinkedList<NamedEntity> otherEntities = other._entities;

			if(_entities.size() != otherEntities.size())
				return false;

			for(int i = 0; i < _entities.size(); i++)
				if(!_entities.get(i).getEntity().equals(otherEntities.get(i).getEntity()))
					return false;

			return true;
		}

		public boolean containsEntity(NamedEntity entity){

			for(NamedEntity ne : _entities){
				if(ne.getEntity().equals(entity.getEntity()))
					return true;
			}

			return false;
		}

		public void merge(AltSegmentation other){

			NamedEntity current, otherCurrent;
			for(int i = 0; i < _entities.size(); i++){
				current = _entities.get(i);
				otherCurrent = other._entities.get(i);

				if(!current.getAttributeTuples().equals(otherCurrent.getAttributeTuples()))
					System.out.println("PIM!");
				else
					System.out.println("PUM!");
			}

		}
	}*/
}
