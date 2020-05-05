package service;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

public class DynamoService {

  private AmazonDynamoDB amazonDynamoDB;
  private String tableName;
  private String primaryKey;

  public DynamoService(String name, String key, String region) {
    tableName = name;
    primaryKey = key;
    amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
        .withRegion(region)
        .build();
  }

  public void storeTweetIds(String tweetId) {
    if (validParameters(tableName, primaryKey)) {
      Map<String, AttributeValue> attributeValues = new HashMap<>();
      attributeValues.put(primaryKey, new AttributeValue().withS(tweetId));
      PutItemRequest putItemRequest = new PutItemRequest()
          .withTableName(tableName)
          .withItem(attributeValues);
      amazonDynamoDB.putItem(putItemRequest);
    }
  }

  private boolean validParameters(String tableName, String primaryKey) {
    return ((null != tableName && null != primaryKey) && (tableName != "" && primaryKey != ""));
  }

}
