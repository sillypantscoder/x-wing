package com.sillypantscoder.xwing;

import com.sillypantscoder.http.HttpResponse;
import com.sillypantscoder.http.HttpServer;

/**
 * The main HTTP handler used for the server.
 */
public class MainServer extends HttpServer.RequestHandler {
	public MainServer() {}
	public HttpResponse get(String path) {
		if (path.equals("/")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/html").setBody(Utils.readFile("client/login.html"));
		if (path.equals("/game")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/html").setBody(Utils.readFile("client/game.html"));
		System.err.println("Error for request: " + path);
		return new HttpResponse().setStatus(404);
	}
	public HttpResponse post(String path, String body) {
		System.err.println("Error for POST request: " + path);
		return new HttpResponse().setStatus(404);
	}
}
