package hello.log4j2;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticsearchTransportClient {

    private final Client client;

    private final BulkProcessor bulkProcessor;

    private final String index;

    private final String type;

    public ElasticsearchTransportClient(Client client, String index, String type,
                                        int maxActionsPerBulkRequest,
                                        int maxConcurrentBulkRequests,
                                        ByteSizeValue maxVolumePerBulkRequest,
                                        TimeValue flushInterval) {
        this.client = client;
        this.index = index;
        this.type = type;
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest requst, Throwable failure) {
            }
        };
        BulkProcessor.Builder builder = BulkProcessor.builder(client, listener)
                .setBulkActions(maxActionsPerBulkRequest)
                .setConcurrentRequests(maxConcurrentBulkRequests)
                .setFlushInterval(flushInterval);
        if (maxVolumePerBulkRequest != null) {
            builder.setBulkSize(maxVolumePerBulkRequest);
        }
        this.bulkProcessor = builder.build();
    }

    public ElasticsearchTransportClient index(Map<String, Object> source) {
        if (((TransportClient)client).connectedNodes().isEmpty()) {
            throw new RuntimeException("client is disconnected");
        }

        String index = this.index.indexOf('\'') < 0 ? this.index : getIndexNameDateFormat(this.index).format(new Date());
        bulkProcessor.add(new IndexRequest(index).type(type).create(false).source(source));

        return this;
    }

    public void close() {
        bulkProcessor.close();
        client.close();
    }

    private static final ThreadLocal<Map<String, SimpleDateFormat>> df = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        public Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<String, SimpleDateFormat>();
        }
    };

    private SimpleDateFormat getIndexNameDateFormat(String index) {
        Map<String, SimpleDateFormat> formatters = df.get();
        SimpleDateFormat formatter = formatters.get(index);
        if (formatter == null) {
            formatter = new SimpleDateFormat();
            formatter.applyPattern(index);
            formatters.put(index, formatter);
        }
        return formatter;
    }
}
