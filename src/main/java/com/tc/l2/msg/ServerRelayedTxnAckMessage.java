/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.l2.msg;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.tc.async.api.EventContext;
import com.tc.io.TCByteBufferInput;
import com.tc.io.TCByteBufferOutput;
import com.tc.net.NodeID;
import com.tc.net.groups.MessageID;
import com.tc.net.groups.NodeIDSerializer;
import com.tc.object.tx.ServerTransactionID;
import com.tc.object.tx.TransactionID;
import com.tc.util.Assert;

/**
 * Ack message sent in reply to Server's Relayed Txn Message.
 */
public class ServerRelayedTxnAckMessage extends ServerTxnAckMessage implements EventContext {

  private static final int SERVER_RELAYED_TXN_ACK_MSG_TYPE = 0;
  private Set<ServerTransactionID> serverTxnIDs;
  private transient NodeID nodeID;

  // To make serialization happy
  public ServerRelayedTxnAckMessage() {
    super(-1);
  }
  
  public ServerRelayedTxnAckMessage(RelayedCommitTransactionMessage ackFor, Set<ServerTransactionID> serverTxnIDs) {
    this(ackFor.messageFrom(), ackFor.getMessageID(), serverTxnIDs);
  }
    
  public ServerRelayedTxnAckMessage(NodeID ackTo, MessageID ackFor, Set<ServerTransactionID> serverTxnIDs) {
    super(SERVER_RELAYED_TXN_ACK_MSG_TYPE, ackFor);
    this.nodeID = ackTo;
    this.serverTxnIDs = serverTxnIDs;
  }

  @Override
  public Set<ServerTransactionID> getAckedServerTxnIDs() {
    return serverTxnIDs;
  }

  @Override
  public NodeID getDestinationID() {
    Assert.assertNotNull(nodeID);
    return nodeID;
  }

  @Override
  protected void basicDeserializeFrom(TCByteBufferInput in) throws IOException {
    Assert.assertEquals(SERVER_RELAYED_TXN_ACK_MSG_TYPE, getType());
    int size = in.readInt();
    serverTxnIDs = new HashSet<ServerTransactionID>(size);
    for (int i = 0; i < size; i++) {
      NodeIDSerializer nodeIDSerializer = new NodeIDSerializer();
      nodeIDSerializer = nodeIDSerializer.deserializeFrom(in);
      NodeID cid = nodeIDSerializer.getNodeID();
      long clientTxID = in.readLong();
      serverTxnIDs.add(new ServerTransactionID(cid, new TransactionID(clientTxID)));
    }
  }

  @Override
  protected void basicSerializeTo(TCByteBufferOutput out) {
    Assert.assertEquals(SERVER_RELAYED_TXN_ACK_MSG_TYPE, getType());
    out.writeInt(serverTxnIDs.size());
    for (ServerTransactionID sTxID : serverTxnIDs) {
      NodeIDSerializer nodeIDSerializer = new NodeIDSerializer(sTxID.getSourceID());
      nodeIDSerializer.serializeTo(out);
      out.writeLong(sTxID.getClientTransactionID().toLong());
    }
  }
}
