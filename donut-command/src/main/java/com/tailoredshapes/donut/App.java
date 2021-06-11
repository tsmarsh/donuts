package com.tailoredshapes.donut;

import com.beerboy.ss.SparkSwagger;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.tailoredshapes.stash.Stash;
import spark.Service;

import java.util.logging.Logger;

import static com.tailoredshapes.underbar.Die.rethrow;
import static spark.Service.ignite;


public class App {

    public Service service;

    private static Logger log = Logger.getLogger(App.class.getName());

    public App(int port, Routes routes){
        service = ignite();
        service.port(port);

        DonutEndpoint<Stash> stashDonutEndpoint = new DonutEndpoint<>(routes, Stash.class);

        rethrow(() -> SparkSwagger.of(service).endpoint(stashDonutEndpoint).generateDoc(), () -> "Failed to generated swagger doc");
    }


    public static void main(String... args){
        String db_address = System.getenv("DB_ADDRESS");
        String db_name = System.getenv("DB_NAME");
        String db_collection = System.getenv("DB_COLLECTION");
        int port = Integer.parseInt(System.getenv("PORT"));

        MongoClient client = new MongoClient(new ServerAddress(db_address));
        MongoDatabase database = client.getDatabase(db_name);

        Repository repository = new MongoRepository(database, db_collection);
        Routes routes = new Routes(repository);

        new App(port, routes);
    }
}
