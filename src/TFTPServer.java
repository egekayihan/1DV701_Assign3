import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class TFTPServer {
    public static final int TFTPPORT = 4970;
    public static final int BUFSIZE = 516;
    public static final String READDIR = "/home/username/read/"; //custom address at your PC
    public static final String WRITEDIR = "/home/username/write/"; //custom address at your PC
    // OP codes
    public static final int OP_RRQ = 1;
    public static final int OP_WRQ = 2;
    public static final int OP_DAT = 3;
    public static final int OP_ACK = 4;
    public static final int OP_ERR = 5;
    public static String mode = "";

    public static void main(String[] args) throws IOException {
        if (args.length > 0)
        {
            System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
            System.exit(1);
        }
        //Starting the server
        try
        {
            TFTPServer server= new TFTPServer();
            server.start();
        }
        catch (SocketException e)
        {e.printStackTrace();}
    }

    private void start() throws IOException
    {
        byte[] buf= new byte[BUFSIZE];

        // Create socket
        DatagramSocket socket= new DatagramSocket(null);

        // Create local bind point
        SocketAddress localBindPoint= new InetSocketAddress(TFTPPORT);
        socket.bind(localBindPoint);

        System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

        // Loop to handle client requests
        while (true)
        {

            final InetSocketAddress clientAddress = receiveFrom(socket, buf);

            // If clientAddress is null, an error occurred in receiveFrom()
            if (clientAddress == null)
                continue;

            final StringBuffer requestedFile= new StringBuffer();
            final int reqtype = ParseRQ(buf, requestedFile);

            new Thread()
            {
                public void run()
                {
                    try
                    {
                        DatagramSocket sendSocket= new DatagramSocket(0);

                        // Connect to client
                        sendSocket.connect(clientAddress);

                        System.out.printf("%s request for %s from %s using port %d\n",
                                (reqtype == OP_RRQ)?"Read":"Write",
                                clientAddress.getHostName(), clientAddress.getPort());

                        // Read request
                        if (reqtype == OP_RRQ)
                        {
                            requestedFile.insert(0, READDIR);
                            HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
                        }
                        // Write request
                        else
                        {
                            requestedFile.insert(0, WRITEDIR);
                            HandleRQ(sendSocket,requestedFile.toString(),OP_WRQ);
                        }
                        sendSocket.close();
                    }
                    catch (SocketException e)
                    {e.printStackTrace();}
                }
            }.start();
        }
    }

    /**
     * Reads the first block of data, i.e., the request for an action (read or write).
     * @param socket (socket to read from)
     * @param buf (where to store the read data)
     * @return socketAddress (the socket address of the client)
     */
    private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) throws IOException {
        // Create datagram packet
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

        // Receive packet
        socket.receive(receivePacket);

        // Get client address and port from the packet
        InetSocketAddress socketAddress = new InetSocketAddress(receivePacket.getAddress(),receivePacket.getPort());

        return socketAddress;
    }

    /**
     * Parses the request in buf to retrieve the type of request and requestedFile
     *
     * @param buf (received request)
     * @param requestedFile (name of file to read/write)
     * @return opcode (request type: RRQ or WRQ)
     */
    private int ParseRQ(byte[] buf, StringBuffer requestedFile)
    {
        // See "TFTP Formats" in TFTP specification for the RRQ/WRQ request contents
        ByteBuffer bufWrap = ByteBuffer.wrap(buf);
        short opcode = bufWrap.getShort();

        int delimiter = -1;

        for (int i = 2; i < buf.length; i++) {
            if (buf[i] == 0) {
                delimiter = i;
                break;
            }
        }

        if (delimiter == -1) {
            System.err.println("Request Packet Corrupted. Exiting");
            System.exit(1);
        }

        String fileName = new String(buf, 2, delimiter - 2);
        requestedFile.append(fileName);

        for (int i = delimiter+1; i < buf.length; i++) {
            if (buf[i] == 0) {
                String temp = new String(buf,delimiter+1,i - (delimiter + 1));
                mode = temp;

                if (temp.equalsIgnoreCase("octet")) {
                    return opcode;
                }

                else {
                    System.err.println("Unspecified mode. Exiting.");
                    System.exit(1);
                }
            }
        }

        System.err.println("Delimiter not found. Exiting");
        System.exit(1);
        return 0;
    }

    /**
     * Handles RRQ and WRQ requests
     *
     * @param sendSocket (socket used to send/receive packets)
     * @param requestedFile (name of file to read/write)
     * @param opcode (RRQ or WRQ)
     */
    private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode)
    {
        if(opcode == OP_RRQ)
        {
            // See "TFTP Formats" in TFTP specification for the DATA and ACK packet contents
            boolean result = send_DATA_receive_ACK(params);
        }
        else if (opcode == OP_WRQ)
        {
            boolean result = receive_DATA_send_ACK(params);
        }
        else
        {
            System.err.println("Invalid request. Sending an error packet.");
            // See "TFTP Formats" in TFTP specification for the ERROR packet contents
            send_ERR(params);
            return;
        }
    }

    /**
     To be implemented
     */
    private boolean send_DATA_receive_ACK(params)
    {return true;}

    private boolean receive_DATA_send_ACK(params)
    {return true;}

    private void send_ERR(params)
    {}
}
