package me.renedo.service;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Slf4j
@ApplicationScoped
public class TweeterService {

    private final static int PAGE_SIZE = 40;

    @Inject
    EventBus bus;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    ThreadContext threadContext;

    @Inject
    RadarService radarService;

    @ConfigProperty(name = "oauth.consumerKey")
    String consumerKey;

    @ConfigProperty(name = "oauth.consumerSecret")
    String consumerSecret;

    @ConfigProperty(name = "oauth.accessToken")
    String accessToken;

    @ConfigProperty(name = "oauth.accessTokenSecret")
    String accessTokenSecret;

    @ConsumeEvent("scrap")
    public void scrap(Integer page) {
        Uni.createFrom().item(() -> getStatuses(page)).onItem().invoke(this::processStatuses)
                .subscribe().with(i -> log.info("Page {} OK", page), f -> log.error("Error porcessing status: {}", f.getMessage()));
    }

    private List<Status> getStatuses(int page) {
        List<Status> statuses = new ArrayList<>();
        Twitter twitter = new TwitterFactory(new ConfigurationBuilder().setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret).build()).getInstance();
        Paging paging = new Paging(page, PAGE_SIZE);
        try {
            statuses.addAll(twitter.getUserTimeline("cyrburgos", paging));
            if (statuses.size() >= PAGE_SIZE) {
                bus.send("scrap", page + 1);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return statuses;
    }

    private void processStatuses(List<Status> statuses) {
        managedExecutor.runAsync(threadContext.contextualRunnable(() ->
                log.info("Created {}", statuses.stream().map(radarService::processStatus).filter(b -> b).count())));
    }
}
