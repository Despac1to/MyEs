package es.index.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

import es.spider.model.SongInfoModel;
import es.util.PropertiesUtil;

public class IndexClient {

	private static final Log logger = LogFactory.getLog(IndexClient.class);
	private static TransportClient client;
	private static Properties properties = PropertiesUtil.getProperties();

	private static IndexClient indexClient;

	private IndexClient() {
	}

	public static IndexClient getInstance() {
		if (client == null) {
			initClient();
		}
		return indexClient;
	}

	private static void initClient() {
		String nodes = properties.getProperty("nodes", "127.0.0.3:9203");
		String clusterName = properties.getProperty("cluster.name", "MyEs");

		// 仅通信客户端(不加入cluster)
		client = TransportClient.builder().settings(setting(clusterName)).build();
		if (nodes != null && nodes.length() > 0) {
			String ipPortArr[] = nodes.split(",");
			if (ipPortArr != null && ipPortArr.length > 0) {
				for (String ipport : ipPortArr) {
					if (ipport != null && ipport.length() > 0) {
						String str[] = ipport.split(":");
						if (str != null && str.length == 2) {
							String ip = str[0].trim();
							String port = str[1].trim();
							client.addTransportAddress(
									new InetSocketTransportAddress(new InetSocketAddress(ip, Integer.valueOf(port))));
						}
					}
				}
			}
		}
		indexClient = new IndexClient();
	}

	private static Settings setting(String clusterName) {
		return Settings.settingsBuilder().put("cluster.name", clusterName).put("client.transport.sniff", true)
				.put("client.transport.ignore_cluster_name", false).put("client.transport.ping_timeout", "5s")
				.put("client.transport.nodes_sampler_interval", "5s").build();
	}

	// 创建索引template
	public void createIndexTemplate(String indexName, String indexTemplate) {
		PutIndexTemplateRequest request = new PutIndexTemplateRequest(indexName).source(indexTemplate);
		client.admin().indices().putTemplate(request).actionGet();
	}

	// 创建索引
	public void createIndex(String indexName, String indexContent) {
		CreateIndexRequest request = new CreateIndexRequest(indexName).source(indexContent);
		client.admin().indices().create(request).actionGet();
	}

	// bulk索引
	public static boolean bulkIndex(String index, String type, List<SongInfoModel> songInfoModels) {
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		if (!CollectionUtils.isEmpty(songInfoModels)) {
			String json;
			for (SongInfoModel songInfoModel : songInfoModels) {
				json = JSON.toJSONString(songInfoModel);
				IndexRequestBuilder indexRequest = client.prepareIndex(index, type, null)
						.setId(String.valueOf(songInfoModel.getId())).setSource(json);
				bulkRequest.add(indexRequest);
			}
		}
		BulkResponse bulkResponse = bulkRequest.get();
		if (bulkResponse.hasFailures()) {
			logger.info("Bukl Index Error!");
			return false;
		}
		return true;
	}

}
