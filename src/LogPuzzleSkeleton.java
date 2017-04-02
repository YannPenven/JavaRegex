import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogPuzzleSkeleton {

	private static List<URL> read_urls(String filename) {
		/*
		 * Returns a list of the puzzle urls from the given log file, extracting
		 * the hostname from the filename itself. Screens out duplicate urls and
		 * returns the urls sorted into increasing order.
		 */
		String base_url = extractBaseUrlFromFileName(filename);

		//TODO: Regex pour extraire les bonnes url
		String patern = "\"GET (.*(puzzle)\\S*)";
		Pattern urlPattern = Pattern.compile(patern);



		List<URL> fullUrls = null;

		BufferedReader f = null;
		
		try {
            Stream<String> stream = Files.lines(Paths.get(filename));
			List<String> urls = findAllUrlsMatchingPattern(urlPattern, stream);
			urls = removeDuplicates(urls);
			sortUrlList(urls);
			fullUrls = formatFullUrls(base_url, urls);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			close(f);
		}

		return fullUrls;
	}

	private static String extractBaseUrlFromFileName(String filename) {
		//TODO
		String patern = "_(\\S*)";
		Pattern urlPattern = Pattern.compile(patern);
		Matcher match = urlPattern.matcher(filename);
		boolean result = match.matches();
		if (result){
			return match.group();
		}
		throw new IllegalArgumentException("filename does not containe matching url patern");

	}

	private static List<String> findAllUrlsMatchingPattern(Pattern urlPattern, Stream<String> stream)
			throws IOException {
		//String line;
		List<String> urls = new ArrayList<String>();
        urls = stream
				.filter(line -> {
                                    Matcher match = urlPattern.matcher(line);
                                    boolean result = match.matches();
                                    if(result){
                                        //urls.add(match.group());
                                        return result;
                                    }
                                    return false;
				                })
                .map(s -> {
					Matcher match = urlPattern.matcher(s);
					boolean result = match.matches();
					if(result){
						return match.group();
					}
                	return null;
				})
				.collect(Collectors.toList());;
		return urls;
	}

	private static List<String> removeDuplicates(List<String> list) {
		//TODO: supprimer les doublons
        Set<String> hs = new HashSet<>();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);
        return list;
	}

	private static void sortUrlList(List<String> urls) {
		//TODO: trier la liste
        Pattern pattern = Pattern.compile("(.{4})\\.jpg");
        Collections.sort(urls, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Matcher match01 = pattern.matcher(o1);
                Matcher matche02 = pattern.matcher(o2);
                boolean result01 = match01.matches();
                boolean result02 = matche02.matches();
                if(result01 && result02){
                    return match01.group().compareTo(matche02.group());
                }
                return o1.compareTo(o2);
            }
        });
	}

	private static List<URL> formatFullUrls(String baseUrl, List<String> urls)
			throws MalformedURLException {
		List<URL> fullUrls = new ArrayList<URL>();
		for (String urlString : urls) {
			fullUrls.add(new URL("https://" + baseUrl + urlString));
		}
		return fullUrls;
	}

	private static void downloadImages(List<URL> imgUrls, String destDirName) {

		// Create destination directory if it doesn't exist
		File destDir = new File(destDirName);
		if (!destDir.exists())
			destDir.mkdir();

		// Create html file
		Path htmlFilePath = Paths.get(destDirName + "index.html");

		PrintWriter htmlFile = null;
		
		try {
			htmlFile = new PrintWriter(Files.newBufferedWriter(htmlFilePath,
					StandardCharsets.UTF_8, StandardOpenOption.CREATE));
			htmlFile.println("<html><body>");

			int i = 0;
			for (URL url : imgUrls) {
				i++;
				String imgFileName = "img" + i + ".jpg";
				htmlFile.print("<img src=\"" + imgFileName + "\">");
				Path destinationPath = Paths.get(destDirName + imgFileName);
				if (!destinationPath.toFile().exists())
					Files.copy(url.openStream(), destinationPath,
							StandardCopyOption.REPLACE_EXISTING);
			}

			htmlFile.println("</body></html>");

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			close(htmlFile);
		}

	}

	public static void close(Closeable c) {
	     if (c == null) return; 
	     try {
	         c.close();
	     } catch (IOException e) {
	         //log the exception
	     }
	}


	public static void main(String[] args) {
		List<URL> urlsToDownload = read_urls("place_code.google.com");
		//TODO: Compléter avec le chemin du dossier destination
		downloadImages(urlsToDownload, "image");
	}

}
