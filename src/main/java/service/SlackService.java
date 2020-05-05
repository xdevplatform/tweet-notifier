package service;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

public class SlackService {

  private String webhook;
  private String slackBotName;

  public SlackService(String url, String name) {
    webhook = url;
    slackBotName = name;
  }

  public void postMessage(String url) {
    if (validParameters(webhook, slackBotName)) {
      SlackApi api = new SlackApi(webhook);
      SlackMessage message = new SlackMessage(slackBotName, url);
      message.setUnfurlLinks(true);
      message.setUnfurlMedia(true);
      api.call(message);
    }
  }

  private boolean validParameters(String webhook, String slackBotName) {
    return ((null != webhook && null != slackBotName) && (webhook != "" && slackBotName != ""));
  }

}
