package crawler.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import crawler.data.entity.WallGetResponse;
import crawler.data.repository.SolrService;
import crawler.util.Util;
import crawler.data.entity.Record;
import crawler.data.entity.WallGetItem;
import jakarta.annotation.PostConstruct;

@Service
public class Indexer {

	@Autowired
	private SolrService solrService;

	@Value("${vk.api.base.url}")
	private String vkBaseUrl;

	@Value("${vk.api.version}")
	private String vkApiVersion;

	@Value("${access.token}")
	private String accessToken;

	@Value("${vk.records.page.size}")
	private int vkRecordsPageSize;

	@Value("${max.vk.user.id}")
	private long maxVkUserId;

	@PostConstruct
	private void startDownload() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				CloseableHttpClient httpclient = HttpClients.createDefault();
				ObjectMapper mapper = JsonMapper.builder()
						.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
				for (long idUser = 1; idUser <= maxVkUserId; ++idUser) {
					System.out.println("Start idUser=" + idUser);
					String url = vkBaseUrl + "?v=" + vkApiVersion + "&access_token=" + accessToken + "&owner_id="
							+ idUser + "&offset=0&count=" + vkRecordsPageSize;
					HttpGet httpget = new HttpGet(url);
					String json = null;
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try (CloseableHttpResponse vkResponse = httpclient.execute(httpget)) {
						HttpEntity entity = vkResponse.getEntity();
						json = EntityUtils.toString(entity);
						EntityUtils.consume(entity);
					} catch (IOException e) {
						System.out.println("idUser=" + idUser + "Error while downloading wall");
						e.printStackTrace();
					}
					if (json == null) {
						continue;
					}
					WallGetResponse response = null;
					try {
						response = mapper.readValue(json, WallGetResponse.class);
					} catch (IOException e) {
						System.out.println("idUser=" + idUser + "Error while parsing response");
						e.printStackTrace();
					}
					if (response == null || response.getResponse() == null || response.getResponse().getItems() == null
							|| response.getResponse().getItems().isEmpty()) {
						continue;
					}
					List<Record> records = new ArrayList<Record>();
					for (WallGetItem item : response.getResponse().getItems()) {
						if (item.getText() == null || item.getText().trim().isEmpty()) {
							continue;
						}
						Record record = new Record();
						record.setId(UUID.randomUUID().toString());
						record.setId_user(idUser);
						record.setText(item.getText());
						List<String> phones = Util.getPhones(item.getText());
						if (!phones.isEmpty()) {
							record.setPhones(Util.concatStrings(phones));
						}
						List<String> carNumbers = Util.getCarNumbers(item.getText());
						if (!carNumbers.isEmpty()) {
							record.setCar_numbers(Util.concatStrings(carNumbers));
						}
						records.add(record);
					}
					if (!records.isEmpty()) {
						System.out.println("idUser=" + idUser + " save " + records.size() + " records");
						solrService.saveList(records);
					}
				}
			}
		});
		thread.start();
	}

}
