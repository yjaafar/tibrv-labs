package com.tibco.tibrv.labs.labg;

//TIBCO Rendezvous API Java Development
//File: MyVC2.java
//TIBCO Education Services
//Copyright 2005 - TIBCO Software Inc.
//ALL RIGHTS RESERVED

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvDispatcher;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvQueue;
import com.tibco.tibrv.TibrvRvdTransport;
import com.tibco.tibrv.TibrvVcTransport;

public class MyVC2 implements TibrvMsgCallback {

	private static final String SUBJECT = "USER04.TEST";
	TibrvRvdTransport conn2;
	TibrvVcTransport vconn2;
	TibrvListener list;
	int i = 0;
	boolean vcUp = false;
	String kk;
	TibrvMsg msg;

	public MyVC2() throws TibrvException {
		Tibrv.open();
		conn2 = new TibrvRvdTransport();

		TibrvQueue q1 = new TibrvQueue();
		list = new TibrvListener(q1, this, conn2, SUBJECT, null);
		TibrvDispatcher d1 = new TibrvDispatcher("Dispatcher1", q1, 10);
		System.err.println("Waiting 5 seconds for a handshake from VC Partner on USER.TEST");

		try {
			d1.join();
			q1.timedDispatch(3);
		} catch (InterruptedException e) {
			conn2.destroy();
			Tibrv.close();
		}

		for (;;) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ee) {
			}
			i = i + 1;
			System.out
					.println("Sending VC Message : Data: Message Number " + i);
			msg = new TibrvMsg();
			msg.setSendSubject(kk);
			msg.add("Data", "Message number " + i);
			vconn2.send(msg);
		}
	}

	@Override
	public void onMsg(TibrvListener l, TibrvMsg msg) {

		try {
			i = i + 1;
			System.out.println("Recieved subject: " + msg.getSendSubject());
			System.out.println("Reply subject: " + msg.getReplySubject());
			String a = (String) msg.get("Data");
			System.out.println("Data : " + a);
			list.destroy();

			if (i == 1) {
				i = i + 1;
				kk = msg.getReplySubject();
				vconn2 = TibrvVcTransport.createConnectVc(kk, conn2);

				System.out.println("Created an Connect VC Object");
				kk = "TEST2";
				System.out.println("Subject is:" + kk);
				new TibrvListener(Tibrv.defaultQueue(), this, conn2, "_RV.INFO.SYSTEM.VC.CONNECTED", null);
				while (!TestForConnection()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ee) {
					}

					System.out.println("Connected");
					new TibrvListener(Tibrv.defaultQueue(), this, vconn2, kk,
							null);
					vcUp = true;
				}
			}

		} catch (TibrvException ee) {
			ee.printStackTrace();
		}
	}

	public boolean TestForConnection() {
		try {
			vconn2.waitForVcConnection(0);
		} catch (TibrvException ex) {
			System.out.println("Returning False");
			return false;
		}
		System.out.println("Returning True");
		return true;
	}

	public static void main(String... args) throws TibrvException {
		MyVC2 vc = new MyVC2();
	}
}
