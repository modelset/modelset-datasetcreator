package modelset.common.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class MarSearchService implements ISearchService {

	private @NonNull String url;
	private int port;
	private @NonNull String searchType;

	public MarSearchService(@NonNull String url, int port, @NonNull String searchType) {
		this.url = url;
		this.port = port;
		this.searchType = searchType;
	}

	@Override
	public SearchResult search(@NonNull Resource r, int max) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			r.save(bos, null);
			
			HttpResponse<JsonNode> jsonResponse = Unirest.post(getURL("search?type=" + searchType + "&max="+max))
					.header("accept", "application/json")
					.accept("*/*")
					//.field("data", bos.toString().getBytes())
					.body(bos.toByteArray())
					//.queryString("apiKey", "123")
					.asJson();
			JsonNode node = jsonResponse.getBody();
			
			SearchResult result = new SearchResult();
			
			JSONObject obj = node.getObject();
			Iterator<String> it = obj.keys();
			while (it.hasNext()) {
				String key = it.next();
				double v = obj.getDouble(key);				
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

	@Nullable
	public Resource getFile(@NonNull String id) {
		HttpResponse<byte[]> response = Unirest.get(getURL("content"))
				.queryString("id", id)				
				.asBytes();
		
		byte[] body = response.getBody();
		ResourceSet rs = new ResourceSetImpl();
		Resource r = rs.createResource(URI.createURI("in-memory.ecore")); // TODO: Put a proper URI		
		try {
			System.out.println(new String(body));
			r.load(new ByteArrayInputStream(body), null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return r;
	}

}

	
