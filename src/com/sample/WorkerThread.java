package com.sample;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class WorkerThread implements Runnable {

	@Override
	public void run() {

		AWSCredentials credentials = new BasicAWSCredentials(AccountInfo.accesskey, AccountInfo.secretAccessKey);
		AmazonSQS sqs = new AmazonSQSClient(credentials);
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		sqs.setRegion(usEast1);
		PublishRequest publishRequest;
		PublishResult publishResult;

		AmazonSNSClient snsClient = new AmazonSNSClient(credentials);
		snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		
		
		while (true) {

			try {
			
				// Receive messages
				System.out.println("Receiving messages from MyQueue.\n");

				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(AccountInfo.myQueueUrl);
				List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
				System.out.println("message #:" + messages.size());
				if (messages.size() != 0) {
					for (Message message : messages) {

						// call sentiment analysis API
						// System.out.println("calling sentiment API");
						AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString(AccountInfo.alchemyAPIString2);
						Document doc;
						NodeList nodes = null;
						String textStr = message.getBody();
						
//						publishRequest = new PublishRequest(AccountInfo.topicArn, textStr);
//						publishResult = snsClient.publish(publishRequest);
						
						System.out.println("published");
			

						try {
							doc = alchemyObj.TextGetTextSentiment(textStr);
							// System.out.println("Sentitments from Text
							// ---------" + getStringFromDocument(doc));
							nodes = doc.getElementsByTagName("results");
						} catch (XPathExpressionException | IOException | SAXException
								| ParserConfigurationException e) {
							e.printStackTrace();
						}
						if (nodes != null) {
							for (int i = 0; i < nodes.getLength(); i++) {
								Node node = nodes.item(i);

								if (node.getNodeType() == Node.ELEMENT_NODE) {
									Element element = (Element) node;

									System.out.println("Sentiment: " + getValue("type", element));
									
									// SNSMessage snsMessage = new SNSMessage();
									String messageText = "$"+textStr+  "    " + "Sentiment: " + getValue("type", element);
									if (!getValue("type", element).equals("neutral")) {
										messageText += "      Score: " + getValue("score", element);
										System.out.println("Score: " + getValue("score", element));
									}
									// snsMessage.setMessage("Tweet--------"
									// + textStr +"\n"+ "Sentiment: "+
									// getValue("type", element)+
									// "\nScore: "+getValue("score",
									// element));
									// snsMessage.setType("Notification");
									// snsMessage.setTopicArn(AccountInfo.topicArn);
									// snsMessage.setToken(token);
									// snsMessage.setSignatureVersion("1");
									// snsMessage.setTimestamp(new
									// Timestamp(new
									// Date().getTime()).toString());

									// publish to an SNS topic
									System.out.println(messageText);
									publishRequest = new PublishRequest(AccountInfo.topicArn, messageText);
									publishResult = snsClient.publish(publishRequest);
									// print MessageId of message published to
									// SNS topic
									System.out.println("MessageId - " + publishResult.getMessageId());

								}
							}
						}

						// Delete a message
						System.out.println("Deleting a message.\n");
						String messageRecieptHandle = messages.get(0).getReceiptHandle();
						sqs.deleteMessage(new DeleteMessageRequest(AccountInfo.myQueueUrl, messageRecieptHandle));

					}
				}

				System.out.println();

			} catch (AmazonServiceException ase) {
				System.out.println("Caught an AmazonServiceException, which means your request made it "
						+ "to Amazon SQS, but was rejected with an error response for some reason.");
				System.out.println("Error Message:    " + ase.getMessage());
				System.out.println("HTTP Status Code: " + ase.getStatusCode());
				System.out.println("AWS Error Code:   " + ase.getErrorCode());
				System.out.println("Error Type:       " + ase.getErrorType());
				System.out.println("Request ID:       " + ase.getRequestId());
			} catch (AmazonClientException ace) {
				System.out.println("Caught an AmazonClientException, which means the client encountered "
						+ "a serious internal problem while trying to communicate with SQS, such as not "
						+ "being able to access the network.");
				System.out.println("Error Message: " + ace.getMessage());
			}
			
			//sleep for .5 sec
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
