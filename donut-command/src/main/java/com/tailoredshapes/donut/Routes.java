package com.tailoredshapes.donut;

import com.beerboy.spark.typify.route.GsonRoute;
import com.tailoredshapes.stash.Stash;
import spark.Request;
import spark.Response;
import spark.Route;

public class Routes {

    private Repository rep;

    public Routes(Repository rep) {
        this.rep = rep;
    }

    public Route read = new StashRoute() {
        @Override
        public Stash handleAndTransform(Request req, Response res) {
            Stash doc = rep.readDoc(Integer.parseInt(req.params("id")));
            if (doc.isEmpty()) {
                res.status(404);
            } else {
                res.status(200);
            }
            res.header("Content-Type", "application/json");
            return doc;
        }
    };

    public Route write = new StashRoute() {
        @Override
        public Stash handleAndTransform(Request req, Response res) {
            Stash doc = rep.writeDoc(Integer.parseInt(req.params("id")), Stash.parseJSON(req.body()));
            res.status(200);
            res.header("Content-Type", "application/json");
            return doc;
        }
    };

    public Route update = new StashRoute() {
        @Override
        public Stash handleAndTransform(Request req, Response res) {
            Stash doc = rep.changeDoc(Integer.parseInt(req.params("id")), Stash.parseJSON(req.body()));
            res.status(200);
            res.header("Content-Type", "application/json");
            return doc;
        }
    };

    public Route delete = new StashRoute(){
        @Override
        public Stash handleAndTransform(Request req, Response res) {
            Stash doc = rep.deleteDoc(Integer.parseInt(req.params("id")));
            res.status(200);
            res.header("Content-Type", "application/json");
            return doc;
        }
    };

}
