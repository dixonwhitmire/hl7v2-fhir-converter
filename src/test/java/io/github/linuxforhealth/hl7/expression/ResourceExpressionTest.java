/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.linuxforhealth.hl7.expression;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import io.github.linuxforhealth.api.EvaluationResult;
import io.github.linuxforhealth.core.expression.SimpleEvaluationResult;
import io.github.linuxforhealth.core.terminology.SimpleCode;
import io.github.linuxforhealth.hl7.message.HL7MessageData;
import io.github.linuxforhealth.hl7.parsing.HL7DataExtractor;
import io.github.linuxforhealth.hl7.parsing.HL7HapiParser;

public class ResourceExpressionTest {
  String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
      + "EVN|A01|20130617154644\r"
      + "PID|1|465 306 5961|000010016^5^M11^SY1^MR^|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
      + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
      + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||";

  @Test
  public void test1_segment() throws IOException {

    Message hl7message = getMessage(message);
    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);

    Structure s = hl7DTE.getStructure("PID", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("PID.3")
        .withValueOf("datatype/Identifier").build();
    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();


    Map<String, EvaluationResult> context = new HashMap<>();

    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    Map<String, Object> result = (Map<String, Object>) value.getValue();
    assertThat(result.get("use")).isEqualTo(null);
    assertThat(result.get("value")).isEqualTo("000010016");



  }



  @Test
  public void test_component_required_missing() throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961||407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||";

