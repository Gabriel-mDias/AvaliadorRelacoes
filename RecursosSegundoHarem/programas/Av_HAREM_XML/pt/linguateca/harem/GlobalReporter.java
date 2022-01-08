/*
 * Created on Jun 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pt.linguateca.harem;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pt.linguateca.util.BucketHashMap;

/**
 * @author nseco
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 * Code Templates
 */
public abstract class GlobalReporter implements Runnable
{
	protected static final int ALL_SUBMISSIONS = 0;

	protected static final int OFFICIAL_SUBMISSIONS = 1;

	protected static final int UNOFFICIAL_SUBMISSIONS = 2;

	protected static final String SELECTIVE = ".selectivo.";

	protected static final String SYSTEM = "Saida";

	protected HashSet<String> _names;

	protected LinkedList<String> _non_oficial;

	protected ReportFilter _reportFilter;

	protected BucketHashMap _bestResults;

	protected boolean _debug;

	protected int _submission_types;

	protected String _dir;

	public static final String DEFAULT_DIR = "participacoes";

	public GlobalReporter(String filter, boolean debug, int submission_types, String unofficialNames, String dir)
	{
		_dir = (dir == null ? DEFAULT_DIR : dir);

		_reportFilter = new ReportFilter(filter);
		_names = new HashSet<String>();
		_bestResults = new BucketHashMap(new LinkedList());
		_non_oficial = new LinkedList<String>();
		_debug = debug;
		_submission_types = submission_types;
		loadUnofficial(unofficialNames);
		loadNames();
	}

	protected abstract void writeOut(LinkedList<? extends Report> reports);

	protected abstract void setBestResults(LinkedList<? extends Report> reports);

	protected abstract Report createReport(String file);

	public void run()
	{
		LinkedList<Report> reports = loadReports();
		//System.out.println(reports);

		setBestResults(reports);
		writeOut(getShuffled(reports));
	}

	protected String getZerofiedNaN(String possibleNaN)
	{		
		double nan = Double.parseDouble(possibleNaN);

		if (Double.isNaN(nan))
			return Double.toString(0);

		return possibleNaN;
	}

	protected LinkedList<Report> loadReports()
	{
		File dir = new File(_dir);

		LinkedList<File> files = getAllFiles(dir, _reportFilter);
		LinkedList<Report> reports = new LinkedList<Report>();

		for (File file : files)
		{
			if (_submission_types == UNOFFICIAL_SUBMISSIONS && isOfficial(file.getName()))
			{
				continue;
			}
			else if (_submission_types == OFFICIAL_SUBMISSIONS && !isOfficial(file.getName()))
			{
				continue;
			}

			reports.add(createReport(file.getPath()));
		}

		return reports;
	}

