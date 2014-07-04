package com.zlycare.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

/**
 * @Description HTTP Request Utilities
 * @File com.zlycare.network.RTHttpClient.java
 * @Author Ryan Tang
 * @Date 2014年6月27日 上午11:15:37
 * @Version v0.1.0
 */
public class RTHttpClient {
	/**
	 * Restful method
	 */
	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final String DELETE = "DELETE";
	public static final String PUT = "PUT";

	/**
	 * Parameter type
	 */
	public static final String TYPE_JSON = "text/json; charset=UTF-8";
	public static final String TYPE_XML = "text/xml; charset=UTF-8";

	/**
	 * Repeat counts when request was failed
	 */
	private final static int REPEATS = 3;
	private final static int READ_TIMEOUT = 10000;
	private final static int CONNECT_TIMEOUT = 5000;

	/**
	 * Callback of HTTP request
	 * 
	 * @Description
	 * @File com.zlycare.network.APIClient.java
	 * @Author Ryan
	 * @Date 2014年6月27日 上午10:09:22
	 * @Version v0.1.0
	 */
	public interface RESTFulRequestListener {
		/**
		 * Executed before request begin
		 * 
		 * @Returen void
		 */
		public void onPreExecuteListener();

		/**
		 * Executed when request successful
		 * 
		 * @Returen void
		 * @param responseString
		 *            : Response JSON string
		 */
		public void onSuccessListener(String responseString);

		/**
		 * Executed when request failed
		 * 
		 * @Returen void
		 * @param responseCode
		 *            : HTTP response status code
		 * @param responseStream
		 *            : Response input stream
		 */
		public void onFailedListener(int responseCode,
				String responseString);
	}

	/**
	 * HTTP Request with RESTFul API
	 * 
	 * @param path
	 * @param json
	 * @param method
	 * @param requestSuccessListener
	 */
	public static void requst(final String path, final String paramType,
			String params, final String method,
			final RESTFulRequestListener requestSuccessListener) {
		
		// Execute async task
		new AsyncTask<Object, Integer, Object>() {

			private int requestCounts = 0;
			private HttpURLConnection conn = null;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				/******** Callback for prepare execute ********/
				requestSuccessListener.onPreExecuteListener();
			}

			@Override
			protected Object doInBackground(Object... params) {
				// Request JSON
				String jsonParam = (String) params[0];
				// Repeat REPEATS times when request failed
				while (requestCounts < REPEATS) {
					try {
						System.out.println("Request Path:" + path);
						URL url = new URL(path);
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod(method);
						conn.setConnectTimeout(CONNECT_TIMEOUT);
						conn.setReadTimeout(READ_TIMEOUT);

						// Add JSON as parameters if not null
						if (jsonParam != null) {
							byte[] data = jsonParam.getBytes();
							conn.setDoOutput(true);
							conn.setRequestProperty("Content-Type", paramType);
							conn.setRequestProperty("Content-Length",
									String.valueOf(data.length));
							OutputStream outputStream = conn.getOutputStream();
							outputStream.write(data);
							outputStream.flush();
							outputStream.close();
						}

						// Encapsulating response result
						ResponseEntity responseEntity = new ResponseEntity();
						responseEntity.setResponseCode(conn.getResponseCode());
						responseEntity.setResponseStream(conn.getInputStream());
						responseEntity.setResponseString(inputStreamToString(conn.getInputStream()));
						return responseEntity;

					} catch (IOException e) {
						requestCounts++;
					} catch (RuntimeException e) {
						requestCounts++;
						// If meet runtime exception, make thread sleep a while
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object result) {
				super.onPostExecute(result);
				ResponseEntity responseEntity = (ResponseEntity) result;
				// Handler response results
				if (responseEntity.getResponseCode() == HttpURLConnection.HTTP_OK) {
					// Response JSON string
					String responseString = responseEntity.getResponseString();
					/************** Callback of request success **************/
					requestSuccessListener.onSuccessListener(responseString);
				}else {
					/**************** Callback of request fail ***************/
					requestSuccessListener.onFailedListener(
							responseEntity.getResponseCode(),
							responseEntity.getResponseString());
				}
			}

		}.execute(params);// Execute AsyncTask with parameter JSON

	}

	/**
	 * InputStream convert to String
	 * 
	 * @Returen String
	 * @param inputStream
	 * @return
	 */
	public static String inputStreamToString(InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuilder.toString();
	}
	
	/**
	 * @Description HTTP response entity
	 * @File com.zlycare.network.APIClient.java
	 * @Author Ryan
	 * @Date 2014年6月27日 上午10:38:56
	 * @Version v0.1.0
	 */
	public static class ResponseEntity {

		private int responseCode;
		private InputStream responseStream;
		private String responseString;

		public ResponseEntity() {
			super();
		}

		public ResponseEntity(int responseCode, String responseString) {
			super();
			this.responseCode = responseCode;
			this.responseString = responseString;
		}

		public ResponseEntity(int responseCode, InputStream responseStream) {
			this.responseCode = responseCode;
			this.responseStream = responseStream;
		}

		public String getResponseString() {
			return responseString;
		}

		public void setResponseString(String responseString) {
			this.responseString = responseString;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public InputStream getResponseStream() {
			return responseStream;
		}

		public void setResponseStream(InputStream responseStream) {
			this.responseStream = responseStream;
		}
	}
}
