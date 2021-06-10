package com.tailoredshapes.donut;

import com.beerboy.ss.SparkSwagger;
import com.tailoredshapes.stash.Stash;
import spark.Service;

import java.util.logging.Logger;

import static com.tailoredshapes.underbar.Die.rethrow;
import static spark.Service.ignite;


public class App {

    public Service service;

    private static Logger log = Logger.getLogger(App.class.getName());

    public App(int port, String prefix, Routes routes){
        service = ignite();
        service.port(port);

        DonutEndpoint<Stash> stashDonutEndpoint = new DonutEndpoint<>(routes, prefix, Stash.class);

        rethrow(() -> SparkSwagger.of(service).endpoint(stashDonutEndpoint).generateDoc(), () -> "Failed to generated swagger doc");
    }


    public static void main(String... args){

    }
}
