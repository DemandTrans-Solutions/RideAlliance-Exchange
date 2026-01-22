package com.clearinghouse.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

@Component
@Slf4j
public class XMLParser {

    public static void main(String[] argv) {
        try {
            // creating a constructor of file class and parsing an XML file
            File file = new File("D:\\ClearingHouse\\DOM-Parser.xml");
            // an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // an instance of builder to parse the specified xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            log.debug("Root element: " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("student");
            // nodeList is not iterable, so we are using for loop
            for (int itr = 0; itr < nodeList.getLength(); itr++) {
                Node node = nodeList.item(itr);
                log.debug("\nNode Name :" + node.getNodeName());
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    log.debug("Student id: " + eElement.getElementsByTagName("id").item(0).getTextContent());
                    log.debug(
                            "First Name: " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
                    log.debug(
                            "Last Name: " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    log.debug("Subject: " + eElement.getElementsByTagName("subject").item(0).getTextContent());
                    log.debug("Marks: " + eElement.getElementsByTagName("marks").item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getServiceAreaFromFile(String filePath) {
        String serviceArea = null;
        try {
            // creating a constructor of file class and parsing an XML file
            File file = new File(filePath);
            // an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // an instance of builder to parse the specified xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            log.debug("Root element: " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("student");
            // nodeList is not iterable, so we are using for loop
            for (int itr = 0; itr < nodeList.getLength(); itr++) {
                Node node = nodeList.item(itr);
                log.debug("\nNode Name :" + node.getNodeName());
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    log.debug("Student id: " + eElement.getElementsByTagName("id").item(0).getTextContent());
                    log.debug(
                            "First Name: " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
                    log.debug(
                            "Last Name: " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    log.debug("Subject: " + eElement.getElementsByTagName("subject").item(0).getTextContent());
                    log.debug("Marks: " + eElement.getElementsByTagName("marks").item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceArea;
    }
}
