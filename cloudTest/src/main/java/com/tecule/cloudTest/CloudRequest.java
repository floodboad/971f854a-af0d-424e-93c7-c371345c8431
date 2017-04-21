package com.tecule.cloudTest;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class CloudRequest implements Runnable {
  private int requestId;
  private String targetUrl;
  private Map<String, String> params;
  private String cookie;

  public CloudRequest(int requestId, String targetUrl, Map<String, String> params, String cookie) {
    this.requestId = requestId;
    this.targetUrl = targetUrl;
    this.params = params;
    this.cookie = cookie;
  }

  public void run() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(targetUrl);

    Form form = new Form();
    for (Entry<String, String> entry : params.entrySet()) {
      form = form.param(entry.getKey(), entry.getValue());
    }

    // Form form = new Form().param("hostName", "test" + requestId).param("description", "test")
    // .param("cpu", "1").param("memory", "1").param("disk", "20")
    // .param("domainId", "e89062dd61bd47b8a57a9fc05619f3b7")
    // .param("groupId", "b800ff55-5069-487f-b155-ef4baf4fd9fd").param("applicationIds", "")
    // .param("manager", "none").param("imageId", "284a884a-8c6e-45aa-b646-88d471a4ce01");
    
    // use Cookie shown in the browser
    Response response = target.request().header("Cookie", cookie).post(Entity.form(form));
    int status = response.getStatus();
    String responseBody = response.readEntity(String.class);
    response.close();
    System.out.println(requestId + ", " + status + ": " + responseBody);
    client.close();

    return;
  }
}