    Message hl7message = getMessage(message);
    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);

    Structure s = hl7DTE.getStructure("PID", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("PID.3")
        .withValueOf("datatype/Identifier").build();

    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();


    Map<String, EvaluationResult> context = new HashMap<>();

    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    assertThat(value).isNull();



  }


  @Test
  public void test_picks_next_value_from_rep_if_first_fails_condition_or_check()
      throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961|^^^MR^SSS^^20091020^20200101~000010017^^^MR~000010018^^^MR|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||";

    Message hl7message = getMessage(message);
    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);

    Structure s = hl7DTE.getStructure("PID", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("PID.3")
        .withValueOf("datatype/Identifier").build();

    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();


    Map<String, EvaluationResult> context = new HashMap<>();

    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    assertThat(value).isNotNull();
    Map<String, Object> result = (Map<String, Object>) value.getValue();
    assertThat(result.get("use")).isEqualTo(null);
    assertThat(result.get("value")).isEqualTo("000010017");
    assertThat(result.get("type")).isNull();



  }



  @Test
  public void test1_segment_rep() throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961|000010016^^^SY1^MR~000010017^^^SY2^SS~000010018^^^MR|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||";
    Message hl7message = getMessage(message);
    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);


    Structure s = hl7DTE.getStructure("PID", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("PID.3")
        .withValueOf("datatype/Identifier").withGenerateList(true).build();

    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();


    Map<String, EvaluationResult> context = new HashMap<>();
    context.put("code", new SimpleEvaluationResult(hl7DTE.getTypes((Segment) s, 3)));


    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    List<Object> results = (List<Object>) value.getValue();
    assertThat(results).hasSize(3);


    Map<String, Object> result = (Map<String, Object>) results.get(0);
    assertThat(result.get("use")).isEqualTo(null);
    assertThat(result.get("value")).isEqualTo("000010016");
    assertThat(result.get("system")).isNull();
    assertThat(result.get("type")).isNotNull();



  }



  @Test
  public void test1_segment_identifier_obx() throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961|000010016^^^MR~000010017^^^MR~000010018^^^MR|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||\r"
        + "OBX|1|TX|1234||First line: ECHOCARDIOGRAPHIC REPORT||||||F||\r";
    Message hl7message = getMessage(message);
    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);


    Structure s = hl7DTE.getStructure("OBX", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("OBX.3")
        .withValueOf("datatype/Identifier").build();

    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();


    Map<String, EvaluationResult> context = new HashMap<>();


    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));
    Map<String, Object> result = (Map<String, Object>) value.getValue();
    assertThat(result.get("use")).isEqualTo(null);
    assertThat(result.get("value")).isEqualTo("1234");
    assertThat(result.get("system")).isEqualTo(null);



  }

  @Test
  public void test1_segment_identifier_obx_cc() throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961|000010016^^^MR~000010017^^^MR~000010018^^^MR|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||\r"
        + "OBX|1|TX|1234^some text^SCT||First line: ECHOCARDIOGRAPHIC REPORT||||||F||\r";


    Message hl7message = getMessage(message);

    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);


    Structure s = hl7DTE.getStructure("OBX", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("OBX.3")
        .withValueOf("datatype/Identifier").withGenerateList(true).build();
    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();

    Map<String, EvaluationResult> context = new HashMap<>();

    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    List<Map<String, Object>> result = (List<Map<String, Object>>) value.getValue();
    Map<String, Object> type = (Map<String, Object>) result.get(0).get("type");

    assertThat(type.get("text")).isEqualTo("some text");
    assertThat(type.get("coding")).isNotNull();
    List<Object> list = (List) type.get("coding");
    Map<String, String> sp = (Map<String, String>) list.get(0);
    assertThat(sp.get("code")).isEqualTo("1234");
    assertThat(sp.get("system")).isEqualTo("http://snomed.info/sct");


  }


  @Test
  public void test1_segment_identifier_obx_cc_known_system() throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961|000010016^^^MR~000010017^^^MR~000010018^^^MR|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||\r"
        + "OBX|1|TX|1234^some text^SCT||First line: ECHOCARDIOGRAPHIC REPORT||||||F||\r";


    Message hl7message = getMessage(message);

    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);


    Structure s = hl7DTE.getStructure("OBX", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("OBX.3")
        .withValueOf("datatype/CodeableConcept").withGenerateList(true).build();

    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();

    Map<String, EvaluationResult> context = new HashMap<>();

    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    List<Map<String, Object>> result = (List<Map<String, Object>>) value.getValue();
    assertThat(result.get(0).get("text")).isEqualTo("some text");
    assertThat(result.get(0).get("coding")).isNotNull();
    List<Object> list = (List) result.get(0).get("coding");
    Map<String, String> sp = (Map<String, String>) list.get(0);
    assertThat(sp.get("code")).isEqualTo("1234");
    assertThat(sp.get("system")).isEqualTo("http://snomed.info/sct");


  }

  @Test
  public void test1_codeable_concept_from_IS_type() throws IOException {
    String message = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r"
        + "EVN|A01|20130617154644\r"
        + "PID|1|465 306 5961|000010016^^^MR~000010017^^^MR~000010018^^^MR|407623|Wood^Patrick^^^MR||19700101|female|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r"
        + "NK1|1|Wood^John^^^MR|Father||999-9999\r" + "NK1|2|Jones^Georgie^^^MSS|MOTHER||999-9999\r"
        + "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||\r"
        + "OBX|1|TX|1234^^SCT||First line: ECHOCARDIOGRAPHIC REPORT|||AA|||F||\r";


    Message hl7message = getMessage(message);

    HL7DataExtractor hl7DTE = new HL7DataExtractor(hl7message);


    Structure s = hl7DTE.getStructure("OBX", 0).getValue();
    ExpressionAttributes attr = new ExpressionAttributes.Builder().withSpecs("OBX.8")
        .withValueOf("datatype/CodeableConcept").withGenerateList(true).build();
    ResourceExpression exp = new ResourceExpression(attr);
    assertThat(exp.getData()).isNotNull();

    Map<String, EvaluationResult> context = new HashMap<>();

    EvaluationResult value = exp.evaluate(new HL7MessageData(hl7DTE), ImmutableMap.copyOf(context),
        new SimpleEvaluationResult(s));

    List<Map<String, Object>> result = (List<Map<String, Object>>) value.getValue();
    assertThat(result.get(0).get("text")).isEqualTo("AA");
    assertThat(result.get(0).get("coding")).isNotNull();
    List<SimpleCode> scs = (List<SimpleCode>) result.get(0).get("coding");
    SimpleCode sc = scs.get(0);
    assertThat(sc.getCode()).isEqualTo("AA");
    assertThat(sc.getSystem()).isEqualTo("http://terminology.hl7.org/CodeSystem/v2-0078");
    assertThat(sc.getDisplay()).isEqualTo("Critically abnormal");


  }


  private static Message getMessage(String message) throws IOException {
    HL7HapiParser hparser = null;

    try {
      hparser = new HL7HapiParser();
      return hparser.getParser().parse(message);
    } catch (HL7Exception e) {
      throw new IllegalArgumentException(e);
    } finally {
      if (hparser != null) {
        hparser.getContext().close();
      }
    }

  }

}
