package com.tailoredshapes.donut;

import com.mongodb.client.MongoDatabase;
import com.tailoredshapes.stash.Stash;
import spark.Service;

import java.util.logging.Logger;

import static spark.Service.ignite;


public class App {

    public Service service;

    private static Logger log = Logger.getLogger(App.class.getName());

    public App(int port, String prefix, Repository rep){
        service = ignite();
        service.port(port);

        service.get("/%s/:id".formatted(prefix), ((req, res) -> {
            Stash doc = rep.readDoc(Integer.parseInt(req.params("id")));
            if(doc.isEmpty()){
                res.status(404);
            } else {
                res.status(200);
            }
            res.header("Content-Type", "application/json");
            return doc.toJSONString();
        }));

        service.put("/%s/:id".formatted(prefix), ((req, res) -> {
            Stash doc = rep.writeDoc(Integer.parseInt(req.params("id")), Stash.parseJSON(req.body()));
            res.status(200);
            res.header("Content-Type", "application/json");
            return doc.toJSONString();
        }));

        service.post("/%s/:id".formatted(prefix), ((req, res) -> {
            Stash doc = rep.changeDoc(Integer.parseInt(req.params("id")), Stash.parseJSON(req.body()));
            res.status(200);
            res.header("Content-Type", "application/json");
            return doc.toJSONString();
        }));

        service.delete("/%s/:id".formatted(prefix), ((req, res) -> {
            Stash doc = rep.deleteDoc(Integer.parseInt(req.params("id")));
            res.status(200);
            res.header("Content-Type", "application/json");
            return doc.toJSONString();
        }));

    }


    public static void main(String... args){

    }
}
