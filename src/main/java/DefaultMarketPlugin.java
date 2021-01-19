import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.*;
import model.History;
import model.order.Client;
import model.order.Instrument;
import model.SubmitOrder;
import model.order.ValidatedOrder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DefaultMarketPlugin implements MarketPlugin {

    private static final String SERVER_URL = "https://hsbc.hackaton.page";
    private static final String PORTFOLIO_ENDPOINT = "/portfolio";
    private static final String BUY_ENDPOINT = "/buy";
    private static final String SELL_ENDPOINT = "/sell";
    private static final String INSTRUMENTS_ENDPOINT = "/instruments";
    private static final String SUBMITTED_ENDPOINT = "/submitted";
    private static final String PROCESSED_ENDPOINT = "/processed";
    private static final String HISTORY_ENDPOINT = "/history";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

    private final HttpRequest.Builder builder;

    public DefaultMarketPlugin(Client client, String password) {
        final var basic = Base64.getEncoder().encodeToString((client.name() + ":" + password).getBytes(StandardCharsets.UTF_8));
        this.builder = HttpRequest
                .newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + basic);
    }


    @Override
    public Portfolio portfolio() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + PORTFOLIO_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, Portfolio.Current.class);
                default -> new Portfolio.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new Portfolio.Failed(e.getMessage());
        }
    }

    @Override
    public ValidatedOrder buy(SubmitOrder.Buy buy) {
        try {
            final var requestBody = writer.writeValueAsString(buy);

            final var request = builder
                    .uri(URI.create(SERVER_URL + BUY_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, ValidatedOrder.Acknowledged.class);
                case 400 -> objectMapper.readValue(body, ValidatedOrder.Rejected.class);
                default -> new ValidatedOrder.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new ValidatedOrder.Failed(e.getMessage());
        }
    }

    @Override
    public ValidatedOrder sell(SubmitOrder.Sell sell) {
        try {
            final var requestBody = writer.writeValueAsString(sell);

            final var request = builder
                    .uri(URI.create(SERVER_URL + SELL_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, ValidatedOrder.Acknowledged.class);
                case 400 -> objectMapper.readValue(body, ValidatedOrder.Rejected.class);
                default -> new ValidatedOrder.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new ValidatedOrder.Failed(e.getMessage());
        }
    }

    @Override
    public History history(Instrument instrument) {
        try {
            final var requestBody = writer.writeValueAsString(new InstrumentHistory(instrument));

            final var request = builder
                    .uri(URI.create(SERVER_URL + HISTORY_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, History.Correct.class);
                default -> new History.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new History.Failed(e.getMessage());
        }
    }

    @Override
    public Instruments instruments() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + INSTRUMENTS_ENDPOINT))
                    .GET()
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, Instruments.Correct.class);
                default -> new Instruments.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new Instruments.Failed(e.getMessage());
        }
    }

    @Override
    public Submitted submitted() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + SUBMITTED_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, Submitted.Correct.class);
                default -> new Submitted.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new Submitted.Failed(e.getMessage());
        }
    }

    @Override
    public Processed processed() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + PROCESSED_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            return switch (response.statusCode()) {
                case 200 -> objectMapper.readValue(body, Processed.Correct.class);
                default -> new Processed.Failed(body);
            };
        } catch (IOException | InterruptedException e) {
            return new Processed.Failed(e.getMessage());
        }
    }
}