	private LinkedList<File> getAllFiles(File dir, FileFilter filter)
	{	
		LinkedList<File> allFiles = new LinkedList<File>();

		try {
			// determine all subdirectories
			File files[] = dir.listFiles();
			int fileLength = files.length;
			for (int i = 0; i < fileLength; i++) {
				if (((File) files[i]).isDirectory()) {
					allFiles.addAll(getAllFiles((File) files[i], filter));
				} else {
					if (filter.accept(files[i])) {
						allFiles.add((File) files[i]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return allFiles;
	}

	protected boolean isOfficial(String report)
	{
		for (Iterator<String> i = _non_oficial.iterator(); i.hasNext();)
		{
			if (report.indexOf(i.next().toString()) != -1)
				return false;
		}

		return true;
	}

	protected void writeHeader()
	{
		Calendar cal = Calendar.getInstance();
		System.out.println("<html LANG=\"pt\">");

		System.out.println("<head>");
		System.out.println("<meta http-equiv=Content-Type content=\"text/html; charset=ISO-8859-1\">");
		System.out
		.println("<title>Resultados --- Segundo HAREM --- " + _reportFilter.getFilter().toString() + "</title>");
		System.out.println("<meta http-equiv=\"EXPIRES\" content=\"-1\">");
		System.out.println("<meta http-equiv=\"PRAGMA\" content=\"NO-CACHE\">");
		System.out.println("<meta http-equiv=\"MAX-AGE\" content=\"0\">");
		System.out.println("<meta http-equiv=\"CACHE-CONTROL\" content=\"NO-CACHE\">");
		System.out.println("</head>");

		System.out.print("<b>");

		System.out.print("<br>");
		System.out.println("Resultados <A HREF=http://www.linguateca.pt/HAREM>Segundo HAREM</A> ");
		System.out.print("<br>");
		System.out.println("Ficheiros utilizados: " + _reportFilter.getFilter().toString());
		System.out.print("<br>");
		System.out.println("Pagina gerada automaticamente em: "
				+ hackMonth(cal.getTime().toString(), cal.get(Calendar.MONTH)));

		System.out.print("</b>");
		System.out.println("<body>");
	}

	protected void writeFooter()
	{
		System.out.println("</body>");
		System.out.println("</html>");
	}

	protected void writeValueInCell(String value, boolean highlight)
	{
		if (highlight){
			System.out.print("<td><font color=green><b>" + value + "</b></font></td>");
		}
		else{
			System.out.print("<td>" + value + "</td>");
		}
	}

	private String hackMonth(String timeStamp, int month)
	{
		String hacked = null;
		String tokens[];

		switch (month)
		{
		case 1:
			hacked = timeStamp.replaceAll("Feb", "Fev");
			break;
		case 3:
			hacked = timeStamp.replaceAll("Apr", "Abr");
			break;
		case 4:
			hacked = timeStamp.replaceAll("May", "Mai");
			break;
		case 7:
			hacked = timeStamp.replaceAll("Aug", "Ago");
			break;
		case 8:
			hacked = timeStamp.replaceAll("Sep", "Set");
			break;
		case 9:
			hacked = timeStamp.replaceAll("Oct", "Out");
			break;
		case 11:
			hacked = timeStamp.replaceAll("Dez", "Dec");
			break;
		default:
			hacked = timeStamp;
		}

		tokens = hacked.split(" ");

		hacked = tokens[2];
		hacked += " " + tokens[1];
		hacked += " " + tokens[5];
		hacked += " " + tokens[3];

		return hacked;
	}

	protected String getPercentage(String number)
	{
		double d = Double.parseDouble(number) * 100;
		return d + "";
	}

	protected String pointToComma(String number)
	{
		return number.replaceFirst("\\.", ",");
	}

	/*protected double truncate(double value) {  
		return Math.round(value * 1000) / 1000d;  
	}*/

	protected String truncate(double value) {  
		DecimalFormat df = new DecimalFormat("#.00");  
		return df.format(value);  
	}  

	protected LinkedList getShuffled(Collection list)
	{
		BucketHashMap shuffled = new BucketHashMap(new HashSet());
		LinkedList flatten;

		for (Iterator i = list.iterator(); i.hasNext();)
		{
			shuffled.put("" + (System.currentTimeMillis() + (int) (Math.random() * 100000)), i.next());
		}

		flatten = flatten(shuffled.values());

		// System.out.println(flatten);

		return flatten;
	}

	private LinkedList flatten(Collection values)
	{
		LinkedList flat = new LinkedList();

		for (Iterator i = values.iterator(); i.hasNext();)
		{
			flat.addAll((HashSet) i.next());
		}

		return flat;
	}

	private void loadUnofficial(String names)
	{
		if (names == null)
			return;

		String[] tokens = names.split(":");

		for (int i = 0; i < tokens.length; i++)
		{
			_non_oficial.add(tokens[i]);
		}
	}

	private void loadNames()
	{
		_names.add("meca");
		_names.add("bagdad");
		_names.add("tripoli");
		_names.add("casablanca");
		_names.add("eritreia");
		_names.add("marraquexe");
		_names.add("jerusalem");
		_names.add("kuwait");
		_names.add("teerao");
		_names.add("riad");
		_names.add("cairo");
		_names.add("abudhabi");
		_names.add("dakar");
		_names.add("bahrein");
		_names.add("tunis");
		_names.add("argel");
		_names.add("rabat");
		_names.add("luxor");
		_names.add("bengazi");
		_names.add("doha");
		_names.add("mascate");
		_names.add("damasco");
		_names.add("ancara");
		_names.add("ama");
		_names.add("asmara");
		_names.add("nicosia");
		_names.add("sana");
		_names.add("qatar");
		_names.add("oman");
		_names.add("iemen");
		_names.add("argel");
		_names.add("manama");
		_names.add("gaza");
	}

	class ReportFilter implements FileFilter
	{
		private String[] _filter;

		public ReportFilter(String filter)
		{
			_filter = filter.split(":");
		}

		public boolean accept(File file)
		{
			for (int i = 0; i < _filter.length; i++)
			{
				if (file.getName().endsWith(_filter[i]))
					return true;
			}

			return false;
		}

		public List<String> getFilter()
		{
			return Arrays.asList(_filter);
		}
	}

}
