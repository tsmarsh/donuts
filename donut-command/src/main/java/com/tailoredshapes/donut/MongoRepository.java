package com.tailoredshapes.donut;

import com.mongodb.client.MongoDatabase;
import com.tailoredshapes.stash.Stash;
import org.bson.Document;

import java.time.Instant;
import java.util.Optional;

import static com.tailoredshapes.underbar.UnderBar.list;

public class MongoRepository implements Repository {


    private final String TIMESTAMP_KEY = "_timestamp";
    private final String VERSION_KEY = "_version";
    private final MongoDatabase db;
    private final String collection;

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
        document.append(TIMESTAMP_KEY, Instant.now().toEpochMilli());
        document.append(VERSION_KEY, 0);
        
        db.getCollection(collection).insertOne(document);
        return Stash.parseJSON(document.toJson());
    }

    @Override
    public Stash readDoc(int id) {
        var docs = list(db.getCollection(collection).find(new Document()
                .append("id", id)).sort(new Document(TIMESTAMP_KEY, -1)));

        if(docs.isEmpty()){
            return new Stash();
        }else {
            return Stash.parseJSON(docs.get(0).toJson());
        }
    }

    @Override
    public Stash readDoc(int id, long atEpochMillis) {

        Optional<Document> doc = Optional.ofNullable(db.getCollection(collection).find(new Document()
                .append("id", id)
                .append(TIMESTAMP_KEY, new Document("$lte", atEpochMillis)))
                .sort(new Document(VERSION_KEY, 1))
                .first());

        return Stash.parseJSON(doc.orElse(new Document()).toJson());
    }

    @Override
    public Stash changeDoc(int id, Stash body) {
        Stash old = readDoc(id);

        Document document = Document.parse(body.toJSONString());
        document.append("id", id);
        document.append(VERSION_KEY, old.asInteger(VERSION_KEY) + 1);
        document.append(TIMESTAMP_KEY, Instant.now().toEpochMilli());

        db.getCollection(collection).insertOne(document);
        return Stash.parseJSON(document.toJson());
    }

    @Override
    public Stash deleteDoc(int id) {
        db.getCollection(collection).findOneAndDelete(new Document().append("id", id));
        return new Stash();
    }
}
