package com.sample;

import java.io.StringWriter;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class Utility {

	static public String buildJASON(SNSMessage msg) {

		JSONObject obj = new JSONObject();

		StringWriter out = new StringWriter();
		try {
			obj.put("Type", msg.getType());
			obj.put("Message", msg.getMessage());
			obj.put("Timestamp", msg.getTimestamp());
			obj.put("TopicArn", msg.getTopicArn());
			obj.put("SignatureVersion", msg.getSignatureVersion());
			obj.write(out);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonText = out.toString();

		return jsonText;
	}
}
