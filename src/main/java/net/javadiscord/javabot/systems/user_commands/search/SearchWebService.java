package net.javadiscord.javabot.systems.user_commands.search;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.javadiscord.javabot.data.config.SystemsConfig;
import net.javadiscord.javabot.util.Responses;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Service class which is used to search the internet for a specifed query using
 * the Bing API.
 */
@RequiredArgsConstructor
@Service
public final class SearchWebService {
	private static final String HOST = "https://api.bing.microsoft.com";
	private static final String PATH = "/v7.0/search";

	private final SystemsConfig systemsConfig;

	private @NotNull SearchResult searchWeb(@NotNull String searchQuery) throws IOException {
		// Construct the URL.
		URL url = new URL(HOST + PATH + "?q=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString()) + "&mkt=en-US&safeSearch=Strict");
		// Open the connection.
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Ocp-Apim-Subscription-Key", systemsConfig.getAzureSubscriptionKey());
		// Receive the JSON response body.
		String response;
		try (Scanner scan = new Scanner(connection.getInputStream()).useDelimiter("\\A")) {
			response = scan.next();
		}
		// Construct the result object.
		SearchResult results = new SearchResult(new HashMap<>(), response);
		// Extract Bing-related HTTP headers.
		Map<String, List<String>> headers = connection.getHeaderFields();
		for (String header : headers.keySet()) {
			if (header == null) continue;      // may have null key
			if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
				results.relevantHeaders.put(header, headers.get(header).get(0));
			}
		}
		return results;
	}

	/**
	 * Creates the embed which contains the search results.
	 *
	 * @param query The specified query.
	 * @return A {@link MessageEmbed}.
	 * @throws IOException If an error occurs.
	 */
	public @NotNull MessageEmbed buildSearchWebEmbed(@NotNull String query) throws IOException {
		query = query.replaceAll("\\r|\\n", "");
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Responses.Type.DEFAULT.getColor())
				.setTitle("Search Results")
				.setFooter("Query: " + (query.length() >= 1000 ? query.substring(0, 1000) + "..." : query));
		SearchResult result = searchWeb(query);
		JsonObject json = JsonParser.parseString(result.jsonResponse).getAsJsonObject();
		JsonArray urls = json.get("webPages").getAsJsonObject().get("value").getAsJsonArray();
		StringBuilder resultString = new StringBuilder();
		for (int i = 0; i < Math.min(3, urls.size()); i++) {
			JsonObject object = urls.get(i).getAsJsonObject();
			String name = object.get("name").getAsString();
			String url = object.get("url").getAsString();
			String snippet = object.get("snippet").getAsString();
			if (snippet.length() > 320) {
				snippet = snippet.substring(0, 320);
				int snippetLastPeriod = snippet.lastIndexOf('.');
				if (snippetLastPeriod != -1) {
					snippet = snippet.substring(0, snippetLastPeriod + 1);
				} else {
					snippet = snippet.concat("...");
				}
			}
			resultString.append("**").append(i + 1).append(". [").append(name).append("](")
					.append(url).append(")** \n").append(snippet).append("\n\n");
		}
		embed.setDescription(resultString);
		return embed.build();
	}

	/**
	 * Simple record class that represents the search results.
	 *
	 * @param relevantHeaders The most relevant headers.
	 * @param jsonResponse    The HTTP Response, formatted as a JSON.
	 */
	private record SearchResult(Map<String, String> relevantHeaders, String jsonResponse) {
	}
}
