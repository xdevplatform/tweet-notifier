package service;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecretsManagerService {

  private AWSSecretsManager awsSecretsManager;

  public SecretsManagerService(String region) {
    awsSecretsManager = AWSSecretsManagerClientBuilder.standard()
        .withRegion(region)
        .build();
  }

  public Map<String, String> getSecrets(String secret) {
    Map<String, String> map = new HashMap<>();

    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secret);
    GetSecretValueResult getSecretValueResponse = null;
    try {
      getSecretValueResponse = awsSecretsManager.getSecretValue(getSecretValueRequest);
    } catch (ResourceNotFoundException e) {
      System.out.println("The requested secret " + secret + " was not found");
    } catch (InvalidRequestException e) {
      System.out.println("The request was invalid due to: " + e.getMessage());
    } catch (InvalidParameterException e) {
      System.out.println("The request had invalid params: " + e.getMessage());
    }

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode secretsJson = null;

    if (null != getSecretValueResponse) {
      String s = getSecretValueResponse.getSecretString();
      try {
        secretsJson = objectMapper.readTree(s);
      } catch (JsonProcessingException e) {
        System.out.println(e.getMessage());
      }
      if (null != secretsJson) {
        Map<String, Object> result = objectMapper.convertValue(secretsJson, Map.class);
        for (Map.Entry entry : result.entrySet()) {
          String key = (String) entry.getKey();
          String value = (String) entry.getValue();
          map.put(key, value);
        }
      }
    }
    return map;
  }

}
