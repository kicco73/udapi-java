/**
 * @author Enrico Carniani
 */

package cnr.ilc.services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import cnr.ilc.rut.utils.Logger;

public class GraphDBClient {
	final private URI statementsApi;
	final private URI queryApi;

	public GraphDBClient(String apiUrl, String repository) throws Exception {
		statementsApi = new URI(String.format("%s/repositories/%s/statements", apiUrl, repository));
		queryApi = new URI(String.format("%s/repositories/%s", apiUrl, repository));
	}

	private HttpRequest buildRequest(URI api, Map<Object,Object> form) {
		return HttpRequest.newBuilder()
				.uri(api)
				.POST(ofFormData(form))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.timeout(Duration.ofSeconds(60))
				.header("Accept", "application/json")
				.build();
	}

	public void postUpdate(String body) {

		HttpClient client = HttpClient.newBuilder()
			.version(Version.HTTP_2)
			.build();

		HttpRequest request = buildRequest(statementsApi, Map.of("update", body));

		client.sendAsync(request, BodyHandlers.ofString())
			.thenApply(HttpResponse::body)
			.thenAccept((String responseBody) -> {
				if (responseBody.length() > 0) {
					Logger.error("GRAPHDB: " + responseBody);
					//System.err.println(body);
				}
			})
			.join();
	}

	public String postQuery(String body) throws Exception {

		HttpClient client = HttpClient.newBuilder()
			.version(Version.HTTP_2)
			.build();

		HttpRequest request = buildRequest(queryApi, Map.of("query", body));

		CompletableFuture<String> cf = client
			.sendAsync(request, BodyHandlers.ofString())
			.thenApply(HttpResponse::body);

		String result = cf.join();
		return result;
	}

	private static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
