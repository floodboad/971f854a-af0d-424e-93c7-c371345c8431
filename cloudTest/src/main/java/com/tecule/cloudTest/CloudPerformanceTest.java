package com.tecule.cloudTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

/**
 * Hello world!
 *
 */
public class CloudPerformanceTest {
  public static void main(String[] args) {
    testCreateVirtualMachine();
  }

  private static void testCreateVirtualMachine() {
    final String targetUrl = "http://localhost:8080/private-cloud/virtual-machine";
    Map<String, String> params = new HashMap<String, String>();
    params.put("hostName", "perfomance test");
    params.put("description", "test");
    params.put("cpu", "1");
    params.put("memory", "1");
    params.put("disk", "20");
    params.put("domainId", "e89062dd61bd47b8a57a9fc05619f3b7");
    params.put("groupId", "b800ff55-5069-487f-b155-ef4baf4fd9fd");
    params.put("applicationIds", "");
    params.put("manager", "none");
    params.put("imageId", "284a884a-8c6e-45aa-b646-88d471a4ce01");

    sendCloudRequest(targetUrl, params);

    return;
  }

  private static void testCreateDisk() {
    final String targetUrl = "http://localhost:8080/private-cloud/disk";
    Map<String, String> params = new HashMap<String, String>();
    params.put("diskName", "perfomance test");
    params.put("description", "test");
    params.put("capacity", "1");
    params.put("manager", "none");
    params.put("aliveDays", "7");

    sendCloudRequest(targetUrl, params);

    return;
  }

  private static void sendCloudRequest(String targetUrl, Map<String, String> params) {
    long startTime = System.currentTimeMillis();

    final String cookie = "JSESSIONID=FFEA0ACA8B8754C4C09EE397353393B8; JSESSIONID=7F4636FE1896651692F9941D08B9F760";
    
    final int testCaseCount = 10;
    final int workerThreadCount = 10;

    ExecutorService pool = Executors.newFixedThreadPool(workerThreadCount);
    for (int i = 0; i < testCaseCount; i++) {
      CloudRequest request = new CloudRequest(i, targetUrl, params, cookie);
      pool.execute(request);
    }
    shutdownPool(pool);

    long endTime = System.currentTimeMillis();
    System.out.println("程序运行时间： " + (endTime - startTime) + "ms");

    return;
  }

  private static void shutdownPool(ExecutorService pool) {
    // call shutdown() to reject incoming tasks, no new tasks will be accepted
    pool.shutdown();

    try {
      // wait for tasks to complete execution
      boolean terminated = pool.awaitTermination(10, TimeUnit.MINUTES);
      if (false == terminated) {
        // timeout before all tasks terminate, call shutdownNow() to cancel any lingering tasks
        pool.shutdownNow();
        System.out.println("force pool to terminate!");

        // wait for tasks to respond to being cancelled
        terminated = pool.awaitTermination(10, TimeUnit.MINUTES);
        if (false == terminated) {
          // timeout before all tasks terminate
          System.out.println("pool did not terminate!");
        }
      }
    } catch (InterruptedException e) {
      // interrupt before tasks terminate
      pool.shutdownNow();
      // preserve interrupt status???
      Thread.currentThread().interrupt();
    }
  }
}
