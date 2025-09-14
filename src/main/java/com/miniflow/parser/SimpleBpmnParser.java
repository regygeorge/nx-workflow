
// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/parser/SimpleBpmnParser.java
// ---------------------------------------------------------------------------
package com.miniflow.parser;

import com.miniflow.core.EngineModel.*;
import com.miniflow.core.EngineModel;
import org.w3c.dom.*;
import org.w3c.dom.Node;


import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class SimpleBpmnParser {
  private static final String ZEEBE_NS = "http://camunda.org/schema/zeebe/1.0";

  public static ProcessDefinition parse(InputStream in){
    try {
      var dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

      Document doc = dbf.newDocumentBuilder().parse(in);
      Element root = doc.getDocumentElement();
      Element process = first(root, "process");
      if (process==null) throw new IllegalArgumentException("No <process>");

      String pid = process.getAttribute("id");
      String pname = process.getAttribute("name");
      EngineModel.Builder b = new EngineModel.Builder(pid, pname);

      Map<String, Element> elById = new HashMap<>();

      // Pass 1: nodes
      NodeList kids = process.getChildNodes();
      for (int i=0;i<kids.getLength();i++){
        org.w3c.dom.Node n = kids.item(i);
        if (n.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE) continue;
        Element e=(Element)n;

        String ln = e.getLocalName(); if (ln==null) continue;
        String id = e.getAttribute("id");
        String name = e.getAttribute("name");
        if (id!=null && !id.isBlank()) elById.put(id, e);

        switch (ln){
          case "startEvent"        -> b.start(id);
          case "endEvent"          -> b.end(id);
          case "userTask"          -> b.userTask(id, name);
          case "exclusiveGateway"  -> b.exclusiveGateway(id, name);
          case "parallelGateway"   -> b.parallelGateway(id, name);
          case "serviceTask", "task" -> {
            Map<String,String> props = readProps(e);
            b.serviceTask(id, name, id, props); // taskType defaults to id
          }
          default -> {}
        }
      }

      // Pass 2: flows with conditions (use SpEL via Expr)
      for (int i=0;i<kids.getLength();i++){
        org.w3c.dom.Node n = kids.item(i);
        if (n.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE) continue;
        Element e=(Element)n;
        if (!"sequenceFlow".equals(e.getLocalName())) continue;

        String from = e.getAttribute("sourceRef");
        String to   = e.getAttribute("targetRef");

        Element exprEl = first(e, "conditionExpression");
        String exprText = (exprEl==null? null : exprEl.getTextContent());

        EngineModel.Condition cond = (vars) -> com.miniflow.core.Expr.evalLogical(exprText, vars);
        EngineModel.Condition condTrue =  v -> true;
        if(exprText==null){
          b.flow(from, to, condTrue);
        }else{
          b.flow(from, to, cond);
        }

      }

      // Build graph
      ProcessDefinition def = b.build();

      // Pass 3: attach props to ALL nodes (so user task form key etc. are available)
      for (var entry : elById.entrySet()){
        String id = entry.getKey();
        Element e = entry.getValue();
        Map<String,String> props = readProps(e);
        if (!props.isEmpty()){
          EngineModel.Node node = def.getNode(id);
          if (node != null) node.props.putAll(props);
        }
      }

      return def;
    } catch (Exception ex){
      throw new RuntimeException("Parse BPMN failed: "+ex.getMessage(), ex);
    }
  }

  // Allow ${...} or #{...}, anything SpEL; Expr.evalLogical handles nulls safely
  private static EngineModel.Condition parseExpr(String raw){
    return vars -> com.miniflow.core.Expr.evalLogical(raw, vars);
  }

  // Namespace-aware props reader
  private static Map<String,String> readProps(Element task){
    Map<String,String> props = new HashMap<>();

    // Task attributes, keep prefix as part of the key (prefix.local)
    NamedNodeMap attrs = task.getAttributes();
    for (int i=0;i<attrs.getLength();i++){
      Attr a = (Attr)attrs.item(i);
      String local = a.getLocalName();
      String prefix = a.getPrefix();
      if (local==null) continue;
      String key = (prefix!=null && !prefix.isBlank()) ? (prefix + "." + local) : local;
      props.put(key, a.getValue());
      if ("type".equals(local)) props.put("type", a.getValue());
    }

    // extensionElements
    Element ext = first(task, "extensionElements");
    if (ext!=null){
      NodeList ch = ext.getChildNodes();
      for (int i=0;i<ch.getLength();i++){
        org.w3c.dom.Node n = ch.item(i);
        if (n.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE) continue;
        Element e=(Element)n;
        String elLocal  = e.getLocalName();
        String elPrefix = e.getPrefix();
        String elKey = (elPrefix!=null && !elPrefix.isBlank()) ? (elPrefix + "." + elLocal) : elLocal;

        // record attributes as elKey.key (with namespace prefix if present)
        NamedNodeMap aa = e.getAttributes();
        for (int j=0;j<aa.getLength();j++){
          Attr a=(Attr)aa.item(j);
          String local = a.getLocalName();
          String prefix = a.getPrefix();
          if (local==null) continue;
          String key = (prefix!=null && !prefix.isBlank()) ? (prefix + "." + local) : local;
          props.put(elKey + "." + key, a.getValue());
        }

        String text = e.getTextContent();
        if (text!=null && !text.isBlank()){
          props.put(elKey, text.trim());
        }

        // special-case: zeebe:formDefinition externalReference -> handy keys
        if ("formDefinition".equals(elLocal) && "zeebe".equals(elPrefix)){
          String extRef = e.getAttributeNS(ZEEBE_NS, "externalReference");
          if (extRef==null || extRef.isBlank()) extRef = e.getAttribute("externalReference");
          if (extRef!=null && !extRef.isBlank()){
            props.put("zeebe.form.externalReference", extRef);
            props.put("form.external", extRef); // alias used by your engine code
          }
        }
      }
    }
    return props;
  }

  private static Element first(Element parent, String local){
    NodeList list = parent.getChildNodes();
    for (int i=0;i<list.getLength();i++){
      org.w3c.dom.Node n = list.item(i);
      if (n.getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE) continue;
      Element e=(Element)n;
      if (local.equals(e.getLocalName())) return e;
    }
    return null;
  }
}
