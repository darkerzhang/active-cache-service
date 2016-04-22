package hello.log4j2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.nosql.appender.NoSqlProvider;
import org.apache.logging.log4j.status.StatusLogger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Plugin(name = "Elasticsearch", category = "Core", printObject = true)
public class ElasticsearchProvider implements NoSqlProvider<ElasticsearchConnection> {

    private static final Logger logger = StatusLogger.getLogger();

    private final ElasticsearchTransportClient client;

    private final String description;

    private ElasticsearchProvider(final ElasticsearchTransportClient client, final String description) {
        this.client = client;
        this.description = "elasticsearch{ " + description + " }";
    }

    @Override
    public ElasticsearchConnection getConnection() {
        //TODO validate client is viable
        return new ElasticsearchConnection(client);
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * Factory method for creating an Elasticsearch provider within the plugin manager.
     *
     * @param cluster The name of the Elasticsearch cluster to which log event documents will be written.
     * @param host    The host name an Elasticsearch server node of the cluster, defaults to localhost.
     * @param port    The port that Elasticsearch is listening on, defaults to 9300
     * @param index   The index that Elasticsearch shall use for indexing
     * @param type    The type of the index Elasticsearch shall use for indexing
     * @return a new Elasticsearch provider
     */
    @PluginFactory
    public static ElasticsearchProvider createNoSqlProvider(
            @PluginAttribute("cluster") String cluster,
            @PluginAttribute("host") String host,
            @PluginAttribute("port") Integer port,
            @PluginAttribute("index") String index,
            @PluginAttribute("type") String type,
            @PluginAttribute("timeout") String timeout,
            @PluginAttribute("maxActionsPerBulkRequest") Integer maxActionsPerBulkRequest,
            @PluginAttribute("maxConcurrentBulkRequests") Integer maxConcurrentBulkRequests,
            @PluginAttribute("maxVolumePerBulkRequest") String maxVolumePerBulkRequest,
            @PluginAttribute("flushInterval") String flushInterval) {

        if (cluster == null || cluster.isEmpty()) {
            cluster = "elasticsearch";
        }
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }
        if (port == null || port == 0) {
            port = 9300;
        }
        if (index == null || index.isEmpty()) {
            index = "log4j2";
        }
        if (type == null || type.isEmpty()) {
            type = "log4j2";
        }
        if (timeout == null || timeout.isEmpty()) {
            timeout = "30s";
        }
        if (maxActionsPerBulkRequest == null) {
            maxActionsPerBulkRequest = 1000;
        }
        if (maxConcurrentBulkRequests == null) {
            maxConcurrentBulkRequests = 2 * Runtime.getRuntime().availableProcessors();
        }
        if (maxVolumePerBulkRequest == null || maxVolumePerBulkRequest.isEmpty()) {
            maxVolumePerBulkRequest = "10m";
        }

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", cluster)
                .put("network.server", false)
                .put("node.client", true)
                .put("client.transport.sniff", true)
                .put("client.transport.ping_timeout", timeout)
                .put("client.transport.ignore_cluster_name", false)
                .put("client.transport.nodes_sampler_interval", "30s")
                .build();

        TransportClient client = TransportClient.builder().settings(settings).build();
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (client.connectedNodes().isEmpty()) {
            logger.error("unable to connect to Elasticsearch cluster");
            return null;
        }
        String description = "cluster=" + cluster + ",host=" + host + ",port=" + port + ",index=" + index + ",type=" + type;
        ElasticsearchTransportClient elasticsearchTransportClient = new ElasticsearchTransportClient(client, index, type,
                maxActionsPerBulkRequest, maxConcurrentBulkRequests,
                ByteSizeValue.parseBytesSizeValue(maxVolumePerBulkRequest, "test"),
                TimeValue.parseTimeValue(flushInterval, TimeValue.timeValueSeconds(30), "test"));
        return new ElasticsearchProvider(elasticsearchTransportClient, description);
    }

}
