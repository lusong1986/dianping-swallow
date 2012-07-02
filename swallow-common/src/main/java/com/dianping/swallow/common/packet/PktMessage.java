package com.dianping.swallow.common.packet;

import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.message.SwallowMessage;

public class PktMessage extends Packet implements Message{

   private static final long serialVersionUID = 2189810352053398027L;
   private SwallowMessage content;
	private Destination dest;
	
	public PktMessage() {
		super();
	}

	public PktMessage(Destination dest, SwallowMessage content){
		super.setPacketType(PacketType.OBJECT_MSG);
		
		this.dest = dest;
		this.content = content;
	}
	
	@Override
	public SwallowMessage getContent() {
		return content;
	}

	@Override
	public Destination getDestination() {
		return dest;
	}
}