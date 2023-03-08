/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

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

public class GraphDBClient {
	final private URI api;

	public GraphDBClient(String apiUrl, String repository) throws Exception {
		api = new URI(String.format("%s/repositories/%s/statements", apiUrl, repository));
	}

	public void post(String body) {

		HttpClient client = HttpClient.newBuilder()
			.version(Version.HTTP_2)
			.build();

		Map<Object,Object> form = Map.of(
			"update", body
		);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(this.api)
				.POST(ofFormData(form))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.timeout(Duration.ofSeconds(20))
				.build();

		client.sendAsync(request, BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenAccept((String responseBody) -> {
					if (responseBody.length() > 0) {
						System.out.println("GRAPHDB: " + responseBody);
						//System.out.println(body);
					}
				})
				.join();

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
