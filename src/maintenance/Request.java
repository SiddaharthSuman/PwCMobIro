package maintenance;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Request {

	static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("application/json; charset=utf-8");

	static final OkHttpClient client = new OkHttpClient();
	
	public static String execute(RequestData data) throws IOException{
		
		okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
				.url(data.getUrl())
				.post(RequestBody.create(MEDIA_TYPE_MARKDOWN, data.getPostBody()));
		
		if(data.getHeaderName() != null){
			requestBuilder.addHeader(data.getHeaderName(), data.getHeaderValue());
		}
		
		okhttp3.Request request = requestBuilder.build();
		
		Response response = client.newCall(request).execute();
		return response.body().string();
	}
}
