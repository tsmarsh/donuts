package com.tailoredshapes.donut;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.tailoredshapes.stash.Stash;
import org.bson.Document;
import org.json.simple.JSONObject;

import javax.print.Doc;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.tailoredshapes.underbar.UnderBar.list;
import static com.tailoredshapes.underbar.UnderBar.optional;

public class MongoRepository implements Repository {


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
        Optional<Document> existing = Optional.ofNullable(db.getCollection(collection).find(new Document().append("id", id)).first());
        if (existing.isPresent()) {
            return new Stash();
        }
        document.append("timestamp", Instant.now().toEpochMilli());
        db.getCollection(collection).insertOne(document);
        return Stash.parseJSON(document.toJson());
    }

    @Override
    public Stash readDoc(int id) {
        var doc = Optional.ofNullable(db.getCollection(collection).find(new Document()
                .append("id", id)).sort(new Document("timestamp", -1)).first());

        return Stash.parseJSON(doc.orElse(new Document()).toJson());
    }

    @Override
    public Stash readDoc(int id, long atEpochMillis) {

        Optional<Document> doc = Optional.ofNullable(db.getCollection(collection).find(new Document()
                .append("id", id)
                .append("timestamp", new Document("$lte", atEpochMillis)))
                .sort(new Document("timestamp", 1))
                .first());

        return Stash.parseJSON(doc.orElse(new Document()).toJson());
    }

    @Override
    public Stash changeDoc(int id, Stash body) {
        Document doc = Document.parse(body.toJSONString());
        doc.append("id", id);
        doc.append("timestamp", Instant.now().toEpochMilli());


        db.getCollection(collection).insertOne(doc);

        return Stash.parseJSON(doc.toJson());
    }

    @Override
    public Stash deleteDoc(int id) {
        db.getCollection(collection).findOneAndDelete(new Document().append("id", id));
        return new Stash();
    }
}
