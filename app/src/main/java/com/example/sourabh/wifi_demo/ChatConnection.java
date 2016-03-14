package com.example.sourabh.wifi_demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
public class ChatConnection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    ArrayList<CommonChat> chatClients = new ArrayList<CommonChat>();

    private static final String TAG = "ChatConnection";

    private int mPort = -1;

    public ChatConnection(Handler handler) {
        mUpdateHandler = handler;
        //if the user is a server create the server socket
        if(NsdChatActivity.mUserChoice.equals("server")) {
            mChatServer = new ChatServer();
        }
    }

    public void tearDown() {
        if(mChatServer!=null)
            mChatServer.tearDown();
        for (CommonChat chatClient : chatClients) {
            chatClient.tearDown();
        }
    }

    //Creating and storing CommonChat objects
    public void commonConnection(InetAddress address, int port, Socket s) {
        CommonChat mChatClient = new CommonChat(address, port, s);
        chatClients.add(mChatClient);
    }

    
    public void sendMessage(String msg) {
        for (CommonChat chatClient : chatClients) {
            chatClient.sendMessage(msg);
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }


    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        if (local) {
            msg = "sent: " + msg;
        } else {
            msg = "received: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private class ChatServer {

        ArrayList<ServerThread> serverThreads = new ArrayList<ServerThread>();
        ServerSocket mServerSocket = null;
        //Thread mThread = null;

        //Creating the thread for chat
        public ChatServer() {
            new ThreadHandler().start();
        }

        public class ThreadHandler extends Thread
        {
            @Override
            public void run() {
                Socket sv_soc= null;

                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                try {
                    //Creating server socket
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    Log.d(TAG, "ServerSocket Created, awaiting connection");
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
                while(true) {
                    try {
                        //Accepting client
                        sv_soc = mServerSocket.accept();
                        Log.d(TAG, "Connected..." + sv_soc.getInetAddress() + " " + sv_soc.getPort());
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught!", e);
                    }
                    //Starting thread for each client
                    if (sv_soc != null) {
                        ServerThread sv_thread = new ServerThread(sv_soc);
                        serverThreads.add(sv_thread);
                        sv_thread.t.start();
                    }
                }
            }
            /*
            public void doSomethingOnAllThreads() {
                for (ServerThread serverThread : serverThreads) {
                    serverThread.otherMethod();
                }
            }
            */
        }

        public void tearDown() {
            //interrupt all server threads
            for (ServerThread serverThread : serverThreads)
                serverThread.t.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {
            Socket sv_soc;
            Thread t;
            public ServerThread(Socket s) {
                sv_soc = s;
                t=new Thread(this);
            }
            @Override
            public void run() {
                //setSocket(sv_soc);
                //if (mChatClient == null) {
                int port = sv_soc.getPort();
                InetAddress address = sv_soc.getInetAddress();
                Log.d(TAG, "commonConnection being called from ServerThread!");
                commonConnection(address, port, sv_soc);
                //}
            }
            /*
            @Override
            public void finalize() throws Throwable {
                Log.d(TAG, "Finalize");
                super.finalize();
            }
            */
        }
    }

    private class CommonChat {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "CommonChat";

        private Thread mSendThread;
        private Thread mRecThread;
        Socket sv_soc = null;

        //Function to handle the chatting
        public CommonChat(InetAddress address, int port, Socket s) {

            Log.d(CLIENT_TAG, "Creating chatClient");
            mAddress = address;
            PORT = port;
            sv_soc = s;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        //Sending Thread
        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    //Log.d(TAG, mAddress + " " + PORT);
                    if(NsdChatActivity.mUserChoice.equals("client")) {
                        //Creating Client socket
                        sv_soc = new Socket(mAddress, PORT);
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    }
                    else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to initialize Client-side socket!", e);
                    e.printStackTrace();
                }
                mRecThread = new Thread(new ReceivingThread());
                mRecThread.start();

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        //Receiving Thread
        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            sv_soc.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
                            if(messageStr.equals("end")){
                                Log.d(CLIENT_TAG, "The end!");
                                break;
                            }
                            updateMessages(messageStr, false);
                        }
                    }
                    input.close();
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }
        }

        public void tearDown() {
            try {
                sv_soc.close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }

        public void sendMessage(String msg) {
            try {
                if (sv_soc == null) {
                    Log.d(CLIENT_TAG, "Socket is null!");
                } else if (sv_soc.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null!");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(sv_soc.getOutputStream())), true);
                out.println(msg);
                out.flush();
                updateMessages(msg, true);
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + msg);
        }
    }
}