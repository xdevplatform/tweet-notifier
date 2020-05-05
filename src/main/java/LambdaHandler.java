import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import service.ChimeService;
import service.DynamoService;
import service.SecretsManagerService;
import service.SlackService;
import service.TwilioService;
import service.TwitterService;

public class LambdaHandler implements RequestHandler<ScheduledEvent, Void> {

  private TwitterService twitterService;
  private ChimeService chimeService;
  private DynamoService dynamoService;
  private SlackService slackService;
  private TwilioService twilioService;
  private static final String BOT_NAME = System.getenv("BOT_NAME");
  private static final String SECRET_NAME = System.getenv("SECRET_NAME");
  private static final String REGION = System.getenv("REGION");
  private static final String SEARCH_STRING = System.getenv("SEARCH_STRING");

  public Void handleRequest(ScheduledEvent event, Context context) {

    SecretsManagerService secretsManagerService = new SecretsManagerService(REGION != null ? REGION : "us-east-1");
    Map<String, String> secrets = secretsManagerService.getSecrets(SECRET_NAME != null ? SECRET_NAME : "tweet-notifier");
    if (null != secrets && secrets.size() > 0) {
      setup(secrets);
      if (null != twitterService) {
        List<String> tweetIds = null;
        try {
          tweetIds = twitterService.getTweets(SEARCH_STRING != null ? SEARCH_STRING : "(from:@CNN OR from:@BBCWorld) lang:en \"breaking news\" -is:retweet", getTime());
        } catch (Exception e) {
          System.out.println(e.getLocalizedMessage());
        }
        if (null != tweetIds && tweetIds.size() > 0) {
          if (null != twilioService) {
            twilioService.sendMessage(tweetUrlsAsString(tweetIds));
          }
          for (String id : tweetIds) {
            String url = tweetAsUrl(id);
            if (null != dynamoService) {
              dynamoService.storeTweetIds(id);
            }
            if (null != slackService) {
              slackService.postMessage(url);
            }
            if (null != chimeService) {
              try {
                chimeService.postMessage(url);
              } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
              }
            }
          }
        }
      }
    }
    return null;
  }

  private void setup(Map<String, String> secrets) {
    if (secrets.containsKey("twitter-api-key") && secrets.containsKey("twitter-api-secret")) {
      twitterService = new TwitterService(secrets.get("twitter-api-key"), secrets.get("twitter-api-secret"));
      if (secrets.containsKey("slack-webhook-url")) {
        slackService = new SlackService(secrets.get("slack-webhook-url"), BOT_NAME != null ? BOT_NAME : "Twitter Bot");
      }
      if (secrets.containsKey("chime-webhook-url")) {
        chimeService = new ChimeService(secrets.get("chime-webhook-url"));
      }
      if (secrets.containsKey("dynamo-table-name") && secrets.containsKey("dynamo-table-pk")) {
        dynamoService = new DynamoService(secrets.get("dynamo-table-name"), secrets.get("dynamo-table-pk"), REGION != null ? REGION : "us-east-1");
      }
      if (secrets.containsKey("twilio-sid") && secrets.containsKey("twilio-token") && secrets.containsKey("twilio-from-number") && secrets.containsKey("twilio-to-number")) {
        twilioService = new TwilioService(secrets.get("twilio-sid"), secrets.get("twilio-token"), secrets.get("twilio-from-number"), secrets.get("twilio-to-number"));
      }
    }
  }

  private String tweetAsUrl(String id) {
    return String.format("https://twitter.com/s/status/%s", id);
  }

  private String tweetUrlsAsString(List<String> tweetsIds) {
    StringBuilder sb = new StringBuilder();
    for (String id : tweetsIds) {
      sb.append(tweetAsUrl(id));
      sb.append("\n\n");
    }
    return sb.toString();
  }

  private String getTime() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(new Date(System.currentTimeMillis() - 3600 * 1000));
  }

}
