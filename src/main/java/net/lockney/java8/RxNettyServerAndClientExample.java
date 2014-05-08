package net.lockney.java8;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.HttpServer;

public class RxNettyServerAndClientExample {

    public static void main(String[] args) throws InterruptedException {

        HttpServer<ByteBuf, ByteBuf> server = RxNetty.createHttpServer(8080, (request, response) -> {
            try {
                if (request.getPath().equals("/error")) {
                    throw new RuntimeException("forced error");
                }
                response.setStatus(HttpResponseStatus.OK);
                return response.writeStringAndFlush("Path Requested =>: " + request.getPath() + "\n");
            } catch (Exception e) {
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
                return response.writeStringAndFlush("Error 500: Bad Request\n");
            }
        });

        server.start();

        HttpClient<ByteBuf, ByteBuf> client = RxNetty.createHttpClient("localhost", 8080);

        client.submit(HttpClientRequest.createGet("/"))

            // flatMap - pass the response into an Observable context
            .flatMap(response -> response.getContent())

            // map - pass the response to a normal method and wrap it in an Observable
            .map(data -> "Client => " + data.toString(Charset.defaultCharset()))

            // wait for the Observable on the current thread
            .toBlockingObservable()

            // execute a method inside the Observable context
            .forEach(System.out::println);

        server.shutdown();
    }
}
