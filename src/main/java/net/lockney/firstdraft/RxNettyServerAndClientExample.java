package net.lockney.firstdraft;

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
import rx.observables.BlockingObservable;

public class RxNettyServerAndClientExample {

    public static void main(String[] args) throws InterruptedException {

        HttpServer<ByteBuf, ByteBuf> server =
            RxNetty.createHttpServer(8080, new SimpleRequestHandler());

        server.start();

        HttpClient<ByteBuf, ByteBuf> client =
            RxNetty.createHttpClient("localhost", 8080);

        Observable<HttpClientResponse<ByteBuf>> response =
            client.submit(HttpClientRequest.createGet("/"));

        // flatMap - put the response into an Observable context
        Observable<ByteBuf> responseContent = response.flatMap(new GetResponseContent());

        // map - pass the response to a normal method and wrap it in an Observable
        Observable<String> stringData = responseContent.map(new GetDataAsString());

        // wait for the Observable on the current thread
        BlockingObservable<String> blockingStringData = stringData.toBlockingObservable();

        // execute a method inside the Observable context
        blockingStringData.forEach(new Print());

        server.shutdown();
    }
}

class SimpleRequestHandler implements RequestHandler<ByteBuf, ByteBuf> {
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
}

class GetResponseContent implements Func1<HttpClientResponse<ByteBuf>, Observable<? extends ByteBuf>> {
    @Override
    public Observable<? extends ByteBuf> call(HttpClientResponse<ByteBuf> response) {
        return response.getContent();
    }
}

class GetDataAsString implements Func1<ByteBuf, String> {
    @Override
    public String call(ByteBuf data) {
        return "Client => " + data.toString(Charset.defaultCharset());
    }
}

class Print implements Action1<String> {
    @Override
    public void call(String data) {
        System.out.println(data);
    }
}
