package com.sinosoft.nagios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		NagiosConfig config = new NagiosConfig();
		config.setNagiosHost("192.168.100.63");
		config.setNagiosPort(80);
		config.setNagiosUserName("nagiosadmin");
		config.setNagiosPassword("123456");
		config.setNagiosUrl("http://192.168.100.63/nagios");
		
		NagiosMonitor monitor = new NagiosMonitor(config);
		List<ServiceStatus> serviceStatusList = monitor.getHostServiceStatus("compute01.stack.local");
		for (ServiceStatus serviceStatus : serviceStatusList) {
			System.out.println(serviceStatus.getServiceName() + ": " + serviceStatus.getServiceStatus().name());			
		}

		System.out.println("Hello World!");
	}

	// http://www.mastertheboss.com/jboss-frameworks/resteasy/resteasy-basic-authentication-example
	private static void nagiosDemo() {
		DefaultHttpClient client = new DefaultHttpClient();
		client.getCredentialsProvider().setCredentials(new AuthScope("192.168.100.63", 80),
				new UsernamePasswordCredentials("nagiosadmin", "123456"));
		HttpGet httppost = new HttpGet("http://192.168.100.63/nagios/cgi-bin/statusjson.cgi?query=hostcount");
		System.out.println("executing request " + httppost.getRequestLine());
		HttpResponse response = null;
		try {
			response = client.execute(httppost);
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			client.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void nagiosDemo2() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(new AuthScope("192.168.100.63", 80),
				new UsernamePasswordCredentials("nagiosadmin", "123456"));
		ClientExecutor executor = new ApacheHttpClient4Executor(httpClient);
		ClientRequest request = new ClientRequest(
				"http://192.168.100.63/nagios/cgi-bin/statusjson.cgi?query=hostcount", executor)
				.accept(MediaType.APPLICATION_JSON);
		ClientResponse<String> response = null;
		try {
			response = request.get(String.class);
		} catch (Exception e1) {
			// throw new CephException("请求服务出错。" + e1.getMessage(), e1);
		}
		// int responseCode = response.getResponseStatus().getStatusCode();
		String message = response.getEntity();
		// logger.debug(message);
		System.out.println(message);
	}
}
