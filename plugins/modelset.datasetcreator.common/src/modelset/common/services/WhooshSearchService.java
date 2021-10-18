package modelset.common.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class WhooshSearchService implements ISearchService {

	private @NonNull String url;
	private int port;
	private @NonNull String searchType;

	public WhooshSearchService(@NonNull String url, int port, @NonNull String searchType) {
		this.url = url;
		this.port = port;
		this.searchType = searchType;
	}

	@Override
	public SearchResult search(@NonNull Resource r, int max) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(toText(r).getBytes());
			
			Unirest.config().reset();
			//int x = 3;
			//Unirest.config().socketTimeout(x * 120000).connectTimeout(x * 120000);
			//Unirest.config().socketTimeout(x * 120000).connectTimeout(x * 120000);
			
			System.out.println(bos.toString());
			HttpResponse<JsonNode> jsonResponse = Unirest.post(getURL("search?type=" + searchType + "&max="+max))
					.header("accept", "application/json")
					.accept("*/*")
					.body(bos.toByteArray())
					.asJson();
			JsonNode node = jsonResponse.getBody();
			
			SearchResult result = new SearchResult();
			
			JSONObject obj = node.getObject();
			Iterator<String> it = obj.keys();
			while (it.hasNext()) {
				String key = it.next();
				double v = obj.getDouble(key);	
				key = key.replaceAll("\\\\", "/");	// This makes it work in Windows :)		
				result.add(key, v);
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getURL(String str) {
		return "http://" + url + ":" + port + "/" + str;
	}

	/**
	 * Converts a Resource to a text just by extrating values from String attributes. 
	 */
	private String toText(@NonNull Resource r) {
		StringBuilder builder = new StringBuilder();
		
		r.getAllContents().forEachRemaining(o -> {
			List<EAttribute> attrs = o.eClass().getEAllAttributes();
			for (EAttribute att : attrs) {
				if (att.isDerived())
					continue;
				
				Object value = o.eGet(att);
				if (value == null)
					continue;
				
				if (att.isMany()) {
					String text = ((List<Object>) value).stream().filter(v -> v instanceof String).map(v -> (String) v).collect(Collectors.joining(" "));
					if (! text.trim().isEmpty())
						builder.append(text);
				} else {
					if (value instanceof String) {
						builder.append(value).append(" ");
					}
				}
			}
		});
		
		return builder.toString();
	}
	
}

	
