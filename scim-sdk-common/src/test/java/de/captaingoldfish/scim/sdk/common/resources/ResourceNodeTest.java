package de.captaingoldfish.scim.sdk.common.resources;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.scim.sdk.common.constants.AttributeNames.RFC7643;
import de.captaingoldfish.scim.sdk.common.constants.ClassPathReferences;
import de.captaingoldfish.scim.sdk.common.exceptions.InternalServerException;
import de.captaingoldfish.scim.sdk.common.resources.complex.Manager;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.resources.complex.Name;
import de.captaingoldfish.scim.sdk.common.schemas.Schema;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.common.utils.FileReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;


/**
 * author Pascal Knueppel <br>
 * created at: 12.10.2019 - 00:00 <br>
 * <br>
 */
public class ResourceNodeTest implements FileReferences
{

  /**
   * will verify that the attributes of a {@link ResourceNode} are correctly set and read
   */
  @Test
  public void testSetAndGetValues()
  {
    final String id = UUID.randomUUID().toString();
    final String externalId = UUID.randomUUID().toString();
    final Meta meta = buildMetaObject();
    TestResource testResource = new TestResource();
    testResource.setId(id);
    testResource.setExternalId(externalId);
    testResource.setMeta(meta);

    Assertions.assertEquals(id, testResource.getId().get());
    Assertions.assertEquals(externalId, testResource.getExternalId().get());
    Assertions.assertEquals(meta, testResource.getMeta().get());
  }

  /**
   * verifies that empty strings can be set as legal values
   */
  @ParameterizedTest
  @ValueSource(strings = {"", " ", "  "})
  public void testAddEmptyStringValue(String emptyString)
  {
    TestResource testResource = new TestResource();
    testResource.setExternalId(emptyString);
    Assertions.assertEquals(emptyString, testResource.getExternalId().get());
  }

