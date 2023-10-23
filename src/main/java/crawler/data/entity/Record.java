package crawler.data.entity;

import org.apache.solr.client.solrj.beans.Field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Record {
	@Field
	private String id;

	@Field
	private String text;

	@Field
	private String phones;

	@Field
	private String card_numbers;

	@Field
	private String car_numbers;

	@Field
	private long id_user;
}
