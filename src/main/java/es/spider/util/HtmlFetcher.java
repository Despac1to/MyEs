package es.spider.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HtmlFetcher {

	private static Log logger = LogFactory.getLog(HtmlFetcher.class);

	public static String fetchUrl(String url) {
		CloseableHttpClient client = HttpClients.createDefault();
		String result = "";
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
				// utf-8
				result = EntityUtils.toString(entity, "utf-8");
			}
		} catch (Exception e) {
			logger.info("Get Response Error!", e);
		} finally {
			if(client != null){
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

}
