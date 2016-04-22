package hello.log4j2;

import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.nosql.appender.NoSqlConnection;
import org.apache.logging.log4j.nosql.appender.NoSqlObject;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ElasticsearchConnection implements NoSqlConnection<Map<String, Object>, ElasticsearchObject> {

    private final ElasticsearchTransportClient client;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public ElasticsearchConnection(final ElasticsearchTransportClient client) {
        this.client = client;
    }

    @Override
    public ElasticsearchObject createObject() {
        return new ElasticsearchObject();
    }

    @Override
    public ElasticsearchObject[] createList(final int length) {
        return new ElasticsearchObject[length];
    }

    @Override
    public void insertObject(final NoSqlObject<Map<String, Object>> object) {
        try {
            client.index(object.unwrap());
        } catch (Exception e) {
            throw new AppenderLoggingException("failed to write log event to Elasticsearch: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            client.close();
        }
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }
}
