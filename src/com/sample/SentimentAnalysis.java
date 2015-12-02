package com.sample;
import java.io.IOException;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import com.alchemyapi.api.AlchemyAPI;

public class SentimentAnalysis {
	public static void main(String[] args) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException{
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString("f645e444a7c14172efadc4b1626880aeb60c0d65");
		Document doc = alchemyObj.TextGetTextSentiment("I hate Bob");
//		Document doc = alchemyObj.TextGetTextSentiment("That hat is nice , Charles.");
        System.out.println("Sentitments from Text ---------"+getStringFromDocument(doc));
        NodeList nodes = doc.getElementsByTagName("results");
        for (int i = 0; i < nodes.getLength(); i++) {
        	Node node = nodes.item(i);

        	if (node.getNodeType() == Node.ELEMENT_NODE) 
        	{
        	Element element = (Element) node;
        	System.out.println("Sentiment :  " + getValue("type", element));
        	System.out.println("Score : " + getValue("score", element));
        	
        	}
        	}   
	}
	private static String getValue(String tag, Element element) {
    	NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
    	Node node = (Node) nodes.item(0);
    	return node.getNodeValue();
    	}
	private static String getStringFromDocument(Document doc) {  
        try {  
          DOMSource domSource = new DOMSource(doc);  
          StringWriter writer = new StringWriter();  
          StreamResult result = new StreamResult(writer);  
          TransformerFactory tf = TransformerFactory.newInstance();  
          Transformer transformer = tf.newTransformer();  
          transformer.transform(domSource, result);  
          return writer.toString();  
        } catch (TransformerException ex) {  
          ex.printStackTrace();  
          return null;  
        }  
      } 
}
