package net.lockney.thirddraft;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class RxNettyServerAndClientExampleSimplified {

    private static final RequestHandler<ByteBuf, ByteBuf> requestHandler = new RequestHandler<ByteBuf, ByteBuf>() {
        @Override
        public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
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
        }
    };

    private static final Func1<HttpClientResponse<ByteBuf>, Observable<? extends ByteBuf>> getResponseContent =
        new Func1<HttpClientResponse<ByteBuf>, Observable<? extends ByteBuf>>() {
            @Override
            public Observable<? extends ByteBuf> call(HttpClientResponse<ByteBuf> response) {
                return response.getContent();
            }
        };

    private static final Func1<ByteBuf, String> getDataAsString = new Func1<ByteBuf, String>() {
        @Override
        public String call(ByteBuf data) {
            return "Client => " + data.toString(Charset.defaultCharset());
        }
    };

    private static final Action1<String> print = new Action1<String>() {
        @Override
        public void call(String data) {
            System.out.println(data);
        }
    };

    public static void main(String[] args) throws InterruptedException {

        HttpServer<ByteBuf, ByteBuf> server = RxNetty.createHttpServer(8080, requestHandler);

        server.start();

        HttpClient<ByteBuf, ByteBuf> client = RxNetty.createHttpClient("localhost", 8080);

        client.submit(HttpClientRequest.createGet("/"))

            // flatMap - pass the response into an Observable context
            .flatMap(getResponseContent)

                // map - pass the response to a normal method and wrap it in an Observable
            .map(getDataAsString)

                // wait for the Observable on the current thread
            .toBlockingObservable()

                // execute a method inside the Observable context
            .forEach(print);

        server.shutdown();
    }
}
