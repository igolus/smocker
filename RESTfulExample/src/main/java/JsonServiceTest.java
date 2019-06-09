import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class JsonServiceTest {

	public static void main(String[] args) {
		try {	
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			bufferedReader.readLine();
			callJsonplaceholder();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void callJsonplaceholder() throws ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(
				"https://reqres.in/api/users");
		getRequest.addHeader("accept", "application/json");

		org.apache.http.HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		BufferedReader br = new BufferedReader(
				new InputStreamReader((response.getEntity().getContent())));

		System.out.println("Output from Server .... \n");
		
		String line = "";
		while((line = br.readLine()) != null){
			System.out.println(line);
		} 
		
		httpClient.getConnectionManager().shutdown();
	}
}