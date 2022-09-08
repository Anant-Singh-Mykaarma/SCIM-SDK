package de.captaingoldfish.scim.sdk.common.request;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.scim.sdk.common.constants.EndpointPaths;
import de.captaingoldfish.scim.sdk.common.constants.enums.HttpMethod;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import de.captaingoldfish.scim.sdk.common.utils.FileReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;


/**
 * author Pascal Knueppel <br>
 * created at: 25.10.2019 - 22:55 <br>
 * <br>
 */
public class BulkRequestOperationTest implements FileReferences
{


  /**
   * tests that the getter and setter methods are working correctly
   */
  @Test
  public void testGetAndSetValues()
  {
    final HttpMethod method = HttpMethod.POST;
    final String bulkId = UUID.randomUUID().toString();
    final String path = EndpointPaths.USERS;
    final String data = readResourceFile(FileReferences.USER_RESOURCE).replaceAll("\\s", "");

    BulkRequestOperation operations = BulkRequestOperation.builder()
                                                          .method(method)
                                                          .bulkId(bulkId)
                                                          .path(path)
                                                          .data(data)
                                                          .build();
    Assertions.assertEquals(method, operations.getMethod());
    Assertions.assertEquals(bulkId, operations.getBulkId().get());
    Assertions.assertEquals(path, operations.getPath());
    Assertions.assertEquals(data, operations.getData().get());

    Assertions.assertEquals(method.name(), operations.get("method").textValue());
    Assertions.assertEquals(bulkId, operations.get("bulkId").textValue());
    Assertions.assertEquals(path, operations.get("path").textValue());
    Assertions.assertEquals(data, operations.get("data").toString());
    Assertions.assertEquals(JsonHelper.readJsonDocument(data), operations.get("data"));
  }

  /**
   * verifies that exceptions are thrown if required attributes are missing if getter are called
   */
  @Test
  public void testThrowExceptionsOnRequiredMissingAttributes()
  {
    BulkRequestOperation operations = new BulkRequestOperation();
    Assertions.assertThrows(BadRequestException.class, operations::getMethod);
    Assertions.assertThrows(BadRequestException.class, operations::getPath);
  }
}
