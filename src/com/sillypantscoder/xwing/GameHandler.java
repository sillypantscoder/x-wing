package com.sillypantscoder.xwing;

import com.sillypantscoder.http.HttpResponse;
import com.sillypantscoder.http.HttpServer;

/**
 * The main HTTP handler used for the server.
 */
public class GameHandler extends HttpServer.RequestHandler {
	public GameHandler() {}
	public HttpResponse get(String path) {
		// Handle request here!
		// For example: if (path.equals("/")) return new HttpResponse().setStatus(200).setBody("hi!");
		System.err.println("Error for request: " + path);
		return new HttpResponse().setStatus(404);
	}
	public HttpResponse post(String path, String body) {
		System.err.println("Error for POST request: " + path);
		return new HttpResponse().setStatus(404);
	}
}
