package com.tailoredshapes.donut;

import com.beerboy.spark.typify.route.GenericRoute;
import com.tailoredshapes.stash.Stash;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class StashRoute implements Route, GenericRoute {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        return ((Stash) this.handleAndTransform(request, response)).toJSONString();
    }
}
