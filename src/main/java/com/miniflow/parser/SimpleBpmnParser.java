
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
  public static ProcessDefinition parse(InputStream in){
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); dbf.setNamespaceAware(true);
      Document doc = dbf.newDocumentBuilder().parse(in); Element root = doc.getDocumentElement();
      Element process = first(root, "process"); if (process==null) throw new IllegalArgumentException("No <process>");

      String pid = process.getAttribute("id"); String pname = process.getAttribute("name");
      EngineModel.Builder b = new EngineModel.Builder(pid, pname);

      Map<String, Element> elById = new HashMap<>();
      NodeList kids = process.getChildNodes();
      // Pass 1: nodes
      for (int i=0;i<kids.getLength();i++){
        Node n = kids.item(i); if (n.getNodeType()!=Node.ELEMENT_NODE) continue; Element e=(Element)n; elById.put(e.getAttribute("id"), e);
        String ln = e.getLocalName(); if (ln==null) continue; String id = e.getAttribute("id"); String name = e.getAttribute("name");
        switch (ln){
          case "startEvent" -> b.start(id);
          case "endEvent"   -> b.end(id);
          case "userTask"   -> b.userTask(id, name);
          case "exclusiveGateway" -> b.exclusiveGateway(id, name);
          case "parallelGateway"  -> b.parallelGateway(id, name);
          case "serviceTask", "task" -> {
            Map<String,String> props = readProps(e); // custom/miniflow props
            b.serviceTask(id, name, id, props);
          }
          default -> {}
        }
      }
      // Pass 2: flows
      for (int i=0;i<kids.getLength();i++){
        Node n = kids.item(i); if (n.getNodeType()!=Node.ELEMENT_NODE) continue; Element e=(Element)n;
        if (!"sequenceFlow".equals(e.getLocalName())) continue;
        String from = e.getAttribute("sourceRef"); String to = e.getAttribute("targetRef");
        EngineModel.Condition cond = EngineModel.Conditions.alwaysTrue();
        Element expr = first(e, "conditionExpression");
        if (expr!=null) { EngineModel.Condition c = parseSimpleExpr(expr.getTextContent()); if (c!=null) cond=c; }
        b.flow(from,to,cond);
      }
      return b.build();
    } catch (Exception ex){ throw new RuntimeException("Parse BPMN failed: "+ex.getMessage(), ex); }
  }

  // Simple ${x=="A"} or ${x!="A"} or numeric equality
  private static EngineModel.Condition parseSimpleExpr(String raw){
    if (raw==null) return null; String s=raw.trim(); if (s.startsWith("${") && s.endsWith("}")) s=s.substring(2,s.length()-1);
    boolean neq = s.contains("!="); String op = neq?"!=":"=="; String[] p = s.split(op,2); if (p.length!=2) return null;
    String key=p[0].trim(); String val=p[1].trim(); Object expected;
    if ((val.startsWith("\"")&&val.endsWith("\""))||(val.startsWith("'")&&val.endsWith("'"))) expected = val.substring(1,val.length()-1);
    else { try{ expected = Long.parseLong(val);}catch(Exception e){ expected = val; } }
    Object exp = expected; return vars -> { boolean eq = java.util.Objects.equals(vars.get(key), exp); return neq? !eq : eq; };
  }

  // Read extensionElements and attributes as key-value props (miniflow.*)
  private static Map<String,String> readProps(Element task){
    Map<String,String> props = new HashMap<>();
    NamedNodeMap attrs = task.getAttributes();
    for (int i=0;i<attrs.getLength();i++){
      Attr a = (Attr)attrs.item(i);
      String ln = a.getLocalName(); String val=a.getValue();
      if (ln==null) continue; // capture namespaced attrs as ln
      if (ln.startsWith("miniflow:")) props.put(ln.substring("miniflow:".length()), val);
      if (ln.equals("type")) props.put("type", val);
    }
    Element ext = first(task, "extensionElements");
    if (ext!=null){
      NodeList ch = ext.getChildNodes();
      for (int i=0;i<ch.getLength();i++){
        Node n = ch.item(i); if (n.getNodeType()!=Node.ELEMENT_NODE) continue; Element e=(Element)n;
        // record every attribute as key=value, using localName
        NamedNodeMap aa = e.getAttributes();
        for (int j=0;j<aa.getLength();j++){
          Attr a=(Attr)aa.item(j); String ln=a.getLocalName(); if (ln==null) continue; props.put(ln, a.getValue());
        }
        // if element text exists, store under its localName
        String text = e.getTextContent(); if (text!=null && !text.isBlank()) props.put(e.getLocalName(), text.trim());
      }
    }
    return props;
  }

  private static Element first(Element parent, String local){
    NodeList list = parent.getChildNodes();
    for (int i=0;i<list.getLength();i++){
      Node n = list.item(i); if (n.getNodeType()!=Node.ELEMENT_NODE) continue; Element e=(Element)n; if (local.equals(e.getLocalName())) return e; }
    return null;
  }
}