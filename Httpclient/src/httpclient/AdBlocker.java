package httpclient;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AdBlocker {
	
	public AdBlocker() {
		
	}
	
	/**
	 * 1. Search for images (already done by Dennis)
	 * 2. Check if they are an advertisement
	 * 3a. If not an advertisement, add to imagePaths
	 * 3b. else: replace the advertisement by an image of a cat
	 * by 1) finding the string of the cat image you want to use (to do this, pass the number of cat you want)
	 * and 2) replacing the string of the image by the string of the cat ( imageTag.attr("src", catPath)
	 * @param page
	 * @param info
	 * @return The new web page, edited to make the magic happen
	 */
	public static String blockAdvertisements(String page, ResponseInfo info) {
		while(info.hasResourceRequests()) { //Empty resource requests
			info.getNextResourceRequest();
		}
		// Parse into well formed document for parsing.
		Document htmlpage = Jsoup.parse(page);
		// Search documents for image tags.
		Elements imageTags = htmlpage.getElementsByTag("img");
		ArrayList<String> imagePaths = new ArrayList<>();
		if (!imageTags.isEmpty()) {
			int catCount = 0;
			for (Element imageTag : imageTags) {
				String path = imageTag.attr("src"); //Find the path of the image
				String alt = imageTag.attr("alt");
				if (!isAdvertisementPicture(path, alt)) {
					imagePaths.add(path);
				} else {
					// width and height do not get changed so should stay the same
					// (we only replace the catPath, not the width/height) 
					String catPath = getCatImagePath(catCount);
					imageTag.attr("src", catPath);
				}
				catCount = (catCount + 1) % catHerdSize;
			}

			// For each path, make resource request.
			for (String path : imagePaths) {
				String typeString = path.substring(path.lastIndexOf(".") + 1);
				ContentType type;
				switch (typeString.toLowerCase()) {
				case "jpg":
					type = ContentType.IMAGEJPG;
					break;
				case "png":
					type = ContentType.IMAGEPNG;
					break;
				case "gif":
					type = ContentType.IMAGEGIF;
					break;
				default:
					type = ContentType.UNKNOWN;
					break;
				}
				info.registerResourceRequest(new ResourceRequest(path, type));
			}
		}
		return htmlpage.toString();
	}
	
	private static boolean isAdvertisementPicture(String path) {
		return path.contains("ad");
	}
	
	private static boolean isAdvertisementPicture(String path, String altText) {
		return path.contains("ad") || altText.contains("Ad");
	}
	
	private static String getCatImagePath(int cat) {
		return "catGifSource/giphy" + String.valueOf(cat) + ".gif";
	}
	
	private static int catHerdSize = 10;
	

}
