package com.tailoredshapes.donut;

import spark.Service;

import java.util.logging.Logger;

import static spark.Service.ignite;


public class App {

    public Service service;

    private static Logger log = Logger.getLogger(App.class.getName());

    public App(int port){
        service = ignite();
        service.port(port);

        service.get("/", ((req, res) -> {
            res.status(200);
            res.body("OK");
            return "OK";
        }));
    }


    public static void main(String... args){

    }
}
