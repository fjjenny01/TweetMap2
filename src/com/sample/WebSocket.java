package com.sample;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

@ServerEndpoint(value = "/echo")
public class WebSocket extends HttpServlet {
	private ConfigurationBuilder cb = new ConfigurationBuilder();
	private TwitterStream twitterStream;
	private AWSCredentials credentials = null;

	static BlockingQueue<String> bq = new ArrayBlockingQueue<String>(1000);

	@OnOpen
	public void open(Session session) {
		try {
			cb.setDebugEnabled(true).setOAuthConsumerKey(AccountInfo.AUTH_CONSUMER_KEY)
					.setOAuthConsumerSecret(AccountInfo.AUTH_CONSUMER_SECRET)
					.setOAuthAccessToken(AccountInfo.AUTH_ACCESS_TOKEN)
					.setOAuthAccessTokenSecret(AccountInfo.AUTH_ACCESS_TOKEN_SECRET);
			
			
			
			ExecutorService executor = Executors.newFixedThreadPool(1);
			Runnable worker = new WorkerThread();
			executor.execute(worker);
			

			// credentials= new BasicAWSCredentials(AccountInfo.accesskey,
			// AccountInfo.secretAccessKey);
			// AmazonSNSClient snsClient = new AmazonSNSClient(credentials);
			// snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
			// ConfirmSubscriptionRequest confirmSubscriptionRequest = new
			// ConfirmSubscriptionRequest()
			// .withTopicArn(AccountInfo.topicArn)
			// .withToken("ddddd");
			// ConfirmSubscriptionResult resutlt =
			// snsClient.confirmSubscription(confirmSubscriptionRequest);
			// System.out.println(resutlt);

			// establish local database connection

			// try {
			// Class.forName("com.mysql.jdbc.Driver");
			// System.out.println("Success loading Mysql Driver!");
			// } catch (Exception e) {
			// System.out.print("Error loading Mysql Driver!");
			// e.printStackTrace();
			// }
			//
			// Connection connect = null;
			// try {
			// connect =
			// DriverManager.getConnection(AccountInfo.JDBC_CONNECTION_URL,
			// AccountInfo.JDBC_USERNAME, AccountInfo.JDBC_PASSWORD);
			//
			// System.out.println("Success connect Mysql server!");
			//
			// System.out.println("executed");
			// } catch (Exception e) {
			// System.out.print("get data error!");
			// e.printStackTrace();
			// }
			//
			// PreparedStatement pstmt = null;
			// String query = "insert into all_tweets(id, tweetId, time,
			// latitude, longitude, keyword, text,sentiment) values(null, ?, ?,
			// ?, ?,'dance',?, null)";
			//
			// try {
			// pstmt = connect.prepareStatement(query);
			//
			// } catch (SQLException fe1) {
			// e1.printStackTrace();
			// }
			//
			// final PreparedStatement pstmt2 = pstmt;

			// create SQS
			try {
				credentials = new BasicAWSCredentials(AccountInfo.accesskey, AccountInfo.secretAccessKey);
				// credentials = new
				// ProfileCredentialsProvider("default").getCredentials();
			} catch (Exception e) {
				throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
						+ "Please make sure that your credentials file is at the correct "
						+ "location (/Users/jinfang/.aws/credentials), and is in valid format.", e);
			}

			AmazonSQS sqs = null;
			String myQueueUrl = null;
			try {
				sqs = new AmazonSQSClient(credentials);
				Region usEast1 = Region.getRegion(Regions.US_EAST_1);
				sqs.setRegion(usEast1);

				System.out.println("===========================================");
				System.out.println("Getting Started with Amazon SQS");
				System.out.println("===========================================\n");

				// Create a queue
				System.out.println("Creating a new SQS queue called MyQueue.\n");
				CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue");
				myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

				// List queues
				System.out.println("Listing all queues in your account.\n");
				for (String queueUrl : sqs.listQueues().getQueueUrls()) {
					System.out.println("  QueueUrl: " + queueUrl);
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

			final AmazonSQS sqs2 = sqs;
			final String myQueueUrl2 = myQueueUrl;

			twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
			StatusListener listener = new StatusListener() {
				@Override
				public void onStatus(Status status) {
					try {
						if (status.getGeoLocation() != null) {

							// write to local database

							// try {
							// pstmt2.setLong(1, status.getUser().getId());
							// pstmt2.setString(2,
							// status.getCreatedAt().toString()); // set
							// // input
							// // parameter
							// pstmt2.setDouble(3,
							// status.getGeoLocation().getLatitude());
							// pstmt2.setDouble(4,
							// status.getGeoLocation().getLongitude());
							// pstmt2.setString(5, status.getText());
							// pstmt2.executeUpdate(); // execute insert
							// // statement
							// } catch (SQLException e) {
							// e.printStackTrace();
							// }

							// send message to SQS
							System.out.println("Sending a message to MyQueue.\n");
							try {
								sqs2.sendMessage(new SendMessageRequest(myQueueUrl2, status.getText()));
							} catch (AmazonServiceException ase) {
								System.out.println("Caught an AmazonServiceException, which means your request made it "
										+ "to Amazon SQS, but was rejected with an error response for some reason.");
								System.out.println("Error Message:    " + ase.getMessage());
								System.out.println("HTTP Status Code: " + ase.getStatusCode());
								System.out.println("AWS Error Code:   " + ase.getErrorCode());
								System.out.println("Error Type:       " + ase.getErrorType());
								System.out.println("Request ID:       " + ase.getRequestId());
							} catch (AmazonClientException ace) {
								System.out
										.println("Caught an AmazonClientException, which means the client encountered "
												+ "a serious internal problem while trying to communicate with SQS, such as not "
												+ "being able to access the network.");
								System.out.println("Error Message: " + ace.getMessage());
							}

							// display the tweet on server
							System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText() + "-");
							double lat = status.getGeoLocation().getLatitude();
							double lon = status.getGeoLocation().getLongitude();
							// System.out.println(Doublef.toString(lat) + "," +
							// Double.toString(lon));
							session.getBasicRemote().sendText(Double.toString(lat) + "," + Double.toString(lon));

							System.out.println("size of bq ========================" + bq.size());
							System.out.println("String from bq: ======================" + bq.peek());
							if (bq.size() > 0) {
								session.getBasicRemote().sendText(bq.take());
							}

						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// System.out.println("@" + status.getUser().getScreenName()
					// + " - " + status.getText()+"-");
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				@Override
				public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
					// System.out.println("Got a status deletion notice id:" +
					// statusDeletionNotice.getStatusId());
				}

				@Override
				public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
					// System.out.println("Got track limitation notice:" +
					// numberOfLimitedStatuses);
				}

				@Override
				public void onScrubGeo(long userId, long upToStatusId) {
					// System.out.println("Got scrub_geo event userId:" + userId
					// + " upToStatusId:" + upToStatusId);
				}

				@Override
				public void onStallWarning(StallWarning warning) {
					System.out.println("Got stall warning:" + warning);
				}

				@Override
				public void onException(Exception ex) {
					ex.printStackTrace();
				}
			};
			twitterStream.addListener(listener);

			FilterQuery filter = new FilterQuery();
			String[] keywordsArray = {
					"ball,soccer,hockey,football,volleyball,badminton,cricket,pingpang,tabletennis,golf,tennis,baseball,rugby,basketball",
					"running,jogging,run,walk,jog,step",
					"dance,tango,yoga,Hip-hop,Belly,Ballet,jazz,morden,swing,country,dancing",
					"swim,swimming,Canoeing,Kayaking,Skurfing,Sailing,sail,canoe,Rowing,Rafting,Kiteboating,Waterskiing,diving,dive,waterski,fishing,Snorkeling,Waboba,Boating" };
			filter.track(keywordsArray);
			twitterStream.filter(filter);
			System.out.println(session.getId() + "opened");
			session.getBasicRemote().sendText(session.getId() + "opened");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@OnMessage
	public void set_Keywords(Session session, String msg, boolean last) {
		if (session.isOpen()) {
			FilterQuery filter = new FilterQuery();
			String[] kw_any = { "." };
			String[] kw_ball = {
					"ball,soccer,hockey,football,volleyball,badminton,cricket,pingpang,tabletennis,golf,tennis,baseball,rugby,basketball" };
			String[] kw_run = { "running,jogging,run,walk,jog,step" };
			String[] kw_dance = { "dance,tango,yoga,Hip-hop,Belly,Ballet,jazz,morden,swing,country,dancing" };
			String[] kw_water = {
					"swim,swimming,Canoeing,Kayaking,Skurfing,Sailing,sail,canoe,Rowing,Rafting,Kiteboating,Waterskiing,diving,dive,waterski,fishing,Snorkeling,Waboba,Boating" };
			String[] kw_all = {
					"ball,soccer,hockey,football,volleyball,badminton,cricket,pingpang,tabletennis,golf,tennis,baseball,rugby,basketball",
					"running,jogging,run,walk,jog,step",
					"dance,tango,yoga,Hip-hop,Belly,Ballet,jazz,morden,swing,country,dancing",
					"swim,swimming,Canoeing,Kayaking,Skurfing,Sailing,sail,canoe,Rowing,Rafting,Kiteboating,Waterskiing,diving,dive,waterski,fishing,Snorkeling,Waboba,Boating" };
			switch (msg) {
			case "ball":
				filter.track(kw_ball);
				twitterStream.filter(filter);
				break;
			case "run":
				filter.track(kw_run);
				twitterStream.filter(filter);
				break;
			case "dance":
				filter.track(kw_dance);
				twitterStream.filter(filter);
				break;
			case "water":
				filter.track(kw_water);
				twitterStream.filter(filter);
				break;
			case "all":
				filter.track(kw_all);
				twitterStream.filter(filter);
				break;
			case "any":
				twitterStream.sample();
				break;

			}

		}
	}

	@OnClose
	public void close(Session session) {

		twitterStream.shutdown();
	}

	public WebSocket() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().write("hello world");
		System.out.println("bbbbb");
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SecurityException {
		// AWSCredentials credentials = new
		// BasicAWSCredentials(AccountInfo.accesskey,
		// AccountInfo.secretAccessKey);

		// Get the message type header.
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA");
		 String messagetype = request.getHeader("x-amz-sns-message-type");
		// System.out.println(messagetype.toString());
		// If message doesn't have the message type header, don't process it.
		 if (messagetype == null)
		 return;
		// Parse the JSON message in the message body
		// and hydrate a Message object with its contents
		// so that we have easy access to the name/value pairs
		// from the JSON message.
		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		System.out.println(builder.toString());
		
		System.out.println("bq size in dopost:  " + bq.size());
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA");

		 SNSMessage msg = readMessageFromJson(builder.toString());
		 System.out.println(msg.getMessage().toString());

		// The signature is based on SignatureVersion 1.
		// If the sig version is something other than 1,
		// throw an exception.
		 if (msg.getSignatureVersion().equals("1")) {
		 // Check the signature and throw an exception if the signature
		 //verification fails.
		 if (isMessageSignatureValid(msg))
		 System.out.println(">>Signature verification succeeded");
		 else {
		 System.out.println(">>Signature verification failed");
		 throw new SecurityException("Signature verification failed.");
		 }
		 }
		 else {
		 System.out.println(">>Unexpected signature version. Unable to verify signature.");
		 throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		 }
		 System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA");
		 // Process the message based on type.
		 if (messagetype.equals("Notification")) {
		 //TODO: Do something with the Message and Subject.
		 //Just log the subject (if it exists) and the message.
		 String logMsgAndSubject = ">>Notification received from topic " +
		 msg.getTopicArn();
		 if (msg.getSubject() != null)
		 logMsgAndSubject += " Subject: " + msg.getSubject();
		 logMsgAndSubject += " Message: " + msg.getMessage();
		 
		 String mes = msg.getMessage();
		 bq.add(mes);
		 System.out.println(logMsgAndSubject);
		
		
		
		 }
		 else if (messagetype.equals("SubscriptionConfirmation"))
		 {
		 //TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics
		 //that you want to enable to add this endpoint as a subscription.
		
		 //Confirm the subscription by going to the subscribeURL location
		 //and capture the return value (XML message body as a string)
		 Scanner sc = new Scanner(new
		 URL(msg.getSubscribeURL()).openStream());
		 StringBuilder sb = new StringBuilder();
		 while (sc.hasNextLine()) {
		 sb.append(sc.nextLine());
		 }
		 System.out.println(">>Subscription confirmation (" +
		 msg.getSubscribeURL() +") Return value: " + sb.toString());
		// //TODO: Process the return value to ensure the endpoint is
		// subscribed.
		 SNSHelper.INSTANCE.confirmTopicSubmission(msg);
		 }
		 else if (messagetype.equals("UnsubscribeConfirmation")) {
		 //TODO: Handle UnsubscribeConfirmation message.
		 //For example, take action if unsubscribing should not have occurred.
		 //You can read the SubscribeURL from this message and
		 //re-subscribe the endpoint.
		 System.out.println(">>Unsubscribe confirmation: " +
		 msg.getMessage());
		 }
		 else {
		 //TODO: Handle unknown message type.
		 System.out.println(">>Unknown message type.");
		 }
		 System.out.println(">>Done processing message: " +
		 msg.getMessageId());
	
	}

	private boolean isMessageSignatureValid(SNSMessage msg) {

		try {
			URL url = new URL(msg.getSigningCertUrl());
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature().getBytes()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);

		}
	}

	private byte[] getMessageBytesToSign(SNSMessage msg) {

		byte[] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	// Build the string to sign for Notification messages.
	private static String buildNotificationStringToSign(SNSMessage msg) {
		String stringToSign = null;

		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	private static String buildSubscriptionStringToSign(SNSMessage msg) {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	private SNSMessage readMessageFromJson(String string) {
		ObjectMapper mapper = new ObjectMapper();
		SNSMessage message = null;
		try {
			message = mapper.readValue(string, SNSMessage.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;
	}

}