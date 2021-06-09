package com.tailoredshapes.donut;

import com.mongodb.client.MongoDatabase;
import com.tailoredshapes.stash.Stash;
import org.bson.Document;
import org.json.simple.JSONObject;

import javax.print.Doc;
import java.util.Optional;

import static com.tailoredshapes.underbar.UnderBar.optional;

public class MongoRepository implements Repository{


    private MongoDatabase db;
    private String collection;

    public MongoRepository(MongoDatabase db, String collection) {
        this.db = db;
        this.collection = collection;
    }

    @Override
    public Stash writeDoc(int id, Stash body) {
        Document document = Document.parse(body.toJSONString());
        document.append("id", id);
        db.getCollection(collection).insertOne(document);
        return Stash.parseJSON(document.toJson());
    }

    @Override
    public Stash readDoc(int id) {
        Optional<Document> doc = Optional.ofNullable(db.getCollection(collection).find(new Document().append("id", id)).first());
        return Stash.parseJSON(doc.orElse(new Document()).toJson());
    }

    @Override
    public Stash changeDoc(int id, Stash body) {
        Document doc = Document.parse(body.toJSONString());
        doc.append("id", id);
        Document update = new Document();
        update.append("$set", doc);
        Document stored = db.getCollection(collection).findOneAndUpdate(new Document().append("id", id), update);

        return Stash.parseJSON(stored.toJson());
    }

    @Override
    public Stash deleteDoc(int id) {
        db.getCollection(collection).findOneAndDelete(new Document().append("id", id));
        return new Stash();
    }
}
