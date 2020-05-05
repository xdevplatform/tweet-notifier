package service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioService {

  private String accountSid;
  private String authToken;
  private String from;
  private String to;

  public TwilioService(String sid, String token, String fromNumber, String toNumber) {
    accountSid = sid;
    authToken = token;
    from = fromNumber;
    to = toNumber;
  }

  public void sendMessage(String url) {
    if (validParameters(accountSid, authToken, from, to)) {

      Twilio.init(accountSid, authToken);

      Message.creator(new PhoneNumber(to), new PhoneNumber(from), url)
          .create();
    }
  }

  private boolean validParameters(String accountSid, String authToken, String from, String to) {
    return ((null != accountSid && null != authToken && null != from && null != to) && (accountSid != "" && authToken != "" && from != "" && to != ""));
  }
}
