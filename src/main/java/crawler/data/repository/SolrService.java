package crawler.data.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import crawler.data.entity.Record;
import crawler.util.Util;
import jakarta.annotation.PostConstruct;

@Service
public class SolrService {
	public static final String HL_PRE_TAG = "@@@hl_pre@@@";
	public static final String HL_POST_TAG = "@@@hl_post@@@";

	@Value("${solr.core}")
	private String solrCore;

	@Value("${solr.host}")
	private String solrUrl;

	private SolrClient client;

	@PostConstruct
	private void setSolrClient() {
		client = new Http2SolrClient.Builder(solrUrl).withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
				.withRequestTimeout(60000, TimeUnit.MILLISECONDS).build();
	}

	public void save(Record record) {
		try {
			client.addBean(solrCore, record);
			client.commit(solrCore);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	public void saveList(List<Record> records) {
		if (records == null || records.isEmpty()) {
			return;
		}
		try {
			client.addBeans(solrCore, records);
			client.commit(solrCore);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	public List<Record> search(String query, int offset, int limit) {
		query = query.trim().toLowerCase();
		String solrQueryStr = "text:(" + query + "~) OR card_numbers:(" + query + ")";
		List<String> phones = Util.getPhones(query);
		if (!phones.isEmpty()) {
			solrQueryStr += " OR phones:(" + Util.concatStrings(phones) + "~)";
		}
		List<String> carNumbers = Util.getCarNumbers(query);
		if (!carNumbers.isEmpty()) {
			solrQueryStr += " OR car_numbers:(" + Util.concatStrings(carNumbers) + "~)";
		}
		SolrQuery solrQuery = new SolrQuery(solrQueryStr);
		solrQuery.setStart(offset);
		solrQuery.setRows(limit);
		solrQuery.setHighlight(true);
		solrQuery.setHighlightSimplePre(HL_PRE_TAG);
		solrQuery.setHighlightSimplePost(HL_POST_TAG);
		solrQuery.setHighlightRequireFieldMatch(true);
		solrQuery.addHighlightField("text");
		try {
			QueryResponse response = client.query(solrCore, solrQuery);
			List<Record> records = response.getBeans(Record.class);
			Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
			for (Record record : records) {
				Map<String, List<String>> hlMap = highlighting.get(record.getId());
				if (hlMap == null) {
					continue;
				}
				List<String> snippets = hlMap.get("text");
				if (snippets == null || snippets.isEmpty()) {
					continue;
				}
				record.setText(snippets.get(0));
			}
			return records;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return new ArrayList<Record>();
	}
}
