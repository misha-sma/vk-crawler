package crawler.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import crawler.data.repository.SolrService;
import crawler.data.entity.Record;

@RestController
public class SearchController {
	public static final int PAGE_SIZE = 20;
	public static final String HL_PRE_TAG_HTML = "<span style=\"color:#007700; font-weight:bold; font-style:italic;\">";
	public static final String HL_POST_TAG_HTML = "</span>";

	@Autowired
	private SolrService solrService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHomePage(@RequestParam(value = "query", required = false) String query) {
		if (query == null || query.trim().isEmpty()) {
			try (InputStream is = this.getClass().getResourceAsStream("/templates/home.html")) {
				byte[] bytes = is.readAllBytes();
				return new String(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "Error!!! Can't get home page";
		}
		System.out.println("search query=" + query);
		List<Record> records = solrService.search(query, 0, PAGE_SIZE);
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("<meta charset=\"UTF-8\" />\n");
		builder.append("<body>\n");
		builder.append("<table>\n");
		builder.append("<tr><th>number</th><th>document</th></tr>\n");
		for (int i = 0; i < records.size(); ++i) {
			Record record = records.get(i);
			builder.append("<tr><td>" + (i + 1) + "</td><td>" + StringEscapeUtils.escapeHtml4(record.getText())
					.replace(SolrService.HL_PRE_TAG, HL_PRE_TAG_HTML).replace(SolrService.HL_POST_TAG, HL_POST_TAG_HTML)
					+ "</td></tr>\n");
		}
		builder.append("</table>\n");
		builder.append("</body>\n");
		builder.append("</html>");
		return builder.toString();
	}

}