  /**
   * verifies that the sortBy attribute is correctly returned from a {@link ResourceNode}
   */
  @TestFactory
  public List<DynamicTest> testGetSortByAttribute()
  {
    JsonNode userSchemaNode = JsonHelper.loadJsonDocument(ClassPathReferences.USER_SCHEMA_JSON);
    Schema userSchema = new Schema(userSchemaNode, null);
    User user = JsonHelper.loadJsonDocument(USER_RESOURCE, User.class);


    List<DynamicTest> dynamicTestList = new ArrayList<>();
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "username", "chuck"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "nickname", "chucky"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "title", "Mr."));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "usertype", "super user"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "name.familyname", "Norris"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "name.givenname", "Carlos"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "name.middlename", "Ray"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "addresses.locality", "Bremen"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "addresses.streetAddress", "somewhere 56"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "addresses.country", "DE"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "phoneNumbers.type", "home"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "phoneNumbers.value", "666-666-666666"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "phoneNumbers.primary", "true"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "emails.value", "chuck@norris.com"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "emails.type", "work"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "emails.primary", "true"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "groups.value", "123456"));
    dynamicTestList.add(getSortByAttributeTest(userSchema, user, "roles.value", "123456"));
    return dynamicTestList;
  }


  private DynamicTest getSortByAttributeTest(Schema userSchema, User user, String attributeName, String expectedValue)
  {
    return DynamicTest.dynamicTest(attributeName + " eq " + expectedValue, () -> {
      SchemaAttribute schemaAttribute = userSchema.getSchemaAttribute(attributeName);
      Optional<JsonNode> sortByAttributeOptional = user.getSortingAttribute(schemaAttribute);
      Assertions.assertTrue(sortByAttributeOptional.isPresent());
      Assertions.assertEquals(expectedValue, sortByAttributeOptional.get().asText());
    });
  }

  /**
   * verifies that an exception is thrown if the developer tries to resets an existing meta attribute
   */
  @Test
  public void testResetMetaAttribute()
  {
    TestResource testResource = new TestResource();
    Assertions.assertDoesNotThrow(() -> testResource.setMeta(Meta.builder().build()));
    Assertions.assertThrows(InternalServerException.class, () -> testResource.setMeta(Meta.builder().build()));
  }

  /**
   * @return a simple meta object
   */
  private Meta buildMetaObject()
  {
    final LocalDateTime created = LocalDateTime.now().withNano(0);
    final LocalDateTime lastModified = LocalDateTime.now().withNano(0);
    final String resourceType = "User";
    final String location = "/Users/" + UUID.randomUUID().toString();
    final String version = "1";
    return Meta.builder()
               .created(created)
               .lastModified(lastModified)
               .resourceType(resourceType)
               .location(location)
               .version(version)
               .build();
  }

  /**
   * makes sure that the method {@link ResourceNode#remove(SchemaAttribute)} works as expected on the main
   * resource
   */
  @DisplayName("removeFromResource works as expected on main resource")
  @Test
  public void testRemoveFromResource()
  {
    Schema userSchema = new Schema(JsonHelper.loadJsonDocument(ClassPathReferences.USER_SCHEMA_JSON));
    SchemaAttribute nameAttribute = userSchema.getSchemaAttribute(RFC7643.NAME);
    SchemaAttribute givenNameAttribute = userSchema.getSchemaAttribute(String.format("%s.%s",
                                                                                     RFC7643.NAME,
                                                                                     RFC7643.GIVEN_NAME));

    User user = JsonHelper.loadJsonDocument(USER_RESOURCE_WITH_EXTENSION, User.class);
    // test complex sub-attribute
    {
      Assertions.assertNotNull(user.getName().flatMap(Name::getGivenName).orElse(null));
      Assertions.assertTrue(user.remove(givenNameAttribute));
      Assertions.assertNull(user.getName().flatMap(Name::getGivenName).orElse(null));
    }

    // test complex
    {
      Assertions.assertNotNull(user.getName().orElse(null));
      Assertions.assertTrue(user.remove(nameAttribute));
      Assertions.assertNull(user.getName().orElse(null));
    }

    SchemaAttribute emailsAttribute = userSchema.getSchemaAttribute(RFC7643.EMAILS);
    SchemaAttribute emailsTypeAttribute = userSchema.getSchemaAttribute(String.format("%s.%s",
                                                                                      RFC7643.EMAILS,
                                                                                      RFC7643.TYPE));

    // test multivalued-complex sub-attribute
    {
      Assertions.assertTrue(user.getEmails().stream().allMatch(email -> email.getType().isPresent()));
      Assertions.assertTrue(user.remove(emailsTypeAttribute));
      Assertions.assertTrue(user.getEmails().stream().noneMatch(email -> email.getType().isPresent()));
    }

    // test multivalued-complex
    {
      Assertions.assertFalse(user.getEmails().isEmpty());
      Assertions.assertTrue(user.remove(emailsAttribute));
      Assertions.assertTrue(user.getEmails().isEmpty());
    }
  }

  /**
   * makes sure that the method {@link ResourceNode#remove(SchemaAttribute)} works as expected on the main
   * resources extensions
   */
  @DisplayName("removeFromResource works as expected on extensions")
  @Test
  public void testRemoveFromExtensionResource()
  {
    Schema enterpriseUserSchema = new Schema(JsonHelper.loadJsonDocument(ClassPathReferences.ENTERPRISE_USER_SCHEMA_JSON));
    SchemaAttribute managerAttribute = enterpriseUserSchema.getSchemaAttribute(RFC7643.MANAGER);
    SchemaAttribute managerRefAttribute = enterpriseUserSchema.getSchemaAttribute(String.format("%s.%s",
                                                                                                RFC7643.MANAGER,
                                                                                                RFC7643.REF));

    User user = JsonHelper.loadJsonDocument(USER_RESOURCE_WITH_EXTENSION, User.class);
    Assertions.assertNotNull(user.getEnterpriseUser()
                                 .flatMap(EnterpriseUser::getManager)
                                 .flatMap(Manager::getRef)
                                 .orElse(null));
    Assertions.assertTrue(user.remove(managerRefAttribute));
    Assertions.assertNull(user.getEnterpriseUser()
                              .flatMap(EnterpriseUser::getManager)
                              .flatMap(Manager::getRef)
                              .orElse(null));

    Assertions.assertNotNull(user.getEnterpriseUser().flatMap(EnterpriseUser::getManager).orElse(null));
    Assertions.assertTrue(user.remove(managerAttribute));
    Assertions.assertNull(user.getEnterpriseUser().flatMap(EnterpriseUser::getManager).orElse(null));
  }

  /**
   * a test implementation of {@link ResourceNode} to have an object to test the methods in the abstract class
   */
  public static class TestResource extends ResourceNode
  {

  }
}
