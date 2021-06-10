package com.tailoredshapes.donut;

import com.beerboy.ss.SparkSwagger;
import com.beerboy.ss.descriptor.MethodDescriptor;
import com.beerboy.ss.rest.Endpoint;

import java.util.logging.Logger;

import static com.beerboy.ss.descriptor.EndpointDescriptor.endpointPath;

public class DonutEndpoint<T> implements Endpoint {

    Logger log = Logger.getLogger(DonutEndpoint.class.getName());

    private final String prefix;
    private final Class<T> clazz;
    private final Routes routes;

    public DonutEndpoint(Routes routes, String prefix, Class<T> clazz) {
        this.routes = routes;
        this.prefix = prefix;
        this.clazz = clazz;
    }


    @Override
    public void bind(SparkSwagger restApi) {

        restApi.endpoint(endpointPath("/")
                .withDescription("CRUD API for your Donut"), (q, a) -> log.info("Request received"))
                .get(MethodDescriptor.Builder.newBuilder()
                        .withPath(":id")
                        .withDescription("Returns a document")
                        .withPathParam()
                        .withName("id")
                        .withObject(Integer.class)
                        .and().withResponseType(clazz), routes.read)
                .put(MethodDescriptor.Builder.newBuilder()
                        .withPath(":id")
                        .withDescription("Creates a document")
                        .withPathParam()
                        .withName("id")
                        .withObject(Integer.class)
                        .and().withResponseType(clazz), routes.write)
                .post(MethodDescriptor.Builder.newBuilder()
                        .withPath(":id")
                        .withDescription("Updates a document")
                        .withPathParam()
                        .withName("id")
                        .withObject(Integer.class)
                        .and().withResponseType(clazz), routes.update)
                .delete(MethodDescriptor.Builder.newBuilder()
                        .withPath(":id")
                        .withDescription("Deletes a document")
                        .withPathParam()
                        .withName("id")
                        .withObject(Integer.class)
                        .and().withResponseType(clazz), routes.delete);

    }
}
